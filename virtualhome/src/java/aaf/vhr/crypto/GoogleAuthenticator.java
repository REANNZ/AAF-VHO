package aaf.vhr.crypto;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Hashtable;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.codec.EncoderException;

/**
 * Java Server side class for Google Authenticator's TOTP generator
 * Thanks to Enrico's blog for the sample code:
 * @see http://thegreyblog.blogspot.com/2011/12/google-authenticator-using-it-in-your.html
 *
 * @see http://code.google.com/p/google-authenticator
 * @see http://tools.ietf.org/id/draft-mraihi-totp-timebased-06.txt
 *
 * http://opensource.org/licenses/BSD-3-Clause
 * Copyright (c) 2013, Warren Strange
 * All rights reserved.
 * https://github.com/wstrange/GoogleAuth
 *
 * Minor modifications for AAF purposes.
 */
public class GoogleAuthenticator {

  public static final int SECRET_SIZE = 10;
  public static final int WINDOW_SIZE = 3;

  /**
   * Generate a random secret key. This must be saved by the server and associated with the
   * users account to verify the code displayed by Google Authenticator.
   * The user must register this secret on their device.
   * @return secret key
   */
  public static String generateSecretKey() {
    try {
      SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
      Base32 codec = new Base32();

      byte[] buffer = new byte[10];
      sr.nextBytes(buffer);
      byte[] bEncodedKey = codec.encode(buffer);
      String encodedKey = new String(bEncodedKey);
      return encodedKey;
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
      throw new RuntimeException(e.getMessage());
    }
  }

  /**
   * Return a URL that generates and displays a QR barcode. The user scans this bar code with the
   * Google Authenticator application on their smartphone to register the auth code. They can also manually enter the
   * secret if desired
   *
   * @param user   user id (e.g. fflinstone)
   * @param host   host or system that the code is for (e.g. myapp.com)
   * @param secret the secret that was previously generated for this user
   * @return the URL for the QR code to scan
   */
  public static String getQRBarcodeURL(String user, String host, String secret, String issuer) throws WriterException, IOException {
    String totpURL = getTotpURL(user, host, secret, issuer);

    // generate data: image URL with ZXing
    // select Medium error correction
    Hashtable<EncodeHintType, String> hintMap = new Hashtable<>();
    hintMap.put(EncodeHintType.ERROR_CORRECTION, "M");
    hintMap.put(EncodeHintType.MARGIN, "0");
    // get QR code bit matrix
    BitMatrix bitMatrix = new QRCodeWriter().encode(totpURL, BarcodeFormat.QR_CODE, 200, 200, hintMap);
    // write out as PNG image into a buffer
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    MatrixToImageWriter.writeToStream(bitMatrix, "png", buffer);
    // return a data URL with Base64-encoded PNG image
    return "data:image/png;base64," + Base64.getEncoder().encodeToString(buffer.toByteArray());
  }

  private static String getTotpURL(String user, String host, String secret, String issuer) {
    if (issuer != null) {
      return String.format("otpauth://totp/%s:%s@%s?secret=%s&issuer=%s", issuer, user, host, secret, issuer);
    } else {
      return String.format("otpauth://totp/%s@%s?secret=%s", user, host, secret);
    }
  }

  /**
   * Check the code entered by the user to see if it is valid
   * @param secret  The users secret.
   * @param code  The code displayed on the users device
   * @param t  The time in msec (System.currentTimeMillis() for example)
   * @return
   */
  public static boolean checkCode(String secret, long code, long timeMsec) {
    Base32 codec = new Base32();
    byte[] decodedKey = codec.decode(secret);

    // convert unix msec time into a 30 second "window"
    // this is per the TOTP spec (see the RFC for details)
    long t = (timeMsec / 1000L) / 30L;
    // Window is used to check codes generated in the near past.
    // You can use this value to tune how far you're willing to go.

    for (int i = -WINDOW_SIZE; i <= WINDOW_SIZE; ++i) {
      long hash;
      try {
        hash = verifyCode(decodedKey, t + i);
      } catch (Exception e) {
        e.printStackTrace();
        throw new RuntimeException(e.getMessage());
      }
      if (hash == code) {
        return true;
      }
    }
    // The validation code is invalid.
    return false;
  }


  private static int verifyCode(byte[] key, long t)
      throws NoSuchAlgorithmException, InvalidKeyException {
    byte[] data = new byte[8];
    long value = t ;
    for (int i = 8; i-- > 0; value >>>= 8) {
      data[i] = (byte) value;
    }

    SecretKeySpec signKey = new SecretKeySpec(key, "HmacSHA1");
    Mac mac = Mac.getInstance("HmacSHA1");
    mac.init(signKey);
    byte[] hash = mac.doFinal(data);

    int offset = hash[20 - 1] & 0xF;

    // We're using a long because Java hasn't got unsigned int.
    long truncatedHash = 0;
    for (int i = 0; i < 4; ++i) {
      truncatedHash <<= 8;
      // We are dealing with signed bytes:
      // we just keep the first byte.
      truncatedHash |= (hash[offset + i] & 0xFF);
    }

    truncatedHash &= 0x7FFFFFFF;
    truncatedHash %= 1000000;

    return (int) truncatedHash;
  }
}
