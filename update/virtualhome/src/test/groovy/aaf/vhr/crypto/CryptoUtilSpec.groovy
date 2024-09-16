package aaf.vhr.crypto;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import spock.lang.Specification;

class CryptoUtilSpec extends Specification {

  def 'test random alphanumeric'() {
    def outputs = new ArrayList<String>()
    for (def i = 0 ; i < 100 ; i++) {
      when:
      def out = CryptoUtil.randomAlphanumeric(200)
      outputs.add(out)

      then:
      200 == out.length()
      1 == Collections.frequency(outputs, out)
    }
  }
}
