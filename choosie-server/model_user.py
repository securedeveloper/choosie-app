from google.appengine.ext import db
from google.appengine.api import urlfetch
from utils import Utils
import facebook
import datetime
import json
import logging
import facebook

class User(db.Model):
    fb_uid = db.StringProperty(required=True)
    first_name = db.StringProperty(required=True)
    last_name = db.StringProperty(required=True)
    username = db.StringProperty()
    created_at = db.DateTimeProperty(auto_now_add=True)
    last_login = db.DateTimeProperty(auto_now_add=True,required=True)
    gender = db.StringProperty()
    fb_access_token = db.StringProperty(required=True)
    fb_access_token_expdate = db.DateTimeProperty(required=True)
    device_id = db.StringProperty()
    email = db.StringProperty()
    info = db.StringProperty()
    nick = db.StringProperty()
    num_posts = db.IntegerProperty(default=0)
    num_votes = db.IntegerProperty(default=0)

    def name(self):
      return self.first_name + " " + self.last_name

    @staticmethod
    def create(fb_access_token, fb_access_token_expdate):
      logging.info('creating')
      user_json = Utils.get_user_from_fb(fb_access_token)
      if (user_json is not None):
        logging.info('user_json is not None: %s' % user_json)
        user = User.fb_user_to_choosie_user(user_json, fb_access_token, fb_access_token_expdate)
        # user.set_friends()
        user.put()

    @staticmethod
    #returns choosie_user from fb_user
    def fb_user_to_choosie_user(fb_user_json, fb_access_token, fb_access_token_expdate):
      data = json.loads(fb_user_json)
     
      user = User(fb_uid = data["id"],
                  first_name = data.get("first_name"),
                  last_name = data.get("last_name"),
                  username = data.get("username"),
                  gender = data.get("gender"),
                  email = data.get("email"),
                  fb_access_token = fb_access_token,
                  fb_access_token_expdate = fb_access_token_expdate)
      return user

    def to_short_json(self):
      return {"fb_uid": self.fb_uid,
              "first_name": self.first_name,
              "last_name": self.last_name,
              "avatar": Utils.get_avatar(self.username)}

    # def set_friends(self):
    #   graph = facebook.GraphAPI(self.fb_access_token)
    #   h = graph.get_connections("me", "friends")
    #   logging.info(h["data"])
    #   self.friends  = [item["id"] for item in h["data"]]

