package reannz

import spock.lang.*

class EncoderTestsSpec extends Specification {

  def 'Ensure we can call encodeAsHTML successfully in our application'() {

    expect:
    '"<script>"'.encodeAsHTML() == '&quot;&lt;script&gt;&quot;'
  }

  def 'Ensure we can call encodeAsBase64 successfully in our application'() {

    expect:
    '"<script>"'.encodeAsBase64() == 'IjxzY3JpcHQ+Ig=='
  }

  def 'Ensure we can call encodeAsMD5 successfully in our application'() {

    expect:
    '"<script>"'.encodeAsMD5() == '94936c5d221680f28f4128694dfd5c3f'
  }
}
