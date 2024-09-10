# AAF API Security
This document provides an overview of how AAF Application Base secures Rest API endpoints that not public in nature.

AAF requires that you authenticate every request by signing it. To sign a request, you calculate a digital signature using a cryptographic hash function. A cryptographic hash is a one-way function that returns a unique hash value based on the input. The input to the hash function includes the text of your request and your secret which is issued to you by an AAF administrator. Secrets must never be publicly shared and ***never*** form part of the web request itself. You're responsible for securely storing and using your secret value. The hash function returns a hash value that you include in the request as your signature.

After receiving your request, AAF recalculates the signature using the same hash function and input that you used to sign the request. If the resulting signature matches the signature in the request, AAF processes the request. Otherwise, the request is rejected.

For additional security we require all requests to be made using Secure Sockets Layer (SSL) by using HTTPS. SSL encrypts the transmission, protecting your request or the response from being viewed in transit.

## Accounts
Once an API public token and secret has been issued the AAF will do additional authorization to specific backend content for authenticated requests using our standard Apache Shiro powered Subject/Role/Permissions access model.

## Dates
The date that you use as part of your request and in generating your signature must match the date of your request. You can include the date as part of your request as either a ``Date`` header or an ``X-AAF-Date`` header. 

The Date value supplied via the above headers must be in the following format, note especially the GMT timezone:

    Sun, 06 Nov 1994 08:49:37 GMT  ; RFC 822, updated by RFC 1123

Finally ensure your server is time synchronized. Requests whose timestamp is out by greater than 1 minute will be rejected.

## Signature Generation

### Required
For all required fields ensure content is converted to lowercase and leading/trailing whitespace removed before generating signatures. NB: This should only be applied during signature generation. Requests themselves should use normal, RFC compliant formatting.

* **Method**: The HTTP method used to call the API endpoint. 
* **RemoteHost**: The remote host making the request. IP if no DNS entry for requester.
* **Path**:  This is the URI-encoded version of the absolute path component of the URI—everything from the HTTP host header to the question mark character ('?') that begins the query string parameters. If the absolute path is empty, use a forward slash (/).
* **Date**: Header defining the date and time that the message was sent. Formatted as EEE, d MMM yyyy HH:mm:ss GMT.

### Additional for POST/PUT requests
* **ContentType**: The MIME type of the body of the request. Lowercase and leading/trailing whitespace removed.
* **Payload**: Body content represented by SHA254 hash with Hex encoding.

### Signature Generation Pseudocode
#### GET/DELETE REQUEST 
    INPUT = 
      method + "\n" +
      remoteHost + "\n" +
      path + "\n" +
      date + "\n"
    
#### POST/PUT REQUEST 
    INPUT = 
      method + "\n" +
      remoteHost + "\n" +
      path + "\n" +
      date + "\n" +
      contentType + "\n" +
      HexEncode(Sha256Hash(Payload)) + "\n"

### Generation
Based on the data assembled for your input create a Message Authentication Code value using the standard HmacSHA256 algorithm. The resultant byte array should be base64 encoded ready to supply to the Authorization header below.
      
## Authorization

To send your public token and request signature we use the Authorization header as defined in RFC2617.

When sending your request format your Authorization header as follows:

Authorization : ``AAF-HMAC-SHA256 token="", signature=""``

Note our identifying auth scheme of AAF-HMAC-SHA256. Token should be given the value of your public API token and the signature value is populated with the result you calculated for the current request as described above.

An example of a fully populated Authorization header:

Authorization : ``AAF-HMAC-SHA256 token="bRomCePVaZMSfrCF", signature="SnZfnPbvq+hXynTy+EMb1H6wY6sHZCedtvXELoSgUJk="``

## Errors
Errors will be responded to with the appropriate HTTP 4xx or 5xx code. They will also contain json data in the format:

``{"error":" ","internalerror":" "}``

Where error will provide you a generic idea of what has gone wrong and internalerror a more specific reasoning as to why the request was ultimately rejected. In some cases internalerror may provide details that uniquely identify part of the request which might not be suitable for consumption by non administrators (i.e in a general web view).

## Example Request and Signature Generation flow
## GET request from 192.168.56.1 (no DNS entry)

    GET /application/api/v1/object HTTP/1.1
    Host: server.aaf.edu.au
    Authorize: AAF-HMAC-SHA256 token="bRomCePVaZMSfrCF", signature="IQLnb/3v4V/gA4HjEV6lJPZvCl2ijCe7MsgwUsd/5W0="
    Date: Fri, 08 Mar 2013 00:18:15 GMT


String sent to signature generation (note trailing blank line is required):

    get
    192.168.56.1
    /application/api/v1/object
    fri, 08 mar 2013 00:18:15 gmt

When combined with the secret key of ``aqlxLASR6Bwz+Y03`` which is associated with the supplied token we compute a signature of ``IQLnb/3v4V/gA4HjEV6lJPZvCl2ijCe7MsgwUsd/5W0=`` which matches the inbound request allowing it to proceed.

## Example python client
This is a very basic client in Python that connects to an AAF API and prints response data to screen. Both the public token and secret are provided by AAF staff. The secret must be closely guarded at all times.

    import urllib2
    from datetime import datetime
    from datetime import timedelta

    import hmac
    from base64 import b64encode
    from hashlib import sha256

    api_server = 'https://server.aaf.edu.au'
    api_endpoint = '/application/api/v1/object'
    token = 'bRomCePVaZMSfrCF'
    secret = 'aqlxLASR6Bwz+Y03'
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
