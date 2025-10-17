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

    private String nombreUsuario = null;
    private boolean registrado = false;


    public UnCliente(Socket socket, String clienteId, ManejadorUsuarios manejadorUsuarios, ManejadorMensajes manejadorMensajes) throws IOException {
        this.clienteId = clienteId;
        this.salida = new DataOutputStream(socket.getOutputStream());
        this.entrada = new DataInputStream(socket.getInputStream());
        this.manejadorUsuarios = manejadorUsuarios;
        this.manejadorMensajes = manejadorMensajes;

        ServidorMulti.contadoresDeMensajes.put(clienteId, 0);
    }

    @Override
    public void run() {
        try {
            while (true) {
                String mensaje = entrada.readUTF();

                if (registrado) {
                    manejadorMensajes.procesar(mensaje, this);
                } else {
                    gestionarAutenticacionOPrueba(mensaje);
                }
            }
        } catch (IOException ex) {
            System.out.println("Cliente '" + (nombreUsuario != null ? nombreUsuario : clienteId) + "' desconectado.");
            limpiar();
        }
    }

    private void gestionarAutenticacionOPrueba(String mensaje) throws IOException {
        if (mensaje.equalsIgnoreCase("Register") || mensaje.equalsIgnoreCase("Login")) {
            boolean exito = manejadorUsuarios.ParaRegistroOlogin(mensaje, this);
            if (exito) {
                this.registrado = true;
                ServidorMulti.contadoresDeMensajes.remove(this.clienteId);
                salida.writeUTF("¡Ahora puedes enviar mensajes normalmente!");
            }
        } else {

            int contador = ServidorMulti.contadoresDeMensajes.get(clienteId) + 1;
            ServidorMulti.contadoresDeMensajes.put(clienteId, contador);
            if (contador >= 3) {
                salida.writeUTF("Has enviado 3 mensajes. Por favor, regístrate usando [Register] o [Login]");
            } else {
                manejadorMensajes.procesar(mensaje, this);
            }
        }
    }

    private void limpiar() {

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
}


