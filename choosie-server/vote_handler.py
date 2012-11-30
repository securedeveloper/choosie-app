import json
import logging
import webapp2

from google.appengine.ext import db

from cache_controller import CacheController
from module_choosie_post import ChoosiePost
from module_vote import Vote
from module_user import User

class VoteHandler(webapp2.RequestHandler):
  def get(self):
    vote_for = int(self.request.get('which_photo'))
    fb_uid = str(self.request.get('fb_uid'))
    # Since the post is taken from the cache, it might not be the most updated version
    # but that's ok, as it is only used as 'parent'
    choosie_post = CacheController.get_model(self.request.get('post_key'))
    
    user = User.get_user_by_fb_uid(fb_uid)
    vote = Vote(parent=choosie_post,
                user=user,
                vote_for=int(vote_for))
    
    prev_vote = vote.prev_vote_for_user_for_post()
    #if the user voted to the same post but for different item, updating the vote
    if (prev_vote != None and prev_vote.vote_for != vote_for):
      prev_vote.vote_for = vote_for
      prev_vote.put()
      self.redirect('/')
    #if voted to same pic - error
    elif(prev_vote != None):
       self.write_error("already voted!")
    else:
      vote.put()
      # Make sure the ChoosiePost is invalidated in cache, so that next time it is asked
      # for, the updated one is retreived.
      CacheController.invalidate(self.request.get('post_key'))
      self.redirect('/')
    
  def post(self):
    get(self)

  def write_error(self, err):
    self.response.out.write(json.dumps({"success" : "no", "error": err}))
