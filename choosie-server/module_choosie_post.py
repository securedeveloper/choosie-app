from google.appengine.ext import db
from google.appengine.api import images
from module_user import User
import logging

class ChoosiePost(db.Model):
  photo1 = db.BlobProperty(required=True)
  photo2 = db.BlobProperty(required=True)
  votes1 = db.IntegerProperty(required=True,default=0)
  votes2 = db.IntegerProperty(required=True,default=0)
  question = db.StringProperty(required=True)
  created_at = db.DateTimeProperty(auto_now_add = True)
  user = db.ReferenceProperty(User,required=True)

  def to_json(self):
    return {"user": self.user.to_short_json(),
            "photo1": self.photo_path(1),
            "photo2": self.photo_path(2),
            "votes1": int(self.votes1),
            "votes2": int(self.votes2),
            "question": str(self.question),
            "created_at": str(self.created_at)
           }

  def photo_path(self, which_photo):
    return '/photo?which_photo=%s&post_key=%s' % (which_photo, self.key()) 

  @staticmethod
  def posts_to_json(choosie_posts):
    feed = []
    for choosie_post in choosie_posts:
      feed.append(choosie_post.to_json())
    return feed;
