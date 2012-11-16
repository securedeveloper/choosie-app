from modules import ChoosiePost
from google.appengine.ext import db
import webapp2
import json

class FeedHandler(webapp2.RequestHandler):
  def get(self):
    choosie_posts = db.GqlQuery('SELECT * '
                                'FROM ChoosiePost '
                                'ORDER BY date DESC LIMIT 10')

    feed = []
    for choosie_post in choosie_posts:
      feed.append(choosie_post.to_json())

    self.response.out.write(json.dumps({"feed" : feed}))
