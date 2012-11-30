from google.appengine.ext import db
from module_user import User
import logging

class Comment(db.Model):
  user = db.ReferenceProperty(User, required = True)
  created_at = db.DateTimeProperty(auto_now_add = True)
  text = db.StringProperty(required = True)

  def to_json(self):
    return {"fb_uid": self.user.fb_uid,
            "user": self.user.to_short_json(),
            "text": self.text,
            "created_at": str(self.created_at.replace(microsecond=0))}
