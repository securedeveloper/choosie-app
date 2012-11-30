import logging

from google.appengine.ext import db
from google.appengine.api import memcache

class CacheController(object):
  @staticmethod
  def get_model(key):
    # If it's in the memcache, just returns it. Otherwise, retrieves from
    # the datastore and saves for later.
    value = memcache.get(key)
    if value is not None:
      return value
    else:
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
