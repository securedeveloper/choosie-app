from google.appengine.ext import db
from google.appengine.api import images

from cache_controller import CacheController
from model_user import User
from model_vote import Vote
from model_comment import Comment
from utils import Utils
import facebook
import logging

class FbPost(db.Model):
  choosie_post_id = db.StringProperty(required = True)
  fb_access_token = db.StringProperty(required = True)
  fb_post_id = db.StringProperty(required = True)
  last_comment_date = db.DateTimeProperty(required = False)

def scrape_comments(self):
	#add ancestor
	fb_posts = FbPost.all()
	for post in fb_posts:
		comments = Utils.get_comments_from_fb_post(self.fb_post_id, self.fb_access_token)
		#update last comment date

