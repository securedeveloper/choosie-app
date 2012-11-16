import webapp2
from modules import ChoosiePost
from google.appengine.ext import db
import jinja2
import os

jinja_environment = jinja2.Environment(
    loader = jinja2.FileSystemLoader(os.path.dirname(__file__)))

class HomepageHandler(webapp2.RequestHandler):
  def get(self):
    self.response.headers['Content-Type'] = 'text/html'

    choosie_posts = db.GqlQuery('SELECT * '
                                'FROM ChoosiePost '
                                'ORDER BY date DESC LIMIT 10')
    template_values = {
      'choosie_posts': choosie_posts
    }
    template = jinja_environment.get_template('index.html')
    self.response.write(template.render(template_values))