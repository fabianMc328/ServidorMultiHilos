package clientemulti;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

public class ParaRecibir implements Runnable {
    final DataInputStream entrada;
    private final PrintStream salidaConsola;

    public ParaRecibir(Socket s) throws IOException {
        entrada = new DataInputStream(s.getInputStream());

        PrintStream tempSalidaConsola;
        try {
            tempSalidaConsola = new PrintStream(System.out, true, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            tempSalidaConsola = System.out;
            e.printStackTrace();
        }
        this.salidaConsola = tempSalidaConsola;
    }

    @Override
    public void run() {
        try {
            while (true) {
                String mensaje = entrada.readUTF();

                if (mensaje.startsWith("Sesion iniciada correctamente")) {
                    ParaMandar.estaLogueado = true;
                } else if (mensaje.startsWith("Sesion cerrada correctamente")) {
                    ParaMandar.estaLogueado = false;
                }

                salidaConsola.println(mensaje);
            }
        } catch (IOException ex) {
            salidaConsola.println("Se perdió la conexión con el servidor.");
            System.exit(0);
        }
    }
}