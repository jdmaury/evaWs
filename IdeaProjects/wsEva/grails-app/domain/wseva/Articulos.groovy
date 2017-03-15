package wseva

class Articulos {
    String id
    int versionArt
    Byte canasta_familiar
    Byte cant_req
    Float cant_ult_compra
    Byte cantidad_permite_decimal
    String clasabcm
    String clasabcu
    String cod_color
    String cod_concesionario
    int cod_descodificado
    String cod_empresa
    String cod_gondola
    String cod_grupo
    String cod_imp
    String cod_marca
    String cod_retencion
    String id_color
    String id_concesionario
    Byte ctrl_ministerio
    Date date_created
    int dcto_automatico
    Byte descodificado
    String descrip1
    String descrip2
    Byte estadistica
    int factor_venta
    Date fecha_descodifica
    Date fecha_fin_descuento
    Date fecha_inicio_descuento
    Date fecha_pre_venta1
    Date fecha_pre_ventaa1
    Date fecha_pre_ventaa2
    Date fecha_ult_compra
    Date fecha_ult_venta
    Byte generico
    String id_gondola
    String id_grupo
    Byte hospitalario
    Byte importado
    Float impto1
    Float impto2
    Float impto3
    Date last_updated
    int linea
    Byte maneja_inventario
    String id_marca
    Float margen_minimo
    String medida
    String motivo_descodificado_id
    String nit_prov_ult_compra
    int nro_ult_compra
    int pctj_comi_vndor
    Byte permite_descuento
    Byte peso_req
    int pre_venta1
    int pre_venta2
    int pre_venta3
    int pre_venta_ant1
    int pre_venta_ant2
    int pre_venta_req
    String referencia
    String id_retencion
    String subgrupo1
    String subgrupo2
    String subgrupo3
    String subgrupo4
    Byte subsidiado
    String talla
    String tipo_retencion
    Byte tipo_venta
    int ult_costo
    Date ultima_modificacion
    int unid_empaque
    String unid_med
    String cod_unid_med_fleje
    Float prec_unid_med_fleje
    String unid_med_fleje



    static constraints = {
    }


    static mapping = {
        table 'dbo.articulo'
        id column:'id_articulo'
        versionArt column:'version'
        canasta_familiar column:'canasta_familiar'
        cant_req column:'cant_req'
        cant_ult_compra column:'cant_ult_compra'
        cantidad_permite_decimal column:'cantidad_permite_decimal'
        clasabcm column:'clasabcm'
        clasabcu column:'clasabcu'
        cod_color column:'cod_color'
        cod_concesionario column:'cod_concesionario'
        cod_descodificado column:'cod_descodificado'
        cod_empresa column:'cod_empresa'
        cod_gondola column:'cod_gondola'
        cod_grupo column:'cod_grupo'
        cod_imp column:'cod_imp'
        cod_marca column:'cod_marca'
        cod_retencion column:'cod_retencion'
        id_color column:'id_color'
        id_concesionario column:'id_concesionario'
        ctrl_ministerio column:'ctrl_ministerio'
        date_created column:'date_created'
        dcto_automatico column:'dcto_automatico'
        descodificado column:'descodificado'
        descrip1 column:'descrip1'
        descrip2 column:'descrip2'
        estadistica column:'estadistica'
        factor_venta column:'factor_venta'
        fecha_descodifica column:'fecha_descodifica'
        fecha_fin_descuento column:'fecha_fin_descuento'
        fecha_inicio_descuento column:'fecha_inicio_descuento'
        fecha_pre_venta1 column:'fecha_pre_venta1'
        fecha_pre_ventaa1 column:'fecha_pre_ventaa1'
        fecha_pre_ventaa2 column:'fecha_pre_ventaa2'
        fecha_ult_compra column:'fecha_ult_compra'
        fecha_ult_venta column:'fecha_ult_venta'
        generico column:'generico'
        id_gondola column:'id_gondola'
        id_grupo column:'id_grupo'
        hospitalario column:'hospitalario'
        importado column:'importado'
        impto1 column:'impto1'
        impto2 column:'impto2'
        impto3 column:'impto3'
        last_updated column:'last_updated'
        linea column:'linea'
        maneja_inventario column:'maneja_inventario'
        id_marca column:'id_marca'
        margen_minimo column:'margen_minimo'
        medida column:'medida'
        motivo_descodificado_id column:'motivo_descodificado_id'
        nit_prov_ult_compra column:'nit_prov_ult_compra'
        nro_ult_compra column:'nro_ult_compra'
        pctj_comi_vndor column:'pctj_comi_vndor'
        permite_descuento column:'permite_descuento'
        peso_req column:'peso_req'
        pre_venta1 column:'pre_venta1'
        pre_venta2 column:'pre_venta2'
        pre_venta3 column:'pre_venta3'
        pre_venta_ant1 column:'pre_venta_ant1'
        pre_venta_ant2 column:'pre_venta_ant2'
        pre_venta_req column:'pre_venta_req'
        referencia column:'referencia'
        id_retencion column:'id_retencion'
        subgrupo1 column:'subgrupo1'
        subgrupo2 column:'subgrupo2'
        subgrupo3 column:'subgrupo3'
        subgrupo4 column:'subgrupo4'
        subsidiado column:'subsidiado'
        talla column:'talla'
        tipo_retencion column:'tipo_retencion'
        tipo_venta column:'tipo_venta'
        ult_costo column:'ult_costo'
        ultima_modificacion column:'ultima_modificacion'
        unid_empaque column:'unid_empaque'
        unid_med column:'unid_med'
        cod_unid_med_fleje column:'cod_unid_med_fleje'
        prec_unid_med_fleje column:'prec_unid_med_fleje'
        unid_med_fleje column:'unid_med_fleje'

        version false

    }
    static class Requerimientos {

        static constraints = {
        }
    }
}
