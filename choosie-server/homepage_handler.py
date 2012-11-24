import webapp2
from module_choosie_post import ChoosiePost
from feed_handler import FeedHandler
from google.appengine.ext import db
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
    choosie_posts, cursor = FeedHandler.GetFeedAndCursor(self.request.get('cursor'), limit)
    template_values = {
      'choosie_posts': choosie_posts,
      'cursor': cursor,
      'limit': limit
    }

    template = jinja_environment.get_template('index.html')
    self.response.write(template.render(template_values))