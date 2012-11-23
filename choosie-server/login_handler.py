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
    fb_access_token_expdate = fb_access_token_expdate.replace(second=0, microsecond=0)
    user = User.get_user_by_fb_uid(fb_uid)
    if not user:
      logging.info("new user!")
      logging.info("access_token:%s" % fb_access_token)
      logging.info("access_token_exp_date:%s" % str(fb_access_token_expdate))
      User.create(fb_access_token, fb_access_token_expdate)
      self.write_success()
    else:
      self.write_error("User exists.")

  def write_error(self, err):
    self.response.out.write(json.dumps({"success" : "no", "error": err}))

  def write_success(self):
    self.response.out.write(json.dumps({"success" : "yes"}))      