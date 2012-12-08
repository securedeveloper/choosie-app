from google.appengine.ext import db
from model_user import User
import logging

class Vote(db.Model):
  user = db.ReferenceProperty(User,required=True)
  created_at = db.DateTimeProperty(auto_now_add=True)
  vote_for= db.IntegerProperty(required=True,choices=set([1, 2]))

  #Returns previous vote for the same user for the same post
  def prev_vote_for_user_for_post(self):
    return Vote.all().filter("user =", self.user).ancestor(self.parent()).get()

  def to_json(self):
    return {
            "fb_uid": self.user.fb_uid,
            "vote_for":self.vote_for,
            "created_at": str(self.created_at.replace(microsecond=0))
           }