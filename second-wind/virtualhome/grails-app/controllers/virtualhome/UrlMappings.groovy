package virtualhome

class UrlMappings {

    static mappings = {
        "/"(view: "/helloworld")
        "500"(view:'/error')
        "404"(view:'/notFound')
    }
}
