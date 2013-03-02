import json
import logging
import webapp2

from cache_controller import CacheController
from model_user import User

class UserEditHandler(webapp2.RequestHandler):
  def post(self, fb_uid):
     user = CacheController.get_user_by_fb_id(fb_uid)
     nick = self.request.get("nick")
     info = self.request.get("info")
     user.nick = nick
     user.info = info
     user.put()
     CacheController.invalidate_user_fb_id(fb_uid)
     self.response.out.write(json.dumps({"success" : "true"}))
