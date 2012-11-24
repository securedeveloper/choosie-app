from module_choosie_post import ChoosiePost
from module_user import User
from google.appengine.ext import db
import webapp2
import json
import logging
from utils import Utils

class FeedHandler(webapp2.RequestHandler):
  def get(self):
    choosie_posts = db.GqlQuery("SELECT * FROM ChoosiePost ORDER BY created_at DESC LIMIT 10")
    self.response.out.write(json.dumps({"feed" : Utils.items_to_json(choosie_posts)}))