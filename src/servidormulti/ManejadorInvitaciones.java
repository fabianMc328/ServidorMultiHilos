package servidormulti;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static servidormulti.ServidorMulti.clientes;
import static servidormulti.ServidorMulti.invitacionesRecibidas;
import static servidormulti.ServidorMulti.partidasActivas;

public class ManejadorInvitaciones {

    public boolean enviarInvitacion(String remitente, String invitado) throws IOException {
        if (remitente.equalsIgnoreCase(invitado)) {
            clientes.get(remitente).salida.writeUTF("No puedes invitarte a ti mismo.");
            return false;
        }

        UnCliente clienteInvitado = clientes.get(invitado);
        if (clienteInvitado == null) {
            clientes.get(remitente).salida.writeUTF("El usuario '" + invitado + "' no está conectado.");
            return false;
        }

        if (partidasActivas.containsKey(remitente)) {
            clientes.get(remitente).salida.writeUTF("Ya estás en una partida.");
            return false;
        }

        if (partidasActivas.containsKey(invitado)) {
            clientes.get(remitente).salida.writeUTF("El usuario '" + invitado + "' ya está en una partida.");
            return false;
        }

        invitacionesRecibidas.putIfAbsent(invitado, ConcurrentHashMap.newKeySet());
        Set<String> invitadores = invitacionesRecibidas.get(invitado);
        if (invitadores.contains(remitente)) {
            clientes.get(remitente).salida.writeUTF("Ya has enviado una invitación a '" + invitado + "'.");
            return false;
        }

        invitadores.add(remitente);
        clienteInvitado.recibirInvitacion(remitente);
        clientes.get(remitente).salida.writeUTF("Invitación enviada a '" + invitado + "'.");
        return true;
    }

    public void aceptarInvitacion(String invitado, String invitador) throws IOException {
        Set<String> invitadores = invitacionesRecibidas.get(invitado);
        if (invitadores == null || !invitadores.contains(invitador)) {
            clientes.get(invitado).salida.writeUTF("No tienes una invitación de '" + invitador + "'.");
            return;
        }

        if (partidasActivas.containsKey(invitado)) {
            clientes.get(invitado).salida.writeUTF("Ya estás en una partida.");
            return;
        }
        if (partidasActivas.containsKey(invitador)) {
            clientes.get(invitado).salida.writeUTF("El usuario '" + invitador + "' ya está en una partida.");
            return;
        }

        UnCliente clienteInvitador = clientes.get(invitador);
        if (clienteInvitador == null) {
            clientes.get(invitado).salida.writeUTF("El usuario que te invitó no está conectado.");
            invitadores.remove(invitador);
            return;
        }

        partidasActivas.put(invitado, invitador);
        partidasActivas.put(invitador, invitado);

        invitadores.remove(invitador);

        clientes.get(invitado).salida.writeUTF("Has aceptado la invitación de '" + invitador + "'. ¡Comienza el juego de Gato!");
        clienteInvitador.salida.writeUTF("El usuario '" + invitado + "' ha aceptado tu invitación. ¡Comienza el juego de Gato!");
    }

    public void rechazarInvitacion(String invitado, String invitador) throws IOException {
        Set<String> invitadores = invitacionesRecibidas.get(invitado);
        if (invitadores == null || !invitadores.contains(invitador)) {
            clientes.get(invitado).salida.writeUTF("No tienes una invitación de '" + invitador + "'.");
            return;
        }

        invitadores.remove(invitador);

        clientes.get(invitado).salida.writeUTF("Has rechazado la invitación de '" + invitador + "'.");
        UnCliente clienteInvitador = clientes.get(invitador);
        if (clienteInvitador != null) {
            clienteInvitador.salida.writeUTF("El usuario '" + invitado + "' ha rechazado tu invitación.");
        }
    }


    public void finalizarPartida(String usuario1, String usuario2) throws IOException {
        partidasActivas.remove(usuario1);
        partidasActivas.remove(usuario2);

        UnCliente cliente1 = clientes.get(usuario1);
        UnCliente cliente2 = clientes.get(usuario2);

        if (cliente1 != null) cliente1.salida.writeUTF("La partida ha terminado.");
        if (cliente2 != null) cliente2.salida.writeUTF("La partida ha terminado.");
    }
}
