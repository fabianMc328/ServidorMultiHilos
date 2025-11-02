package servidormulti;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class UnCliente implements Runnable {

    public final DataOutputStream salida;
    public final DataInputStream entrada;
    private final String clienteId;
    private final ManejadorUsuarios manejadorUsuarios;
    private final ManejadorMensajes manejadorMensajes;
    private final ManejadorGrupos manejadorGrupos; // Necesario para unir a "todos"

    private String oponenteEnJuego = null;
    private String nombreUsuario = null;
    private boolean registrado = false;
    private int idGrupoActual = 1; // ID 1 = "todos"
    private String nombreGrupoActual = "todos";

    public UnCliente(Socket socket, String clienteId, ManejadorUsuarios manejadorUsuarios, ManejadorMensajes manejadorMensajes, ManejadorGrupos manejadorGrupos) throws IOException {
        this.clienteId = clienteId;
        this.salida = new DataOutputStream(socket.getOutputStream());
        this.entrada = new DataInputStream(socket.getInputStream());
        this.manejadorUsuarios = manejadorUsuarios;
        this.manejadorMensajes = manejadorMensajes;
        this.manejadorGrupos = manejadorGrupos; // Asignar

        ServidorMulti.contadoresDeMensajes.put(clienteId, 0);
    }

    @Override
    public void run() {
        try {
            while (true) {
                String mensaje = entrada.readUTF();
                if (registrado && (mensaje.equalsIgnoreCase("/login") || mensaje.equalsIgnoreCase("/register"))) {
                    salida.writeUTF("Ya estás logueado como: " + nombreUsuario);
                    continue;
                }


                if (registrado) {

                    manejadorMensajes.procesar(mensaje, this);
                } else {

                    if (mensaje.equalsIgnoreCase("/register") || mensaje.equalsIgnoreCase("/login")) {
                        gestionarAutenticacion(mensaje);
                    } else {

                        gestionarMensajesDeInvitado(mensaje);
                    }
                }
            }
        } catch (IOException ex) {
            System.out.println("Cliente '" + (nombreUsuario != null ? nombreUsuario : clienteId) + "' desconectado.");
            limpiar();
        }
    }

    private void gestionarAutenticacion(String mensaje) throws IOException {
        boolean exito = manejadorUsuarios.ParaRegistroOlogin(mensaje, this);
        if (exito) {
            this.registrado = true;
            ServidorMulti.contadoresDeMensajes.remove(this.clienteId);
            manejadorGrupos.cambiarGrupo("todos", this);
        }
    }

    private void gestionarMensajesDeInvitado(String mensaje) throws IOException {
        int contador = ServidorMulti.contadoresDeMensajes.get(clienteId) + 1;
        ServidorMulti.contadoresDeMensajes.put(clienteId, contador);
        if (contador > 3) {
            salida.writeUTF("Has enviado 3 mensajes. Por favor, regístrate usando [/register] o [/login]");
        } else {

            if (this.idGrupoActual != 1) {
                this.idGrupoActual = 1;
                this.nombreGrupoActual = "todos";
            }

            manejadorMensajes.procesar(mensaje, this);
        }
    }
    private void limpiar() {
        String nombreUsuarioLimpio = (nombreUsuario != null) ? nombreUsuario : clienteId;
        if (ServidorMulti.partidasActivas.containsKey(nombreUsuarioLimpio)) {
            String oponente = ServidorMulti.partidasActivas.get(nombreUsuarioLimpio);
            UnCliente clienteOponente = ServidorMulti.clientes.get(oponente);

            if (clienteOponente != null) {
                try {
                    clienteOponente.salida.writeUTF("Tu oponente '" + nombreUsuarioLimpio + "' se ha desconectado. Has ganado por abandono.");
                } catch (IOException e) {

                }
            }

            try {
                if (nombreUsuario != null) {
                    manejadorMensajes.procesarAbandono(nombreUsuarioLimpio, oponente);
                } else {
                    manejadorMensajes.manejadorInvitaciones.finalizarPartida(nombreUsuarioLimpio, oponente);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (this.nombreUsuario != null) {
            manejadorGrupos.actualizarEstadoLectura(this);
        }

        if (nombreUsuario != null) {
            ServidorMulti.clientes.remove(nombreUsuario);
        } else {
            ServidorMulti.clientes.remove(clienteId);
        }
        ServidorMulti.contadoresDeMensajes.remove(clienteId);
    }


    public String getClienteId() { return clienteId; }
    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }

    public int getIdGrupoActual() { return idGrupoActual; }
    public String getNombreGrupoActual() { return nombreGrupoActual; }

    public void setGrupoActual(int idGrupo, String nombreGrupo) {
        this.idGrupoActual = idGrupo;
        this.nombreGrupoActual = nombreGrupo;
    }

    public String getOponenteEnJuego() {
        return oponenteEnJuego;
    }
    public void setOponenteEnJuego(String oponenteEnJuego) {
        this.oponenteEnJuego = oponenteEnJuego;
    }
    public void recibirInvitacion(String desdeUsuario) throws IOException {
        salida.writeUTF("[INVITACION] Invitación para jugar Gato de " + desdeUsuario +
                ". Para aceptar: /aceptar " + desdeUsuario +
                ", para rechazar: /rechazar " + desdeUsuario);
    }
}