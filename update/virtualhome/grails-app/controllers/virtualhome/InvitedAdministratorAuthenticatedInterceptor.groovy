package virtualhome

class InvitedAdministratorAuthenticatedInterceptor {

    InvitedAdministratorAuthenticatedInterceptor() {
        match uri:"/inviteadministrator/**"
    }

    boolean before() {
        accessControl { true }
    }

    boolean after() { true }

    void afterView() {
        // no-op
    }
}
