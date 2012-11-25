import logging
import webapp2

from google.appengine.api import images
from google.appengine.ext import db

from cache_controller import CacheController
from module_choosie_post import ChoosiePost
from module_user import User

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

    # Save this post in the datastore, and also in the memcache.
    choosie_post.put()
    CacheController.set_model(choosie_post)
    self.redirect('/')