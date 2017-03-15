package wseva

import grails.converters.JSON
import groovy.sql.Sql
import groovy.util.logging.Slf4j

import static org.springframework.http.HttpStatus.*
import grails.transaction.Transactional
import groovy.json.JsonSlurper

@Transactional(readOnly = true)
class ArticulosController {
    def sessionFactory
    def generalService
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    def lista()
    {
        def aja =generalService.cerrarConteo3("BL01",[])
    }

    def plm()
    {
        def a=generalService.getArticulosPorBloque("BL01")
        render a as JSON
    }


    def estructura()
    {
        try{
            def peticion=request.JSON
            String usuario=peticion.username
            String codigoTienda=peticion.codTienda
            println "usuario esD "+usuario
            def codigoLocalidad=grailsApplication.config.getProperty('localidad.codigo.localidad')
            def resultado=generalService.definirEstructura(peticion,codigoLocalidad)      //
            if(resultado[0])//Si no hay artículos repetidos, la lista de bloques puede mandarse
            {
                def listaEstructura=resultado[1]
                def validarEstructuraPeticion=generalService.validarEstructuraPeticion2(listaEstructura,usuario,codigoTienda)

                response.status = 200
                render(['status': 'success', /*'data': listaEstructura,*/'message':validarEstructuraPeticion,/*resultado:resultado[2]*/] as JSON)
            }
            else
            {
                response.status = 200
                render(['status': 'error', 'data': [],'message':'Articulos repetidos',/*'comentarios':resultado[2]*/] as JSON)
            }


        }catch (JSONException){
            response.status = 500
            render(['status': 'fail', 'data':[],'message':"Ha ocurrido un error en el servidor"] as JSON)
        }




    }


    def pp()
    {
        render generalService.cerrarTodo()
    }

    def mm()
    {
        def cerrarConteo3=generalService.validarArticulosConteo3("BL03")
        render cerrarConteo3
    }

    def a()
    {
        log.info("PRUEBA ARTICULOS LOG")

        def articulosDiferentes=generalService.articulosDiferentes2("BL01")
        if(articulosDiferentes[0])
        {
            def lista=articulosDiferentes[1]
            //def decision=generalService.existeItemEnLista(lista,"000088")
            //println "La decision es "+decision


            render (['status':'success','data':articulosDiferentes[1]]as JSON)
        }
        else
            render (['data':[],'message':articulosDiferentes[1]]as JSON)

    }


    /*def index()
    {
        def inputFile = new File("C:/Eva/articulos.json")
        def InputJSON = new JsonSlurper().parseText(inputFile.text)
        render InputJSON as JSON
    }*/

    def index() {
        try{
            if (request.method == 'GET')
            {

                def salida=[]
                def sql = new Sql(sessionFactory.currentSession.connection())
                sql.eachRow("""
with
-- Temporal 1
temp as (select A.cod_imp Articulo, A.descrip1 Descripcion, A.descodificado Descodificado,
                    g.cod_gondola CodBloque, g.descrip as DescripBloque, A.factor_orden as FactorOrden, A.unid_empaque as Factor,
                    A.unid_med as UnidadEmpPedido, A.cod_unid_med_fleje as UnidadEmpInventario,       (sum(V.cantidad)/28) as Promedio
             from articulo A
             inner join ventas_articulo V on V.id_articulo = A.id_articulo
             inner join gondola g on g.id_gondola=A.id_gondola
             where v.date_created between DATEADD(day,-29,getdate()) and DATEADD(day,-1,getdate())
             group by A.cod_imp, A.descrip1, A.unid_med, g.cod_gondola, g.descrip, A.unid_empaque, A.cod_unid_med_fleje, pre_venta1, pre_venta_ant1, A.factor_orden, A.descodificado),
 
--Temporal 2
temp2 as (Select distinct a.cod_imp Articulo, a.descrip1 Descripcion, a.descodificado Descodificado,
                                  gd.cod_gondola CodBloque, gd.descrip as DescripBloque, a.factor_orden as FactorOrden, a.unid_empaque as Factor,
                                  a.unid_med as UnidadEmpPedido, a.cod_unid_med_fleje UnidadEmpInventario,
                                  g2.descrip Familia, a.pre_venta1 as PrecioVenta, pre_venta_ant1 as PrecioVentaAnt,
             substring((Select ','+ac1.cod_articulo  AS [text()] From articulo_cod ac1 Where ac1.cod_imp = ac2.cod_imp For XML PATH ('')), 2, 1000) [CodigosDeBarra]
             From articulo a
             left join articulo_cod ac2 on a.cod_imp = ac2.cod_imp
             left join grupo g on A.id_grupo = g.id_grupo
             left join grupo g2 on g2.cod_grupo = g.cod_grupo and g2.subgrupo1 = '00'
                    left join gondola gd on gd.id_gondola=a.id_gondola
             )
-- Consulta 
             select temp2.Articulo, temp2.Descripcion, temp2.Descodificado, temp2.FactorOrden,
                    temp2.CodBloque, temp2.DescripBloque, temp2.Factor, temp2.UnidadEmpPedido, temp2.UnidadEmpInventario,
                    Familia, [CodigosDeBarra], temp2.PrecioVenta, temp2.PrecioVentaAnt,
             case when promedio is null then 0 else promedio end Promedio
             from temp2
             left join temp on temp2.Articulo = temp.Articulo
             group by temp2.Articulo, temp2.Descripcion, temp2.Descodificado, Familia, [CodigosDeBarra],
                     temp2.CodBloque, temp2.DescripBloque, temp2.FactorOrden, temp2.Factor, temp2.UnidadEmpPedido, temp2.UnidadEmpInventario, temp2.PrecioVenta, temp2.PrecioVentaAnt, temp.Promedio
                        """){row->

                    //render row

                    def resultado=["Articulo":row.Articulo,"Descripcion":row.Descripcion,"Bloque":row.CodBloque?:"BL00","DescripcionBloque":row.DescripBloque?:"Sin Bloque","Subcategoria":row.Familia,
                                   "CantidadSugerida":generalService.redondear(row.Promedio),"CodigoBarra":row.CodigosDeBarra,"PreVentaAnt":row.PrecioVentaAnt?:0,"PreVenta":row.PrecioVenta?:0,"Descodificado":row.Descodificado,
                                   "Factor":row.Factor?:1,"UnidadEmpPedido":row.UnidadEmpPedido,"UnidadEmpInventario":row.UnidadEmpInventario?:"","FactorOrden":row.FactorOrden?:1]
                    salida<<resultado
                }

                log.info("Enviando lista de artículos a PDT...")
                response.status=200
                render ([status:'success',data:salida]as JSON)
            }
            else
            {
                log.info("Solo se admiten peticiones vía GET...")
                response.status=400
                render ([status:'fail',message:'Solo se admiten peticiones vía GET...'] as JSON)
            }
        }catch (Exception ex){
            response.status=500
            log.info("Ha ocurrido un error interno en el servidor...")
            render ([status:'Error',message:'Ha ocurrido un error interno en el servidor'] as JSON)
        }
    }

    def show(Articulos articulo) {
        respond articulo
    }

    def create() {
        respond new Articulos(params)
    }

}
