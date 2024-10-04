package virtualhome

class ManagedSubjectInterceptor {

    ManagedSubjectInterceptor() {
        match(controller:'managedSubject').except(action: ['list', 'create', 'save', 'createcsv', 'savecsv'])
    }

    boolean before() {
        if(!params.id) {
            log.warn "ID was not present"

            response.sendError 404
            return false
        }

        def managedSubjectInstance = ManagedSubject.get(params.id)
        if (!managedSubjectInstance) {
            log.warn "managedSubjectInstance was not a valid instance"

            response.sendError 404
            return false
        }

        true
    }

    boolean after() { true }

    void afterView() {
        // no-op
    }
}
