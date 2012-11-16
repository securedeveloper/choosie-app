from google.appengine.ext import db
from google.appengine.api import images

class esh:
  def to_json(self):
    return {
            "photo1": self.photo_path(1),
            "photo2": self.photo_path(2),
            "votes1": int(self.votes1),
            "votes2": int(self.votes2),
            "question": str(self.question),
            "date": str(self.date)
           }



class ChoosiePost(db.Model):
  photo1 = db.BlobProperty()
  photo2 = db.BlobProperty()
  votes1 = db.IntegerProperty()
  votes2 = db.IntegerProperty()
  question = db.StringProperty()
  date = db.DateTimeProperty(auto_now_add = True)

  def to_json(self):
    return {
            "photo1": self.photo_path(1),
            "photo2": self.photo_path(2),
            "votes1": int(self.votes1),
            "votes2": int(self.votes2),
            "question": str(self.question),
            "date": str(self.date)
           }

  def photo_path(self, which_photo):
    return "/photo?which_photo=" + str(which_photo) + "&post_key=" + str(self.key())