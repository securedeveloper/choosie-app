from cache_controller import CacheController
from model_user import User
from model_comment import Comment
from model_vote import Vote
from google.appengine.ext import db
import webapp2
from datetime import datetime
from utils import Utils
import logging
import json
from google.appengine.api import urlfetch

class NotifyHandler(webapp2.RequestHandler):
  notify_type = {
      "new_post":1,
      "comment":2,
      "vote": 3
  }
  def get(self):
    message = "i love soja milk"
    # users = db.GqlQuery("SELECT * FROM User WHERE device_id > :1",None)
    users = User.all()
    devices = []
    user_ids = []
    # devices = ["APA91bHlkage-d5iZhdRAZtKS1gUy8q1LcEyGXmmhBzaGXxMTp6S5QIxQGkp8HxiTExI6cim4KngJXzamvbjzOKLrRUPKQpQqKaevHAEertE_PKKT8UjkLabSDTw8ljXYVt59k_xqIWK"]
    for user in users.run():
      # devices.append(user.device_id)
      if user.device_id is not None:
        devices.append(user.device_id)
        user_ids.append(user.fb_uid)


    result = NotifyHandler.send_notifiction(NotifyHandler.notify_type["new_post"],
                                            "ron",
                                            "1",
                                            devices,
                                            user_ids)


    self.response.write("result is " + result)

  @staticmethod
  def send_notifiction(notification_type, text, post_key, device_ids_array, user_ids_array):
    logging.info("Sending notification to devices: %s", ", ".join(device_ids_array))
    logging.info("Sending notification to users: %s", ", ".join(user_ids_array))
    data = {
          "data": {"type": notification_type,
                   "text": text,
                   "post_key": post_key},
          "registration_ids": device_ids_array
    }

    result = urlfetch.fetch(url = "https://android.googleapis.com/gcm/send",
                             payload = json.dumps(data),
                             method = urlfetch.POST,
                             headers = {'Content-Type':'application/json','Authorization':'key=AIzaSyDAkpOHWW6km87yen5W9C8y1MaGn7-hQKw'})
    result_is_json = False

    try:
      result_data = json.loads(result.content)
      result_is_json = True
    except Exception, e:
      logging.info("It is not json. Eshhhh.")

    if not result_is_json:
      return result.content

    logging.info(result_data)

    if result_data.get("canonical_ids") is 0:
      for index, result in enumerate(result_data["results"]):
        if result.get("registration_id") is not None:
          logging.info("Need to change device_id for user [%s]. New ID: %s", user_ids_array[index], result)
          user = CacheController.get_user_by_fb_id(user_ids_array[index])
          user.device_id = result.get("registration_id")
          user.put()
          CacheController.invalidate_user_fb_id(user_ids_array[index])
    

    return str(result)


