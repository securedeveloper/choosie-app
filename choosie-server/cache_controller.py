import logging

from google.appengine.ext import db
from google.appengine.api import memcache

from module_vote import Vote
from module_comment import Comment

VOTES_NAMESPACE = 'VOTES'
COMMENTS_NAMESPACE = 'COMMENTS'

class CacheController(object):
  @staticmethod
  def get_model(key):
    # If it's in the memcache, just returns it. Otherwise, retrieves from
    # the datastore and saves for later.
    value = memcache.get(key)
    if value is not None:
      logging.info('Saved a data store call for %s.' % key)
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
  def get_votes_for_post(post_key):
    votes = memcache.get(post_key, namespace=VOTES_NAMESPACE)
    if votes is not None:
      logging.info('Saved a data store call for votes.')
      return votes
    else:
      logging.info('Retreiving votes for [%s] from data store.' % post_key)
      post = CacheController.get_model(post_key)
      votes = Vote.all().ancestor(post)
      memcache.set(post_key, votes, namespace=VOTES_NAMESPACE)
      return votes

  @staticmethod
  def invalidate_votes(post_key):
    memcache.delete(post_key, namespace=VOTES_NAMESPACE)

  @staticmethod
  def get_comments_for_post(post_key):
    comments = memcache.get(post_key, namespace=COMMENTS_NAMESPACE)
    if comments is not None:
      logging.info('Saved a data store call for comments.')
      return comments
    else:
      logging.info('Retreiving comments for [%s] from data store.' % post_key)
      post = CacheController.get_model(post_key)
      comments = Comment.all().ancestor(post)
      memcache.set(post_key, comments, namespace=COMMENTS_NAMESPACE)
      return comments

  @staticmethod
  def invalidate_comments(post_key):
    memcache.delete(post_key, namespace=COMMENTS_NAMESPACE)
    
