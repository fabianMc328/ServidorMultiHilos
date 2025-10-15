package servidormulti;

public class ManejadorUsuarios {

    private final UsuariosBD usuariosBD;

    public ManejadorUsuarios() {
        usuariosBD = new UsuariosBD();
    }

    public synchronized boolean RegistrarUsuario(String usuario, String contraseña) {
        return usuariosBD.registrarUsuario(usuario, contraseña);
    }

    public synchronized boolean VerificarUsuario(String usuario, String contra) {
        return usuariosBD.verificarLogin(usuario, contra);
    }
}
