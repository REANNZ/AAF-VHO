package aaf.base.workflow

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@ToString(includeNames=true, includeFields=true, excludes="start, terminate")
@EqualsAndHashCode
class TaskOutcome {
  static auditable = true
  
  String name
  String description
  
  List start = []
  List terminate = []
  
  static belongsTo = [ task: Task ]

  static hasMany = [  start: String,
  terminate: String ]

  static constraints = {
    name(nullable:false, blank:false)
    description(nullable:false, blank:false)
    start(validator: {val ->
     val.size() > 0
    })
  }
}
