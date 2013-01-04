from cache_controller import CacheController
from model_user import User
from model_comment import Comment
from model_vote import Vote
from google.appengine.ext import db
import webapp2
import logging
from model_user import User

class RegisterDeviceHandler(webapp2.RequestHandler):
  def get(self):
    self.post()

  def post(self):
    fb_uid = str(self.request.get('fb_uid'))
    device_id = str(self.request.get('device_id'))
    user = CacheController.get_user_by_fb_id(fb_uid)
    if user is not None:
      logging.info("device id was set to " + device_id)
      user.device_id = device_id
      user.put()
      CacheController.invalidate_user_fb_id(fb_uid)
      logging.info("Device was registered successfully for user " + user.first_name + " " + user.last_name)
    else:
      logging.error("No user was found with user_id %s. Failed to register device." % fb_uid)
    