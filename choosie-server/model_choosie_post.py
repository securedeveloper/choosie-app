from google.appengine.ext import db
from google.appengine.api import images

from cache_controller import CacheController
from model_user import User
from model_vote import Vote
from model_comment import Comment
from utils import Utils
import facebook
import logging

class ChoosiePost(db.Model):
  photo1 = db.BlobProperty(required = True)
  photo2 = db.BlobProperty(required = True)
  question = db.StringProperty(indexed = False, required = True)
  created_at = db.DateTimeProperty(auto_now_add = True)
  user = db.ReferenceProperty(User, required = False)
  user_fb_id = db.StringProperty()
  updated_at = db.DateTimeProperty(indexed = True, auto_now = True)
  photo = db.BlobProperty()

  def to_json(self):
    votes = self.votes()
    return {"key": str(self.key()),
            "user": self.get_user().to_short_json(),
            "votes": Utils.items_to_json(votes),
            "comments": Utils.items_to_json(self.comments()),
            "photo1": self.photo_path(1),
            "photo2": self.photo_path(2),
            "question": str(self.question),
            "created_at": str(self.created_at),
            "updated_at": str(self.updated_at)
           }

  def get_user(self):
    if not self.user_fb_id:
      # Old versions had the 'user' reference property. We switched to user_fb_id.
      self.user_fb_id = self.user.fb_uid
      self.user = None
      self.put()

    return CacheController.get_user_by_fb_id(self.user_fb_id)

  def votes(self):
    return Vote.get_votes_for_post(str(self.key()))

  def comments(self):
    return Comment.get_comments_for_post(str(self.key()))

  def photo_path(self, which_photo):
    return '/photo?which_photo=%s&post_key=%s' % (which_photo, self.key())

  def publish_to_facebook(self):
    Utils.create_post_image(self)
    attach = {"picture": self.photo}
    graph = facebook.GraphAPI(self.user.fb_access_token)
    response = graph.put_wall_post("ola!", attach)