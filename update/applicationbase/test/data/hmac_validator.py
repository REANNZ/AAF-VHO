import hmac
from base64 import b64encode
from hashlib import sha256

# Lets us validate HMAC values using Sha256 for building
# test cases or checking remote clients using a different tech

# python hmac_validator.py

secret_key = 'aqlxLASR6Bwz+Y03'

to_sign = """get
192.168.56.1
/application/api/v1/object
fri, 08 mar 2013 00:18:15 gmt"""

print b64encode(hmac.new(secret_key, to_sign, sha256).digest())
