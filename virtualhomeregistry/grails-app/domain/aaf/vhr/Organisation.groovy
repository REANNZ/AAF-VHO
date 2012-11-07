package aaf.vhr

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@ToString(includeNames=true, includeFields=true, excludes="description, url, frURL")
@EqualsAndHashCode
class Organisation  {
  static auditable = true

  String name
  String displayName
  String description
  String url
  String frURL

  long frID
  
  boolean active = false
  
  Date dateCreated
  Date lastUpdated

  static constraints = {
    name(nullable: false, blank: false)
    displayName(nullable: false, blank: false)
    description(nullable:true, blank: false, maxSize:2000)

    url(nullable: false, blank: false, url: true)

    frURL(nullable: false, blank: false, url: true)

    dateCreated(nullable:true)
    lastUpdated(nullable:true)
  }
}
