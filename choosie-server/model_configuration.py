from google.appengine.ext import db

class ChoosieConfiguration(db.Model):
  post_to_fb = db.BooleanProperty(default=True)
  
  @staticmethod
  def post_to_fb_setting():
    q = ChoosieConfiguration.all() # only one configuration object will exist
    for configuration in q.run(limit=1):
      return configuration.post_to_fb
    #create the entry for future use
    configuration = ChoosieConfiguration(post_to_fb=True)
    configuration.put()
    return True #if not stated in configuration, default is True

