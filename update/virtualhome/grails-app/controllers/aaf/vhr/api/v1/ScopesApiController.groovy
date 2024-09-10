package aaf.vhr.api.v1

import aaf.vhr.*

class ScopesApiController extends aaf.base.api.ApiBaseController {
  static defaultAction = "index"
  def beforeInterceptor = [action: this.&validateRequest, except:['list']]

  def list() {
    log.info "ScopesApi.list() executed"
    if (!grailsApplication.config.aaf.vhr.scopes.base_scope) {
      log.error "ScopesAPI error: config.aaf.vhr.scopes.base_scope is not set"
     response.sendError 500
     return
    }

    def baseScope = grailsApplication.config.aaf.vhr.scopes.base_scope
    def scopes = [ baseScope ]

    Organization.list().findAll{it.orgScope && it.functioning()}.each {
      def baseOrgScope = it.orgScope + "." + baseScope
      scopes.add(baseOrgScope)

      it.groups.findAll{it.groupScope && it.functioning()}.each {
        def groupScope = it.groupScope + "." + baseOrgScope
        scopes.add(groupScope)
      }
    }

    render(contentType: 'application/json') { [entityId: grailsApplication.config.aaf.vhr.sharedtoken.idp_entityid, updated_at: new Date(), scopes: scopes] }
  }

}
