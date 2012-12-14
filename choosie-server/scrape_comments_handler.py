from cache_controller import CacheController
from model_user import User
from model_comment import Comment
from google.appengine.ext import db
import webapp2
from datetime import datetime
from utils import Utils
import logging
import json

class ScrapeCommentsHandler(webapp2.RequestHandler):
  def post(self):
    fb_post_id = str(self.request.get('fb_post_id'))
    fb_access_token = str(self.request.get('fb_access_token'))
    json_comments = Utils.get_json_comments_from_fb_post(fb_post_id, fb_access_token)

    comments = []
    ScrapeCommentsHandler.parse_json_to_comments_array(json_comments)


  @staticmethod
  def parse_json_to_comments_array(json_comments):
    data = json.loads(json_comments)
    logging.info("parsed data: " + str(data))

    json_data = data["data"]
    logging.info("parsed json data: " + str(json_data))

    comments = []
    for json_comment in json_data:
      comment = Comment(user_fb_id=json_comment["from"]["id"],
                        created_at=Utils.parse_utf_format_datetime(json_comment["created_time"]),
                        text=json_comment["message"])
      comments.append(comment)
      logging.info("Added new comment: " + comment.to_string_for_choosie_post() + "\n")