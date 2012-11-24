import webapp2
from module_choosie_post import ChoosiePost
from google.appengine.api import images
from google.appengine.ext import db
from module_user import User
import logging

class PostsHandler(webapp2.RequestHandler):
  def shrinkImage(self, data):
    img = images.Image(data)
    max_width = 800
    max_height = 800;
    ratio = min(max_width/img.width, max_height/img.height)
    img.resize(width=ratio*img.width, height=ratio*img.height)
    img.im_feeling_lucky()
    return img.execute_transforms(output_encoding=images.PNG)
  
  def post(self):
    
    user = User.get_user_by_fb_uid(self.request.get('fb_uid'))
    logging.info(self.request.get('fb_uid'))
    if user is None:
       self.error(500)
       logging.error("user not found!")
       return;

    logging.info("user found!")
    choosie_post = ChoosiePost(question = self.request.get('question'),
                               user = user,
                               photo1 = db.Blob(self.shrinkImage(self.request.get('photo1'))),
                               photo2 = db.Blob(self.shrinkImage(self.request.get('photo2'))))

    choosie_post.put()
    self.redirect('/')