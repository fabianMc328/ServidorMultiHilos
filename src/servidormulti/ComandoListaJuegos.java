package servidormulti;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;

public class ComandoListaJuegos implements Comando {

    private final EstadoServidor estado;

    public ComandoListaJuegos(EstadoServidor estado) {
        this.estado = estado;
    }

    @Override
    public void ejecutar(UnCliente remitente, String[] argumentos) throws IOException {
        if (remitente.getNombreUsuario() == null) {
            remitente.salida.writeUTF("Debes iniciar sesion para ver tus partidas.");
            return;
        }

        String nombreRemitente = remitente.getNombreUsuario();
        Set<String> oponentes = estado.partidasActivas.get(nombreRemitente);

        if (oponentes == null || oponentes.isEmpty()) {
            remitente.salida.writeUTF("No tienes ninguna partida activa.");
            return;
        }

        StringBuilder sb = new StringBuilder("--- Tus Partidas Activas ---\n");
        String focoActual = remitente.getOponenteEnFoco();

        for (String oponente : oponentes) {
            String gameId = crearGameId(nombreRemitente, oponente);
            TableroGato tablero = estado.tablerosPartidas.get(gameId);

            if (tablero == null) continue;

            sb.append("- Vs: ").append(oponente);

            if (oponente.equals(focoActual)) {
                sb.append(" (ENFOCADO)");
            }

            if (tablero.getTurno().equals(nombreRemitente)) {
                sb.append(" - (Es tu turno)\n");
            } else {
                sb.append(" - (Turno de ").append(oponente).append(")\n");
            }
        }
        remitente.salida.writeUTF(sb.toString());
    }


    private String crearGameId(String j1, String j2) {
        String[] nombres = {j1, j2};
        Arrays.sort(nombres);
        return nombres[0] + "-" + nombres[1];
    }
}