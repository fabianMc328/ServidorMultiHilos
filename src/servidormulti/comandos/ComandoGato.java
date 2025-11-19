package servidormulti.comandos;

import servidormulti.*;
import servidormulti.bd.RankingBD;
import servidormulti.Juego.TableroGato;
import servidormulti.servicios.ManejadorInvitaciones;

import java.io.IOException;
import java.util.Arrays;

public class ComandoGato implements Comando {

    private final EstadoServidor estado;
    private final RankingBD rankingBD;
    private final ManejadorInvitaciones manejadorInvitaciones;

    public ComandoGato(EstadoServidor estado, RankingBD rankingBD, ManejadorInvitaciones manejadorInvitaciones) {
        this.estado = estado;
        this.rankingBD = rankingBD;
        this.manejadorInvitaciones = manejadorInvitaciones;
    }

    @Override
    public void ejecutar(UnCliente remitente, String[] argumentos) throws IOException {
        if (remitente.getNombreUsuario() == null) {
            remitente.salida.writeUTF("Debes iniciar sesion para jugar.");
            return;
        }

        String nombreRemitente = remitente.getNombreUsuario();
        String nombreOponente = remitente.getOponenteEnFoco();

        if (nombreOponente == null) {
            remitente.salida.writeUTF("No estas enfocado en ninguna partida. Usa /juego-focus <oponente> para elegir una.");
            return;
        }

        UnCliente clienteOponente = estado.getCliente(nombreOponente);

        if (clienteOponente == null) {
            remitente.salida.writeUTF("Tu oponente '" + nombreOponente + "' ya no está conectado. La partida termino.");
            manejadorInvitaciones.finalizarPartida(nombreRemitente, nombreOponente);
            return;
        }

        String gameId = crearGameId(nombreRemitente, nombreOponente);
        TableroGato tablero = estado.tablerosPartidas.get(gameId);

        if (tablero == null || tablero.isTerminado()) {
            remitente.salida.writeUTF("No hay ninguna partida activa con '" + nombreOponente + "' o ya ha terminado.");
            return;
        }

        if (!tablero.getTurno().equals(nombreRemitente)) {
            remitente.salida.writeUTF("No es tu turno en la partida con '" + nombreOponente + "'.");
            return;
        }

        if (argumentos.length < 2) {
            remitente.salida.writeUTF("Comando invalido. Usa: /gato [fila] [columna]");
            return;
        }

        try {
            int fila = Integer.parseInt(argumentos[0]);
            int col = Integer.parseInt(argumentos[1]);

            boolean exito = tablero.hacerMovimiento(fila, col);
            if (!exito) {
                remitente.salida.writeUTF("Movimiento invalido. (Posicion ocupada o fuera de rango [0-2]).");
                return;
            }

            String estadoTablero = "\n" + tablero.mostrarTablero();
            remitente.salida.writeUTF("Tu movimiento (vs " + nombreOponente + "):" + estadoTablero);
            clienteOponente.salida.writeUTF("Movimiento de " + nombreRemitente + " (vs ti):" + estadoTablero);

            if (tablero.verificarGanador()) {
                tablero.setTerminado(true);
                remitente.salida.writeUTF("¡Felicidades, has ganado!");
                clienteOponente.salida.writeUTF("¡" + nombreRemitente + " ha ganado la partida!");

                rankingBD.actualizarResultados(nombreRemitente, nombreOponente, false);
                manejadorInvitaciones.finalizarPartida(nombreRemitente, nombreOponente);

            } else if (tablero.tableroCompleto()) {
                tablero.setTerminado(true);
                remitente.salida.writeUTF("¡La partida ha terminado en empate!");
                clienteOponente.salida.writeUTF("¡La partida ha terminado en empate!");

                rankingBD.actualizarResultados(nombreRemitente, nombreOponente, true);
                manejadorInvitaciones.finalizarPartida(nombreRemitente, nombreOponente);

            } else {
                tablero.cambiarTurno();
                remitente.salida.writeUTF("Turno de " + nombreOponente + ".");
                clienteOponente.salida.writeUTF("Es tu turno.");
            }

        } catch (NumberFormatException e) {
            remitente.salida.writeUTF("Comando invalido. Fila y columna deben ser numeros (0, 1, o 2).");
        }
    }
    private String crearGameId(String j1, String j2) {
        String[] nombres = {j1, j2};
        Arrays.sort(nombres);
        return nombres[0] + "-" + nombres[1];
    }
}