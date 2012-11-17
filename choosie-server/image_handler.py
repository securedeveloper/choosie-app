import webapp2
from module_choosie_post import ChoosiePost
from google.appengine.ext import db

class ImageHandler(webapp2.RequestHandler):
  def get(self):
    choosie_post = db.get(self.request.get('post_key'))
    which_photo = int(self.request.get('which_photo'))
    result = {
    1 : choosie_post.photo1,
    2 : choosie_post.photo2
    }[which_photo]
      
    self.response.headers['Content-Type'] = 'image/png'
    self.response.out.write(result)