import webapp2
from module_choosie_post import ChoosiePost
from google.appengine.ext import db
from module_vote import Vote
from module_user import User
import json
import logging

class VoteHandler(webapp2.RequestHandler):
  def get(self):
    vote_for = int(self.request.get('which_photo'))
    fb_uid = str(self.request.get('fb_uid'))
    choosie_post = db.get(self.request.get('post_key'))
    
    user = User.get_user_by_fb_uid(fb_uid)
    vote = Vote(parent=choosie_post,
               user=user,
               vote_for=int(vote_for))
    
    if vote.is_exist():
      self.write_error("already voted!")
    else:
      vote.put()
      vote.set_votes()
      self.redirect('/')
    
  def post(self):
    get(self)

  def write_error(self, err):
    self.response.out.write(json.dumps({"success" : "no", "error": err}))

 