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

            if (exito) {
                synchronized (ServidorMulti.clientes) {
                    ServidorMulti.clientes.remove(cliente.getClienteId());
                    cliente.setNombreUsuario(usuario);
                    ServidorMulti.clientes.put(usuario, cliente);
                }
            }

        } else if (comando.equalsIgnoreCase("/login")) {

            boolean credencialesCorrectas = usuariosBD.verificarLogin(usuario, contra);

            if (!credencialesCorrectas) {
                cliente.salida.writeUTF("Datos no correctos o no existen.");
                return false;
            }

            synchronized (ServidorMulti.clientes) {

                if (ServidorMulti.clientes.containsKey(usuario)) {
                    cliente.salida.writeUTF("Error: El usuario '" + usuario + "' ya esta en linea.");
                    return false;

                } else {
                    cliente.salida.writeUTF("Sesion iniciada correctamente.");

                    ServidorMulti.clientes.remove(cliente.getClienteId());
                    cliente.setNombreUsuario(usuario);
                    ServidorMulti.clientes.put(usuario, cliente);

                    exito = true;
                }
            }
        }

        return exito;
    }

    public void cerrarSesion(UnCliente cliente) {
        synchronized (ServidorMulti.clientes) {
            ServidorMulti.clientes.remove(cliente.getNombreUsuario());
            cliente.resetearEstadoAInvitado();
            ServidorMulti.clientes.put(cliente.getClienteId(), cliente);
        }

        ServidorMulti.contadoresDeMensajes.put(cliente.getClienteId(), 0);
    }

}