package aaf.base.identity

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@ToString(includeNames=true, includeFields=true, excludes="apiKey")
@EqualsAndHashCode
class ApiSubject extends Subject {
  static auditable = true

  String apiKey
  String description

  static constraints = {
    apiKey nullable:false, blank:false
    description nullable:false, blank: false
  }

  static mapping = {
    table 'base_subject_api'
  }
}
