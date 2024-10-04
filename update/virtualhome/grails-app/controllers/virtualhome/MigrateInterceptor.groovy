package virtualhome

class MigrateInterceptor {

    MigrateInterceptor() {
        match(controller:'migrate').except(action: ['oops'])
    }

    boolean before() {
        if(!session.getAttribute(MigrateController.MIGRATION_USER)) {
            redirect action: 'oops'
            return false
        }
        return true
    }

    boolean after() { true }

    void afterView() {
        // no-op
    }
}
