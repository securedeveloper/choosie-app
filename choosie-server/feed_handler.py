from module_choosie_post import ChoosiePost
from module_user import User
from google.appengine.ext import db
import webapp2
import json
import logging
from utils import Utils

class FeedHandler(webapp2.RequestHandler):
	def get(self):
		choosie_posts, cursor = FeedHandler.GetFeedAndCursor(self.request.get('cursor'), self.request.get('limit'))
		choosie_posts_json = Utils.items_to_json(choosie_posts)
		self.response.out.write(json.dumps({'feed' : choosie_posts_json, 'cursor': cursor}))
		
	@staticmethod
	def GetFeedAndCursor(cursor, limit = 10):
		if not limit:
			limit = 10
		posts = ChoosiePost.all()
		if cursor:
			posts.with_cursor(cursor)
		posts.order("-created_at")
		posts_result = []
		for post in posts.run(limit=int(limit)):
			posts_result.append(post)
		new_cursor = posts.cursor()
		return (posts_result, new_cursor)