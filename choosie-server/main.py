import webapp2

# Handlers
from feed_handler import FeedHandler
from posts_handler import PostsHandler
from image_handler import ImageHandler
from image_handler import BlobImageServeHandler
from vote_handler import VoteHandler
from homepage_handler import HomepageHandler
from login_handler import LoginHandler
from comments_handler import CommentsHandler
from post_item_handler import PostItemHandler
from scrape_comments_handler import ScrapeCommentsHandler
from scrape_handler import ScrapeHandler
from notify_handler import NotifyHandler
from register_device_handler import RegisterDeviceHandler
from collect_crash_handler import CollectCrashHandler
from user_handler import UserHandler
from user_edit_handler import UserEditHandler

app = webapp2.WSGIApplication([('/', HomepageHandler),
                               ('/posts/new', PostsHandler),
                               ('/photo', ImageHandler),
                               ('/blobphoto/([^/]+)?', BlobImageServeHandler),
                               ('/votes/new', VoteHandler),
                               ('/feed', FeedHandler),
                               ('/login', LoginHandler),
                               ('/comments/new', CommentsHandler),
                               ('/posts/(.+)', PostItemHandler),
                               ('/scrape', ScrapeCommentsHandler),
                               ('/scrape_fb', ScrapeHandler),
                               ('/send', NotifyHandler),
                               ('/register', RegisterDeviceHandler),
                               ('/scrape_fb', ScrapeHandler),
                               ('/collectcrash', CollectCrashHandler),
                               ('/user/(\d+)', UserHandler),
                               ('/user/edit/(\d+)', UserEditHandler)
                               ],
                              debug=True)
