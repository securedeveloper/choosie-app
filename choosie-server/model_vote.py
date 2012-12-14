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
  is_scraped = db.BooleanProperty()
  scraped_user_details = db.StringProperty()

  # Returns previous vote for the same user for the same post
  def prev_vote_for_user_for_post(self):
    return Vote.all().filter("user_fb_id =", self.user_fb_id).ancestor(self.parent()).get()

  def to_json(self):
    user_details = ast.literal_eval(self.scraped_user_details) if self.is_scraped else self.get_user().to_short_json() 
    return {"user": user_details,
            "vote_for": self.vote_for,
            "created_at": str(self.created_at.replace(microsecond=0))
           }

  def to_string_for_choosie_post(self):
    as_dict = {"vote_for": self.vote_for,
               "created_at": str(self.created_at.replace(microsecond=0))}
    # Two different string represantations (see comment in model_comment.py)
    if not self.is_scraped:
      as_dict["user_fb_id"] = self.user_fb_id
    else:
      as_dict["user"] = ast.literal_eval(self.scraped_user_details)
    return str(as_dict)

  @staticmethod
  def from_string_for_choosie_post(vote_str):
    as_dict = ast.literal_eval(vote_str)
    if "user_fb_id" in as_dict:
      # For option 1 (regualr, not scraped comments), we need to also get the user
      # details.
      user = CacheController.get_user_by_fb_id(as_dict["user_fb_id"])
      del as_dict["user_fb_id"]
      as_dict["user"] = user.to_short_json()
    return as_dict

             
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

