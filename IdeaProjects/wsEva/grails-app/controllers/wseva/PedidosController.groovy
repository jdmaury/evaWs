package wseva
import grails.transaction.Transactional
import grails.converters.JSON
import java.text.DateFormat
import java.text.SimpleDateFormat


@Transactional(readOnly = true)
class PedidosController {
    def generalService

    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    def a()
    {
        DateFormat df= new SimpleDateFormat("yyyyMMdd_HHmmss")
        println df.format(new Date()).toString()

    }
    def tienda(){
        try{
            if (request.method == 'GET')
            {
                def codigoLocalidad=grailsApplication.config.getProperty('localidad.codigo.localidad')
                def nombreLocalidad=grailsApplication.config.getProperty('localidad.nombre.localidad')
                response.status=200
                log.info("Información de tienda enviada exitosamente...")
                render([status:'success',data:[codigoLocalidad:codigoLocalidad,nombreLocalidad:nombreLocalidad]] as JSON)
            }
            else
            {
                log.error("Error. Solo se admiten solicitudes via GET...")
                response.status=400
                render ([status:'fail',message:'Solo se admiten solicitudes via GET...'] as JSON)
            }
        }catch (Exception ex)
        {
            response.status=500
            log.error("Error. Ha ocurrido un error interno en el servidor...")
            render ([status:'Error',message:'Ha ocurrido un error interno en el servidor'] as JSON)
        }
    }

    def index(){
        try{
            File carpetaPDT=new File("C:/PDT/Pedido/Sincronizado")
            File carpetaPDT2=new File("C:/PDT/Pedido/NoSincronizado")
            //if(!carpetaPDT.exists())
            carpetaPDT.mkdirs()
            //if(!carpetaPDT2.exists())
            carpetaPDT2.mkdirs()

            if (request.method == 'POST')
            {
                def peticion=request.JSON
                def pedidos=peticion.pedidos
                def codigoTienda=peticion.codTienda
                generalService.archivosPorBloque(peticion)

                if(!codigoTienda.toString().isEmpty() && codigoTienda)
                {

                    String codigoLocalidad=grailsApplication.config.getProperty('localidad.codigo.localidad').toString()
                    //def nombreArchivo=generalService.nombrePedidoPdt(codigoLocalidad)


                    def listaBloques=generalService.archivosPorBloque(peticion)//--TENER EN CUENTA
                    def archivosEnviados=0
                    def exitoEnviado=true
                    listaBloques.each {blq->
                        def nombreArchivo=blq.getValue()

                        File file11=new File("C:/PDT/Pedido/Sincronizado/${nombreArchivo}.txt")
                        File file22=new File("C:/PDT/Pedido/NoSincronizado/${nombreArchivo}.txt")

                        log.info("Código de tienda recibido "+codigoTienda)
                        log.info("Código localidad archivo: "+codigoLocalidad+", Código tienda: "+codigoTienda)

                        pedidos.each{

                            if(it.PLU && it.IdPedido && it.EAN && it.Bloque && it.Descripcion && it.Cantidad && it.Usuario && (it.Bloque==blq.getKey()))
                            {
                                println "MANDE EL ARTICULO "+it
                                log.info("Artículo enviado: "+it+"\r\n")

                                def plu=it.PLU
                                //int factor=generalService.traerFactorVenta(plu)
                                int unidadEmpaque=generalService.traerUnidadEmpaque(plu)
                                //log.info("Factor para articulo ${it} es ${factor}")
                                log.info("Unidad de empaque para articulo ${it} es ${unidadEmpaque}\r\n")
                                //int factorVenta=(Integer)it.Cantidad*factor
                                int unid_empaque=(Integer)it.Cantidad*unidadEmpaque
                                //log.info("FactorVenta para articulo ${it} es ${factorVenta}")
                                log.info("Cantidad x unidad de empaque para articulo ${it} es ${unid_empaque}\r\n")

                                def query="from Articulos as a where a.cod_imp='${plu}'"
                                def results=Articulos.findAll(query)

                                if(results && codigoTienda==codigoLocalidad)
                                {
                                    if(results.descodificado[0]==1)
                                    {
                                        log.info("El articulo con PLU=${plu} y bloque ${it.Bloque} se encuentra en la BD y su descodificado=${results.descodificado}")
                                        println "El articulo con PLU=${plu} y bloque ${it.Bloque} se encuentra en la BD y su descodificado=${results.descodificado}"
                                        if(!file22.exists())
                                            file22.createNewFile()
                                        file22<<it.Usuario<<","<<codigoTienda<<","<<it.PLU<<","<<unid_empaque<<"\r\n"
                                        log.info("********* Se agregó el articulo ${it} al archivo ${file22} *********\r\n")

                                    }
                                    else
                                    {
                                        log.info("El articulo con PLU=${plu} y bloque ${it.Bloque} se encuentra en la BD y su descodificado=${results.descodificado}")
                                        println "El articulo con PLU=${plu} y bloque ${it.Bloque} se encuentra en la BD y su descodificado=${results.descodificado}"

                                        if(!file11.exists())
                                            file11.createNewFile()
                                        file11<<it.Usuario<<","<<codigoTienda<<","<<it.PLU<<","<<unid_empaque<<"\r\n"
                                        log.info("********* Se agregó el articulo ${it} al archivo ${file11} *********")

                                    }
                                }
                                else //SI EL PRODUCTO CON ESE PLU NO EXISTE O EL CODIGO DE LA TIENDA ES DIFERENTE A LOCALIDAD
                                {
                                    log.info("El articulo con PLU=${plu} y bloque ${it.Bloque} NO se encuentra en la BD y su descodificado=${results.descodificado}")
                                    println  "El articulo con PLU=${plu} y bloque ${it.Bloque} NO se encuentra en la BD y su descodificado=${results.descodificado}"
                                    if(!file22.exists())
                                        file22.createNewFile()
                                    file22<<it.Usuario<<","<<codigoTienda<<","<<it.PLU<<","<<unid_empaque<<"\r\n"
                                    log.info("********* Se agregó el articulo ${it} al archivo ${file22} *********\r\n")


                                }
                            }
                            else
                            {
                                log.error("Articulo ${it} no cumple con todos los campos requeridos.\r\n")
                                //println "Uno de los campos JSON no esta completo o este artículo no pertenece al bloque ${blq.getKey()}"
                            }

                        }
                        generalService.validarRutaArchivo(file11) //VALIDAR RUTA DEL ARCHIVO A VER SI EXISTE
                        generalService.validarRutaArchivo(file22) //VALIDAR RUTA DEL ARCHIVO A VER SI EXISTE

                        String rutaUrl=grailsApplication.config.getProperty('localidad.url.central')//http://192.168.33.145:8080/Eva
                        log.info("\r\nrutaUrl archivo : ${rutaUrl}\r\n")

                        if(file11.length()!=0){
                            println "SINCRONIZADO-> Archivo de ${nombreArchivo}.txt enviado a central..."
                            log.info("SINCRONIZADO-> Archivo ${nombreArchivo}.txt enviado a central...\r\n")
                            def enviado=generalService.enviarArchivoEva(file11,"${nombreArchivo}.txt",rutaUrl)
                            //generalService.enviarArchivoEva(file11,"${nombreArchivo}.txt",rutaUrl)

                            if(enviado!="200")
                            {


                                log.info("Conexión perdida, creando archivo ${nombreArchivo}.txt localmente")
                                exitoEnviado=false
                                //println "ENTRE ACA para enviar a NoSincronizado el archivo ${nombreArchivo}.txt"
                                File temp=new File("C:/PDT/Pedido/NoSincronizado/${nombreArchivo}.txt")
                                if(temp)
                                    temp<<file11.text
                                else {
                                    temp.createNewFile()
                                    temp<<file11.text

                                }
                                file11.delete()
                            }

                        }
                        else{
                            //file11.delete()
                        }
                        if(file22.length()!=0){
                            //println "NO SINCRONIZADOS-> Archivo de ${nombreArchivo}.txt enviado a central..."

                            println "Archivo No sincronizado ${nombreArchivo}.txt creado localmente"
                            log.info("\r\nArchivo No sincronizado ${nombreArchivo}.txt creado localmente...\r\n")
                            //generalService.enviarArchivoEva(file22, "${nombreArchivo}.txt",rutaUrl)

                        }
                        else{
                            //file22.delete()
                        }


                        if(file11.length()>0 || file22.length()>0){
                            archivosEnviados++
                        }

                    }
                    if(archivosEnviados>0)
                    {
                        response.status=200
                        log.info("Archivo(s) creados y enviado(s)")
                        if(exitoEnviado==false)
                            render (['status':'success','message':"Conexión perdida con el servidor central. Archivos creados localmente",'data':[]] as JSON)
                        else
                            render (['status':'success','message':"Archivo(s) creado(s) y enviado(s)",'data':[]] as JSON)

                    }
                    else
                    {
                        response.status=400
                        render (['status':'error','message':"No se enviaron archivos",'data':[]] as JSON)
                    }
                }
                else{
                    response.status=400
                    log.error("Codigo de tienda no especificado")
                    render (['status':'error','message':'Código de tienda no especificado'] as JSON)
                }
                //if(file)
            }
            else
            {
                response.status=400
                log.info("Solo se admiten solicitudes vía POST")
                render (['status':'error','message':'Solo se admiten solicitudes vía POST'] as JSON)
            }
        }catch(JSONException){
            response.status=500
            log.error("JSON incorrecto. Verifique")
            render (['status':'Fail','message':'JSON incorrecto. Verifique','data':[]] as JSON)
        }
        catch(FileNotFoundException fe){
            response.status=500
            render (['status':'Fail','message':'Ha ocurrido un error. Verifique que la ruta C:/PDT/Pedido/Sincronizado y C:/PDT/Pedido/NoSincronizado  exista','data':[]] as JSON)
        }catch(Exception ex){
            response.status=500
            render (['status':'Fail','data':[]] as JSON)
        }
    }

    def show(Pedidos pedidos) {
        respond pedidos
    }

    def create() {
        respond new Pedidos(params)
    }

}
