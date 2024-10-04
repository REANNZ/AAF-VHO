package virtualhome

class RoleInterceptor {

    RoleInterceptor() {
        match(controller:'role').except(action: ['list', 'create', 'save', 'finalization', 'finalizationerror'])
    }

    boolean before() {
        if(!params.id) {
            log.warn "Role ID was not present"

            flash.type = 'info'
            flash.message = 'controllers.aaf.base.admin.role.noroleid'

            redirect action:'list'
            return false
        }

        def role = Role.get(params.id)
        if (!role) {
            log.warn "No role for ${params.id} located"

            flash.type = 'error'
            flash.message = 'controllers.aaf.base.admin.role.nonexistant'

            redirect(action: "list")
            return false
        }

        true
    }

    boolean after() { true }

    void afterView() {
        // no-op
    }
}
