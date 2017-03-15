package wseva

class UrlMappings {

    static mappings = {
        "/EVApdt/api/V1.0/$controller/$action?/$id?(.$format)?"{
            constraints {
                // apply constraints here
            }
        }

        "/EVApdt/api/V1.0/inventarios"{
            //controller="Inv_pdt_estados"
            controller="Articulos"
            action="estructura"
            //action="confirmarInventario"
        }

        "/EVApdt/api/V1.0/login"{
            controller="Usuarios"
            action="login"
        }

        "/EVApdt/api/V1.0/tienda"{
            controller="Pedidos"
            action="tienda"
        }

        "/EVApdt/api/V1.0/inventarios/estados/$idBloque?/$idConteo?"{
            controller="Inv_pdt_estados"
            action="index"
        }



        "/"(view:"/index")
        "500"(view:'/error')
        "404"(view:'/notFound')
    }
}
