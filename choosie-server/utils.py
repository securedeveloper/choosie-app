from google.appengine.ext import db
from google.appengine.api import urlfetch
from google.appengine.api import images
from datetime import datetime
import urllib2
import cStringIO
import logging
import os

class Utils():
    @staticmethod
    def get_user_from_fb(fb_access_token):
      url = "https://graph.facebook.com/me?access_token=" + fb_access_token 
      result = urlfetch.fetch(url)
      if result.status_code != 200:
        return None
      return result.content
      
    @staticmethod
    def items_to_json(items):
      arr = []
      for item in items:
        arr.append(item.to_json())
      return arr

    @staticmethod
    def get_access_token_from_request(request):
      return datetime.fromtimestamp(long(request.get('fb_access_token_expdate')) / 1e3)

    @staticmethod
    def create_post_image(choosie_post):
      img1 = images.Image(choosie_post.photo1)
      img2 = images.Image(choosie_post.photo2)
      icon_1_path = os.path.join(os.path.split(__file__)[0], '1.png')
      icon_2_path = os.path.join(os.path.split(__file__)[0], '2.png')
      icon1 = open(icon_1_path).read()
      icon2 = open(icon_2_path).read()
      img_icon_1 = images.Image(image_data=icon1)
      img_icon_2 = images.Image(image_data=icon2)
      composite = images.composite([(img1, 0, 0, 1.0, images.TOP_LEFT),
      (img2, img1.width, 0, 1.0, images.TOP_LEFT), (img_icon_1, 0, 0, 0.3, images.TOP_LEFT),
      (img_icon_2, img1.width, 0, 0.3, images.TOP_LEFT)], img1.width + img2.width, img1.height)
      choosie_post.photo = db.Blob(composite)
      choosie_post.put()
      logging.info('___saved')

    @staticmethod
    def get_json_comments_from_fb_post(fb_post_id, access_token):
      url = "https://graph.facebook.com/" + fb_post_id + "/comments?access_token=" + access_token
      logging.info('URL to fetch: ' + url)
      result = urlfetch.fetch(url)
      logging.info('result: ' + str(result.content))
      if result.status_code != 200:
        return None
      return result.content

    @staticmethod
    def parse_iso_format_datetime(datetime_str):
        #"2008-09-03T20:56:35.450686Z
        return datetime.strptime(datetime_str, "%Y-%m-%dT%H:%M:%S.%f")

    @staticmethod
    def parse_utf_format_datetime(datetime_str):
        #"2008-09-03T20:56:35+450686Z
        return datetime.strptime(datetime_str, "%Y-%m-%dT%H:%M:%S+%f")
    @staticmethod
    def get_avatar(fb_id_or_username):
      return 'http://graph.facebook.com/%s/picture' % fb_id_or_username
    
    @staticmethod
    def write_file_to_blobstore(data):
      # Warning: They say this feature is experimental. Careful.
      # See https://developers.google.com/appengine/docs/python/blobstore/overview#Writing_Files_to_the_Blobstore
      
      # Create the file
      file_name = files.blobstore.create(mime_type='application/octet-stream')
      # Open the file and write to it
      with files.open(file_name, 'a') as f:
        f.write(data)
      # Finalize the file. Do this before attempting to read it.
      files.finalize(file_name)
      # Get the file's blob key
      blob_key = files.blobstore.get_blob_key(file_name)
      return blob_key
  