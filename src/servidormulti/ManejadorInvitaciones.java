package servidormulti;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static servidormulti.ServidorMulti.clientes;
import static servidormulti.ServidorMulti.invitacionesRecibidas;
import static servidormulti.ServidorMulti.partidasActivas;

public class ManejadorInvitaciones {
    private final BloqueosBD bloqueosBD;
    public ManejadorInvitaciones(BloqueosBD bloqueosBD) {
        this.bloqueosBD = bloqueosBD;
    }


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

        if (bloqueosBD.estaBloqueado(remitente, invitado)) {
            clientes.get(remitente).salida.writeUTF("No puedes invitar a '" + invitado + "' porque lo tienes bloqueado.");
            return false;
        }

        if (bloqueosBD.estaBloqueado(invitado, remitente)) {
            clientes.get(remitente).salida.writeUTF("No puedes invitar a '" + invitado + "' porque te tiene bloqueado.");
            return false;
        }


        if (partidasActivas.containsKey(remitente)) {
            clientes.get(remitente).salida.writeUTF("Ya estás en una partida.");
            return false;
        }

        if (partidasActivas.containsKey(invitado)) {
            clientes.get(remitente).salida.writeUTF("El usuario '" + invitado + "' ya esta en una partida.");
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
            clientes.get(invitado).salida.writeUTF("El usuario que te invito no está conectado.");
            invitadores.remove(invitador);
            return;
        }

        partidasActivas.put(invitado, invitador);
        partidasActivas.put(invitador, invitado);

        invitadores.remove(invitador);
        TableroGato nuevoJuego = new TableroGato('X');
        ServidorMulti.tablerosPartidas.put(invitado, nuevoJuego);
        ServidorMulti.tablerosPartidas.put(invitador, nuevoJuego); // Ambos apuntan al mismo tablero


        ServidorMulti.simbolosJugadores.put(invitador, 'X');
        ServidorMulti.simbolosJugadores.put(invitado, 'O');


        String instrucciones = "\nPara jugar, envía: /gato [fila] [columna] (ej: /gato 0 1)";
        String tableroInicial = nuevoJuego.mostrarTablero();


        clientes.get(invitado).salida.writeUTF("Has aceptado la invitacion de '" + invitador + "'. ¡Comienza el juego de Gato!\n" +
                "Tú eres 'O'. Espera el turno de 'X'.\n" + tableroInicial + instrucciones);

        clienteInvitador.salida.writeUTF("El usuario '" + invitado + "' ha aceptado tu invitacion. ¡Comienza el juego de Gato!\n" +
                "Tú eres 'X'. Es tu turno.\n" + tableroInicial + instrucciones);

    }

    public void rechazarInvitacion(String invitado, String invitador) throws IOException {
        Set<String> invitadores = invitacionesRecibidas.get(invitado);
        if (invitadores == null || !invitadores.contains(invitador)) {
            clientes.get(invitado).salida.writeUTF("No tienes una invitacion de '" + invitador + "'.");
            return;
        }

        invitadores.remove(invitador);

        clientes.get(invitado).salida.writeUTF("Has rechazado la invitacion de '" + invitador + "'.");
        UnCliente clienteInvitador = clientes.get(invitador);
        if (clienteInvitador != null) {
            clienteInvitador.salida.writeUTF("El usuario '" + invitado + "' ha rechazado tu invitacion.");
        }
    }


    public void finalizarPartida(String usuario1, String usuario2) throws IOException {
        partidasActivas.remove(usuario1);
        partidasActivas.remove(usuario2);
        ServidorMulti.tablerosPartidas.remove(usuario1);
        ServidorMulti.tablerosPartidas.remove(usuario2);
        ServidorMulti.simbolosJugadores.remove(usuario1);
        ServidorMulti.simbolosJugadores.remove(usuario2);

        UnCliente cliente1 = clientes.get(usuario1);
        UnCliente cliente2 = clientes.get(usuario2);

        if (cliente1 != null) cliente1.salida.writeUTF("La partida ha terminado.");
        if (cliente2 != null) cliente2.salida.writeUTF("La partida ha terminado.");
    }
}