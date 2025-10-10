package clientemulti;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
public class ParaMandar implements Runnable{
    final BufferedReader teclado = new BufferedReader(new InputStreamReader(System.in));
    final DataOutputStream salida ;
    public ParaMandar(Socket s) throws IOException {
        this.salida = new DataOutputStream(s.getOutputStream());
    }

    @Override
    public void run() {
        try {
            while (true) {
                String mensaje = teclado.readLine();
                if (mensaje != null && !mensaje.isEmpty()) {
                        salida.writeUTF(mensaje);
                    }
                }
            }
         catch (IOException e) {
            System.out.println("Error enviando mensaje: " + e.getMessage());
        }
    }

}

