package virtualhome

class LostPasswordInterceptor {

    LostPasswordInterceptor() {
        match(controller:'lostPassword').except(action: ['start', 'obtainsubject', 'complete', 'unavailable', 'support', 'logout'])
    }

    boolean before() {
        def managedSubjectInstance = ManagedSubject.get(session.getAttribute(CURRENT_USER))

        if(!managedSubjectInstance) {
            log.error "No ManagedSubject stored in session, requesting login before accessing password change"
            
            flash.type = 'info'
            flash.message = 'controllers.aaf.vhr.lostpassword.requiresaccount'
            redirect action: 'start'
            return false
        }

        if(!managedSubjectInstance.canChangePassword() || managedSubjectInstance.failedResets >= grailsApplication.config.aaf.vhr.passwordreset.reset_attempt_limit.intValue()) {
            if(managedSubjectInstance.failedResets >= grailsApplication.config.aaf.vhr.passwordreset.reset_attempt_limit.intValue()) {
                String reason = "Locked by forgotten password process due to many failed login attempts"
                String requestDetails = """
User Agent: ${request.getHeader('User-Agent')}
Remote Host: ${request.getRemoteHost()}
Remote IP: ${request.getRemoteAddr()}"""

                managedSubjectInstance.lock(reason, 'lost_password_max_attempts_reached', requestDetails, null)
            }

            redirect action: 'support'
            return false
        }

        true
    }

    boolean after() { true }

    void afterView() {
        // no-op
    }
}
