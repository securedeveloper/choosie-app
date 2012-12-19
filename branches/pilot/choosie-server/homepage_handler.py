from google.appengine.api import memcache
import json
import webapp2

from feed_handler import FeedHandler
from model_choosie_post import ChoosiePost
from utils import Utils

import jinja2
import os

jinja_environment = jinja2.Environment(
    loader = jinja2.FileSystemLoader(os.path.dirname(__file__)))

class HomepageHandler(webapp2.RequestHandler):
  def get(self):
    self.response.headers['Content-Type'] = 'text/html'
    limit = self.request.get('limit')
    if limit:
      limit = int(limit)
    choosie_posts, cursor = FeedHandler.get_feed_and_cursor(self.request.get('cursor'), limit)

    template_values = {
      'choosie_posts': json.loads(json.dumps(Utils.items_to_json(choosie_posts))),
      'cursor': cursor,
      'limit': limit
    }

    template = jinja_environment.get_template('index.html')
    self.response.write(template.render(template_values))