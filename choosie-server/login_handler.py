from module_user import User
from google.appengine.ext import db
import webapp2
import datetime

class LoginHandler(webapp2.RequestHandler):
  def post(self):
    fb_access_token = str(self.request.get('fb_access_token'))
    fb_uid = str(self.request.get('fb_uid'))
    fb_access_token_expdate = datetime.datetime.now()
    #take fb_access_token expdata
    user = User.get_user_by_fb_uid(fb_uid)
    if not user:
        User.create(fb_access_token, fb_access_token_expdate)
    self.redirect('/')