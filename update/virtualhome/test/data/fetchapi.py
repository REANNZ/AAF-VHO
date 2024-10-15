import urllib2
import urllib
from datetime import datetime
from datetime import timedelta
import json

import hmac
import hashlib
from base64 import b64encode
from hashlib import sha256

api_server = 'http://brainslave.dev.bradleybeddoes.com:8181'
api_endpoint = '/virtualhomeregistry/api/v1/login/show'
token = 'bRomCePVaZMSfrCF'
secret = 'sCzxOzYznkb2YaSW'
requesting_host = '192.168.56.1' # DNS entry unless no reverse

now = datetime.utcnow()
nowHTTP = now.strftime('%a, %d %b %Y %H:%M:%S GMT')

inputs = """get
%s
%s
%s
""" % (requesting_host, api_endpoint, nowHTTP.lower())

signature = b64encode(hmac.new(secret, inputs, sha256).digest())
authorize = 'AAF-HMAC-SHA256 token="%s", signature="%s"' % (token, signature)

req = urllib2.Request('%s%s' % (api_server, api_endpoint))
req.headers = { 'Authorization' : authorize,
                'X-AAF-Date' : nowHTTP }

response = urllib2.urlopen(req)
response_content = response.read()

print response_content

json_dict = { 'book': {'name': 'some name', 'hello': 'world', 'values':['xyz', 'abc'] }}

# convert json_dict to JSON
json_data = json.dumps(json_dict)

print json_data

h = hashlib.new('sha256')
h.update(json_data)

inputs2 = """post
%s
%s
%s
%s
%s
"""  % (requesting_host, api_endpoint, nowHTTP.lower(), 'application/json', h.hexdigest())

print h.hexdigest()

print inputs2
signature = b64encode(hmac.new(secret, inputs2, sha256).digest())
authorize = 'AAF-HMAC-SHA256 token="%s", signature="%s"' % (token, signature)


headers = { 'Authorization' : authorize,
            'X-AAF-Date' : nowHTTP,
            'Content-Type': 'application/json'}

req2 = urllib2.Request('%s%s' % (api_server, api_endpoint), json_data, headers)

response = urllib2.urlopen(req2)
response_content = response.read()

print response_content
