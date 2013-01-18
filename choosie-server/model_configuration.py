from google.appengine.ext import db

class ChoosieConfiguration(db.Model):
  post_to_fb = db.BooleanProperty(default=True)
  send_to_rest = db.BooleanProperty(default=True)

  @staticmethod
  def post_to_fb_setting():
    q = ChoosieConfiguration.all() # only one configuration object will exist
    for configuration in q.run(limit=1):
      return configuration.post_to_fb
    #create the entry for future use
    configuration = ChoosieConfiguration(post_to_fb=True)
    configuration.put()
    return True #if not stated in configuration, default is True

  @staticmethod
  def get_send_to_rest_setting():
    q = ChoosieConfiguration.all() # only one configuration object will exist
    for configuration in q.run(limit=1):
      if configuration.send_to_rest is not None:
        return configuration.send_to_rest
      else:
        configuration.send_to_rest = True
        configuration.put()
        return True

    configuration = ChoosieConfiguration(post_to_fb= True, send_to_rest = True)
    configuration.put()
    return True

  @staticmethod
  def set_sent_to_rest_setting(value):
    q = ChoosieConfiguration.all() # only one configuration object will exist
    for configuration in q.run(limit=1):
      configuration.send_to_rest = value
      configuration.put()

