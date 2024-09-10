package aaf.base.identity

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@ToString(includeNames=true, includeFields=true)
@EqualsAndHashCode
class Role {
  static auditable = true
  
  String name
  String description
  
  boolean protect

  static hasMany = [ subjects: Subject, permissions: Permission ]

  static constraints = {
    name(nullable: false, blank: false, unique: true)
    description(nullable:true)
  }

  static mapping = {
    table 'base_role'
  }

  def listSubjects() {
    subjects.findAll{ !it.instanceOf(aaf.base.identity.ApiSubject) }
  }

  def listApiSubjects() {
    subjects.findAll{ it.instanceOf(aaf.base.identity.ApiSubject) }
  }
}
