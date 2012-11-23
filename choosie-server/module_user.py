from google.appengine.ext import db
from google.appengine.api import urlfetch
from utils import Utils
import facebook
import datetime
import json
import logging


class User(db.Model):
    fb_uid = db.StringProperty(required=True)
    first_name = db.StringProperty(required=True)
    last_name = db.StringProperty(required=True)
    username = db.StringProperty()
    created_at = db.DateTimeProperty(auto_now_add=True)
    last_login = db.DateTimeProperty(auto_now_add=True,required=True)
    # email = db.EmailProperty()
    gender = db.StringProperty()
    fb_access_token = db.StringProperty(required=True)
    fb_access_token_expdate = db.DateTimeProperty(required=True)

    @staticmethod
    def create(fb_access_token, fb_access_token_expdate):
      logging.info('creating')
      user_json = Utils.get_user_from_fb(fb_access_token)
      if (user_json is not None):
        logging.info('user_uson not none')
        user = User.fb_user_to_choosie_user(user_json,fb_access_token, fb_access_token_expdate)
        user.put()

    @staticmethod
    #returns choosie_user from fb_user
    def fb_user_to_choosie_user(fb_user_json, fb_access_token, fb_access_token_expdate):
      data = json.loads(fb_user_json)
     
      user = User(fb_uid = data["id"],
                  first_name = data["first_name"],
                  last_name = data["last_name"],
                  username = data["username"],
                  gender = data["gender"],
                  fb_access_token = fb_access_token,
                  fb_access_token_expdate = fb_access_token_expdate)
      return user

    @staticmethod
    def get_user_by_fb_uid(fb_uid):
      return db.GqlQuery("SELECT * from User where fb_uid = :1", fb_uid).get()


    def avatar(self):
      return 'http://graph.facebook.com/%s/picture' % self.username

    def to_short_json(self):
      return {"fb_uid": self.fb_uid,
              "first_name": self.first_name,
              "last_name": self.last_name,
              "avatar": self.avatar()}
    

