package aaf.vhr.crypto;

import java.util.List;
import java.util.ArrayList;

import org.junit.Assert;

import spock.lang.Specification;

public class CryptoUtilSpec extends Specification {

  def 'testRandomAlphanumeric'() {
    def outputs = new ArrayList<String>()
    for (def i = 0 ; i < 100 ; i++) {
      when:
      def out = CryptoUtil.randomAlphanumeric(200)

      then:
      200 == out.length()
      !outputs.contains(out)
      outputs.add(out)
    }
  }
}
