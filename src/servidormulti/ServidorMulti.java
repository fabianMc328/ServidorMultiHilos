package servidormulti;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ServidorMulti {
    public static Map<String, UnCliente> clientes = new ConcurrentHashMap<>();
    public static Map<String, Integer> contadoresDeMensajes = new ConcurrentHashMap<>();
    public static Map<String, Set<String>> invitacionesRecibidas = new ConcurrentHashMap<>();
    public static Map<String, String> partidasActivas = new ConcurrentHashMap<>();
    public static Map<String, TableroGato> tablerosPartidas = new ConcurrentHashMap<>();
    public static Map<String, Character> simbolosJugadores = new ConcurrentHashMap<>();


    public static void main(String[] args) throws IOException {

        UsuariosBD usuariosBD = new UsuariosBD();
        BloqueosBD bloqueosBD = new BloqueosBD();
        RankingBD rankingBD = new RankingBD();
        ManejadorUsuarios manejadorUsuarios = new ManejadorUsuarios(usuariosBD);
        ManejadorMensajes manejadorMensajes = new ManejadorMensajes(bloqueosBD, usuariosBD, rankingBD);

        System.out.println("Servidor de chat iniciado...");
        try (ServerSocket servidorSocket = new ServerSocket(8080)) {
            int contadorId = 0;
            while (true) {
                Socket s = servidorSocket.accept();
                String clienteId = String.valueOf(contadorId++);
                UnCliente unCliente = new UnCliente(s, clienteId, manejadorUsuarios, manejadorMensajes);
                clientes.put(clienteId, unCliente);
                new Thread(unCliente).start();
                System.out.println("Se conectó un nuevo cliente con ID temporal: " + clienteId);
            }
        }
    }
}