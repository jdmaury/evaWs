package wseva

class Inv_pdt_articulos {
    String articulo
    int umEmpaque
    int umUnidad
    int conteo
    String usuario
    String tienda
    String bloque
    String plu

    static mapping = {
        version false
    }


    static constraints = {
        articulo    nullable:false
        umEmpaque   nullable:false
        umUnidad    nullable:false
        conteo      nullable:false
        usuario     nullable:false
        bloque      nullable:false
        tienda      nullable:false
        plu         nullable:false

    }
}
