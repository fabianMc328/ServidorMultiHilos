package servidormulti.Servicios;

import servidormulti.BD.BloqueosBD;
import servidormulti.EstadoServidor;
import servidormulti.Juego.TableroGato;
import servidormulti.UnCliente;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ManejadorInvitaciones {
    private final BloqueosBD bloqueosBD;
    private final EstadoServidor estado;

    public ManejadorInvitaciones(BloqueosBD bloqueosBD, EstadoServidor estado) {
        this.bloqueosBD = bloqueosBD;
        this.estado = estado;
    }

    private String crearGameId(String j1, String j2) {
        String[] nombres = {j1, j2};
        Arrays.sort(nombres);
        return nombres[0] + "-" + nombres[1];
    }


    public boolean enviarInvitacion(String remitente, String invitado) throws IOException {

        if (remitente.equalsIgnoreCase(invitado)) {
            estado.getCliente(remitente).salida.writeUTF("No puedes invitarte a ti mismo.");
            return false;
        }

        UnCliente clienteInvitado = estado.getCliente(invitado);
        if (clienteInvitado == null) {
            estado.getCliente(remitente).salida.writeUTF("El usuario '" + invitado + "' no está conectado.");
            return false;
        }

        if (bloqueosBD.estaBloqueado(remitente, invitado)) {
            estado.getCliente(remitente).salida.writeUTF("No puedes invitar a '" + invitado + "' porque lo tienes bloqueado.");
            return false;
        }
        if (bloqueosBD.estaBloqueado(invitado, remitente)) {
            estado.getCliente(remitente).salida.writeUTF("No puedes invitar a '" + invitado + "' porque te tiene bloqueado.");
            return false;
        }

        if (estado.partidasActivas.containsKey(remitente) && estado.partidasActivas.get(remitente).contains(invitado)) {
            estado.getCliente(remitente).salida.writeUTF("Ya estas en una partida con '" + invitado + "'.");
            return false;
        }


        estado.invitacionesRecibidas.putIfAbsent(invitado, ConcurrentHashMap.newKeySet());
        Set<String> invitadores = estado.invitacionesRecibidas.get(invitado);
        if (invitadores.contains(remitente)) {
            estado.getCliente(remitente).salida.writeUTF("Ya has enviado una invitacion a '" + invitado + "'.");
            return false;
        }

        invitadores.add(remitente);
        clienteInvitado.recibirInvitacion(remitente);
        estado.getCliente(remitente).salida.writeUTF("Invitacion enviada a '" + invitado + "'.");
        return true;
    }

    public void aceptarInvitacion(String invitado, String invitador) throws IOException {
        Set<String> invitadores = estado.invitacionesRecibidas.get(invitado);
        if (invitadores == null || !invitadores.contains(invitador)) {
            estado.getCliente(invitado).salida.writeUTF("No tienes una invitacion de '" + invitador + "'.");
            return;
        }


        if (estado.partidasActivas.containsKey(invitado) && estado.partidasActivas.get(invitado).contains(invitador)) {
            estado.getCliente(invitado).salida.writeUTF("Ya estas en una partida con '" + invitador + "'.");
            return;
        }


        UnCliente clienteInvitador = estado.getCliente(invitador);
        if (clienteInvitador == null) {
            estado.getCliente(invitado).salida.writeUTF("El usuario que te invito no esta conectado.");
            invitadores.remove(invitador);
            return;
        }

        String gameId = crearGameId(invitado, invitador);

        TableroGato nuevoJuego = new TableroGato(invitador, invitado);

        estado.tablerosPartidas.put(gameId, nuevoJuego); // Usa 'estado.tablerosPartidas'

        estado.partidasActivas.putIfAbsent(invitado, ConcurrentHashMap.newKeySet());
        estado.partidasActivas.get(invitado).add(invitador);

        estado.partidasActivas.putIfAbsent(invitador, ConcurrentHashMap.newKeySet());
        estado.partidasActivas.get(invitador).add(invitado);

        invitadores.remove(invitador);


        UnCliente clienteInvitado = estado.getCliente(invitado);
        clienteInvitado.setOponenteEnFoco(invitador);
        clienteInvitador.setOponenteEnFoco(invitado);


        String instrucciones = "\nEstás enfocado en esta partida.\nPara jugar, envía: /gato [fila] [columna] (ej: /gato 0 1)\n" +
                "Para cambiar de partida, usa: /juego-focus <oponente>";
        String tableroInicial = nuevoJuego.mostrarTablero();

        clienteInvitado.salida.writeUTF("Has aceptado la invitación de '" + invitador + "'. ¡Comienza el juego de Gato!\n" +
                "Tú eres 'O'. Espera el turno de 'X'.\n" + tableroInicial + instrucciones);

        clienteInvitador.salida.writeUTF("El usuario '" + invitado + "' ha aceptado tu invitación. ¡Comienza el juego de Gato!\n" +
                "Tú eres 'X'. Es tu turno.\n" + tableroInicial + instrucciones);
    }

    public void rechazarInvitacion(String invitado, String invitador) throws IOException {
        Set<String> invitadores = estado.invitacionesRecibidas.get(invitado);
        if (invitadores == null || !invitadores.contains(invitador)) {
            estado.getCliente(invitado).salida.writeUTF("No tienes una invitación de '" + invitador + "'.");
            return;
        }
        invitadores.remove(invitador);
        estado.getCliente(invitado).salida.writeUTF("Has rechazado la invitacion de '" + invitador + "'.");
        UnCliente clienteInvitador = estado.getCliente(invitador);
        if (clienteInvitador != null) {
            clienteInvitador.salida.writeUTF("El usuario '" + invitado + "' ha rechazado tu invitacion.");
        }
    }

    public void finalizarPartida(String usuario1, String usuario2) throws IOException {
        String gameId = crearGameId(usuario1, usuario2);


        if (estado.partidasActivas.containsKey(usuario1)) {
            estado.partidasActivas.get(usuario1).remove(usuario2);
        }
        if (estado.partidasActivas.containsKey(usuario2)) {
            estado.partidasActivas.get(usuario2).remove(usuario1);
        }

        estado.tablerosPartidas.remove(gameId);
        UnCliente cliente1 = estado.getCliente(usuario1);
        UnCliente cliente2 = estado.getCliente(usuario2);

        if (cliente1 != null) {
            cliente1.salida.writeUTF("La partida con '" + usuario2 + "' ha terminado.");
            if (cliente1.getOponenteEnFoco() != null && cliente1.getOponenteEnFoco().equals(usuario2)) {
                cliente1.setOponenteEnFoco(null);
                cliente1.salida.writeUTF("Tu foco ha sido limpiado.");
            }
        }
        if (cliente2 != null) {
            cliente2.salida.writeUTF("La partida con '" + usuario1 + "' ha terminado.");
            if (cliente2.getOponenteEnFoco() != null && cliente2.getOponenteEnFoco().equals(usuario1)) {
                cliente2.setOponenteEnFoco(null);
                cliente2.salida.writeUTF("Tu foco ha sido limpiado.");
            }
        }
    }
}