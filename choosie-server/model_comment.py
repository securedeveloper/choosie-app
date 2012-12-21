import ast
import logging
import json

from google.appengine.api import memcache
from google.appengine.ext import db

from cache_controller import CacheController
from model_user import User
from utils import Utils

COMMENTS_NAMESPACE = 'COMMENTS_2'

class Comment(db.Model):
  user_fb_id = db.StringProperty()
  created_at = db.DateTimeProperty(auto_now_add = True)
  text = db.StringProperty(required = True,multiline=True)
  is_scraped = db.BooleanProperty()
  scraped_user_details = db.StringProperty()

  def to_json(self):
    user_details = ast.literal_eval(self.scraped_user_details) if self.is_scraped else self.get_user().to_short_json() 
    return {"user": user_details,
            "text": self.text,
            "created_at": str(self.created_at.replace(microsecond=0))}
      
  def to_string_for_choosie_post(self):
    as_dict = {"text": self.text,
               "created_at": str(self.created_at.replace(microsecond=0))}
    # Two different string represantations:
    if not self.is_scraped:
      # For regular comments, we store only the user_fb_id.
      # u"{'text': 'aaa',
      #    'created_at': '2012-12-14 18:14:56',
      #    'user_fb_id': u'100004697466881'}"
      as_dict["user_fb_id"] = self.user_fb_id
    else:
      # For scraped comments, we store more details about the user.
      # u"{'text': u'wadap',
      #    'created_at': '2012-12-14 13:23:39',
      #    'user': {'fb_uid': u'508640191',
      #             'first_name': u'Idan',
      #             'last_name': u'Klinger',
      #             'avatar': u'http://graph.facebook.com/508640191/picture'}}"
      as_dict["user"] = ast.literal_eval(self.scraped_user_details)
    return str(as_dict)


  @staticmethod
  def from_string_for_choosie_post(comment_str):
    # When parsing the string represantation (see to_string_for_choosie_post() above),
    # there are two options.
    as_dict = ast.literal_eval(comment_str)
    if "user_fb_id" in as_dict:
      # For option 1 (regualr, not scraped comments), we need to also get the user
      # details.
      user = CacheController.get_user_by_fb_id(as_dict["user_fb_id"])
      if user:
        del as_dict["user_fb_id"]
        as_dict["user"] = user.to_short_json()
      else:
        as_dict["user"] = {'fb_uid': as_dict["user_fb_id"]}
    return as_dict

  def get_user(self):
    return CacheController.get_user_by_fb_id(self.user_fb_id)


  @staticmethod
  def get_comments_for_post(post_key):
    comments = memcache.get(post_key, namespace=COMMENTS_NAMESPACE)
    if comments is not None:
      logging.info('Skipped a data store call for comments.')
      return comments
    else:
      logging.info('Retrieving comments for [%s] from data store.' % post_key)
      post = CacheController.get_model(post_key)
      comments = Comment.all().ancestor(post)
      memcache.set(post_key, comments, namespace=COMMENTS_NAMESPACE)
      return comments

  @staticmethod
  def invalidate_comments(post_key):
    memcache.delete(post_key, namespace=COMMENTS_NAMESPACE)

  @staticmethod
  def parse_json_to_comments_array(json_comments):
    data = json.loads(json_comments)
    logging.info("parsed data: " + str(data))

    json_data = data["data"]
    logging.info("********** parsed json data: " + str(json_data))

    comments = []
    for json_comment in json_data:
      comment = Comment(user_fb_id=json_comment["from"]["id"],
                        created_at=Utils.parse_utf_format_datetime(json_comment["created_time"]),
                        text=json_comment["message"])
      comments.append(comment)
      logging.info("Added new comment: " + comment.to_string_for_choosie_post())
