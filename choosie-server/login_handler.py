from module_user import User
from google.appengine.ext import db
import webapp2
from datetime import datetime
import logging
import json

class LoginHandler(webapp2.RequestHandler):
  def post(self):
    bobo = str(self.request.get('fb_access_toke'))
    fb_access_token = str(self.request.get('fb_access_token'))
    fb_uid = str(self.request.get('fb_uid'))
    fb_access_token_expdate = datetime.fromtimestamp(long(self.request.get('fb_access_token_expdate')) / 1e3)
    # fb_access_token_expdate = fb_access_token_expdate.replace(second=0, microsecond=0)
    user = User.get_user_by_fb_uid(fb_uid)
    if not user:
      User.create(fb_access_token, fb_access_token_expdate)
    else:
      # updating access token fields
      user.fb_access_token = fb_access_token
      user.fb_access_token_expdate = fb_access_token_expdate
      user.put()

    self.redirect('/')