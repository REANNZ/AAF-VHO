package aaf.vhr.crypto;

import java.util.List;
import java.util.ArrayList;

import org.junit.Assert;

import spock.lang.Specification;

public class CryptoUtilSpec extends Specification {

  def 'testRandomAlphanumeric'() {
    List<String> outputs = new ArrayList<String>();
    for (int i = 0; i < 100; i++) {
      String out = CryptoUtil.randomAlphanumeric(200);
      Assert.assertEquals(200, out.length());
      Assert.assertFalse(outputs.contains(out));
      outputs.add(out);
    }

    Assert.assertEquals(100, outputs.size());
  }

}
