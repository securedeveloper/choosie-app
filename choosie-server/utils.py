from google.appengine.ext import db
from google.appengine.api import urlfetch


class Utils():
    @staticmethod
    def get_user_from_fb(fb_access_token):
      url = "https://graph.facebook.com/me?access_token=" + fb_access_token 
      result = urlfetch.fetch(url)
      if result.status_code != 200:
        return None
      return result.content;

    @staticmethod
    def items_to_json(items):
      arr = []
      for item in items:
        arr.append(item.to_json())
      return arr

    