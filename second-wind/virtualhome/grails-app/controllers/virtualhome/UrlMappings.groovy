package virtualhome

class UrlMappings {

    static mappings = {
        "/"(controller:'helloworld')
        "500"(view:'/error')
        "404"(view:'/notFound')
    }
}
