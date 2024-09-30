package virtualhome

// This does the same job as the old SecurityFilters.groovy file
class SecurityInterceptor {

    SecurityInterceptor() {
        match(uri: "/dashboard/**")
        match(uri: "/organisations/**")
        match(uri: "/groups/**")
        match(uri: "/accounts/**")
        match(uri: "/backend/**")
    }

    boolean before() {
        accessControl { true }
    }

    boolean after() {
        true
    }

    void afterView() {
        // no-op
    }
}
