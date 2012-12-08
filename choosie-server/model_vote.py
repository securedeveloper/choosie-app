import logging

from cache_controller import CacheController
from google.appengine.api import memcache
from google.appengine.ext import db
from model_user import User

VOTES_NAMESPACE = 'VOTES_2'

class Vote(db.Model):
  user = db.ReferenceProperty(User, required = False)
  user_fb_id = db.StringProperty()
  created_at = db.DateTimeProperty(auto_now_add=True)
  vote_for = db.IntegerProperty(required=True,choices=set([1, 2]))

  #Returns previous vote for the same user for the same post
  def prev_vote_for_user_for_post(self):
    return Vote.all().filter("user =", self.user).ancestor(self.parent()).get()

  def to_json(self):
    return {"user": self.get_user().to_short_json(),
            "vote_for":self.vote_for,
            "created_at": str(self.created_at.replace(microsecond=0))
           }
           
  def get_user(self):
    if not self.user_fb_id:
      # Old versions had the 'user' reference property. We switched to user_fb_id.
      self.user_fb_id = self.user.fb_uid
      self.user = None
      self.put()

    return CacheController.get_user_by_fb_id(self.user_fb_id)


  @staticmethod
  def get_votes_for_post(post_key):
    votes = memcache.get(post_key, namespace=VOTES_NAMESPACE)
    if votes is not None:
      logging.info('Skipped a data store call for votes.')
      return votes
    else:
      logging.info('Retreiving votes for [%s] from data store.' % post_key)
      post = CacheController.get_model(post_key)
      votes = Vote.all().ancestor(post)
      memcache.set(post_key, votes, namespace=VOTES_NAMESPACE)
      return votes

  @staticmethod
  def invalidate_votes(post_key):
    memcache.delete(post_key, namespace=VOTES_NAMESPACE)

