from google.appengine.ext import blobstore
from google.appengine.ext import db
from google.appengine.ext import deferred
from google.appengine.api import images

from cache_controller import CacheController
from model_user import User
from model_vote import Vote
from model_comment import Comment
from model_configuration import ChoosieConfiguration
from utils import Utils

import ast
import facebook
import logging
import sys
from time import sleep
import datetime
from StringIO import *
from notify_handler import NotifyHandler
from model_configuration import ChoosieConfiguration

CHOOSIE_POST_TYPE_DILEMMA = 1
CHOOSIE_POST_TYPE_YES_NO = 2

CHOOSIE_POST_TYPES = set([CHOOSIE_POST_TYPE_DILEMMA, CHOOSIE_POST_TYPE_YES_NO])

class ChoosiePost(db.Model):
  photo1 = db.BlobProperty(required = False)
  photo2 = db.BlobProperty(required = False)
  photo1_blob_key = blobstore.BlobReferenceProperty()
  photo2_blob_key = blobstore.BlobReferenceProperty()
  fb_photo_blob_key = blobstore.BlobReferenceProperty()
  question = db.StringProperty(indexed = False, required = True)
  created_at = db.DateTimeProperty(auto_now_add = True)
  user_fb_id = db.StringProperty()
  updated_at = db.DateTimeProperty(indexed = True, auto_now = True)
  photo = db.BlobProperty()
  comments = db.StringListProperty()
  votes = db.StringListProperty()
  fb_post_id = db.StringProperty()
  posted_to_fb = db.BooleanProperty(default=False)
  post_type_id = db.IntegerProperty(required=True, choices=CHOOSIE_POST_TYPES, default=1)
  
  def to_json(self):
    logging.info('Post type ID = %d' % self.post_type_id )
    return {"key": str(self.key()),
            "user": self.get_user().to_short_json(),
            "votes": self.get_serialized_votes(),
            "comments": self.get_serialized_comments(),
            "photo1": self.photo_path(1),
            "photo2": self.photo2_path(),
            "question": self.question,
            "created_at": str(self.created_at),
            "updated_at": str(self.updated_at),
            "post_type": self.post_type()
           }

  def post_type(self):
    if self.post_type_id is None:
      return CHOOSIE_POST_TYPE_DILEMMA
    return self.post_type_id

  def photo2_path(self):
    if self.post_type() == CHOOSIE_POST_TYPE_DILEMMA:
      return self.photo_path(2)
    else:
      return "/images/update_choozie.png"

  def get_user(self):
    return CacheController.get_user_by_fb_id(self.user_fb_id)

  def get_cached_votes(self):
    return Vote.get_votes_for_post(str(self.key()))

  def get_serialized_votes(self):
    # updated_post.votes is a StringListProperty. Each vote_str is inflated to a dictionary
    # that looks like:
    # {"vote_for": 1,
    #  "user": {"fb_uid": "152343",
    #           ...}
    # }
    logging.info('For question [%s], returning %d votes.', self.question, len(self.votes))
    return [Vote.from_string_for_choosie_post(vote_str) for vote_str in self.votes]

  def get_cached_comments(self):
    return Comment.get_comments_for_post(str(self.key()))

  def get_serialized_comments(self):
    # updated_post.comments is a StringListProperty. Each comment_str is inflated to a dictionary
    # that looks like:
    # {"text": "blahblah",
    #  "user": {"fb_uid": "152343",
    #           ...}
    # }
    return [Comment.from_string_for_choosie_post(comment_str) for comment_str in self.comments if comment_str]

  def photo_path(self, which_photo):
    # Option 1: Using OLD method (photo is stored in the ChoosiePost itself)
    if ((which_photo == 0 and self.photo)
        or (which_photo == 1 and self.photo1)
        or (which_photo == 2 and self.photo2)):
      return '/photo?which_photo=%s&post_key=%s' % (which_photo, self.key())

    # Option 2: Using Blobstore method (photo is stored as a Blob in the Blobstore)
    photo_to_blob_key = {0: self.fb_photo_blob_key,
                         1: self.photo1_blob_key,
                         2: self.photo2_blob_key}
    blob_key = photo_to_blob_key[which_photo].key()
    return '/blobphoto/%s' % blob_key

  def publish_to_facebook(self, domain):
    # Utils.create_post_image(self)
    if ChoosieConfiguration.post_to_fb_setting():
      deferred.defer(self.publish_dillema_on_wall, self.key(), domain)
    else:
      logging.info("skipped fb publishing due to configuration settings")

  def publish_dillema_on_wall(self, choosie_post_key, domain):
    try:
      choosie_post = CacheController.get_model(str(choosie_post_key))
      logging.info("publishing on wall")
      logging.info("publishing with access_token " + choosie_post.get_user().fb_access_token)
      graph = facebook.GraphAPI(choosie_post.get_user().fb_access_token)
      logging.info("type = " + str(choosie_post.post_type()))
      if choosie_post.post_type() == CHOOSIE_POST_TYPE_DILEMMA:
        pic = Utils.create_dillema_post_image(self)
        question = choosie_post.question + '\n(Start your comment with #1 or #2 to help me choose.)'
      else:
        pic = Utils.create_yesno_post_image(self)
        img1_blob_reader = blobstore.BlobReader(choosie_post.photo1_blob_key)
        question = choosie_post.question + '\n(Start your comment with #yes or #no to help me choose.)'
      picIO = StringIO(pic)
      response = graph.put_photo(picIO, question.encode('utf-8'))
      logging.info(str(response))
      choosie_post.fb_post_id = response['post_id']
      choosie_post.posted_to_fb = True
      choosie_post.put()
    except Exception, e:
      logging.error("Facebook publishing failed: %s" % e)

  def notify_vote_async(self, choosie_post_key, user_fb_uid, name_of_from_user):
    try:
      user = CacheController.get_user_by_fb_id(user_fb_uid)
      result = NotifyHandler.send_notifiction(NotifyHandler.notify_type["vote"],
                                              name_of_from_user,
                                              str(choosie_post_key), 
                                              [user])

      logging.info("result of notifying on vote from " + name_of_from_user + " : " +  result)
    except Exception, e:
      logging.error("Faled to notify on new vote: %s" % e)


  def notify_comment_async(self, choosie_post_key, user_fb_uid, commenter_name):
    try:
      user = CacheController.get_user_by_fb_id(user_fb_uid)
      result = NotifyHandler.send_notifiction(NotifyHandler.notify_type["comment"],
                                              commenter_name,
                                              str(choosie_post_key),
                                              [user])

      logging.info("result of notifying on comment from " + commenter_name + " : "  + result)
    except Exception, e:
      logging.error("Faled to notify on new comment: %s" % e)

  def notify_friends(self):
    deferred.defer(self.notify_friends_async, self.key())

  def top_users(self):
    return ['100002024985258', '617068239', '624205547', '100001175049155', '1241855576', '100000707183816', '508640191', '1137431418',
            '1298401028','749156674', '1300315570', '1438198086', '631996760', '100000345475034',
            '665685278','100000834903975', '587727489', '1549874146', '100000345384628', '594341581', '1780333990',
            '587021920', '755595360', '1670197107', '588175278', '629616771', '530730008', '624988027', '603274673', '100004590640266', '573474136', '100001896302257', '670874576', '595614277', '812988819']

  def notify_friends_async(self, choosie_post_key):
    try:
      choosie_post = CacheController.get_model(str(choosie_post_key))

      logging.info("choosie post key = %s", str(choosie_post.key()))
      for user in User.all():
        logging.info("%s: device %s, uid: %s", user.name(), user.device_id, user.fb_uid)

      users = User.all()
      top_list = self.top_users()
      recipients = [u for u in User.all()
                   if ((ChoosieConfiguration.get_send_to_rest_setting() == False) and (u.fb_uid in top_list) or (ChoosieConfiguration.get_send_to_rest_setting() == True))]

      logging.info("First selection: %s", ", ".join([user.name() for user in recipients]))

      recipients = [u for u in recipients
                    if u.device_id is not None and user.fb_uid != choosie_post.user_fb_id]

      result = NotifyHandler.send_notifiction(NotifyHandler.notify_type["new_post"],
                                              self.get_user().display_name(), 
                                              str(choosie_post_key),
                                              recipients)

      ChoosieConfiguration.set_sent_to_rest_setting(not ChoosieConfiguration.get_send_to_rest_setting())

      logging.info("result of notifying friends= " + result)
    except Exception, e:
      logging.error("Faled to notify friends on new post: %s" % e)


  def add_comment_to_post(self, comment):
    db.run_in_transaction(ChoosiePost.add_comment_to_post_transaction, self.key(), comment)
    if self.user_fb_id != comment.user_fb_id:
      # device_id = self.get_user().device_id
      commenter_name = comment.get_user().display_name()
      deferred.defer(self.notify_comment_async, self.key(), self.user_fb_id, commenter_name)

  @staticmethod
  def add_comment_to_post_transaction(choosie_post_key, comment):
    updated_post = db.get(choosie_post_key)
    # updated_post.comments is a StringListProperty. We add the new comment as a string.
    updated_post.comments.append(comment.to_string_for_choosie_post())
    updated_post.put()
    CacheController.set_model(updated_post)

  def add_scraped_comments_to_post(self, comments, votes):
    db.run_in_transaction(ChoosiePost.add_scraped_comments_to_post_transaction, self.key(), comments, votes)

  @staticmethod
  def add_scraped_comments_to_post_transaction(choosie_post_key, comments, votes):
    updated_post = db.get(choosie_post_key)
    comments_as_strings = [str(comment) for comment in comments]

    for comment_str in comments_as_strings:
      # Add only new comments to updated_post.comments
      if comment_str not in updated_post.comments:
        updated_post.comments.append(comment_str)

    for vote in votes:
      updated_post.add_vote_to_post_internal(vote)
    updated_post.put()
    CacheController.set_model(updated_post)

  def add_vote_to_post(self, vote, is_new):
    db.run_in_transaction(ChoosiePost.add_vote_to_post_transaction, self.key(), vote)
    logging.info("user_id" + self.user_fb_id)
    logging.info("vote_id " + vote.user_fb_id)

    if is_new:
      user = CacheController.get_user_by_fb_id(self.user_fb_id)
      if user.num_votes:
        user.num_votes += 1
      else:
        user.num_votes = 1
      user.put()
      CacheController.invalidate_user_fb_id(user.fb_uid)

    if self.user_fb_id != vote.user_fb_id and self.created_at > datetime.datetime.now() - datetime.timedelta(0.1): # if i didnt vote on my self and the post was uploaded less then 2.5 hours ago
      vote_from = vote.get_user().display_name()
      deferred.defer(self.notify_vote_async, self.key(), self.user_fb_id, vote_from)

  @staticmethod
  def add_vote_to_post_transaction(choosie_post_key, new_vote):
    updated_post = db.get(choosie_post_key)
    updated_post.add_vote_to_post_internal(new_vote)
    updated_post.put()
    CacheController.set_model(updated_post)

  def add_vote_to_post_internal(self, new_vote):
    logging.info("Adding vote.")
    # Before adding a new vote, remove any existing votes by the same user.
    for existing_vote_str in self.votes:
      # updated_post.votes is a StringListProperty. To make the comparison,
      # each vote is 'inflated' to a dictionary, and its user ID is compared to the new_vote's.
      existing_vote_dict = Vote.from_string_for_choosie_post(existing_vote_str, keep_shallow=True)
      if existing_vote_dict["user"]["fb_uid"] == new_vote.user_fb_id:
        self.votes.remove(existing_vote_str)
        break

    # updated_post.votes is a StringListProperty. We add the new vote as a string.
    self.votes.append(new_vote.to_string_for_choosie_post())