package aaf.vhr

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

import groovy.time.TimeCategory

@ToString
@EqualsAndHashCode
class TwoStepSession {
  static auditable = true

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
