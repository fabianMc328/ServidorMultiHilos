package servidormulti.Servicios;

import servidormulti.UnCliente;

import java.io.IOException;

public class GeneradorAyuda {

    public void enviarListaDeComandos(UnCliente remitente) throws IOException {
        boolean logueado = (remitente.getNombreUsuario() != null);
        StringBuilder sb = new StringBuilder("--- Lista de Comandos Disponibles ---\n");

        if (logueado) {
            sb.append("\n== General ==\n");
            sb.append("@<usuario> <mensaje>   - Enviar mensaje privado.\n");
            sb.append("/bloquear <usuario>     - Bloquear a un usuario.\n");
            sb.append("/desbloquear <usuario>  - Desbloquear a un usuario.\n");
            sb.append("/cerrar-sesion          - Cerrar tu sesion actual.\n");

            sb.append("\n== Grupos ==\n");
            sb.append("/lista-grupos           - Ver todos los grupos.\n");
            sb.append("/crear-grupo <nombre>   - Crear un nuevo grupo.\n");
            sb.append("/unirse-grupo <nombre>  - Unirse a un grupo.\n");
            sb.append("/abandonar-grupo      - Salir de tu grupo actual y volver a 'todos'.\n");
            sb.append("/borrar-grupo <nombre>  - Borrar un grupo (solo creador).\n");

            sb.append("\n== Juego de Gato ==\n");
            sb.append("/invitar <usuario>      - Invitar a jugar.\n");
            sb.append("/aceptar <usuario>      - Aceptar una invitación.\n");
            sb.append("/rechazar <usuario>     - Rechazar una invitacion.\n");
            sb.append("/juego-lista            - Muestra tus partidas activas.\n");
            sb.append("/juego-focus <oponente> - Enfoca una partida para enviar comandos /gato.\n");
            sb.append("/gato <fila> <col>    - (En partida) Hacer un movimiento (ej: /gato 0 1).\n");

            sb.append("\n== Ranking ==\n");
            sb.append("/ranking                - Ver el top 20 del ranking de Gato.\n");
            sb.append("/h2h <usuario>          - Ver tu historial contra otro jugador.\n");

        } else {
            // Comandos para invitados (no logueados)
            sb.append("/login                  - Iniciar sesion.\n");
            sb.append("/register               - Registrar un nuevo usuario.\n");
            sb.append("/lista-grupos           - Ver todos los grupos.\n");
        }

        remitente.salida.writeUTF(sb.toString());
    }
}