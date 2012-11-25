import logging
import webapp2

from cache_controller import CacheController

class PostItemHandler(webapp2.RequestHandler):
  def get(self, key):
    logging.info(key)
    # This post is got from the cache. That's ok, because all of the actual
    # dynamic data (votes, comments) is retrieved from the datastore during
    # choosie_post.to_json().
    choosie_post = CacheController.get_model(key)
    self.response.out.write(choosie_post.to_json())