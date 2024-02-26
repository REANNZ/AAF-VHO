package aaf.vhr.idp;

import java.io.IOException;
import java.net.URI;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.TimeZone;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.net.URIBuilder;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VhrSessionValidator {
	private final String DATE_FORMAT = "EEE, d MMM yyyy HH:mm:ss z";
	private final String AUTHORIZE_HEADER = "AAF-HMAC-SHA256 token=\"%s\", signature=\"%s\"";
	
	Logger log = LoggerFactory.getLogger("aaf.vhr.idp.VhrSessionValidator");
	
	private String apiServer;
	private String apiEndpoint;
	private String apiToken;
	private String apiSecret;
	private String requestingHost;
	
	public VhrSessionValidator(String apiServer, String apiEndpoint, String apiToken, String apiSecret, String requestingHost) {
		this.apiServer = apiServer;
		this.apiEndpoint = apiEndpoint;
		this.apiToken = apiToken;
		this.apiSecret = apiSecret;
		this.requestingHost = requestingHost;
	}
	
	public String validateSession(String vhrSessionID) {
		return validateSession(vhrSessionID, null, false, null, null);
	}

	public String validateSession(String vhrSessionID, Instant notBefore, boolean mfaRequested, Instant authnInstantArr[], boolean mfaArr[]) {
		CloseableHttpClient httpClient = null;
		HttpGet request = null;
		ClassicHttpResponse response = null;
		
		try {
			log.info("Contacting VHR API for sessionID {} details", vhrSessionID); 
			
			String requestPath = String.format(this.apiEndpoint, vhrSessionID);
			
			Date requestDate = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
			sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
			String requestDateHeader = sdf.format(requestDate);
			
			String requestSignature = calculateSecret(requestPath, requestDateHeader);
			String requestAuthorizeHeader = String.format(AUTHORIZE_HEADER, this.apiToken, requestSignature);
			
			URIBuilder builder = new URIBuilder(new URI(this.apiServer));
			builder.setPath(requestPath);
			request = new HttpGet(builder.build());
			
			// Format our request to communicate with AAF API
			request.setHeader("Date", requestDateHeader);
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
					String authnInstant_str = (String) responseJSON.get("authnInstant");
					Instant authnInstant = null;
					if (authnInstant_str != null) {
					    log.debug("VHR response includes an AuthnInstant: {}", authnInstant_str);
					    authnInstant = Instant.parse(authnInstant_str);
					    log.debug("VHR response authnInstant as java.time.instant is: {}", authnInstant);
					}
					// if notBefore was requested and we are either missing authnInstant or authnInstant was *before* notBefore, reject the session
					if (notBefore != null && (authnInstant == null || authnInstant.compareTo(notBefore) < 0)) {
					    log.info("Rejecting username {} as authnInstant {} is earlier than the notBefore threshold {}", remoteUser, authnInstant, notBefore);
					    remoteUser = null;
					};

					Boolean mfa_obj = (Boolean) responseJSON.get("mfa");

					// if MFA was requested and we are either missing mfa status or it is False, reject the session
					if (mfaRequested && (mfa_obj == null || !mfa_obj.booleanValue())) {
					    log.info("Rejecting username {} as MFA is requested but {}", remoteUser, mfa_obj==null ? "status is unknown" : "was not used");
					    remoteUser = null;
					};
					
					if(remoteUser != null) {
						log.info("VHR API advises sessionID {} belongs to user {}, setting for REMOTE_USER.", vhrSessionID, remoteUser);
						if (authnInstant != null && authnInstantArr != null && authnInstantArr.length>=1) {
							log.info("VHR API sets authnInstant to {}.", authnInstant);
							authnInstantArr[0]=authnInstant;
						};
						if (mfa_obj != null && mfaArr != null && mfaArr.length>=1) {
							log.info("VHR API sets MFA status to {}.", mfa_obj.booleanValue());
							mfaArr[0]= mfa_obj.booleanValue();
						};
						return remoteUser;
					}
				}
            } else {
            	log.error("VHR API error for sessionID {}",vhrSessionID); 
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
			log.error("Exception when contacting VHR API for sessionID {} details.\nMessage: {}", vhrSessionID, e.getMessage());
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
	
	private String calculateSecret(String requestPath, String requestDateHeader) throws NoSuchAlgorithmException, InvalidKeyException {
		StringBuffer input = new StringBuffer();
	    input.append("get\n");
	    input.append(String.format("%s\n", this.requestingHost.toLowerCase()));
	    input.append(String.format("%s\n", requestPath.toLowerCase()));
	    input.append(String.format("%s\n", requestDateHeader.toLowerCase()));
	    
	    log.debug("Creating request signature from following input:\n{}", input.toString());
	    
	    SecretKeySpec signingKey = new SecretKeySpec(apiSecret.getBytes(), "HmacSHA256");
	    Mac mac = Mac.getInstance("HmacSHA256");
	    mac.init(signingKey);
	    byte[] rawHmac = mac.doFinal(input.toString().getBytes());
	    
		return new String(Base64.encodeBase64(rawHmac));
	}
}
