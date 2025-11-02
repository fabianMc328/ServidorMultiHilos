package servidormulti;

import java.io.IOException;

public class ManejadorUsuarios {
    private final UsuariosBD usuariosBD;

    public ManejadorUsuarios(UsuariosBD usuariosBD) {
        this.usuariosBD = usuariosBD;
    }

    public boolean ParaRegistroOlogin(String comando, UnCliente cliente) throws IOException {
        String usuario = cliente.entrada.readUTF();
        String contra = cliente.entrada.readUTF();
        boolean exito = false;
        if (comando.equalsIgnoreCase("/register")) {
            exito = usuariosBD.registrarUsuario(usuario, contra);
            cliente.salida.writeUTF(exito ? "Usuario registrado correctamente." : "El usuario ya existe.");
        } else if (comando.equalsIgnoreCase("/login")) {
            exito = usuariosBD.verificarLogin(usuario, contra);
            cliente.salida.writeUTF(exito ? "Sesión iniciada correctamente." : "El usuario no existe o contraseña incorrecta.");
        }
        if (exito) {

            ServidorMulti.clientes.remove(cliente.getClienteId());
            cliente.setNombreUsuario(usuario);
            ServidorMulti.clientes.put(usuario, cliente);
        }
        return exito;
    }
}