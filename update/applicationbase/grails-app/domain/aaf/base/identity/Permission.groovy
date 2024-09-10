package aaf.base.identity

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@ToString(includeNames=true, includeFields=true)
@EqualsAndHashCode
class Permission {
  static auditable = true

  static public final String defaultPerm = "aaf.base.identity.WildcardPermission"
  static public final String wildcardPerm = "aaf.base.identity.WildcardPermission"
  static public final String adminPerm = "aaf.base.identity.AllPermission"

  String type
  String possibleActions = "*"
  String actions = "*"
  String target
  boolean managed

  Subject subject
  Role role

  static belongsTo = [subject: Subject, role: Role]

  static transients = [ "owner" ]

  static constraints = {
    type(nullable: false, blank: false)
    possibleActions(nullable: false, blank: false)
    actions(nullable: false, blank: false)
    target(nullable: false, blank: false)

    subject(nullable:true)
    role(nullable:true)
  }

  static mapping = {
    table 'base_permission'
  }

  def setOwner (def owner) {
    if (owner instanceof Subject)
    this.subject = owner

    if (owner instanceof Role)
    this.role = owner
  }

  def getOwner() {
    if(this.subject != null)
    return subject

    if(this.role != null)
    return role

    return null
  }

  def getDisplayType() {
    def components = type.tokenize('.')
    if(components)
      components[components.size() - 1]
    else
      type
  }
}
