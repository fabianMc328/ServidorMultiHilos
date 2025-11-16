package servidormulti;

import java.io.IOException;

public class ManejadorUsuarios {
    private final UsuariosBD usuariosBD;
    private final EstadoServidor estado;

    public ManejadorUsuarios(UsuariosBD usuariosBD, EstadoServidor estado) {
        this.usuariosBD = usuariosBD;
        this.estado = estado;
    }


    public boolean procesarAutenticacion(String comando, String usuario, String contra, UnCliente cliente) throws IOException {
        boolean exito = false;

        if (comando.equalsIgnoreCase("/register")) {
            exito = usuariosBD.registrarUsuario(usuario, contra);
            cliente.salida.writeUTF(exito ? "Usuario registrado correctamente." : "El usuario ya existe.");

            if (exito) {
                estado.moverClienteLogueado(cliente.getClienteId(), usuario, cliente);
                cliente.setNombreUsuario(usuario);

            }

        } else if (comando.equalsIgnoreCase("/login")) {

            boolean credencialesCorrectas = usuariosBD.verificarLogin(usuario, contra);

            if (!credencialesCorrectas) {
                cliente.salida.writeUTF("Datos no correctos o no existen.");
                return false;
            }


            if (estado.getCliente(usuario) != null) {
                cliente.salida.writeUTF("Error: El usuario '" + usuario + "' ya esta en linea.");
                return false;

            } else {
                cliente.salida.writeUTF("Sesion iniciada correctamente.");
                estado.moverClienteLogueado(cliente.getClienteId(), usuario, cliente);
                cliente.setNombreUsuario(usuario);

                exito = true;
            }

        }

        return exito;
    }

    public void cerrarSesion(UnCliente cliente) {
        estado.moverClienteInvitado(cliente.getNombreUsuario(), cliente.getClienteId(), cliente);
        cliente.resetearEstadoAInvitado();
        estado.contadoresDeMensajes.put(cliente.getClienteId(), 0);

    }
}