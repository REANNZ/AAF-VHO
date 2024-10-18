package aaf.base

import org.apache.shiro.SecurityUtils
import org.apache.shiro.authz.permission.WildcardPermission

import aaf.base.identity.Subject

class AAFBaseTagLib {

  static namespace = "aaf"
  static returnObjectForTags = ['subject']

  // Create bootstrap compatible tooltips using Grails i18n
  def tooltip = { attrs ->
    def msg = g.message(encodeAs:'HTML', code:attrs.code)
    out << r.img(dir:'images', file:'help.png', title:msg, width:'16px', height:'16px', rel:'tooltip', 'data-placement':'right')
  }

  // This tag only writes its body to the output if the current user is an application wide administrator
  def isAdministrator = { attrs, body ->
    if(checkPermission('app:administration')) {
      out << body()
    }
  }

  // This tag only writes its body to the output if the current user has the given permission.
  def hasPermission = {attrs, body ->
    def target = attrs.target
    if (target) {
      if (checkPermission(target))
        out << body()
    } else {
      throwTagError('Tag [hasPermission] must have [in] attribute.')
    }
  }

  // This tag only writes its body to the output if the current user has any of the given permissions.
  def hasAnyPermission = {attrs, body ->
    def inList = attrs.in

    if(inList) {
      if(inList.any { checkPermission(it) } )
        out << body()
    } else {
      throwTagError('Tag [hasAnyPermission] must have [in] attribute.')
    }
  }

  // This tag only writes its body to the output if the current user does not have the given permission.
  def lacksPermission = {attrs, body ->
    def target = attrs.target
    if (target) {
      if (!checkPermission(target))
        out << body()
    } else {
      throwTagError('Tag [lacksPermission] must have [in] attribute.')
    }
  }

  // This tag only writes its body to the output if the current user has none of the given permissions.
  def lacksAllPermissions = {attrs, body ->
    def inList = attrs.in

    if(inList) {
      if(!inList.any { checkPermission(it) } )
        out << body()
    } else {
      throwTagError('Tag [hasAnyPermission] must have [in] attribute.')
    }
  }

  // This tag only writes its body to the output if the current subject is logged in.
  def isLoggedIn = {attrs, body ->
    if (checkAuthenticated()) {
      out << body()
    }
  }

  // This tag only writes its body to the output if the current subject isn't logged in.
  def isNotLoggedIn = {attrs, body ->
    if (!checkAuthenticated()) {
      out << body()
    }
  }

  // Provides markup that renders the principal of the logged in user
  def principal = {
    Long id = SecurityUtils.getSubject()?.getPrincipal()
    if (id) {
      def subject = Subject.get(id)
      if (subject)
        out << subject.principal
    }
  }

  // Provides markup the renders the common name of the logged in subject
  def subjectCN = { attrs, body ->
    Long id = SecurityUtils.getSubject()?.getPrincipal()
    if (id) {
      def subject = Subject.get(id)
      out << subject.cn
    }
  }

  // Provides access to the logged in subject object
  def subject = { attrs, body ->
    def subject = null
    Long id = SecurityUtils.getSubject()?.getPrincipal()
    if (id) {
      subject = Subject.get(id)
    }
    subject
  }

  private boolean checkPermission(String target) {
    Long id = SecurityUtils.getSubject()?.getPrincipal()
    if (id) {
      def subject = Subject.get(id)
      // Ensure we have a hibernate session open - it isn't on generic views and error pages
      if (subject && subject.isAttached())
      {
        WildcardPermission permission = new WildcardPermission(target, false)
        SecurityUtils.subject?.isPermitted(permission)
      }
      else
        false
    } else
      false
  }

  private boolean checkAuthenticated() {
    return SecurityUtils.subject?.authenticated
  }
}
