from google.appengine.ext import db
from google.appengine.api import images

from cache_controller import CacheController
from model_user import User
from model_vote import Vote
from model_comment import Comment
from utils import Utils

import ast
import facebook
import logging

class ChoosiePost(db.Model):
  photo1 = db.BlobProperty(required = True)
  photo2 = db.BlobProperty(required = True)
  question = db.StringProperty(indexed = False, required = True)
  created_at = db.DateTimeProperty(auto_now_add = True)
  user_fb_id = db.StringProperty()
  updated_at = db.DateTimeProperty(indexed = True, auto_now = True)
  photo = db.BlobProperty()
  comments = db.StringListProperty()
  votes = db.StringListProperty()

  def to_json(self):
    return {"key": str(self.key()),
            "user": self.get_user().to_short_json(),
            "votes": self.get_serialized_votes(),
            "comments": self.get_serialized_comments(),
            "photo1": self.photo_path(1),
            "photo2": self.photo_path(2),
            "question": str(self.question),
            "created_at": str(self.created_at),
            "updated_at": str(self.updated_at)
           }

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
    return '/photo?which_photo=%s&post_key=%s' % (which_photo, self.key())

  def publish_to_facebook(self):
    Utils.create_post_image(self)
    attach = {"picture": self.photo}
    graph = facebook.GraphAPI(self.user.fb_access_token)
    response = graph.put_wall_post("ola!", attach)

  def add_comment_to_post(self, comment):
    db.run_in_transaction(ChoosiePost.add_comment_to_post_transaction, self.key(), comment)

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

  def add_vote_to_post(self, vote):
    db.run_in_transaction(ChoosiePost.add_vote_to_post_transaction, self.key(), vote)

  @staticmethod
  def add_vote_to_post_transaction(choosie_post_key, new_vote):
    updated_post = db.get(choosie_post_key)
    updated_post.add_vote_to_post_internal(new_vote)
    CacheController.set_model(updated_post)

  def add_vote_to_post_internal(self, new_vote):
    # Before adding a new vote, remove any existing votes by the same user.
    for existing_vote_str in self.votes:
      # updated_post.votes is a StringListProperty. To make the comparison,
      # each vote is 'inflated' to a dictionary, and its user ID is compared to the new_vote's.
      existing_vote_dict = ast.literal_eval(existing_vote_str)
      existing_voter = (existing_vote_dict['user_fb_id'] if 'user_fb_id' in existing_vote_dict
                        else existing_vote_dict['user']['fb_uid'])
      if existing_voter == new_vote.user_fb_id:
        self.votes.remove(existing_vote_str)
        break

    # updated_post.votes is a StringListProperty. We add the new vote as a string.
    self.votes.append(new_vote.to_string_for_choosie_post())