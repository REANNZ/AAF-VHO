package virtualhome

class LogoutInterceptor {

    LogoutInterceptor() {
        match controller: 'auth', action: '*', exclude:'login'
    }

    boolean before() {
        accessControl { true }
    }

    boolean after() { true }

    void afterView() {
        // no-op
    }
}
