package aaf.vhr.idp;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.net.URIBuilder;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VhrBasicAuthValidator {
  private final String DATE_FORMAT = "EEE, d MMM yyyy HH:mm:ss z";
  private final String AUTHORIZE_HEADER = "AAF-HMAC-SHA256 token=\"%s\", signature=\"%s\"";

  Logger log = LoggerFactory.getLogger("aaf.vhr.idp.VhrBasicAuthValidator");

  private String apiServer;
  private String apiEndpoint;
  private String apiToken;
  private String apiSecret;
  private String requestingHost;

  public VhrBasicAuthValidator(String apiServer, String apiEndpoint, String apiToken, String apiSecret, String requestingHost) {
    this.apiServer = apiServer;
    this.apiEndpoint = apiEndpoint;
    this.apiToken = apiToken;
    this.apiSecret = apiSecret;
    this.requestingHost = requestingHost;
  }

  public String authenticate(String login, String password) {
    CloseableHttpClient httpClient = null;
    HttpPost request = null;
    ClassicHttpResponse response = null;

    try {
      log.info("Contacting VHR API to validate credential for: ", login); 
      
      URIBuilder builder = new URIBuilder(new URI(this.apiServer));
      builder.setPath(this.apiEndpoint);
      request = new HttpPost(builder.build());

      Date requestDate = new Date();
      SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
      sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
      String requestDateHeader = sdf.format(requestDate);
      request.setHeader("Date", requestDateHeader);

      List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
      nameValuePairs.add(new BasicNameValuePair("login", login));
      nameValuePairs.add(new BasicNameValuePair("password", password));
      request.setEntity(new UrlEncodedFormEntity(nameValuePairs));
      
      // Format our request to communicate with AAF API
      String requestSignature = calculateSecret(request);
      String requestAuthorizeHeader = String.format(AUTHORIZE_HEADER, this.apiToken, requestSignature);
      request.setHeader("Authorization", requestAuthorizeHeader);

      log.info("Create complete VHR API request with the following details:");
      log.info("Date: " + requestDateHeader);
      log.info("Authorization: " + requestAuthorizeHeader);
      log.info("Request: " + request);

      httpClient = HttpClients.createDefault();
      response = httpClient.execute(request);

      log.info("Response status: {}", response.getCode());
      if(response.getCode() == HttpStatus.SC_OK){
        JSONObject responseJSON = parseJSON(response.getEntity());
        if(responseJSON != null) {
          String remoteUser = (String) responseJSON.get("remote_user");

          if(remoteUser != null) {
            log.info("VHR API advises basic authentication for {} is valid, supplying {} as REMOTE_USER.", login, remoteUser);
            return remoteUser;
          }
        }
      } else {
        log.error("VHR API error for login {}", login);
        JSONObject responseJSON = parseJSON(response.getEntity());
        if(responseJSON != null) {
          String error = (String) responseJSON.get("error");
          String internalerror = (String) responseJSON.get("internalerror");
          log.error("VHR API Error: {}", error);
          log.error("VHR API Internal Error: {}", internalerror);
        } else {
          log.error("VHR API error with no JSON error detail provided.");
        }
        request.abort();
      }
    } catch (Exception e) {
      log.error("Exception casued when ontacting VHR API for basic authentication with login {}.\nMessage: {}", login, e.getMessage());
      e.printStackTrace();
    } finally {
      if(request != null)
        request.reset();
    }

    return null;
  }

  private JSONObject parseJSON(HttpEntity entity) throws ParseException, org.apache.hc.core5.http.ParseException, IOException {
    ContentType contentType = ContentType.parse(entity.getContentType());
    if(contentType.getMimeType().equals(ContentType.APPLICATION_JSON.getMimeType())) {
      String responseJSON = EntityUtils.toString(entity);

      JSONParser parser = new JSONParser();
      Object obj = parser.parse(responseJSON);
      return (JSONObject) obj;
    }
    return null;
  }

  private String calculateSecret(HttpPost request) throws NoSuchAlgorithmException, InvalidKeyException, IllegalStateException, IOException, URISyntaxException {
    StringBuffer input = new StringBuffer();
      input.append(String.format("%s\n", request.getMethod().toLowerCase()));
      input.append(String.format("%s\n", this.requestingHost.toLowerCase()));
      input.append(String.format("%s\n", request.getUri().getPath().toLowerCase()));
      input.append(String.format("%s\n", request.getFirstHeader("Date").getValue().toLowerCase()));
      input.append(String.format("%s\n", request.getEntity().getContentType().toLowerCase()));
      input.append(String.format("%s\n", encodeBody(request)));

      log.error("Creating request signature from following input:\n{}", input.toString());

      SecretKeySpec signingKey = new SecretKeySpec(apiSecret.getBytes(), "HmacSHA256");
      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(signingKey);
      byte[] rawHmac = mac.doFinal(input.toString().getBytes());

    return new String(Base64.encodeBase64(rawHmac));
  }
  
  private String encodeBody(HttpPost request) throws IllegalStateException, IOException { 
	  String content = convertStreamToString(request.getEntity().getContent());
	  return DigestUtils.sha256Hex(content);
  }
  
  public static String convertStreamToString(java.io.InputStream is) {
    java.util.Scanner s = new java.util.Scanner(is, "UTF-8").useDelimiter("\\A");
    return s.hasNext() ? s.next() : "";
  }
}
