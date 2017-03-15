package wseva

class Inv_pdt_estados {

    String bloque
    int conteo
    String estado
    String usuario
    static constraints = {
    }

    static  mapping = {
        bloque nullable:false
        conteo nullable:false
        estado nullable:false
        usuario nullable:false

        version false
    }
}