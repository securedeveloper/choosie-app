from google.appengine.ext import db
from google.appengine.api import urlfetch
from datetime import datetime
import urllib2
import cStringIO
from google.appengine.api import images
import logging
class Utils():
    @staticmethod
    def get_user_from_fb(fb_access_token):
      url = "https://graph.facebook.com/me?access_token=" + fb_access_token 
      result = urlfetch.fetch(url)
      if result.status_code != 200:
        return None
      return result.content;
      
    @staticmethod
    def items_to_json(items):
      arr = []
      for item in items:
        arr.append(item.to_json())
      return arr

    @staticmethod
    def get_access_token_from_request(request):
      return datetime.fromtimestamp(long(request.get('fb_access_token_expdate')) / 1e3)

    @staticmethod
    def create_post_image(choosie_post):
      img1 = images.Image(choosie_post.photo1)
      img1.resize(240,352)
      img2 = images.Image(choosie_post.photo2)
      img2.resize(240,352)
      composite = images.composite([(img1, 0, 0, 1.0, images.TOP_LEFT),
      (img2, 240, 0, 1.0, images.TOP_LEFT)], 480, 704)
      choosie_post.photo = db.Blob(composite)
      choosie_post.put()
      logging.info('___saved')

    