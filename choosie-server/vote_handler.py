import webapp2
from modules import ChoosiePost
from google.appengine.ext import db

class VoteHandler(webapp2.RequestHandler):
  def get(self):
    choosie_post = db.get(self.request.get('post_key'))
    vote_for = int(self.request.get('which_photo'))
    save_vote = True
    if vote_for == 1:
      choosie_post.votes1 += 1
    elif vote_for == 2:
      choosie_post.votes2 += 1
    else:
      save_vote = False
    if save_vote:
      choosie_post.put()
    self.redirect('/')
  def post(self):
    get(self)