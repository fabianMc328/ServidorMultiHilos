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

                    if(partes[0].contains(",")){
                        String [] partes2 = partes[0].split(",");
for(int i = 0; i < partes2.length; i++){
    String DirijidoA= partes2[i];

    if(DirijidoA.contains("@")){
       DirijidoA =  partes2[i].substring(1);
    }

    UnCliente cliente = ServidorMulti.clientes.get(DirijidoA);
    cliente.salida.writeUTF(Thread.currentThread().getName()+"DICE: "+ mensaje);

}
                    }else{

                    String DirijidoA = partes[0].substring(1);
                    UnCliente cliente = ServidorMulti.clientes.get(DirijidoA);
                        cliente.salida.writeUTF(Thread.currentThread().getName()+"DICE:  "+ mensaje);}

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

