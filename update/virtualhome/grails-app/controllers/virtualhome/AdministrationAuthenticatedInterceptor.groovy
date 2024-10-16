package virtualhome

class AdministrationAuthenticatedInterceptor {

    AdministrationAuthenticatedInterceptor() {
        match uri: "/administration/**"
    }

    boolean before() {
        accessControl { true }
     }

    boolean after() { true }

    void afterView() {
        // no-op
    }
}
