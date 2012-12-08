import webapp2

from cache_controller import CacheController
from module_choosie_post import ChoosiePost

class ImageHandler(webapp2.RequestHandler):
  def get(self):
    choosie_post = CacheController.get_model(self.request.get('post_key'))
    which_photo = int(self.request.get('which_photo'))
    result = {
    0 : choosie_post.photo,
    1 : choosie_post.photo1,
    2 : choosie_post.photo2
    }[which_photo]
      
    self.response.headers['Content-Type'] = 'image/png'
    self.response.headers['Cache-Control'] = 'max-age=290304000, private'
    self.response.out.write(result)