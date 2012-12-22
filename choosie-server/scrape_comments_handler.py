from cache_controller import CacheController
from model_user import User
from model_comment import Comment
from model_vote import Vote
from google.appengine.ext import db
import webapp2
from datetime import datetime
from utils import Utils
import logging
import json
import string
import re

class ScrapeCommentsHandler(webapp2.RequestHandler):
  def post(self):
    choosie_post_key = str(self.request.get('choosie_post_key'))

    comments, votes, error = ScrapeCommentsHandler.scrape_comments_and_votes_from_facebook(choosie_post_key)

    if error:
      message = error
    else:
      message = ("Scraped %d comments and %d votes.<br><br>Comments: %s<br><br><br><br>Votes: %s"
                 % (len(comments), len(votes), json.dumps(comments), json.dumps([vote.to_json() for vote in votes])))
    self.response.write(message)


  @staticmethod
  def scrape_comments_and_votes_from_facebook(choosie_post_key):
    choosie_post = CacheController.get_model(choosie_post_key)
    if not choosie_post:
      return (None, None, "[%s] is not a valid ChoosiePost key" % choosie_post_key)

    fb_post_id = choosie_post.fb_post_id
    if not fb_post_id:
      return (None, None, "[%s] doesn't have an associated Facebook post ID" % choosie_post_key)

    fb_access_token = choosie_post.get_user().fb_access_token
    json_comments, error = Utils.get_json_comments_from_fb_post(fb_post_id, fb_access_token)
    if error:
      return (None, None, "Couldn't scraped comments for post with ID [%s] (user FB UID = [%s]). Error: %s"
                          % (fb_post_id, choosie_post.fb_post_id, error))

    comments, votes = ScrapeCommentsHandler.parse_facebook_comments(json_comments)

    if choosie_post_key and (len(comments) > 0 or len(votes) > 0):
      choosie_post = CacheController.get_model(choosie_post_key)
      if choosie_post:
        choosie_post.add_scraped_comments_to_post(comments, votes)
    
    return comments, votes, None


  @staticmethod
  def parse_facebook_comments(json_comments):
    data = json.loads(json_comments)
    logging.info("parsed data: " + str(data))

    json_data = data["data"]
    logging.info("parsed json data: " + str(json_data))

    comments = []
    votes = []
    for json_comment in json_data:
      comment = ScrapeCommentsHandler.build_comment_model_from_fb_comment(json_comment)
      vote = ScrapeCommentsHandler.try_parse_vote(comment)
      if vote:
        votes.append(vote)
      logging.info("Added new comment: %s", comment.to_string_for_choosie_post())
      comments.append(comment.to_json())

    return comments, votes

  @staticmethod
  def build_comment_model_from_fb_comment(json_comment):
    comment = Comment(user_fb_id=json_comment["from"]["id"],
                      created_at=Utils.parse_utf_format_datetime(json_comment["created_time"]),
                      text=json_comment["message"])
    choosie_user = CacheController.get_user_by_fb_id(json_comment["from"]["id"])
    if choosie_user is None:
      comment.is_scraped = True
      comment.scraped_user_details = str(ScrapeCommentsHandler.build_user_details(json_comment))
    return comment

  @staticmethod
  def try_parse_vote(choosie_comment):
    # If comment is something like: 'A is nicer' or 'B: because it is better' create a Vote object too.
    words = re.findall(r"[\w']+", choosie_comment.text)
    if len(words) > 0:
      vote = ScrapeCommentsHandler.vote_from_comment(words)

      if vote:
        return Vote(user_fb_id=choosie_comment.user_fb_id,
                    created_at=choosie_comment.created_at,
                    vote_for=vote,
                    is_scraped=choosie_comment.is_scraped,
                    scraped_user_details=choosie_comment.scraped_user_details)
    else:
      return None

  @staticmethod
  def vote_from_comment(comment_words):
    user_string_to_photo_number = {'1': 1,
                                   '2': 2}
    for word in comment_words:
      if word in user_string_to_photo_number:
        return user_string_to_photo_number[word]
    return None

  @staticmethod
  def build_user_details(json_comment):
    full_name = json_comment["from"]["name"]
    first_name, _, last_name = full_name.partition(" ")
    return {"fb_uid": json_comment["from"]["id"],
            "first_name": first_name,
            "last_name": last_name,
            "avatar": Utils.get_avatar(json_comment["from"]["id"])}