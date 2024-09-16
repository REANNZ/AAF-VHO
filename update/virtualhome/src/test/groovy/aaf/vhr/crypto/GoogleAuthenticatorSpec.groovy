package aaf.vhr.crypto;

import java.util.List;
import java.util.ArrayList;

import spock.lang.Specification;

public class GoogleAuthenticatorTest extends Specification {

  public void testGenerateSecret() {
    String secret = GoogleAuthenticator.generateSecretKey();
    String totpURL = GoogleAuthenticator.getTotpURL('testuser', 'vhr.test.edu.au', secret, null)

    println secret
    println totpURL

    assert secret.length() == 16
    assert totpURL == "otpauth://totp/testuser@vhr.test.edu.au?secret=" + secret
  }

  public void testGenerateSecretWithIssuer() {
    String secret = GoogleAuthenticator.generateSecretKey();
    String totpURL = GoogleAuthenticator.getTotpURL('testuser', 'vhr.test.edu.au', secret, "Issuer%20Org")

    println secret
    println totpURL

    assert secret.length() == 16
    assert totpURL == "otpauth://totp/Issuer%20Org:testuser@vhr.test.edu.au?secret=" + secret + "&issuer=Issuer%20Org"
  }

  /*
  If you want to test this register your device with the QR code at:
  https://www.google.com/chart?chs=200x200&chld=M%7C0&cht=qr&chl=otpauth://totp/testuser@vhr.test.edu.au%3Fsecret%3DLAZLDDYD4WYSDULO

  Then enable here providing the code it outputs.

  Haccccccky at best.

  public void testCheckCode() {
    assert (GoogleAuthenticator.checkCode('DPS6XA5YWTZFQ4FI', 980964, System.currentTimeMillis()) == true)
  }
  */
}
