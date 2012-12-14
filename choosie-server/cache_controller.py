import logging

from google.appengine.ext import db
from google.appengine.api import memcache

USER_FB_ID_NAMESPACE = 'USER_FB_ID'

class CacheController(object):
  @staticmethod
  def get_model(key):
    # If it's in the memcache, just returns it. Otherwise, retrieves from
    # the datastore and saves for later.
    value = memcache.get(key)
    if value is not None:
      logging.info('Skipped a data store call for %s.' % key)
      return value
    else:
      logging.info('Retreiving [%s] from data store.' % key)
      value = db.get(key)
      CacheController.set_model(value)
      return value

  @staticmethod
  def set_model(model):
    # Puts a single post in the memcache.
    memcache.set(str(model.key()), model)

  @staticmethod
  def set_multi_models(models):
    # This uses dict comprehension to create a dictionary that looks like that:
    # {'model1.key()': [model1],
    #  'model2.key()': [model2], ...}
    #
    # memcache.set_multi() receives this mapping and puts multiple values at the
    # same time.
    mapping = {str(model.key()): model for model in models}
    memcache.set_multi(mapping)

  @staticmethod
  def invalidate(key):
    memcache.delete(key)

  @staticmethod
  def get_user_by_fb_id(user_fb_id):
    user = memcache.get(user_fb_id, namespace=USER_FB_ID_NAMESPACE)
    if user is not None:
      logging.info('Skipped a data store call for user.')
      return user
    else:
      logging.info('Retreiving user with fb_uid [%s] from data store.' % user_fb_id)
      user = db.GqlQuery("SELECT * from User where fb_uid = :1", user_fb_id).get()
      memcache.set(user_fb_id, user, namespace=USER_FB_ID_NAMESPACE)
      return user
