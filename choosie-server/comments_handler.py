from cache_controller import CacheController
from model_user import User
from model_choosie_post import ChoosiePost
from model_comment import Comment
import webapp2
import logging

class CommentsHandler(webapp2.RequestHandler):
  def post(self):
    fb_uid = str(self.request.get('fb_uid'))
    text = str(self.request.get('text'))
    # Since the post is taken from the cache, it might not be the most updated version
    # but that's ok, as it is only used as 'parent'
    choosie_post = CacheController.get_model(self.request.get('post_key'))
    comment = Comment(parent=choosie_post,
                      user_fb_id=self.request.get('fb_uid'),
                      text=text)
    comment.put()
    ChoosiePost.add_comment_to_post(choosie_post, comment)
    # Make sure the ChoosiePost's comments are invalidated in cache, so that next time
    # they are asked for, the updated are retreived.
    Comment.invalidate_comments(self.request.get('post_key'))

    self.response.write('Comment added.')
