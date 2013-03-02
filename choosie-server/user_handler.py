import json
import logging
import webapp2

from cache_controller import CacheController
from model_choosie_post import ChoosiePost
from model_user import User
from model_vote import Vote

class UserHandler(webapp2.RequestHandler):
  def get(self, fb_uid):
    # This post is got from the cache. That's ok, because all of the actual
    # dynamic data (votes, comments) is retrieved from the datastore during
    # choosie_post.to_json().
    user = CacheController.get_user_by_fb_id(fb_uid)

    self.response.out.write(json.dumps(self.profile_details_json(user)))


  def profile_details_json(self, user):
    return {
            "fb_uid": user.fb_uid,
            "nick": user.nick,
            "info": user.info,
            "created_at": user.created_at.strftime("%Y-%m-%dT%H:%M"),
            "num_posts": self.get_num_posts(user),
            "num_votes": self.get_num_votes(user)
            }


  def get_num_posts(self, user):
    if user.num_posts is None:
      q = ChoosiePost.all()
      q.filter("user_fb_id =", user.fb_uid)
      user.num_posts = q.count()
      user.put()
    return user.num_posts

  def get_num_votes(self,user):
    if user.num_votes is None:
      q = Vote.all()
      q.filter("user_fb_id =", user.fb_uid)
      user.num_votes = q.count()
      user.put()

    return user.num_votes