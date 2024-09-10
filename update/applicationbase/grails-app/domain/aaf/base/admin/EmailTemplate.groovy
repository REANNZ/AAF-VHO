package aaf.base.admin

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@ToString(includeNames=true, includeFields=true, excludes="content")
@EqualsAndHashCode
class EmailTemplate {

  String name
  String content

  Date dateCreated
  Date lastUpdated

  static mapping = {
    content type: 'text'
  }

  static constraints = {
    name nullable: false, blank: false, unique:true
    content validator: {val ->
      if (val == null || val == '')
        return ['domains.aaf.base.emailtemplate.content']
    }
  }

}
