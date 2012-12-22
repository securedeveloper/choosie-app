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
import datetime
from model_choosie_post import ChoosiePost
from scrape_comments_handler import ScrapeCommentsHandler

class ScrapeHandler(webapp2.RequestHandler):
  def get(self):
    logging.info("Scraping comments...")
    q = ChoosiePost.all().filter("posted_to_fb = ", True).filter("created_at > ", datetime.datetime.now() - datetime.timedelta(1))
    posts_found = False
    for p in q.run():
      try:
        posts_found = True
        choosie_post_key = str(p.key())
        logging.info(choosie_post_key)
        comments, votes, error = ScrapeCommentsHandler.scrape_comments_and_votes_from_facebook(choosie_post_key)
        if error:
          logging.warn("Error scraping post from FB. Error = %s", error)
      except Exception, e:
        logging.error("Unexpected error while scraping Facebook comments for post [%s] (%s).", p.key(), p.question)

    if (posts_found is False):
      logging.info("no posts to scrape")
    