package wseva

class Usuarios {

    String id
    int versionUsr
    Byte account_expired
    Byte account_locked
    String apellido
    String cod_empresa
    Date date_created
    String email
    Byte enabled
    Date last_updated
    String nombre
    String password
    Byte password_expired
    String rol_id
    String token_supervisor
    String username
    String usuario


    static constraints = {
    }


    static mapping = {
        table 'dbo.usuario'
        id column:'id_usuario'
        versionUsr column:'version'
        account_expired column: 'account_expired'
        account_locked column: 'account_locked'
        apellido column: 'apellido'
        cod_empresa column: 'cod_empresa'
        date_created column: 'date_created'
        email column: 'email'
        enabled column: 'enabled'
        last_updated column: 'last_updated'
        nombre column: 'nombre'
        password column: 'password'
        password_expired column: 'password_expired'
        rol_id column: 'rol_id'
        token_supervisor column: 'token_supervisor'
        username column: 'username'
        usuario column: 'usuario'
        version false
    }
}
