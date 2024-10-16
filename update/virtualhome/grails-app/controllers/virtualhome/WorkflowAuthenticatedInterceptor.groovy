package virtualhome

class WorkflowAuthenticatedInterceptor {

    WorkflowAuthenticatedInterceptor() {
        match uri: "/workflow/**"
    }

    boolean before() {
        accessControl { true }
    }

    boolean after() { true }

    void afterView() {
        // no-op
    }
}
