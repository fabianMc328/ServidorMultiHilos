package servidormulti;

import java.io.*;
import java.net.Socket;

public class UnCliente implements Runnable {

    final DataOutputStream salida;
    final DataInputStream entrada;

    public UnCliente(Socket socket) throws IOException {
        salida = new DataOutputStream(socket.getOutputStream());
        entrada = new DataInputStream(socket.getInputStream());
    }

    @Override
    public void run() {
        String mensaje;
        LectorMensajes lector = new LectorMensajes(entrada);
        ManejadorMensajes manejador = new ManejadorMensajes(this);

        while (true) {
            try {
                mensaje = lector.leer();
                manejador.procesar(mensaje);
            } catch (IOException ex) {

            }
        }
    }
}

