package servidormulti;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ProcesadorComandos {

    private final Map<String, Comando> comandosDisponibles = new HashMap<>();
    private final GeneradorAyuda generadorAyuda;
    private final ManejadorGrupos manejadorGrupos;

    public ProcesadorComandos(
            ManejadorUsuarios manejadorUsuarios,
            ManejadorGrupos manejadorGrupos,
            RankingBD rankingBD,
            GeneradorAyuda generadorAyuda,
            ManejadorInvitaciones manejadorInvitaciones,
            EstadoServidor estado,
            BloqueosBD bloqueosBD,
            UsuariosBD usuariosBD
    ) {
        this.generadorAyuda = generadorAyuda;
        this.manejadorGrupos = manejadorGrupos;

        comandosDisponibles.put("/ranking", new ComandoRanking(rankingBD));
        comandosDisponibles.put("/cerrar-sesion", new ComandoCerrarSesion(manejadorUsuarios, manejadorGrupos, generadorAyuda));

        comandosDisponibles.put("/invitar", new ComandoInvitar(manejadorInvitaciones, generadorAyuda));
        comandosDisponibles.put("/aceptar", new ComandoAceptar(manejadorInvitaciones, generadorAyuda));
        comandosDisponibles.put("/rechazar", new ComandoRechazar(manejadorInvitaciones, generadorAyuda));

        comandosDisponibles.put("/h2h", new ComandoH2H(rankingBD, usuariosBD, generadorAyuda));

        comandosDisponibles.put("/bloquear", new ComandoBloquear(bloqueosBD, usuariosBD, generadorAyuda));
        comandosDisponibles.put("/desbloquear", new ComandoDesbloquear(bloqueosBD, generadorAyuda));

        comandosDisponibles.put("/gato", new ComandoGato(estado, rankingBD, manejadorInvitaciones));
        comandosDisponibles.put("/juego-lista", new ComandoListaJuegos(estado));
        comandosDisponibles.put("/juego-focus", new ComandoEnfocarJuego(estado));
    }

    public boolean ejecutar(String mensaje, UnCliente remitente) throws IOException {
        String[] partes = mensaje.split(" ", 2);
        String nombreComando = partes[0].toLowerCase();

        Comando comando = comandosDisponibles.get(nombreComando);

        if (comando != null) {
            String[] argumentos;
            if (partes.length > 1) {
                argumentos = partes[1].split(" ");
            } else {
                argumentos = new String[0];
            }

            comando.ejecutar(remitente, argumentos);
            return true;

        } else {
            boolean comandoDeGrupo = manejadorGrupos.procesarComandoGrupo(mensaje, remitente);

            if (comandoDeGrupo) {
                return true;
            } else {

                remitente.salida.writeUTF("Comando desconocido: '" + nombreComando + "'");
                generadorAyuda.enviarListaDeComandos(remitente);
                return true;
            }

        }
    }

    public void enviarAyuda(UnCliente remitente) throws IOException {
        generadorAyuda.enviarListaDeComandos(remitente);
    }
}