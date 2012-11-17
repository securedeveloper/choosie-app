from google.appengine.ext import db
from google.appengine.api import images

class ChoosiePost(db.Model):
  photo1 = db.BlobProperty(required=True)
  photo2 = db.BlobProperty(required=True)
  votes1 = db.IntegerProperty(required=True)
  votes2 = db.IntegerProperty(required=True)
  question = db.StringProperty(required=True)
  created_at = db.DateTimeProperty(auto_now_add = True)
  user = db.ReferenceProperty(required=True)

  def to_json(self):
    return {
            "photo1": self.photo_path(1),
            "photo2": self.photo_path(2),
            "votes1": int(self.votes1),
            "votes2": int(self.votes2),
            "question": str(self.question),
            "created_at": str(self.created_at)
           }

  def photo_path(self, which_photo):
    return '/photo?which_photo=%s&post_key=%s' % (which_photo, self.key()) 
    # return "/photo?which_photo=" + str(which_photo) + "&post_key=" + str(self.key())

  @staticmethod
  def posts_to_json(choosie_posts):
    feed = []
    for choosie_post in choosie_posts:
      feed.append(choosie_post.to_json())
    return feed;
