import json
import logging
import webapp2

from google.appengine.ext import db

from cache_controller import CacheController
from model_choosie_post import ChoosiePost
from model_vote import Vote
from model_user import User

class VoteHandler(webapp2.RequestHandler):
  def get(self):
    vote_for = int(self.request.get('which_photo'))
    fb_uid = str(self.request.get('fb_uid'))
    user = CacheController.get_user_by_fb_id(fb_uid)

    if not user:
      self.write_error("Can't add vote: User with fb_uid %s is not logged in." % fb_uid)
      return

    # Since the post is taken from the cache, it might not be the most updated version
    # but that's ok, as it is only used as 'parent'
    choosie_post = CacheController.get_model(self.request.get('post_key'))

    vote = Vote(parent=choosie_post,
                user_fb_id=fb_uid,
                vote_for=int(vote_for))

    prev_vote = vote.prev_vote_for_user_for_post()
    #if the user voted to the same post but for different item, updating the vote
    if (prev_vote is not None and prev_vote.vote_for != vote_for):
      prev_vote.vote_for = vote_for
      prev_vote.put()
      choosie_post.add_vote_to_post(vote, False)
      self.response.write('Vote changed to photo number %d.' % vote_for)
    #if voted to same pic - error
    elif(prev_vote != None):
       self.write_error("already voted!")
    else:
      vote.put()
      ChoosiePost.add_vote_to_post(choosie_post, vote, True)
      # Make sure the ChoosiePost is invalidated in cache, so that next time it is asked
      # for, the updated one is retreived.
      Vote.invalidate_votes(self.request.get('post_key'))
      self.response.write('A new vote issued for photo number %d.' % vote_for)

  def post(self):
    # Delegates the POST handling to the GET handling.
    # TODO: Fix in the client to actually use POST and not GET, and get rid of the GET handler.
    get(self)

  def write_error(self, err):
    self.response.out.write(json.dumps({"success" : "no", "error": err}))
