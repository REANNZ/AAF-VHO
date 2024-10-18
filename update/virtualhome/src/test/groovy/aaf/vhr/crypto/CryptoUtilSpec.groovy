package aaf.vhr.crypto;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import spock.lang.Specification;

class CryptoUtilSpec extends Specification {

  def 'test random alphanumeric'() {
    when:
    def outputs = new ArrayList<String>()
    def out = CryptoUtil.randomAlphanumeric(200)
    outputs.add(out)

    then:
    200 == out.length()
    1 == Collections.frequency(outputs, out)

    where:
    i << (0..100)
  }
}
