import webapp2
from modules import ChoosiePost
from google.appengine.api import images
from google.appengine.ext import db

class UploadHandler(webapp2.RequestHandler):
  def shrinkImage(self, data):
    img = images.Image(data)
    img.resize(width=200, height=200)
    img.im_feeling_lucky()
    return img.execute_transforms(output_encoding=images.PNG)
  
  def post(self):
    choosie_post = ChoosiePost()
    choosie_post.question = self.request.get('question')
    choosie_post.votes1 = 0
    choosie_post.votes2 = 0
    if self.request.get('photo1') and self.request.get('photo2'):
      choosie_post.photo1 = db.Blob(self.shrinkImage(self.request.get('photo1')))
      choosie_post.photo2 = db.Blob(self.shrinkImage(self.request.get('photo2')))

    choosie_post.put()
    self.redirect('/')