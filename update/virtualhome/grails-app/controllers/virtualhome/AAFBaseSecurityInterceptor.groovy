package virtualhome


class AAFBaseSecurityInterceptor {

    AAFBaseSecurityInterceptor() {
        
    }

    boolean before() { true }

    boolean after() { true }

    void afterView() {
        // no-op
    }
}
