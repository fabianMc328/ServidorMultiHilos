package clientemulti;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
public class ParaRecibir implements Runnable{
    final DataInputStream entrada;
    public ParaRecibir(Socket s) throws IOException {
        entrada = new DataInputStream(s.getInputStream());
    }

    @Override
    public void run() {
        try {
            while (true) {
                String mensaje = entrada.readUTF();
                System.out.println( mensaje);
            }
        } catch (IOException ex) {
            System.out.println("Conexión cerrada o error: " + ex.getMessage());
        }
    }

}

