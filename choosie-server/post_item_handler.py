import webapp2
from module_choosie_post import ChoosiePost
from google.appengine.api import images
from google.appengine.ext import db
from module_user import User
import logging

class PostItemHandler(webapp2.RequestHandler):
  def get(self, key):
    logging.info(key)
    choosie_post = db.get(key)
    self.response.out.write(choosie_post.to_json())