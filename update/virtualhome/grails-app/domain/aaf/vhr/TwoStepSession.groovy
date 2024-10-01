package aaf.vhr

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import grails.plugins.orm.auditable.Auditable

import groovy.time.TimeCategory

@ToString
@EqualsAndHashCode
class TwoStepSession implements Auditable {
  String value
  Date expiry

  static belongsTo = [managedSubject:ManagedSubject]
  static mapping = {
    value index: 'Value_Idx'
  }

  def populate() {
    use (TimeCategory) {
      value = aaf.vhr.crypto.CryptoUtil.randomAlphanumeric(64)
      expiry = new Date() + 90.days
    }
  }
}
