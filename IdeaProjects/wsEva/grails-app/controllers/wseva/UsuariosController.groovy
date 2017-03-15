package wseva

import grails.converters.JSON
import org.grails.web.json.JSONException

import static org.springframework.http.HttpStatus.*
import grails.transaction.Transactional
import groovy.json.JsonSlurper

@Transactional(readOnly = true)
class UsuariosController {

    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    def login()
    {
        try{

            if(request.method=='POST')
            {
                def peticion=request.JSON
                def usuario=peticion.username
                def pass=peticion.password

                if(usuario && pass)
                {
                    def existe= Usuarios.where {username==usuario;password==pass}.toList()[0]

                    if(existe)
                    {
                        response.status=200
                        def resultado=["username":existe.username,"password":existe.password,"enabled":existe.enabled,"nombre":existe.nombre,"apellido":existe.apellido]

                        render ([data:resultado,status:'success',message: 'Sesión iniciada exitosamente'] as JSON)
                    }
                    else
                    {
                        response.status=200
                        render ([status:'error',message: 'No existe algún usuario con esas credenciales. Verifique'] as JSON)
                    }

                }
                else
                {
                    response.status=400
                    render ([status:'error',message: 'Verifique que existan los campos username y password en el JSON'] as JSON)
                }
            }
            else
            {
                response.status=400
                render ([status:'error',message: 'Sólo se admiten solicitudes POST'] as JSON)
            }



            //println peticion


        }catch (JSONException){
            response.status=400
            render ([status:'error',message: 'Verifique su estructura JSON'] as JSON)
        }catch(Exception ex){
            response.status=400
            render ([status:'error',message: 'Ha ocurrido un error interno en el servidor'] as JSON)
        }





    }

    /*def index()
    {
        def inputFile = new File("C:/Eva/usuarios.json")
        def InputJSON = new JsonSlurper().parseText(inputFile.text)
        render InputJSON as JSON
    }*/

    def index() {

        try {
            if (request.method == 'GET')
            {
                def query="Select u from Usuarios u where u.rol_id='ROLE_VENDEDOR'"
                log.info("Enviando lista de usuarios...")
                def results=Usuarios.executeQuery(query)
                def salida=[]
                results.each {
                    def resultado=["username":it.username,"password":it.password,"enabled":it.enabled,"nombre":it.nombre,"apellido":it.apellido]
                    salida<<resultado
                }
                response.status=200
                render ([data:salida,status:'success'] as JSON)
            }
            else
            {
                response.status=400
                render ([status:'error'] as JSON)
            }
        }catch (Exception ex){
                response.status=500
                render ([status:'fail',message:'Ha ocurrido un error interno en el servidor'] as JSON)
        }




    }

    def show(Usuarios usuarios) {
        respond usuarios
    }

    def create() {
        respond new Usuarios(params)
    }

    @Transactional
    def save(Usuarios usuarios) {
        if (usuarios == null) {
            transactionStatus.setRollbackOnly()
            notFound()
            return
        }

        if (usuarios.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond usuarios.errors, view:'create'
            return
        }

        usuarios.save flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.created.message', args: [message(code: 'usuarios.label', default: 'Usuarios'), usuarios.id])
                redirect usuarios
            }
            '*' { respond usuarios, [status: CREATED] }
        }
    }

    def edit(Usuarios usuarios) {
        respond usuarios
    }

    @Transactional
    def update(Usuarios usuarios) {
        if (usuarios == null) {
            transactionStatus.setRollbackOnly()
            notFound()
            return
        }

        if (usuarios.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond usuarios.errors, view:'edit'
            return
        }

        usuarios.save flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.updated.message', args: [message(code: 'usuarios.label', default: 'Usuarios'), usuarios.id])
                redirect usuarios
            }
            '*'{ respond usuarios, [status: OK] }
        }
    }

    @Transactional
    def delete(Usuarios usuarios) {

        if (usuarios == null) {
            transactionStatus.setRollbackOnly()
            notFound()
            return
        }

        usuarios.delete flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.deleted.message', args: [message(code: 'usuarios.label', default: 'Usuarios'), usuarios.id])
                redirect action:"index", method:"GET"
            }
            '*'{ render status: NO_CONTENT }
        }
    }

    protected void notFound() {
        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'usuarios.label', default: 'Usuarios'), params.id])
                redirect action: "index", method: "GET"
            }
            '*'{ render status: NOT_FOUND }
        }
    }
}
