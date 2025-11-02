package clientemulti;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

public class ParaRecibir implements Runnable{
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
                if (mensaje.startsWith("Sesión iniciada correctamente")) {
                    ParaMandar.estaLogueado = true;
                }
                salidaConsola.println(mensaje);
            }
        } catch (IOException ex) {
            salidaConsola.println("Conexión cerrada o error: " + ex.getMessage());
        }
    }
}