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
        }
        this.salidaConsola = tempSalidaConsola;
    }

    @Override
    public void run() {
        try {
            while (true) {
                String mensaje = entrada.readUTF();
                String mensajeMin = mensaje.toLowerCase(); // Para comparar sin importar mayúsculas

                // --- ARREGLO ROBUSTO ---
                // Usamos 'contains' en lugar de 'startsWith' para ser más flexibles
                // y asegurarnos de que el cliente SIEMPRE se entere si entra o sale.

                if (mensajeMin.contains("sesion iniciada") || mensajeMin.contains("sesión iniciada")) {
                    ParaMandar.estaLogueado = true;
                }
                else if (mensajeMin.contains("sesion cerrada") || mensajeMin.contains("sesión cerrada")) {
                    ParaMandar.estaLogueado = false;
                }
                // -----------------------

                salidaConsola.println(mensaje);
            }
        } catch (IOException ex) {
            salidaConsola.println("\n[AVISO] Se perdió la conexión con el servidor.");
            System.exit(0);
        }
    }
}