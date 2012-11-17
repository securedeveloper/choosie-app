import webapp2

# Handlers
from feed_handler import FeedHandler
from upload_handler import UploadHandler
from image_handler import ImageHandler
from vote_handler import VoteHandler
from homepage_handler import HomepageHandler
from login_handler import LoginHandler


from google.appengine.ext import db
from google.appengine.api import images

app = webapp2.WSGIApplication([('/', HomepageHandler),
                               ('/upload', UploadHandler),
                               ('/photo', ImageHandler),
                               ('/vote', VoteHandler),
                               ('/feed', FeedHandler),
                               ('/login', LoginHandler)
                               ],
                              debug=True)
