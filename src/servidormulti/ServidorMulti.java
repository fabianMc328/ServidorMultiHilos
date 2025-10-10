package servidormulti;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class ServidorMulti {
    public static HashMap<String, UnCliente> clientes = new HashMap<String, UnCliente>();
   public static Map<String ,Integer> contadoresDeMensajes = new HashMap<>();
static int cont = 0;

    public static void main(String[] args) throws IOException {
        ServerSocket servidorSocket = new ServerSocket(8080);
        int contadorId = 0;
        while (true) {
            Socket s = servidorSocket.accept();
            String clienteId = Integer.toString( contadorId);

            UnCliente unCliente = new UnCliente(s,clienteId);
            Thread hilo = new Thread(unCliente, clienteId);
            clientes.put(clienteId, unCliente);
            contadoresDeMensajes.put(clienteId, 0);
            hilo.start();
            System.out.println("se conecto cliente "+clienteId);
            contadorId++;
        }
    }
}
