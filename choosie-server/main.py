import webapp2

# Handlers
from feed_handler import FeedHandler
from posts_handler import PostsHandler
from image_handler import ImageHandler
from vote_handler import VoteHandler
from homepage_handler import HomepageHandler
from login_handler import LoginHandler
from comments_handler import CommentsHandler
from google.appengine.ext import db
from google.appengine.api import images

app = webapp2.WSGIApplication([('/', HomepageHandler),
                               ('/posts/new', PostsHandler),
                               ('/photo', ImageHandler),
                               ('/votes/new', VoteHandler),
                               ('/feed', FeedHandler),
                               ('/login', LoginHandler),
                               ('/comments/new', CommentsHandler)
                               ],
                              debug=True)
