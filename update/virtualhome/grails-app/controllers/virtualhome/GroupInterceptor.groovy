package virtualhome

class GroupInterceptor {

    GroupInterceptor() {
        match(controller:'group').except(action: ['list', 'create', 'save'])
    }

    boolean before() {
        if(!params.id) {
            log.warn "ID was not present"

            flash.type = 'info'
            flash.message = message(code: 'controllers.aaf.vhr.group.no.id')

            redirect action:'list'
            return false
        }

        def groupInstance = Group.get(params.id)
        if (!groupInstance) {
            log.warn "groupInstance was not a valid instance"

            flash.type = 'info'
            flash.message = 'controllers.aaf.vhr.group.notfound'

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
