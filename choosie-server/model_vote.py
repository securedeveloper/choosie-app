import ast
import logging

from cache_controller import CacheController
from google.appengine.api import memcache
from google.appengine.ext import db
from model_user import User

VOTES_NAMESPACE = 'VOTES_2'

class Vote(db.Model):
  user_fb_id = db.StringProperty()
  created_at = db.DateTimeProperty(auto_now_add=True)
  vote_for = db.IntegerProperty(required=True,choices=set([1, 2]))

  # Returns previous vote for the same user for the same post
  def prev_vote_for_user_for_post(self):
    return Vote.all().filter("user_fb_id =", self.user_fb_id).ancestor(self.parent()).get()

  def to_json(self):
    return {"user": self.get_user().to_short_json(),
            "vote_for": self.vote_for,
            "created_at": str(self.created_at.replace(microsecond=0))
           }

  def to_string_for_choosie_post(self):
    return str({"user_fb_id": self.user_fb_id,
                "vote_for": self.vote_for,
                "created_at": str(self.created_at.replace(microsecond=0))})

  @staticmethod
  def from_string_for_choosie_post(shallow_vote_str):
    shallow_vote_dict = ast.literal_eval(shallow_vote_str)
    user = CacheController.get_user_by_fb_id(shallow_vote_dict["user_fb_id"])
    return {"user": user.to_short_json(),
            "vote_for": shallow_vote_dict["vote_for"],
            "created_at": shallow_vote_dict["created_at"]}

             
  def get_user(self):
    return CacheController.get_user_by_fb_id(self.user_fb_id)


  @staticmethod
  def get_votes_for_post(post_key):
    votes = memcache.get(post_key, namespace=VOTES_NAMESPACE)
    if votes is not None:
      logging.info('Skipped a data store call for votes.')
      return votes
    else:
      logging.info('Retrieving votes for [%s] from data store.' % post_key)
      post = CacheController.get_model(post_key)
      votes = Vote.all().ancestor(post)
      memcache.set(post_key, votes, namespace=VOTES_NAMESPACE)
      return votes

  @staticmethod
  def invalidate_votes(post_key):
    memcache.delete(post_key, namespace=VOTES_NAMESPACE)

