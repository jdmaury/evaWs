package wseva

import com.google.gson.JsonObject
import grails.transaction.Transactional
import org.apache.http.HttpEntity
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.ContentType
import org.apache.http.entity.mime.MultipartEntityBuilder
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONObject

import java.text.DateFormat
import java.text.SimpleDateFormat


@Transactional

class GeneralService {

    def enviarArchivoEva(File file, String nombreArchivo, String rutaServerArchivo) {

        String estado=""
        try
        {
            CloseableHttpClient client = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost("${rutaServerArchivo}/api/subirArchivo");

            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addTextBody("application/json", "{'nombre':'Archivo de prueba','crc32':0,'ruta':'${today()}','zip':false}");
            //builder.addTextBody("password", "pass");
            builder.addBinaryBody("file", file,
                    ContentType.APPLICATION_OCTET_STREAM, nombreArchivo);

            HttpEntity multipart = builder.build();
            httpPost.setEntity(multipart);

            CloseableHttpResponse response = client.execute(httpPost);
            //assertThat(response.getStatusLine().getStatusCode(), equalTo(200));
            String respuesta = EntityUtils.toString(response.getEntity());
            JSONObject json=new JSONObject(respuesta)
            println "Se envió el archivo ${nombreArchivo} con la siguiente respuesta del servidor ..." + json.get("codigo")
            log.info("\r\nSe envió el archivo ${nombreArchivo} con la siguiente respuesta del servidor ..." + json.get("codigo")+"\r\n")
            //JSONObject jo=json
            estado=json.get("codigo").toString()
            client.close();

        }catch (Exception ex)
        {
            estado="400"
        }
        return estado
    }

    int traerFactorVenta(String plu)
    {
        def query="Select a.factor_venta from Articulos a Where a.cod_imp='${plu}'"
        int factor=Articulos.executeQuery(query)[0]?:1
        log.info("Factor para el articulo con PLU=${plu} es ${factor}")

        return factor
    }
    int traerUnidadEmpaque(String plu)
    {
        def query="Select a.unid_empaque from Articulos a Where a.cod_imp='${plu}'"
        int unidadEmpaque=Articulos.executeQuery(query)[0]?:1
        log.info("Unidad de empaque para el articulo con PLU=${plu} es ${unidadEmpaque}")

        return unidadEmpaque
    }

    //Pedidos_[codigoTienda]_AAAAMMDD_HHMMss.txt. Ejemplo: Pedidos_119_20170101143105.txt
    def nombrePedidoPdt(String codTienda, String codBloque) {

        DateFormat df = new SimpleDateFormat("yyyyMMdd_HHmmss")
        String nombreArchivom = df.format(new Date()).toString()//20170202_164408
        return "Pedidos_${codTienda}_${codBloque}_${nombreArchivom}"

        //String nombreArchivo="Pedidos_${codTienda}"

    }



    def today() {
        DateFormat df = new SimpleDateFormat("/yyyyMM/dd/")
        return df.format(new Date()).toString()
    }

    def redondear(double decimal) {

        return decimal.round()
    }

    def validarRutaArchivo(File file) {
        if (!file.exists()) //Si el archivo o carpeta no existen
            log.info("Archivo ${file} o carpeta no existen..Verifique...")
        //println "Archivo ${file} o carpeta no existen..Verifique..."

    }


    def crearEstadoConteo(String bloque, int conteo, String usuario) {
        Inv_pdt_estados estados = new Inv_pdt_estados()
        estados.bloque = bloque
        estados.conteo = conteo
        estados.usuario = usuario
        estados.estado = "Bloqueado"
        estados.save(flush: true)
    }


    def validarConteo(String bloque, int conteo,String usuario) {

        String mensaje = ""
        if (conteo < 4 && conteo > 0)
        {
            def existe=Inv_pdt_estados.find{bloque == bloque && conteo == conteo}
            if (existe)//Si el conteo existe
            {
                if(existe.estado=='Cerrado')
                {
                    mensaje = "Conteo ${conteo} para bloque ${bloque} ya se encuentra cerrado. Verifique"
                    return [false, mensaje]
                }
                else
                {
                    if(existe.usuario==usuario)//si el usuario es dueño de ese bloque
                    {
                        if(conteo==3)
                        {
                            mensaje="Inicio de conteo ${conteo} para bloque ${bloque}"
                            def articulosDiferentes=['data':articulosDiferentes2(bloque)[1]]
                            return [true, mensaje,articulosDiferentes]
                        }
                        else
                        {
                            mensaje = "Inicio de conteo ${conteo} para bloque ${bloque}"
                            return [true, mensaje]
                        }
                    }
                    else
                    {
                        mensaje = "El conteo ${conteo} existe y se encuentra ${existe.estado}"
                        return [false, mensaje] //EL CONTEO EXISTE Y SE ENCUENTRA BLOQUEADO
                    }
                }
            } else {
                if (conteo == 3)//SI NO EXISTE HAY QUE PREGUNTAR SI EL CONTEO ES 3
                {
                    def existeConteo1 = Inv_pdt_estados.where { bloque == bloque && conteo == 1 && estado == "Cerrado" }
                    def existeConteo2 = Inv_pdt_estados.where { bloque == bloque && conteo == 2 && estado == "Cerrado" }
                    if (existeConteo1 && existeConteo2) {
                        crearEstadoConteo(bloque, conteo,usuario)
                        def articulosDiferentes=['data':articulosDiferentes2(bloque)[1]]
                        mensaje = "El conteo ${conteo} se creó porque se encontraron los conteos 1 y 2 del bloque ${bloque} cerrados"
                        return [true, mensaje,articulosDiferentes]
                    } else {
                        mensaje = "No se encontraron conteos 1 y 2 cerrados para el bloque ${bloque}. Verifique"
                        return [false, mensaje]
                    }
                } else {
                    mensaje = "Conteo ${conteo} iniciado exitosamente. Estado: Bloqueado"
                    crearEstadoConteo(bloque, conteo,usuario)
                    return [true, mensaje]
                }
            }
        }
        else
        {
            mensaje = "Número de conteo ${conteo} no válido. Verifique"
            return [false, mensaje]
        }
    }



    def puedeCerrarseConteo(String bloque, int conteo,String usuario)
    {
        String mensaje=""
        if (conteo < 4 && conteo > 0)
        {
            def existe=Inv_pdt_estados.find {bloque == bloque && conteo == conteo}
            if(existe)
            {
                if(usuario==existe.usuario)
                {
                    if(existe.estado=="Bloqueado")
                    {
                        //SEGUIR ACA
                        /*if(conteo==3)
                        {
                            def puedeCerrar=validarArticulosConteo3(bloque)
                            if(!puedeCerrar[0])//SI NO PUEDE CERRARSE
                            {
                                mensaje = "Artículo ingresado, pero el bloque ${bloque} aún no puede ser cerrado. Quedan ${puedeCerrar[1]} articulos por cerrar del conteo ${conteo}"

                            }
                            else
                            {
                                //existe.estado="Cerrado"
                                //existe.save(flush:true)
                                mensaje = "Items 1 y 2 todos!! El conteo ${conteo} del bloque ${bloque} ha sido actualizado a Cerrado"
                            }

                        }
                        else
                        {
                            //existe.estado="Cerrado"
                            //existe.save(flush:true)
                            mensaje = "El conteo ${conteo} del bloque ${bloque} ha sido actualizado a Cerrado"

                        }*/
                        mensaje = "Puede ingresar artículos en el conteo ${conteo} del bloque ${bloque}"
                        return [true, mensaje]
                    }
                    else
                    {
                        mensaje = "El conteo ${conteo} del bloque ${bloque} ya se encuentra cerrado"
                        return [false, mensaje]
                    }
                }
                else
                {
                    mensaje = "El conteo ${conteo} del bloque ${bloque} pertenece al usuario ${existe.usuario}."
                    return [false, mensaje]
                }
            }
            else
            {
                mensaje = "El conteo ${conteo} del bloque ${bloque} NO existe. Verifique"
                return [false, mensaje]
            }
        }
        else
        {
            mensaje = "Número de conteo ${conteo} para el bloque ${bloque} no válido. Verifique"
            return [false, mensaje]
        }
    }


    def cerrarConteo(String bloque, int conteo,String usuario)
    {
        String mensaje=""
        if (conteo < 4 && conteo > 0)
        {
            def existe=Inv_pdt_estados.find {bloque == bloque && conteo == conteo}
            if(existe)
            {
                if(usuario==existe.usuario)
                {
                    if(existe.estado=="Bloqueado")
                    {
                        //SEGUIR ACA
                        /*if(conteo==3)
                        {
                            def puedeCerrar=validarArticulosConteo3(bloque)
                            if(!puedeCerrar[0])//SI NO PUEDE CERRARSE
                            {
                                mensaje = "Artículo ingresado, pero el bloque ${bloque} aún no puede ser cerrado. Quedan ${puedeCerrar[1]} articulos por cerrar del conteo ${conteo}"

                            }
                            else
                            {
                                //existe.estado="Cerrado"
                                //existe.save(flush:true)
                                mensaje = "Items 1 y 2 todos!! El conteo ${conteo} del bloque ${bloque} ha sido actualizado a Cerrado"
                            }

                        }
                        else
                        {
                            //existe.estado="Cerrado"
                            //existe.save(flush:true)
                            mensaje = "El conteo ${conteo} del bloque ${bloque} ha sido actualizado a Cerrado"

                        }*/
                        mensaje = "El conteo ${conteo} del bloque ${bloque} ha sido actualizado a Cerrado"
                        return [true, mensaje]
                    }
                    else
                    {
                        mensaje = "El conteo ${conteo} del bloque ${bloque} ya se encuentra cerrado"
                        return [false, mensaje]
                    }
                }
                else
                {
                    mensaje = "El conteo ${conteo} del bloque ${bloque} pertenece al usuario ${existe.usuario}. No puede ser cerrado por usted"
                    return [false, mensaje]
                }
            }
            else
            {
                mensaje = "El conteo ${conteo} del bloque ${bloque} NO existe. Verifique"
                return [false, mensaje]
            }
        }
        else
        {
            mensaje = "Número de conteo ${conteo} para el bloque ${bloque} no válido. Verifique"
            return [false, mensaje]
        }
    }

    def liberarConteo(String bloque, int conteo,String usuario)
    {
        String mensaje=""
        if (conteo < 4 && conteo > 0)
        {
            def existe=Inv_pdt_estados.find {bloque == bloque && conteo == conteo}
            if(existe)
            {
                if(existe.estado=="Bloqueado")
                {
                    //if(usuario==existe.usuario)
                    //{
                        existe.delete(flush:true)
                        mensaje = "El conteo ${conteo} del bloque ${bloque} ha sido liberado. Ya no existe en la BD"
                        return [true, mensaje]
                    //}
                    //else
                    //{
//                        mensaje = "El conteo ${conteo} del bloque ${bloque} NO fue bloqueado por usted. Verifique."
                        //return [false, mensaje]
  //                  }
                }//Si es igual a cerrado
                else
                {
                    mensaje = "El conteo ${conteo} del bloque ${bloque} no puede ser liberado. Ya ha sido confirmado"
                    return [false, mensaje]
                }

            }
            else
            {
                mensaje = "El conteo ${conteo} del bloque ${bloque} NO existe. Verifique"
                return [false, mensaje]
            }

        }
        else
        {
            mensaje = "Número de conteo ${conteo} no válido. Verifique"
            return [false, mensaje]
        }
    }





    def definirEstructura(def request,def codigoLocalidad) {
        def bloques = [:]
        def conteo = [:]
        def articulos = []

        JSONObject peticion = request
        JSONArray inventario = peticion.Inventario
        String usuario = peticion.username
        String codTienda = peticion.codTienda
        String mensaje = ""

        //println "INVENTARIO " + inventario
        println "usuario " + usuario
        println "codTienda " + codTienda
        println "length " + inventario.length()

        def vacio = validarInventarioPeticion(inventario)
        def comentarios = []
        def hayRepetidos=0
        if (!vacio && usuario && codTienda)
        {
            if (vacio || codigoLocalidad.toString() != codTienda)
            {
                if (vacio)//PARA DETECTAR SI TODO EL ARREGLO ESTÁ VACÍO...
                    mensaje = "Inventario vacío. Verifique"
                if (codigoLocalidad.toString() != codTienda)
                    mensaje = mensaje + "...Tiendas no coinciden. "
                return [false, mensaje]
            } else
            {
                inventario.each { inv ->
                    bloques.put(inv.Bloque, [])//INSERTAR LOS BLOQUES
                    conteo.put(inv.Conteo, [])
                    if (inv.Articulo && inv.UM && inv.Unidad && inv.Conteo && inv.Bloque && inv.PLU)
                    {

                        def itemRepetido=itemRepetido(articulos,inv.PLU,inv.Conteo,inv.Bloque)
                        if(!itemRepetido)//Si el elemento no está repetido
                        {
                            articulos.add([Articulo: inv.Articulo, UM: inv.UM, Unidad: inv.Unidad, Conteo: inv.Conteo, Bloque: inv.Bloque, Usuario: usuario, PLU: inv.PLU])
                            comentarios<<["status": "success", "message": "Artículo NO repetido. Puede ser ingresado" + inv]//puedeConfirmar[1]

                        }
                        else
                        {
                            hayRepetidos++
                            comentarios<<["status": "error", "message": "Articulo REPETIDO" + inv]//puedeConfirmar[1]
                        }

                    } else{
                        comentarios<<["status": "error", "message": "Elemento ignorado" + inv]//puedeConfirmar[1]
                    }

                    // println "meti el articulo"+inv
                }


                if(hayRepetidos>0)
                {
                    mensaje="Hay campos repetidos, verifique"
                    return [false,mensaje,comentarios]
                }
                else
                {
                    bloques.each { blq ->
                        def bloqueTemp = [:]
                        conteo.each { cnt ->
                            def temp = []
                            //println "Ahora temp está vacio"
                            articulos.each { art ->
                                if (cnt.getKey() == art.Conteo && art.Bloque == blq.getKey()) {
                                    temp.add(art)
                                    //println "Agregue el articulo "+art+" del bloque "+art.Bloque+" con el conteo "+art.Conteo
                                }
                            }
                            //println "Valor de temp es "+temp+"\n"
                            if (temp)//Por que si temp no existe se crea el array vacío
                            {
                                conteo[cnt.getKey()] = temp
                                bloqueTemp.put(cnt.getKey(), temp)
                            }
                        }

                        bloques[blq.getKey()] = bloqueTemp
                    }
                    //println "la lista de articulos es "+articulos
                    return [true,bloques,comentarios]
                }
                //println "La lista final de artículos es!!!!!!!!!!!!!!"

            }
        }
        else
        {
            mensaje="Verifique que los campos Inventario, username y codTienda existan y tengan valor."
            return [false,mensaje]
        }

    }




    def validarEstructuraPeticion(def estructura, String usuario,String codTienda)
    {


        def bloques=estructura
        def comentarios=[]
        bloques.each{blq->
            def listaItemsCompletos=[]

            JSONObject conteos=new JSONObject()
            conteos=blq.getValue()
            conteos.each {cnt ->

                def listaConteo3=[]
                //println "blq.getKey"+blq.getKey()
                //println "cnt.getKey"+cnt.getKey()

                int conteoActual=Integer.parseInt(cnt.getKey())
                String bloqueActual=blq.getKey()


                def puedeCerrarseConteo=puedeCerrarseConteo(bloqueActual,conteoActual,usuario)//(bloque,conteo,usuario)//--ESTA LINEA
                if(!puedeCerrarseConteo[0])
                {
                    comentarios<<["status":"error","bloque":bloqueActual,"conteo":cnt.getKey(),"mensaje":puedeCerrarseConteo[1]]//puedeConfirmar[1]
                }
                else
                {

                    comentarios<<["status":"success","bloque":bloqueActual,"conteo":cnt.getKey(),"mensaje":puedeCerrarseConteo[1]]//puedeConfirmar[1]


                    //EN ESTE HUECO TENGO QUE INSERTAR LOS ARTÍCULOS
                    def listaArticulos=cnt.getValue()
                    listaArticulos.each {articulo ->

                        if(conteoActual==3)
                        {

                            //SOLO AGREGAR ARTICULOS PERMITIDOS, ESDECIR AQUELLOS QUE TUVIERON DIFERENCIAS EN EL CONTEO 1 Y 2
                            def articulosDiferentes=articulosDiferentes2(articulo.Bloque)[1]//LISTA DE ARTICULOS QUE TUVIERON DIFERENCIA EN ESE BLOQUE
                            def decision=existeItemEnLista(articulosDiferentes,articulo.PLU)//Verifico si cierto PLU se encuentra en una lista
                            if(decision)
                            {
                                listaConteo3.add([Articulo: articulo.Articulo, UM: articulo.UM, Unidad: articulo.Unidad, Conteo: articulo.Conteo, Bloque: articulo.Bloque, Usuario: usuario, PLU: articulo.PLU])
                                comentarios<<["status":"success","mensaje":"EXITO Artículo ${articulo.Articulo} para el bloque ${bloqueActual} conteo ${conteoActual}.está en la lista de diferentes"]//puedeConfirmar[1]
                            }
                            else
                            {
                                println "ARTICULO "+articulo+" no está en la lista de diferencias"
                                comentarios<<["status":"error","mensaje":"No se insertó artículo ${articulo.Articulo} para el bloque ${bloqueActual} conteo ${conteoActual} ya que no está en la lista de diferentes"]//puedeConfirmar[1]
                            }


                        }
                        else
                        {
                            listaItemsCompletos.add([Articulo: articulo.Articulo, UM: articulo.UM, Unidad: articulo.Unidad, Conteo: articulo.Conteo, Bloque: articulo.Bloque, Usuario: usuario, PLU: articulo.PLU])
                            //ingresarArticulo(articulo.Articulo,articulo.UM,articulo.Unidad,articulo.Conteo,articulo.Usuario,articulo.Bloque,articulo.PLU,codTienda)
                        }

                    }
                    //EN ESTE HUECO TENGO QUE INSERTAR LOS ARTÍCULOS


                    def existe=Inv_pdt_estados.find{bloque == bloqueActual && conteo == conteoActual}
                    if(conteoActual==3)
                    {
                        //println "ITEMS CONTEO 3 AL FINALIZAR ESTE BLOQUE ${bloqueActual} "+listaConteo3
                        if(listaConteo3.size()>0)
                        {
                            def a=cerrarConteo3(bloqueActual,listaConteo3,codTienda)
                            comentarios<<a[1]
                        }


                        //println "entre a cerrar conteo"
                    }
                    else
                    {
                        existe.estado="Cerrado"
                        existe.save(flush:true)
                        String mensaje="Conteo ${conteoActual} del bloque ${bloqueActual} cerrado exitosamente"
                        comentarios<<["status":"success","bloque":bloqueActual,"conteo":cnt.getKey(),"mensaje":mensaje]//puedeConfirmar[1]
                    }




                }









                //SE DEBE CERRAR EL CONTEO EN LA ULTIMA LINEA
            }
            //println "Lista de items para bloque ${blq.getKey()} : "+listaItemsCompletos
            if(blq.getKey()=="BL01")
            {
                println "Entraré a comprobar la siguiente lista "+listaItemsCompletos
                def b=verificarItemsCompletosBloque("BL01",listaItemsCompletos,"C02")
                comentarios<<b[1]
            }


        }
        return comentarios
    }



    def validarEstructuraPeticion2(def estructura, String usuario,String codTienda)
    {


        def bloques=estructura
        def comentarios=[]
        bloques.each{blq->
            def listaItemsCompletos=[]

            JSONObject conteos=new JSONObject()
            conteos=blq.getValue()
            conteos.each {cnt ->

                def listaConteo1=[]
                def listaConteo2=[]
                def listaConteo3=[]

                def listaConteo1o2=[]
                //println "blq.getKey"+blq.getKey()
                //println "cnt.getKey"+cnt.getKey()

                int conteoActual=Integer.parseInt(cnt.getKey())
                String bloqueActual=blq.getKey()


                def puedeCerrarseConteo=puedeCerrarseConteo(bloqueActual,conteoActual,usuario)//(bloque,conteo,usuario)//--ESTA LINEA
                if(!puedeCerrarseConteo[0])
                {
                    comentarios<<["status":"error","bloque":bloqueActual,"conteo":cnt.getKey(),"mensaje":puedeCerrarseConteo[1]]//puedeConfirmar[1]
                }
                else
                {

                    comentarios<<["status":"success","bloque":bloqueActual,"conteo":cnt.getKey(),"mensaje":puedeCerrarseConteo[1]]//puedeConfirmar[1]


                    //EN ESTE HUECO TENGO QUE INSERTAR LOS ARTÍCULOS
                    def listaArticulos=cnt.getValue()
                    listaArticulos.each {articulo ->

                        if(conteoActual==3)
                        {
                            def articulosDiferentes=articulosDiferentes2(articulo.Bloque)//LISTA DE ARTICULOS QUE TUVIERON DIFERENCIA EN ESE BLOQUE
                            println "RESULTADOOO  erentes salida ${conteoActual} bloque ${articulo.Bloque}....."+articulosDiferentes
                            if(articulosDiferentes[0])//SI HAY CONTEO 1 Y 2 CERRADOS PARA EL BLOQUE, SE RETORNA LA LISTA DE ARTICULOS DIFERENTES
                            {

                                println "ArticuloarticulosDiferentesactual es "+articulosDiferentes
/*el error está en esta linea*/ def decision=existeItemEnLista(articulosDiferentes[1],articulo.PLU)//Verifico si cierto PLU se encuentra en una lista
                                if(decision)
                                {

                                    listaConteo3.add([Articulo: articulo.Articulo, UM: articulo.UM, Unidad: articulo.Unidad, Conteo: articulo.Conteo, Bloque: articulo.Bloque, Usuario: usuario, PLU: articulo.PLU])
                                    comentarios<<["status":"success","mensaje":"EXITO Artículo ${articulo.Articulo} para el bloque ${bloqueActual} conteo ${conteoActual}.está en la lista de diferentes"]//puedeConfirmar[1]
                                }
                                else
                                {
                                    println "ARTICULO "+articulo+" no está en la lista de diferencias"
                                    comentarios<<["status":"error","mensaje":"No se insertó artículo ${articulo.Articulo} para el bloque ${bloqueActual} conteo ${conteoActual} ya que no está en la lista de diferentes"]//puedeConfirmar[1]
                                }
                            }
                            else
                            {
                                comentarios<<["status":"error","mensaje":articulosDiferentes[1]]//puedeConfirmar[1]
                                //comentarios<<articulosDiferentes[1]
                            }

                        }
                        else
                        {
                            /*if(conteoActual==1)
                                listaConteo1.add([Articulo: articulo.Articulo, UM: articulo.UM, Unidad: articulo.Unidad, Conteo: articulo.Conteo, Bloque: articulo.Bloque, Usuario: usuario, PLU: articulo.PLU])
                            if(conteoActual==2)
                                listaConteo2.add([Articulo: articulo.Articulo, UM: articulo.UM, Unidad: articulo.Unidad, Conteo: articulo.Conteo, Bloque: articulo.Bloque, Usuario: usuario, PLU: articulo.PLU])*/

                            if(Integer.parseInt(articulo.Conteo)==conteoActual)
                                listaConteo1o2.add([Articulo: articulo.Articulo, UM: articulo.UM, Unidad: articulo.Unidad, Conteo: articulo.Conteo, Bloque: articulo.Bloque, Usuario: usuario, PLU: articulo.PLU])
                        }

                    }
                    //EN ESTE HUECO TENGO QUE INSERTAR LOS ARTÍCULOS

                    println "ConteoActualANTESDENTRAR"+conteoActual
                    if((blq.getKey()=="BL01" || blq.getKey()=="BL02") && (conteoActual==1 || conteoActual==2))
                    {
                        //println "Entraré a comprobar la siguiente lista "+listaItemsCompletos
                        def b=verificarItemsCompletosBloque(blq.getKey(),listaConteo1o2,conteoActual,"C02")


                        if(b[0])
                        {
                            comentarios<<b[1]
                            def existe=Inv_pdt_estados.find{bloque == bloqueActual && conteo == conteoActual}
                            existe.estado="Cerrado"
                            existe.save(flush:true)
                            comentarios<<["status":"success","mensaje":"CERRADOOOOAGAIN. Bloque ${bloqueActual} conteo ${conteoActual} PASÓ A CERRADO  exitosamente"]//puedeConfirmar[1]
                        }
                        else
                        {
                            comentarios<<b[1]
                            comentarios<<["status":"error","mensaje":"NOPUEDESERCERRADO Bloque ${bloqueActual} conteo ${conteoActual}ARTÍCULOS INCOMPLETOS. VERIFIQUE "]//puedeConfirmar[1]
                        }


                    }






                }








            }

        }
        return comentarios
    }


    def formatearPeticion(def request, def codigoLocalidad)
    {
        def bloques=[:]
        def conteo=[:]
        def articulos=[]

        JSONObject peticion=request
        JSONArray inventario=peticion.Inventario
        String usuario=peticion.username
        String codTienda=peticion.codTienda
        String mensaje=""

        //println "INVENTARIO "+inventario
        println "usuario "+usuario
        println "codTienda "+codTienda
        println "length "+inventario.length()

        def vacio=validarInventarioPeticion(inventario)
        def comentarios=[]
        if(!vacio && usuario && codTienda)
        {
            if(vacio || codigoLocalidad.toString()!=codTienda)
            {
                if(vacio)//PARA DETECTAR SI TODO EL ARREGLO ESTÁ VACÍO...
                    mensaje="Inventario vacío. Verifique"
                if(codigoLocalidad.toString()!=codTienda)
                    mensaje=mensaje+"...Tiendas no coinciden. "
                return [false,mensaje]
            }
            else
            {
                inventario.each {inv->
                    bloques.put(inv.Bloque,[])//INSERTAR LOS BLOQUES
                    conteo.put(inv.Conteo,[])
                    if(inv.Articulo && inv.UM && inv.Unidad && inv.Conteo && inv.Bloque && inv.PLU)
                    {
                        articulos.add([Articulo:inv.Articulo,UM:inv.UM,Unidad:inv.Unidad,Conteo:inv.Conteo,Bloque:inv.Bloque,Usuario:usuario,PLU:inv.PLU])
                    }
                    else
                        comentarios<<["status":"error","mensaje":"Elemento ignorado"+inv]//puedeConfirmar[1]

                    //println "meti el articulo"+inv
                }

                bloques.each {blq->
                    def bloqueTemp=[:]
                    conteo.each {cnt->
                        def temp=[]
                        //println "Ahora temp está vacio"
                        articulos.each {art->
                            if(cnt.getKey()==art.Conteo && art.Bloque==blq.getKey())
                            {
                                temp.add(art)
                                //println "Agregue el articulo "+art+" del bloque "+art.Bloque+" con el conteo "+art.Conteo
                            }
                        }
                        //println "Valor de temp es "+temp+"\n"
                        if(temp)//Por que si temp no existe se crea el array vacío
                        {
                            conteo[cnt.getKey()]=temp
                            bloqueTemp.put(cnt.getKey(),temp)
                        }
                    }

                    bloques[blq.getKey()]=bloqueTemp
                }



                //AHORA PROCEDEMOS A INSERTAR EL ARTICULO EN NUESTRA BD...RECORRIENDO LA ESTRUCTURA E INGRESANDO SOLO
                //LOS DATOS VÁLIDOS..

                //AQUI VAMOS A INSERTAR LOS ARTICULOS EN LA TABLA SIEMPRE Y CUANDO PASE LA VALIDACION DEL BLOQUE



                bloques.each {blq->


                    JSONObject conteos=new JSONObject()
                    conteos=blq.getValue()
                    conteos.each {cnt->

//-voy por acá

                        println "blq.getKey"+blq.getKey()
                        println "usuario"+usuario
                        println "cnt.getKey"+cnt.getKey()
                        def puedeConfirmar=cerrarConteo(blq.getKey().toString(),Integer.parseInt(cnt.getKey()),usuario)//(bloque,conteo,usuario)//--ESTA LINEA

                        //println "JUAS TAG "+puedeConfirmar
                        if(!puedeConfirmar[0])
                            comentarios<<["status":"error","bloque":blq.getKey(),"conteo":cnt.getKey(),"mensaje":puedeConfirmar[1]]//puedeConfirmar[1]
                        else//si existen conteos 1 y 2 cerrados para ese bloque
                        {
                            comentarios<<["status":"success","bloque":blq.getKey(),"conteo":cnt.getKey(),"mensaje":puedeConfirmar[1]]//puedeConfirmar[1]
                            def listaArticulos=cnt.getValue()
                            listaArticulos.each {articulo->
                                println "Se ingresó el artículo... "+articulo
                                def query="Select inv from Inv_pdt_articulos as inv where inv.bloque='${articulo.Bloque}' and inv.conteo='${articulo.Conteo}'"
                                def listaArt=Inv_pdt_articulos.executeQuery(query)
                                def existeListaFinal=existeItemEnLista(listaArt,articulo.PLU)

                                if(articulo.Conteo=="3")//creo que inserta repetidos en conteo 3
                                {
                                    def articulosDiferentes=articulosDiferentes2(articulo.Bloque)[1]//LISTA DE ARTICULOS QUE TUVIERON DIFERENCIA EN ESE BLOQUE
                                    def decision=existeItemEnLista(articulosDiferentes,articulo.PLU)//Verifico si cierto PLU se encuentra en una lista
                                    //en este caso verifico si el PLU se encuentra dentro de la lista de articulos que tuvieron diferencia


                                    if(decision)//si el articulo existe dentro de los que tuvieron diferencia..se puede ingresar
                                    {
                                        if(!existeListaFinal)
                                        {
                                            ingresarArticulo(articulo.Articulo,articulo.UM,articulo.Unidad,articulo.Conteo,articulo.Usuario,articulo.Bloque,articulo.PLU,codTienda)
                                            comentarios<<["status":"success","bloque":blq.getKey(),"conteo":cnt.getKey(),"mensaje":"Artículo ${articulo.Articulo} ingresado porque tuvo diferencias en el bloque ${articulo.Bloque}"]
                                        }
                                        else
                                        {
                                            comentarios<<["status":"error","bloque":blq.getKey(),"conteo":cnt.getKey(),"mensaje":"DUPLICADO Artículo ${articulo.Articulo} DUPLICADO bloque ${articulo.Bloque} para conteo 3"]
                                        }


                                    }
                                    else
                                        comentarios<<["status":"error","bloque":blq.getKey(),"conteo":cnt.getKey(),"mensaje":"Artículo ${articulo.Articulo} no presentó diferencias en el bloque ${articulo.Bloque}"]
                                }
                                else
                                {
                                    //Esta es para validar que no se ingrese el mismo articulo con el mismo bloque y el mismo conteo...


                                    println "ENTRE ACÁ JEJEJEJEJEJEJEJ"

                                    if(!existeListaFinal)//En este caso si el articulo no existe en la lista de articulos final se inserta
                                    {
                                        println "LISTA ARTICULOS BLOQUE ${articulo.Bloque} ---------------------- "+listaArt
                                        ingresarArticulo(articulo.Articulo,articulo.UM,articulo.Unidad,articulo.Conteo,articulo.Usuario,articulo.Bloque,articulo.PLU,codTienda)
                                    }
                                    else
                                        comentarios<<["status":"error","bloque":blq.getKey(),"conteo":cnt.getKey(),"mensaje":"DUPLICADO Artículo ${articulo.Articulo} DUPLICADO bloque ${articulo.Bloque}"]
                                }
                            }
                        }
                    }
                }
                return [true,bloques,comentarios]
            }
        }
        else
        {
            mensaje="Verifique que los campos Inventario, username y codTienda existan y tengan valor"
            return [false,mensaje]
        }
    }
    def articulosDiferentes2(String bloque)
    {
        def existeConteo1 = Inv_pdt_estados.where { bloque == bloque && conteo == 1 && estado == "Cerrado" }
        def existeConteo2 = Inv_pdt_estados.where { bloque == bloque && conteo == 2 && estado == "Cerrado" }
        if (existeConteo1 && existeConteo2)
        {
            def salida=[]
            def query="Select inv from Inv_pdt_articulos as inv"
            def lista=Inv_pdt_articulos.executeQuery(query)
            lista.each {art->
                def artConteo1=Inv_pdt_articulos.where {bloque==bloque && conteo==1 && plu=="${art.plu}"}.toList()[0]
                def artConteo2=Inv_pdt_articulos.where {bloque==bloque && conteo==2 && plu=="${art.plu}"}.toList()[0]

                def articulosYaExisteEnLista=existeItemEnLista(salida,art.plu)//Buscamos el PLU en la lista salida que estamos armando

                if(!articulosYaExisteEnLista)
                {
                    def listaArt=[]
                    if(artConteo1 && artConteo2)
                    {
                        if(artConteo1.umEmpaque!=artConteo2.umEmpaque || artConteo1.umUnidad!=artConteo2.umUnidad)
                        {
                            listaArt=["Articulo":artConteo1.articulo,"UM1":artConteo1.umEmpaque,"UM2":artConteo2.umEmpaque,"UE1":artConteo1.umUnidad,"UE2":artConteo2.umUnidad,"plu":artConteo2.plu]
                            salida<<listaArt
                        }
                    }
                    else
                    {
                        if(artConteo1 && !artConteo2)
                        {
                            listaArt=["Articulo":artConteo1.articulo,"UM1":artConteo1.umEmpaque,"UM2":"null","UE1":artConteo1.umUnidad,"UE2":"null","plu":artConteo1.plu]
                            salida<<listaArt
                        }
                        else
                        {
                            if(!artConteo1 && artConteo2)
                            {
                                listaArt=["Articulo":artConteo2.articulo,"UM1":"null","UM2":artConteo2.umEmpaque,"UE1":"null","UE2":artConteo2.umUnidad,"plu":artConteo2.plu]
                                salida<<listaArt
                            }
                        }
                    }
                }
                //println "ArtConteo1"+artConteo1
                //println "ArtConteo2"+artConteo2
                //println "----------------------------------------------"
            }
            return [true,salida]
        }
        else
        {
            String mensaje="No existe conteo 1 y 2 cerrados para este bloque. Verifique"
            return [false,mensaje]
        }
    }

    def articulosDiferentes(String bloque)
    {
        //Verificamos que existan conteo 1 y 2 cerrados..para ese bloque
        def existeConteo1 = Inv_pdt_estados.where { bloque == bloque && conteo == 1 && estado == "Cerrado" }
        def existeConteo2 = Inv_pdt_estados.where { bloque == bloque && conteo == 2 && estado == "Cerrado" }
        if (existeConteo1 && existeConteo2)
        {
            def salida=[]
            def query1="Select inv from Inv_pdt_articulos as inv where inv.bloque='${bloque}' and inv.conteo=1"
            def query2="Select inv from Inv_pdt_articulos as inv where inv.bloque='${bloque}' and inv.conteo=2" //Articulos que estuvieron en ambos conteos
            def listaConteo1=Inv_pdt_articulos.executeQuery(query1)
            def listaConteo2=Inv_pdt_articulos.executeQuery(query2)
            listaConteo1.each {artConteo1->
                listaConteo2.each {artConteo2->
                    if(artConteo1.plu==artConteo2.plu)
                    {
                        if(artConteo1.umEmpaque!=artConteo2.umEmpaque || artConteo1.umUnidad!=artConteo2.umUnidad)
                        {
                            def listaArt=[]
                            listaArt=["Articulo":artConteo1.articulo,"UM1":artConteo1.umEmpaque,"UM2":artConteo2.umEmpaque,"UE1":artConteo1.umUnidad,"UE2":artConteo2.umUnidad,"plu":artConteo2.plu]
                            salida<<listaArt
                        }
                    }
                }
            }
            return [true,salida]
        }
        else
        {
            String mensaje="No existe conteo 1 y 2 cerrados para este bloque. Verifique"
            return [false,mensaje]
        }

          /*def listaArt=[]
          def artConteo1=Inv_pdt_articulos.where {plu==plu && bloque==bloque && conteo==1}.toList()[0]
          def artConteo2=Inv_pdt_articulos.where {plu==plu && bloque==bloque && conteo==2}.toList()[0]
          if(artConteo1 && artConteo2)
          {
              if(artConteo1.umEmpaque!=artConteo2.umEmpaque || artConteo1.umUnidad!=artConteo2.umUnidad)
                  listaArt=["Articulo":artConteo1.articulo,"UM1":artConteo1.umEmpaque,"UM2":artConteo2.umEmpaque,"UE1":artConteo1.umUnidad,"UE2":artConteo2.umUnidad]
          }
        return listaArt*/
   }

    def existeItemEnLista(def lista, String plu)//Para validar que el artículo está en la lista de items que tuvieron diferencia
    {

        def existe=false
        lista.find{item->

            if(item.plu==plu)
            {
                println "item.plu ${item.plu} es igual a  "+plu
                existe=true
                return true
            }
        }
        return existe
    }

    def existeItemEnLista2(def lista, String plu)//Para validar que el artículo está en la lista de items que tuvieron diferencia
    {//TEMPORAL
        def existe=false
        println "LISTA EXISTE ITEM EN LISTA 2 "+lista
        lista.find{item->

            if(item.PLU==plu)
            {
                println "item.PLU ${item.PLU} es igual a  "+plu
                existe=true
                return true
            }
        }
        return existe
    }


    def archivosPorBloque(def request)
    {
        def pedidos=request.pedidos
        def codigoTienda=request.codTienda

        //println "JUAS JUAS JAUS "+codigoTienda
        //def nombrePedidoPdt(String codTienda, String codBloque) {
        def listaBloques=[:]
        pedidos.eachWithIndex{ped,ind->
            if(!listaBloques.containsKey(ped.Bloque))//SI NO CONTIENE LA LLAVE LA AGREGAMOS
            {
                String nombreArchivoBloque=nombrePedidoPdt(codigoTienda,ped.Bloque)//File file11=new File("C:/PDT/Pedido/Sincronizado/${nombreArchivo}.txt")
                listaBloques.put(ped.Bloque,nombreArchivoBloque)
            }

        }

        return listaBloques

    }

    def getEstadoConteo(String bloque, int conteo) {
        def existe=Inv_pdt_estados.find {bloque == bloque && conteo == conteo}
        String mensaje=""
        String estado=""
        String usuario=""
        if(existe)
        {
            usuario=existe.usuario
            estado=existe.estado
            mensaje = "Conteo ${conteo} para el bloque ${bloque} se encuentra en estado ${estado}"
            return [true, mensaje, estado,usuario]
        }
        else
        {
            mensaje = "Bloque o conteo no existen. Verifique"
            return [false, mensaje]
        }

    }

    def conteosPorBloque(String bloque, int tipo) {
        def query=""
        if(tipo==0)
            query = "Select inv From Inv_pdt_estados as inv Where inv.bloque='${bloque}'"
        else
            query = "Select inv From Inv_pdt_estados as inv order by inv.bloque asc"
        println query
        def lista = Inv_pdt_estados.executeQuery(query)
        println lista
        def salida=[]
        if(lista)
        {

                lista.each {
                    def resultado=["Bloque":it.bloque,"Conteo":it.conteo,"Estado":it.estado,"Usuario":it.usuario]
                    salida<<resultado
                }
                return [true, salida]

        }
        else
        {
            return [false, salida]
        }
    }

    def itemRepetido(def lista,String plu, String conteo, String bloque)
    {
        def existe=false
        //println "La lista ahora va ${lista}"
        lista.find{item->
            //println "entre a comparar el siguiente item ${item} con los valores PLU=${plu}, conteo=${conteo} bloque=${bloque}"
            if(item.PLU==plu && item.Conteo==conteo && item.Bloque==bloque)
            {
                //println "Pude entrar y el elemento está repetido"
                existe=true
                return true
            }
        }
        return existe
    }

    def ingresarArticulo(String articulo,String UM,String Unidad,String Conteo,String Usuario,String Bloque,String PLU,String codTienda)
    {
        Inv_pdt_articulos articuloPdt=new Inv_pdt_articulos()
        articuloPdt.articulo=articulo
        articuloPdt.umEmpaque=Integer.parseInt(UM)
        articuloPdt.umUnidad=Integer.parseInt(Unidad)
        articuloPdt.conteo=Integer.parseInt(Conteo)
        articuloPdt.usuario=Usuario
        articuloPdt.tienda=codTienda
        articuloPdt.bloque=Bloque
        articuloPdt.plu=PLU
        articuloPdt.save(flush:true)
    }

    def validarInventarioPeticion(def inventario)//para validar que el inventario no se mande vacío al momento de hacer el post
    {
        def vacio=0
        inventario.each{
            //if(it.size()==0)
            //Articulo:inv.Articulo,UM:inv.UM,Unidad:inv.Unidad,Conteo:inv.Conteo,Bloque:inv.Bloque,Usuario:usuario,PLU:inv.PLU])
            if(!(it.Articulo && it.UM && it.Unidad && it.Conteo && it.Bloque && it.PLU))
                vacio++
        }
        println "INVENTARIO SIZE "+inventario.size()
        println "VACIO "+vacio
        if(vacio==inventario.size())
            return true
        else
            return false
    }

    def cerrarTodo()//todos los bloques deben estar cerrados
    {
        def query="Select inv from Inv_pdt_estados as inv"
        def lista=Inv_pdt_estados.executeQuery(query)
        def listaPendientes=[]

        lista.each {art->


            def existeConteo1 = Inv_pdt_estados.where { bloque == art.bloque && conteo == 1 && estado == "Cerrado" }.toList()[0]
            def existeConteo2 = Inv_pdt_estados.where { bloque == art.bloque && conteo == 2 && estado == "Cerrado" }.toList()[0]
            def existeConteo3 = Inv_pdt_estados.where { bloque == art.bloque && conteo == 3 && estado == "Cerrado" }.toList()[0]

            println "Bloque "+art.bloque
            println "E1"+existeConteo1
            println "E2"+existeConteo2
            println "E3"+existeConteo3
            if(!(existeConteo1 && existeConteo2 && existeConteo3))
            {
                println "Falta alguno del bloque ${art.bloque}"
                if(!(listaPendientes.contains(art.bloque)))
                    listaPendientes.add(art.bloque)
            }

        }

        if(listaPendientes.size()>0)
            return [false,listaPendientes]
        else
        {
            if(listaPendientes.size()==0)
            {
                return [true,"Todos los bloques poseen sus respectivos 3 conteos y este inventario puede ser confirmado"]

                //[código de tienda],[usuario],[Fecha del inventario],[código del producto],[cant. en unidades.,[cant. por empaque],[bloque]
            }

        }


    }

    def validarArticulosConteo3(String bloque)
    {
        //Esto es para validar que todos los artículos que presentaron diferencia en los conteos 1 y 2 de cierto Bloque, deben aparecer con conteo 3 obligatoriamente.
        def listaArticulosDiferentes=articulosDiferentes2(bloque)[1]
        def articulosPendientesPorIngresar=0
        println "LISTA ARTICULOS DIFERENTES ES "+listaArticulosDiferentes

        listaArticulosDiferentes.each {art->
            println "Holi "+art
            def artConteo3=Inv_pdt_articulos.where {plu==art.plu && bloque==bloque && conteo==3}.toList()[0]
            if(!artConteo3)
            {
                println "No existe artículo:${art.Articulo} con conteo 3 para el bloque ${bloque}"
                articulosPendientesPorIngresar++
                println "SUBTOTAL-------------ArticulosPendientesPorIngresar "+articulosPendientesPorIngresar
            }

        }

        println "TOTAL-------ArticulosPendientesPorIngresar "+articulosPendientesPorIngresar

        if(articulosPendientesPorIngresar>0)
            return [false,articulosPendientesPorIngresar]
        else{
            if(articulosPendientesPorIngresar==0)
                return [true,articulosPendientesPorIngresar]
            else
                return [false,articulosPendientesPorIngresar]
        }
    }

    def cerrarConteoSi(String bloque, int conteo)
    {
        //ACA TOCA VALIDAR CUANDO EL CONTEO ES 3
        String mensaje=""
        def existe=Inv_pdt_estados.find{bloque == bloque && conteo == conteo}
        def bool=false
        if(existe)
        {
            existe.estado="Cerrado"
            existe.save(flush:true)
            mensaje="Conteo ${conteo}  del bloque ${bloque} cerrado exitosamente"
            bool=true
        }
        else
        {
            mensaje = "El conteo ${conteo} del bloque ${bloque} NO existe. Verifique"
            bool=false

        }
        return [bool,mensaje]
    }


    def getArticulosPorBloque(String bloque)
    {
        def salida=[]
        //def query="Select art.cod_imp From Articulos as art Where art.cod_gondola='${bloque}' and art.pre_venta1>150"
        def query="from Articulos as art Where art.cod_gondola=?"
        def lista=Articulos.findAll(query,["${bloque}"],[max:3])
        lista.each {it->
            salida.add(["plu":it.cod_imp])
        }

        return salida
    }

    def verificarItemsCompletosBloque(String bloque, def listaItems,int conteo,String codigoTienda)
    {
        println "listaItems "+listaItems

        def itemsPorBloque=getArticulosPorBloque(bloque)
        println "\nLista items por bloque GENERAL es "+itemsPorBloque+"\n"
        def articulosEnAmbasListas=0
        def comentarios=[]



        listaItems.each{itemsAComprobar->
            if(existeItemEnLista(itemsPorBloque,itemsAComprobar.PLU))
            {

                articulosEnAmbasListas++
            }
        }

        println "Items en ambas listas "+articulosEnAmbasListas

        if(articulosEnAmbasListas==itemsPorBloque.size())
        {
            def listaConteos=[]
            listaItems.each{articulo->

                ingresarArticulo(articulo.Articulo,articulo.UM,articulo.Unidad,conteo.toString(),articulo.Usuario,articulo.Bloque,articulo.PLU,codigoTienda)
                comentarios<<["status":"success","mensaje":"NUEVO Articulo ${articulo.Articulo} para conteo ${conteo} bloque ${articulo.Bloque} ingresado exitosamente"]//puedeConfirmar[1]


            }
            def existe=Inv_pdt_estados.find{bloque == bloque && conteo == conteo}
            existe.estado="Cerrado"
            existe.save(flush:true)
            comentarios<<["status":"success","mensaje":"ARTICULOS COMPLETOS PARA BLOQUE ${bloque} CONTEO ${conteo}"]//puedeConfirmar[1]


            return [true,comentarios]
        }
        else
        {
            println "No hay articulos conteos porque faltan items......"
            def itemsFaltan=itemsPorBloque.size()-articulosEnAmbasListas
            comentarios<<["status":"error","mensaje":"FALTAN ${itemsFaltan} ARTICULOS COMPLETOS PARA BLOQUE ${bloque} CONTEO ${conteo}"]//puedeConfirmar[1]
            return [false,comentarios]
        }
    }


    def cerrarConteo3(String bloque, def listaConteo3, String codigoTienda)//Comparamos la lista de conteo 3 con la lista de los elementos que tuvieron diferencia de ese conteo
    {
        //ingresarArticulo(articulo.Articulo,articulo.UM,articulo.Unidad,articulo.Conteo,articulo.Usuario,articulo.Bloque,articulo.PLU,"C02")
        //ITEMS CONTEO 3 AL FINALIZAR ESTE BLOQUE BL02 [[Articulo:Atun, UM:8, Unidad:3, Conteo:3, Bloque:BL02, Usuario:lvesga, PLU:00002]]
        def itemsDiferentes=articulosDiferentes2(bloque)[1]
        def articulosEnAmbasListas=0

        def comentarios=[]
        //def existeItemEnLista(def lista, String plu)//Para validar que el artículo está en la lista de items que tuvieron diferencia

        itemsDiferentes.each {item->
            println "ITEMGUEPTONS "+item
            if(existeItemEnLista2(listaConteo3,item.plu))
                articulosEnAmbasListas++
        }

        println "items diferentes size="+itemsDiferentes.size()

        if(articulosEnAmbasListas==itemsDiferentes.size())
        {
            println "hola guapo entré acá"
            listaConteo3.each{articulo->
                println "Articulooooooooo "+articulo
                ingresarArticulo(articulo.Articulo,articulo.UM,articulo.Unidad,articulo.Conteo,articulo.Usuario,articulo.Bloque,articulo.PLU,codigoTienda)
                comentarios<<["status":"success","mensaje":"NUEVO Articulo ${articulo.Articulo} para conteo ${articulo.Conteo} bloque ${articulo.Bloque} ingresado exitosamente"]//puedeConfirmar[1]
            }
            def existe=Inv_pdt_estados.find{bloque == bloque && conteo == 3}
            existe.estado="Cerrado"
            existe.save(flush:true)
            comentarios<<["status":"success","mensaje":"NUEVO. Bloque ${bloque} conteo 3 PASÓ A CERRADO  exitosamente"]//puedeConfirmar[1]

            return [true,comentarios]

        }
        else{
            println "entre aca no se puede cerrar"
            comentarios<<["status":"error","mensaje":"NUEVO .Bloque ${bloque} conteo 3 AÚN NO PUEDE SER CERRADO. FALTAN ITEMS"]//puedeConfirmar[1]
            return [false,comentarios]
        }

        //println "ARTICULOS EN AMBAS LISTAS "+articulosEnAmbasListas

    }

    def conteosACerrar(String bloque,def listaConteos)
    {
        def comentarios=[]
        listaConteos.each{conteoActual->
            def existe=Inv_pdt_estados.find{bloque == bloque && conteo == conteoActual}
            if(existe)
            {
                existe.estado="Cerrado"
                existe.save(flush:true)
                comentarios<<["status":"success","mensaje":"BOMBASTIC. Bloque ${bloque} conteo ${conteoActual} PASÓ A CERRADO  exitosamente"]//puedeConfirmar[1]
            }

        }

        return comentarios
    }





}