import logging
import webapp2
import math

from google.appengine.api import images
from google.appengine.ext import db

from cache_controller import CacheController
from model_choosie_post import ChoosiePost
from model_user import User
from utils import Utils

class PostsHandler(webapp2.RequestHandler):
  def shrinkImage(self, data):
    img = images.Image(data)
    max_width = 800
    max_height = 800
    logging.info("width-" + str(img.width))
    logging.info("height-" + str(img.height))
    ratio = math.ceil(min(float(max_width)/float(img.width), float(max_height)/float(img.height)))
    if ratio < 1.0:
      logging.info("ratio:" + str(ratio))
      # Only shrink the image: if it is already smaller than 800px on both axes
      # do nothing.
      img.resize(width=ratio*img.width, height=ratio*img.height)
    img.im_feeling_lucky()
    return img.execute_transforms(output_encoding=images.PNG)
  
  def post(self):
    user = CacheController.get_user_by_fb_id(self.request.get('fb_uid'))
    logging.info(self.request.get('fb_uid'))
    if user is None:
       self.error(500)
       logging.error("user not found!")
       return

    logging.info("user found!")

    #updating user access token cause he might added publish_stream permission
    # user.fb_access_token = str(self.request.get('fb_access_token'))
    # user.fb_access_token_expdate = Utils.get_access_token_from_request(self.request)
    # user.put()

    photo1_blob_key = Utils.write_file_to_blobstore(self.shrinkImage(self.request.get('photo1')))
    photo2_blob_key = Utils.write_file_to_blobstore(self.shrinkImage(self.request.get('photo2')))

    choosie_post = ChoosiePost(question = self.request.get('question'),
                               user_fb_id = self.request.get('fb_uid'),
                               photo1_blob_key = photo1_blob_key,
                               photo2_blob_key = photo2_blob_key)

    # Save this post in the datastore, and also in the memcache.
    choosie_post.put()
    CacheController.set_model(choosie_post)
    logging.info("share:" + self.request.get("share_to_fb", default_value="off"))
    if (self.request.get("share_to_fb") == "on"):
      logging.info("publishing!!")
      choosie_post.publish_to_facebook(self.request.host_url)  
    self.redirect('/')
    