package aaf.vhr.api.v1

import aaf.vhr.Organization
import aaf.vhr.Group

import grails.test.mixin.*
import grails.test.spock.*

import spock.lang.Specification
import grails.testing.web.controllers.ControllerUnitTest

//import java.text.SimpleDateFormat
//import java.util.TimeZone
class ScopesApiControllerSpec extends Specification implements ControllerUnitTest<ScopesApiController>{

  def "scopes: 500 if base scope is not set"() {
    setup:
    grailsApplication.config.aaf.vhr.scopes.base_scope = null

    when:
    controller.list()

    then:
    response.status == 500
  }

  def "scopes: valid json response"() {
    setup:
    grailsApplication.config.aaf.vhr.scopes.base_scope = 'vho.example.org'

    when:
    controller.list()

    then:
    response.status == 200
    response.contentType == 'application/json;charset=UTF-8'
    response.json.scopes.size() == 1
  }

  def "scopes: valid json response with organization"() {
    setup:
    grailsApplication.config.aaf.vhr.scopes.base_scope = 'vho.example.org'
    def org1 = new Organization(active:true, orgScope: 'org1')
    def org2 = new Organization(active:false, orgScope: 'org2')
    def org3 = new Organization(active:true)


    when:
    controller.list()

    then:
    response.status == 200
    response.contentType == 'application/json;charset=UTF-8'
    response.json.scopes.size() == 2
    response.json.scopes.contains('org1.vho.example.org')
    !response.json.scopes.contains('org2.vho.example.org')
  }

  def "scopes: valid json response with organization and groups"() {
    setup:
    grailsApplication.config.aaf.vhr.scopes.base_scope = 'vho.example.org'
    def org1 = new Organization(active:true, orgScope: 'org1')
    def org2 = new Organization(active:false, orgScope: 'org2')
    def org3 = new Organization(active:true)

    def org1_group1 = new Group(active: true, organization: org1, groupScope: 'group1')
    def org1_group2 = new Group(active: false, organization: org1, groupScope: 'group2')
    def org1_group3 = new Group(active: true, organization: org1)

    // should be ignored because org is inactive / has no scope
    def org2_group1 = new Group(active: true, organization: org2, groupScope: 'group1')
    def org3_group1 = new Group(active: true, organization: org3, groupScope: 'group1')

    when:
    controller.list()

    then:
    response.status == 200
    response.contentType == 'application/json;charset=UTF-8'
    response.json.scopes.size() == 3
    response.json.scopes.contains('org1.vho.example.org')
    !response.json.scopes.contains('org2.vho.example.org')
    response.json.scopes.contains('group1.org1.vho.example.org')
    !response.json.scopes.contains('group2.org2.vho.example.org')
  }

}
