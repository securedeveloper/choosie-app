import ast
import logging

from google.appengine.api import memcache
from google.appengine.ext import db

from cache_controller import CacheController
from model_user import User

COMMENTS_NAMESPACE = 'COMMENTS_2'

class Comment(db.Model):
  user_fb_id = db.StringProperty()
  created_at = db.DateTimeProperty(auto_now_add = True)
  text = db.StringProperty(required = True)

  def to_json(self):
    return {"user": self.get_user().to_short_json(),
            "text": self.text,
            "created_at": str(self.created_at.replace(microsecond=0))}
  
  def to_string_for_choosie_post(self):
    return str({"user_fb_id": self.user_fb_id,
                "text": self.text,
                "created_at": str(self.created_at.replace(microsecond=0))})

  @staticmethod
  def from_string_for_choosie_post(shallow_comment_str):
    shallow_comment = ast.literal_eval(shallow_comment_str)
    user = CacheController.get_user_by_fb_id(shallow_comment["user_fb_id"])
    return {"user": user.to_short_json(),
            "text": shallow_comment["text"],
            "created_at": shallow_comment["created_at"]}

  def get_user(self):
    return CacheController.get_user_by_fb_id(self.user_fb_id)


  @staticmethod
  def get_comments_for_post(post_key):
    comments = memcache.get(post_key, namespace=COMMENTS_NAMESPACE)
    if comments is not None:
      logging.info('Skipped a data store call for comments.')
      return comments
    else:
      logging.info('Retreiving comments for [%s] from data store.' % post_key)
      post = CacheController.get_model(post_key)
      comments = Comment.all().ancestor(post)
      memcache.set(post_key, comments, namespace=COMMENTS_NAMESPACE)
      return comments

  @staticmethod
  def invalidate_comments(post_key):
    memcache.delete(post_key, namespace=COMMENTS_NAMESPACE)
    
   