package wseva

import grails.converters.JSON
import org.grails.web.json.JSONException

import static org.springframework.http.HttpStatus.*
import grails.transaction.Transactional

@Transactional(readOnly = true)
class Inv_pdt_estadosController {
    def generalService

    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    def index()
    {
        try{
            String bloque=params?.idBloque
            //println bloque
            int conteo=0
            if(params.idConteo)
                conteo=Integer.parseInt(params.idConteo)
            //println "Conteo es "+conteo
            def codigoLocalidad=grailsApplication.config.getProperty('localidad.codigo.localidad')
            def peticion=request.JSON
            def codTienda=peticion?.codTienda
            String usuario=peticion?.username

            if(request.method=='POST')
            {
                println "EL USUARIO ES "+usuario
                println "EL CODIGO DE LA TIENDA ES  "+codTienda

                if(codTienda && usuario)
                {
                    println "CODIGO TIENDA ES "+codTienda
                    println "CODIGO LOCALIDAD ES "+codigoLocalidad
                    if(codTienda==codigoLocalidad)
                    {
                        def validarConteo=generalService.validarConteo(bloque,conteo,usuario) //[decision,mensaje]
                        if(validarConteo[0])
                        {
                            if(conteo==3)
                            {
                                response.status=200
                                render (['status':'success','message':validarConteo[1],'articulosDiferentes':validarConteo[2]] as JSON)
                            }
                            else {
                                response.status=200
                                render (['status':'success','message':validarConteo[1]] as JSON)
                            }


                        }
                        else
                        {
                            response.status=200
                            render (['status':'error','message':validarConteo[1]] as JSON)
                        }
                    }
                    else
                    {
                        response.status=200
                        render (['status':'error','message':"Tiendas no coinciden. Por favor verifique"] as JSON)
                    }
                }
                else
                {
                    response.status=400
                    render (['status':'error','message':'Datos incorrectos. Por favor verifique'] as JSON)
                }
            }
            else
            {
                if(request.method=='DELETE')//PARA LIBERAR CONTEO
                {
                    def liberarConteo=generalService.liberarConteo(bloque,conteo,usuario)
                    if(liberarConteo[0])//si se cerró el conteo
                    {
                        response.status=200
                        render (['status':'success','message':liberarConteo[1]] as JSON)
                    }
                    else
                    {
                        response.status=200
                        render (['status':'error','message':liberarConteo[1]] as JSON)
                    }
                }
                else
                {
                    //-------------PARA CERRAR----------PRUEBAS
                    if(request.method=='PUT')//PARA LIBERAR CONTEO
                    {
                        def cerrarConteo=generalService.cerrarConteo(bloque,conteo,usuario)
                        if(cerrarConteo[0])//si se cerró el conteo
                        {
                            response.status=200
                            render (['status':'success','message':cerrarConteo[1]] as JSON)
                        }
                        else
                        {
                            response.status=400
                            render (['status':'fail','message':cerrarConteo[1]] as JSON)
                        }
                    }
                    else
                    {
                        if(request.method=='GET')
                        {
                            if(params.idConteo && params.idBloque)
                            {

                                def data=[]
                                def estadoConteo=generalService.getEstadoConteo(bloque,conteo) //[decision,mensaje]
                                if(estadoConteo[0])
                                {

                                    data=['Bloque':bloque,'Conteo':conteo,"Estado":estadoConteo[2],"Usuario":estadoConteo[3]]
                                    response.status=200
                                    render (['status':'success','data':data] as JSON)
                                }
                                else
                                {
                                    response.status=400
                                    render (['status':'error','message':estadoConteo[1],'data':data] as JSON)
                                }
                            }
                            else
                            {
                                if(!params.idConteo)
                                {
                                    def conteosBloque
                                    if(!params.idBloque)
                                        conteosBloque=generalService.conteosPorBloque(bloque,1)
                                    else
                                        conteosBloque=generalService.conteosPorBloque(bloque,0)
                                        //conteosBloque=generalService.conteosPorBloque(bloque,0)




                                    if(conteosBloque[0])
                                    {
                                        response.status=200
                                        render (['status':'success','data':conteosBloque[1]] as JSON)
                                    }
                                    else
                                    {
                                        response.status=200
                                        render (['status':'success','data':conteosBloque[1]] as JSON)
                                    }

                                }
                            }



                        }
                    }

                    //-------------PARA CERRAR----------PRUEBAS


                    //response.status=400
                    //render (['status':'fail','mensaje':"Tipo de petición no permitida. Verifique"] as JSON)
                }

            }
        }catch (JSONException)
        {
            response.status=500
            render (['status':'fail','message':'Formato de JSON inválido. Verifique'] as JSON)
        }
    }





    def confirmarInventario()
    {
        try{
            def peticion=request.JSON
            def codigoLocalidad=grailsApplication.config.getProperty('localidad.codigo.localidad')
            def resultado=generalService.formatearPeticion(peticion,codigoLocalidad)

            if(!resultado[0])
            {
                response.status = 400
                render(['status': 'error', 'message': resultado[1]] as JSON)
            }
            else
            {
                response.status = 200
                render(['status': 'success', 'data': resultado[1],'message':resultado[2]] as JSON)
            }
        }catch (JSONException){
            response.status = 500
            render(['status': 'fail', 'data':[],'message':"Ha ocurrido un error en el servidor"] as JSON)
        }



    }

    def show(Inv_pdt_estados inv_pdt_estados) {
        respond inv_pdt_estados
    }

    def create() {
        respond new Inv_pdt_estados(params)
    }

    @Transactional
    def save(Inv_pdt_estados inv_pdt_estados) {
        if (inv_pdt_estados == null) {
            transactionStatus.setRollbackOnly()
            notFound()
            return
        }

        if (inv_pdt_estados.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond inv_pdt_estados.errors, view:'create'
            return
        }

        inv_pdt_estados.save flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.created.message', args: [message(code: 'inv_pdt_estados.label', default: 'Inv_pdt_estados'), inv_pdt_estados.id])
                redirect inv_pdt_estados
            }
            '*' { respond inv_pdt_estados, [status: CREATED] }
        }
    }

    def edit(Inv_pdt_estados inv_pdt_estados) {
        respond inv_pdt_estados
    }

    @Transactional
    def update(Inv_pdt_estados inv_pdt_estados) {
        if (inv_pdt_estados == null) {
            transactionStatus.setRollbackOnly()
            notFound()
            return
        }

        if (inv_pdt_estados.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond inv_pdt_estados.errors, view:'edit'
            return
        }

        inv_pdt_estados.save flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.updated.message', args: [message(code: 'inv_pdt_estados.label', default: 'Inv_pdt_estados'), inv_pdt_estados.id])
                redirect inv_pdt_estados
            }
            '*'{ respond inv_pdt_estados, [status: OK] }
        }
    }

    @Transactional
    def delete(Inv_pdt_estados inv_pdt_estados) {

        if (inv_pdt_estados == null) {
            transactionStatus.setRollbackOnly()
            notFound()
            return
        }

        inv_pdt_estados.delete flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.deleted.message', args: [message(code: 'inv_pdt_estados.label', default: 'Inv_pdt_estados'), inv_pdt_estados.id])
                redirect action:"index", method:"GET"
            }
            '*'{ render status: NO_CONTENT }
        }
    }

    protected void notFound() {
        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'inv_pdt_estados.label', default: 'Inv_pdt_estados'), params.id])
                redirect action: "index", method: "GET"
            }
            '*'{ render status: NOT_FOUND }
        }
    }
}
