from model_user import User
from google.appengine.ext import db
import webapp2
from datetime import datetime
from utils import Utils
import logging
import json

class LoginHandler(webapp2.RequestHandler):
  def post(self):
    fb_access_token = str(self.request.get('fb_access_token'))
    fb_uid = str(self.request.get('fb_uid'))
    fb_access_token_expdate = Utils.get_access_token_from_request(self.request)
    user = User.get_user_by_fb_uid(fb_uid)
    if not user:
      User.create(fb_access_token, fb_access_token_expdate)
    else:
      # updating access token fields
      user.fb_access_token = fb_access_token
      user.fb_access_token_expdate = fb_access_token_expdate
      user.put()

    self.redirect('/')