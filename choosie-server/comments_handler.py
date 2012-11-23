from module_user import User
from module_choosie_post import ChoosiePost
from module_comment import Comment
from google.appengine.ext import db
import webapp2
import logging

class CommentsHandler(webapp2.RequestHandler):
  def post(self):
    fb_uid = str(self.request.get('fb_uid'))
    text = str(self.request.get('text'))
    choosie_post = db.get(self.request.get('post_key'))
    
    user = User.get_user_by_fb_uid(fb_uid)
    comment = Comment(parent=choosie_post,
                      user=user,
                      text=text)
    
    comment.put()
    self.redirect('/')
    