import urllib
import webapp2

from google.appengine.ext import blobstore
from google.appengine.ext.webapp import blobstore_handlers

from cache_controller import CacheController
from model_choosie_post import ChoosiePost


class ImageHandler(webapp2.RequestHandler):
  def get(self):
    choosie_post = CacheController.get_model(self.request.get('post_key'))
    which_photo = int(self.request.get('which_photo'))
    result = {
      0: choosie_post.photo,
      1: choosie_post.photo1,
      2: choosie_post.photo2
    }[which_photo]

    self.response.headers['Content-Type'] = 'image/png'
    self.response.headers['Cache-Control'] = 'max-age=290304000, private'
    self.response.out.write(result)


class BlobImageServeHandler(blobstore_handlers.BlobstoreDownloadHandler):
  def get(self, resource):
    resource = str(urllib.unquote(resource))
    blob_info = blobstore.BlobInfo.get(resource)
    self.response.headers['Cache-Control'] = 'max-age=290304000, private'
    self.send_blob(blob_info, content_type='image/png')
