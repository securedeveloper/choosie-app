import json
import logging
import webapp2

from cache_controller import CacheController
from datetime import datetime
from model_choosie_post import ChoosiePost
from model_user import User
from utils import Utils

class FeedHandler(webapp2.RequestHandler):
    def get(self):
        choosie_posts, cursor = FeedHandler.get_feed_and_cursor(self.request.get('cursor'),
                                                                self.request.get('limit'),
                                                                self.request.get('timestamp'))
        choosie_posts_json = Utils.items_to_json(choosie_posts)
        self.response.out.write(json.dumps({'feed': choosie_posts_json,
                                            'cursor': cursor,
                                            'timestamp': datetime.utcnow().isoformat()}))
        
    @staticmethod
    def get_feed_and_cursor(cursor, limit = 10, timestamp = None):
        if not limit:
            limit = 10
        limit = int(limit)
        logging.info('Retrieving %d posts from db' % limit)
        posts = ChoosiePost.all()
        if cursor:
            posts.with_cursor(cursor)
        if timestamp:
            created_after = Utils.parse_iso_format_datetime(timestamp)
            posts.filter('created_at >', created_after)
        posts.order("-created_at")
        posts_result = []
        for post in posts.run(limit=limit):
            posts_result.append(post)
        new_cursor = posts.cursor()
        CacheController.set_multi_models(posts_result)
        return (posts_result, new_cursor)
        
