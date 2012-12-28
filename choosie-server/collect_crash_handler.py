from datetime import datetime
from google.appengine.api import mail
from google.appengine.ext import db

import cgi
import logging
import webapp2


class CrashReport(db.Model):
  created_at = db.DateTimeProperty(auto_now_add = True)
  stacktrace = db.TextProperty(required = True)
  version = db.StringProperty(required = True)
  package = db.StringProperty(required = True)


class CollectCrashHandler(webapp2.RequestHandler):
  def post(self):
    stacktrace = str(self.request.get('stacktrace'))
    version = str(self.request.get('package_version'))
    package = str(self.request.get('package_name'))

    if not stacktrace or not version or not package:
      self.response.write("This script is used to collect field test crash stacktraces. "
                          "No personal information is transmitted, collected or stored.")
      return

    report = CrashReport(stacktrace=stacktrace, version=version, package=package)
    report.put()

    self.response.write('Crash report is saved.')


  def get(self):
    reports = CrashReport.all().order("-created_at")
    self.response.write("""
    <html>
       <style>
         body {
           font-family: sans-serif
         }
       </style>
       <head>
         <title>Crash reports</title>
       </head>
       <body>
         <h1>Crash reports</h1>
         <p>Current time: %s</p>
         <hr>
    """ % datetime.now())
    for report in reports:
      self.response.write("""
         <p>Crash occurred at: %s</p>
         <p>App name: %s (version: %s)</p>
         <p>Stacktrace:
           <pre>%s</pre>
         </p>
         <hr>"""
         % (report.created_at,
            cgi.escape(report.package),
            cgi.escape(report.version),
            cgi.escape(report.stacktrace)))
    self.response.write("""
      </body>
    </html>""")
    
