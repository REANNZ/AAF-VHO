package virtualhome

class OrganizationInterceptor {

    OrganizationInterceptor() {
        match(controller:'organization').except(action: ['list', 'create', 'save'])
    }

    boolean before() {
        if(!params.id) {
            log.warn "ID was not present"

            flash.type = 'info'
            flash.message = message(code: 'controllers.aaf.vhr.organization.no.id')

            redirect action:'list'
            return false
        }

        def organizationInstance = Organization.get(params.id)
        if (!organizationInstance) {
            log.warn "organizationInstance was not a valid instance"

            flash.type = 'info'
            flash.message = 'controllers.aaf.vhr.organization.notfound'

            redirect action:'list'
            return false
        }

        true
    }
    boolean after() { true }

    void afterView() {
        // no-op
    }
}
