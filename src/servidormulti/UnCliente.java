package servidormulti;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;
  public class UnCliente implements Runnable {

    final DataOutputStream salida;
    final BufferedReader teclado = new BufferedReader(new InputStreamReader(System.in));
    final DataInputStream entrada;

    UnCliente(Socket s) throws IOException {
        salida = new DataOutputStream(s.getOutputStream());
        entrada = new DataInputStream(s.getInputStream());
    }

    @Override
    public void run() {
        String mensaje;
        while (true) {
            try {
                mensaje = entrada.readUTF();
                if (mensaje.startsWith("@")){
                    String[] partes = mensaje.split(" ");
                    String DirijidoA = partes[0].substring(1);
                    UnCliente cliente = ServidorMulti.clientes.get(DirijidoA);
                    cliente.salida.writeUTF(mensaje);
                }else{
                    for( UnCliente cliente : ServidorMulti.clientes.values() ){
                        cliente.salida.writeUTF(mensaje);
                    }

                }

            } catch (IOException ex) {

            }
        }
    }
}

