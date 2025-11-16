package servidormulti.comandos;

import servidormulti.EstadoServidor;
import servidormulti.Juego.TableroGato;
import servidormulti.UnCliente;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;

public class ComandoEnfocarJuego implements Comando {

    private final EstadoServidor estado;

    public ComandoEnfocarJuego(EstadoServidor estado) {
        this.estado = estado;
    }

    @Override
    public void ejecutar(UnCliente remitente, String[] argumentos) throws IOException {
        if (remitente.getNombreUsuario() == null) {
            remitente.salida.writeUTF("Debes iniciar sesion para cambiar de foco.");
            return;
        }
        if (argumentos.length == 0) {
            remitente.salida.writeUTF("Uso: /juego-focus <oponente>");
            return;
        }

        String nombreRemitente = remitente.getNombreUsuario();
        String oponente = argumentos[0];

        if (oponente.equalsIgnoreCase(nombreRemitente)) {
            remitente.salida.writeUTF("No puedes jugar contra ti mismo.");
            return;
        }

        Set<String> oponentes = estado.partidasActivas.get(nombreRemitente);

        if (oponentes == null || !oponentes.contains(oponente)) {
            remitente.salida.writeUTF("No tienes una partida activa con '" + oponente + "'.");
            return;
        }

        remitente.setOponenteEnFoco(oponente);
        remitente.salida.writeUTF("Ahora estás enfocado en la partida con '" + oponente + "'.");

        String gameId = crearGameId(nombreRemitente, oponente);
        TableroGato tablero = estado.tablerosPartidas.get(gameId);

        if (tablero != null) {
            remitente.salida.writeUTF(tablero.mostrarTablero());
            if (tablero.getTurno().equals(nombreRemitente)) {
                remitente.salida.writeUTF("Es tu turno.");
            } else {
                remitente.salida.writeUTF("Es el turno de '" + oponente + "'.");
            }
        }
    }

    private String crearGameId(String j1, String j2) {
        String[] nombres = {j1, j2};
        Arrays.sort(nombres);
        return nombres[0] + "-" + nombres[1];
    }
}