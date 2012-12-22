from __future__ import with_statement

from google.appengine.api import files

from google.appengine.ext import db
from google.appengine.api import urlfetch
from google.appengine.api import images
from google.appengine.ext import blobstore
from datetime import datetime
from StringIO import *
import urllib2
import cStringIO
import logging
import os

class Utils():
    @staticmethod
    def get_user_from_fb(fb_access_token):
      url = "https://graph.facebook.com/me?access_token=" + fb_access_token
      logging.info("url= " + url)
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
      img1_blob_reader = blobstore.BlobReader(choosie_post.photo1_blob_key)
      img2_blob_reader = blobstore.BlobReader(choosie_post.photo2_blob_key)
      img1 = images.Image(image_data=img1_blob_reader.read())
      img2 = images.Image(image_data=img2_blob_reader.read())
      return Utils.compose_two_images(img1, img2)

    @staticmethod
    def load_image(file_name):
      file_path = os.path.join(os.path.split(__file__)[0], file_name)
      icon = open(file_path).read()
      img = images.Image(image_data=icon)
      return img

    @staticmethod
    def compose_two_images(img1, img2):
      img_icon_1 = Utils.load_image('1.png')
      img_icon_2 = Utils.load_image('2.png')
      corner_tr = Utils.load_image('corner-tr.png')
      corner_bl = Utils.load_image('corner-bl.png')
      corner_br = Utils.load_image('corner-br.png')
      margin = 6
      # img_icon_1          img_icon_2
      #       |    corner_tr   |      
      #       | margin  |      |   margin
      #       / ------- \      / -------- \  <--  corner_tr
      #       |         |      |          |
      #       |   img1  |      |   img2   |
      # margin|         |margin|          |margin
      #       |         |      |          |
      #       \ ------- /      \ -------- /
      #       ^ margin  ^      ^  margin  ^
      # corner_bl corner_br  corner_bl corner_br
      composite = images.composite(
          [(img1, margin, margin, 1.0, images.TOP_LEFT),
           (img2, img1.width + 2*margin, margin, 1.0, images.TOP_LEFT),
           (corner_tr, margin + img1.width - corner_tr.width, margin, 1.0, images.TOP_LEFT),
           (corner_tr, 2*margin + img1.width + img2.width - corner_tr.width, margin, 1.0, images.TOP_LEFT),
           (corner_br, margin + img1.width - corner_br.width, margin + img1.height - corner_br.height, 1.0, images.TOP_LEFT),
           (corner_br, 2*margin + img1.width + img2.width - corner_br.width, margin + img1.height - corner_br.height, 1.0, images.TOP_LEFT),
           (corner_bl, margin, margin + img1.height - corner_br.height, 1.0, images.TOP_LEFT),
           (corner_bl, 2*margin + img1.width, margin + img1.height - corner_br.height, 1.0, images.TOP_LEFT),
           (img_icon_1, margin, margin, 1.0, images.TOP_LEFT),
           (img_icon_2, img1.width + 2*margin, margin, 1.0, images.TOP_LEFT)],
          img1.width + img2.width + 3*margin,
          img1.height + 2*margin)
      logging.info('created image')
      return composite

    @staticmethod
    def get_json_comments_from_fb_post(fb_post_id, access_token):
      url = "https://graph.facebook.com/" + fb_post_id + "/comments?access_token=" + access_token
      logging.info('URL to fetch: ' + url)
      result = urlfetch.fetch(url)
      logging.info('result: ' + str(result.content))
      if result.status_code != 200:
        return None, result.content
      return result.content, None

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
  