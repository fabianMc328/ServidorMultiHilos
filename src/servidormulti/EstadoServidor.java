package servidormulti;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


public class EstadoServidor {


    public final Map<String, UnCliente> clientes = new ConcurrentHashMap<>();
    public final Map<String, Integer> contadoresDeMensajes = new ConcurrentHashMap<>();
    public final Map<String, Set<String>> invitacionesRecibidas = new ConcurrentHashMap<>();
    public final Map<String, Set<String>> partidasActivas = new ConcurrentHashMap<>();
    public final Map<String, TableroGato> tablerosPartidas = new ConcurrentHashMap<>();


    public EstadoServidor() {
        System.out.println("Estado del Servidor inicializado.");
    }


    public void agregarCliente(String id, UnCliente cliente) {
        clientes.put(id, cliente);
    }

    public void moverClienteLogueado(String idInvitado, String idUsuario, UnCliente cliente) {
        synchronized (clientes) {
            clientes.remove(idInvitado);
            clientes.put(idUsuario, cliente);
        }
    }

    public void moverClienteInvitado(String idUsuario, String idInvitado, UnCliente cliente) {
        synchronized (clientes) {
            clientes.remove(idUsuario);
            clientes.put(idInvitado, cliente);
        }
    }

    public void removerCliente(String id) {
        clientes.remove(id);
    }

    public UnCliente getCliente(String id) {
        return clientes.get(id);
    }
}
