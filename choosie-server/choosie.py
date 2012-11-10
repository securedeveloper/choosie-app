import logging
import jinja2
import os
import webapp2

from google.appengine.ext import db
from google.appengine.api import images


jinja_environment = jinja2.Environment(
    loader = jinja2.FileSystemLoader(os.path.dirname(__file__)))


class ChoosiePost(db.Model):
  photo1 = db.BlobProperty()
  photo2 = db.BlobProperty()
  votes1 = db.IntegerProperty()
  votes2 = db.IntegerProperty()
  question = db.StringProperty()
  date = db.DateTimeProperty(auto_now_add = True)


class MainPage(webapp2.RequestHandler):
  def get(self):
    self.response.headers['Content-Type'] = 'text/html'

    choosie_posts = db.GqlQuery('SELECT * '
                                'FROM ChoosiePost '
                                'ORDER BY date DESC LIMIT 10')
    template_values = {
      'choosie_posts': choosie_posts
    }
    template = jinja_environment.get_template('index.html')
    self.response.write(template.render(template_values))


class UploadHandler(webapp2.RequestHandler):
  def shrinkImage(self, img):
    img = images.Image(img)
    img.resize(width=200, height=200)
    img.im_feeling_lucky()
    return img.execute_transforms(output_encoding=images.PNG)
	
  def post(self):
    choosie_post = ChoosiePost()
    choosie_post.question = self.request.get('question')
    choosie_post.votes1 = 0
    choosie_post.votes2 = 0
    if self.request.get('photo1'):
	  logging.info(len(self.request.get('photo1')))
	  choosie_post.photo1 = db.Blob(self.shrinkImage(self.request.get('photo1')))
    if self.request.get('photo2'):
      choosie_post.photo2 = db.Blob(self.shrinkImage(self.request.get('photo2')))
    choosie_post.put()
    self.redirect('/')


class ImageHandler1(webapp2.RequestHandler):
  def get(self):
    choosie_post = db.get(self.request.get('image_id'))
    if choosie_post.photo1:
      self.response.headers['Content-Type'] = 'image/png'
      self.response.out.write(choosie_post.photo1)
    else:
	  self.response.out.write('Image not found. :(')

class ImageHandler2(webapp2.RequestHandler):
  def get(self):
    choosie_post = db.get(self.request.get('image_id'))
    if choosie_post.photo2:
      self.response.headers['Content-Type'] = 'image/png'
      self.response.out.write(choosie_post.photo2)
    else:
	  self.response.out.write('Image not found. :(')

class VotesHandler1(webapp2.RequestHandler):
  def get(self):
    choosie_post = db.get(self.request.get('vote_id'))
    if not choosie_post.votes1:
      choosie_post.votes1 = 0
    choosie_post.votes1 += 1
    choosie_post.put()
    self.redirect('/')

class VotesHandler2(webapp2.RequestHandler):
  def get(self):
    choosie_post = db.get(self.request.get('vote_id'))
    if not choosie_post.votes2:
      choosie_post.votes2 = 0
    choosie_post.votes2 += 1
    choosie_post.put()
    self.redirect('/')

app = webapp2.WSGIApplication([
             ('/', MainPage),
             ('/upload', UploadHandler),
 		     ('/photo1', ImageHandler1),
 		     ('/photo2', ImageHandler2),
 		     ('/vote1', VotesHandler1),
 		     ('/vote2', VotesHandler2)
         ],
         debug=True)