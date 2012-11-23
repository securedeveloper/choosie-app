from google.appengine.ext import db
from module_user import User
import logging

class Vote(db.Model):
  user = db.ReferenceProperty(User,required=True)
  created_at = db.DateTimeProperty(auto_now_add=True)
  vote_for= db.IntegerProperty(required=True,choices=set([1, 2]))

  def set_votes(self):
    if self.vote_for == 1:
      self.parent().votes1 += 1
    elif self.vote_for == 2:
      self.parent().votes2 += 1
    self.parent().put()

  #Checks if vote with the same user and post already exists

  def is_exist(self):
    q = Vote.all().filter("user =", self.user).ancestor(self.parent())
    return q.get() != None

  def to_json(self):
    return {
              "fb_uid": self.user.fb_uid,
              "vote_for":self.vote_for,
              "created_at": str(self.created_at)
           }