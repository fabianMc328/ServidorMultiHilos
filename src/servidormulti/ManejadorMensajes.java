package servidormulti;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;

public class ManejadorMensajes {
    private final BloqueosBD bloqueosBD;
    private final UsuariosBD usuariosBD;
    final ManejadorInvitaciones manejadorInvitaciones;
    private final RankingBD rankingBD;
    private final GruposBD gruposBD;
    private final ManejadorGrupos manejadorGrupos;
    private final ManejadorUsuarios manejadorUsuarios;


    private final EstadoServidor estado;

    public ManejadorMensajes(BloqueosBD bloqueosBD, UsuariosBD usuariosBD, RankingBD rankingBD, GruposBD gruposBD, ManejadorGrupos manejadorGrupos, ManejadorUsuarios manejadorUsuarios, ManejadorInvitaciones manejadorInvitaciones, EstadoServidor estado) { // Recibe el estado
        this.bloqueosBD = bloqueosBD;
        this.usuariosBD = usuariosBD;
        this.rankingBD = rankingBD;
        this.manejadorInvitaciones = manejadorInvitaciones;
        this.gruposBD = gruposBD;
        this.manejadorGrupos = manejadorGrupos;
        this.manejadorUsuarios = manejadorUsuarios;
        this.estado = estado;
    }


    public void procesar(String mensaje, UnCliente remitente) throws IOException {
        String nombreRemitente = remitente.getNombreUsuario() != null ? remitente.getNombreUsuario() : "Invitado-" + remitente.getClienteId();

        if (mensaje.equalsIgnoreCase("/cerrar-sesion")) {
            if (remitente.getNombreUsuario() == null) {
                remitente.salida.writeUTF("No puedes cerrar sesion si no has iniciado sesion.");
                enviarListaDeComandos(remitente);
                return;
            }

            manejadorGrupos.actualizarEstadoLectura(remitente);
            manejadorUsuarios.cerrarSesion(remitente);

            remitente.salida.writeUTF("Sesion cerrada correctamente. Has vuelto a ser un Invitado.");
            enviarListaDeComandos(remitente);
            return;
        }

        if (mensaje.startsWith("/invitar ")) {
            if (remitente.getNombreUsuario() == null) {
                remitente.salida.writeUTF("Debes iniciar sesion para usar este comando.");
                enviarListaDeComandos(remitente);
                return;
            }
            String invitado = mensaje.substring(9).trim();
            manejadorInvitaciones.enviarInvitacion(nombreRemitente, invitado);
            return;
        }
        if (mensaje.startsWith("/aceptar ")) {
            if (remitente.getNombreUsuario() == null) {
                remitente.salida.writeUTF("Debes iniciar sesion para usar este comando.");
                enviarListaDeComandos(remitente);
                return;
            }
            String invitador = mensaje.substring(9).trim();
            manejadorInvitaciones.aceptarInvitacion(nombreRemitente, invitador);
            return;
        }
        if (mensaje.startsWith("/rechazar ")) {
            if (remitente.getNombreUsuario() == null) {
                remitente.salida.writeUTF("Debes iniciar sesion para usar este comando.");
                enviarListaDeComandos(remitente);
                return;
            }
            String invitador = mensaje.substring(10).trim();
            manejadorInvitaciones.rechazarInvitacion(nombreRemitente, invitador);
            return;
        }
        if (mensaje.equalsIgnoreCase("/ranking")) {
            if (remitente.getNombreUsuario() == null) {
                remitente.salida.writeUTF("Debes iniciar sesion para usar este comando.");
                enviarListaDeComandos(remitente);
                return;
            }
            String ranking = rankingBD.getRankingGeneral();
            remitente.salida.writeUTF(ranking);
            return;
        }
        if (mensaje.startsWith("/h2h ")) {
            if (remitente.getNombreUsuario() == null) {
                remitente.salida.writeUTF("Debes iniciar sesion para usar este comando.");
                enviarListaDeComandos(remitente);
                return;
            }
            String oponente = mensaje.substring(5).trim();
            if (oponente.equalsIgnoreCase(remitente.getNombreUsuario())) {
                remitente.salida.writeUTF("No puedes compararte contigo mismo.");
                return;
            }
            if (!usuariosBD.existeUsuario(oponente)) {
                remitente.salida.writeUTF("El usuario '" + oponente + "' no existe.");
                return;
            }
            String h2hStats = rankingBD.getH2H(remitente.getNombreUsuario(), oponente);
            remitente.salida.writeUTF(h2hStats);
            return;
        }

        if (mensaje.startsWith("/gato ")) {
            if (remitente.getNombreUsuario() == null) {
                remitente.salida.writeUTF("Debes iniciar sesion para jugar.");
                return;
            }
            procesarMovimientoGato(mensaje, remitente);
            return;
        }

        if (mensaje.equalsIgnoreCase("/juego-lista")) {
            if (remitente.getNombreUsuario() == null) {
                remitente.salida.writeUTF("Debes iniciar sesion para ver tus partidas.");
                return;
            }
            listarPartidas(remitente);
            return;
        }

        if (mensaje.startsWith("/juego-focus ")) {
            if (remitente.getNombreUsuario() == null) {
                remitente.salida.writeUTF("Debes iniciar sesion para cambiar de foco.");
                return;
            }
            enfocarPartida(mensaje, remitente);
            return;
        }

        if (mensaje.startsWith("/bloquear ")) {
            if (remitente.getNombreUsuario() == null) {
                remitente.salida.writeUTF("Debes iniciar sesion para usar este comando.");
                enviarListaDeComandos(remitente);
                return;
            }
            bloquearUsuario(mensaje, remitente);
            return;
        } else if (mensaje.startsWith("/desbloquear ")) {
            if (remitente.getNombreUsuario() == null) {
                remitente.salida.writeUTF("Debes iniciar sesion para usar este comando.");
                enviarListaDeComandos(remitente);
                return;
            }
            desbloquearUsuario(mensaje, remitente);
            return;
        }

        if (mensaje.startsWith("@")) {
            if (remitente.getNombreUsuario() == null) {
                remitente.salida.writeUTF("Debes iniciar sesion para enviar mensajes privados.");
                enviarListaDeComandos(remitente);
                return;
            }
            procesarMensajePrivado(mensaje, remitente, nombreRemitente);
            return;
        }

        if (mensaje.startsWith("/")) {
            boolean comandoManejado = manejadorGrupos.procesarComandoGrupo(mensaje, remitente);

            if (!comandoManejado) {
                remitente.salida.writeUTF("Comando desconocido: '" + mensaje.split(" ")[0] + "'");
                enviarListaDeComandos(remitente);
            }
            return;
        }

        procesarMensajeGrupo(mensaje, remitente, nombreRemitente);
    }

    private void enviarListaDeComandos(UnCliente remitente) throws IOException {
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
            sb.append("/juego-lista            - Muestra tus partidas activas.\n"); // NUEVO
            sb.append("/juego-focus <oponente> - Enfoca una partida para enviar comandos /gato.\n"); // NUEVO
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

    private void bloquearUsuario(String mensaje, UnCliente remitente) throws IOException {
        String aBloquear = mensaje.substring(10).trim();
        if (aBloquear.equalsIgnoreCase(remitente.getNombreUsuario())) {
            remitente.salida.writeUTF("No puedes bloquearte a ti mismo.");
            return;
        }
        if (!usuariosBD.existeUsuario(aBloquear)) {
            remitente.salida.writeUTF("El usuario '" + aBloquear + "' no existe.");
            return;
        }
        boolean exito = bloqueosBD.bloquearUsuario(remitente.getNombreUsuario(), aBloquear);
        remitente.salida.writeUTF(exito ? "Has bloqueado a " + aBloquear : "Ya estaba bloqueado.");
    }

    private void desbloquearUsuario(String mensaje, UnCliente remitente) throws IOException {
        String aDesbloquear = mensaje.substring(12).trim();
        boolean exito = bloqueosBD.desbloquearUsuario(remitente.getNombreUsuario(), aDesbloquear);
        remitente.salida.writeUTF(exito ? "Has desbloqueado a " + aDesbloquear : "Ese usuario no estaba bloqueado.");
    }

    private void procesarMensajePrivado(String mensaje, UnCliente remitente, String nombreRemitente) throws IOException {
        String[] partes = mensaje.split(" ", 2);
        if (partes.length < 2) {
            remitente.salida.writeUTF("Formato incorrecto. Usa: @usuario1,usuario2 mensaje");
            return;
        }
        String todosLosDestinos = partes[0].substring(1);
        String mensajePrivado = partes[1];
        String[] destinosIndividuales = todosLosDestinos.split(",");

        for (String destino : destinosIndividuales) {
            String nombreLimpio = destino.trim();
            if (nombreLimpio.isEmpty()) {
                continue;
            }

            if (nombreLimpio.equalsIgnoreCase(nombreRemitente)) {
                remitente.salida.writeUTF("No puedes enviarte mensajes privados a ti mismo.");
                continue;
            }
            UnCliente clienteDestino = estado.getCliente(nombreLimpio);


            if (clienteDestino != null) {
                boolean tuBloqueaste = bloqueosBD.estaBloqueado(nombreRemitente, nombreLimpio);
                boolean teBloquearon = bloqueosBD.estaBloqueado(nombreLimpio, nombreRemitente);

                if (tuBloqueaste) {
                    remitente.salida.writeUTF("No puedes enviar mensajes a " + nombreLimpio + " porque lo tienes bloqueado.");
                    continue;
                }
                if (teBloquearon) {
                    remitente.salida.writeUTF("No puedes enviar mensajes a " + nombreLimpio + " porque te tiene bloqueado.");
                    continue;
                }

                clienteDestino.salida.writeUTF("(Privado) " + nombreRemitente + " DICE: " + mensajePrivado);
            } else {
                remitente.salida.writeUTF("El usuario '" + nombreLimpio + "' no está conectado.");
            }
        }
    }

    private void procesarMensajeGrupo(String mensaje, UnCliente remitente, String nombreRemitente) throws IOException {
        if (remitente.getNombreUsuario() == null && remitente.getIdGrupoActual() != 1) {
            remitente.salida.writeUTF("Como invitado, solo puedes chatear en 'todos'. Escribe /unirse-grupo todos");
            return;
        }

        long nuevoMensajeId = gruposBD.guardarMensaje(remitente.getIdGrupoActual(), nombreRemitente, mensaje);

        String mensajeCompleto = String.format("[%s] %s DICE: %s",
                remitente.getNombreGrupoActual(),
                nombreRemitente,
                mensaje);

        for (UnCliente clienteDestino : estado.clientes.values()) {


            if (clienteDestino == remitente) {
                continue;
            }

            if (clienteDestino.getIdGrupoActual() == remitente.getIdGrupoActual()) {

                boolean puedeEnviar = true;
                String nombreDestino = clienteDestino.getNombreUsuario();
                String nombreRemitenteLogueado = remitente.getNombreUsuario();

                if (nombreRemitenteLogueado != null && nombreDestino != null) {
                    if (bloqueosBD.estaBloqueado(nombreDestino, nombreRemitenteLogueado)) {
                        puedeEnviar = false;
                    }
                    if (bloqueosBD.estaBloqueado(nombreRemitenteLogueado, nombreDestino)) {
                        puedeEnviar = false;
                    }
                }

                if (puedeEnviar) {
                    clienteDestino.salida.writeUTF(mensajeCompleto);
                }
            }
        }
    }


    private void listarPartidas(UnCliente remitente) throws IOException {
        String nombreRemitente = remitente.getNombreUsuario();
        Set<String> oponentes = estado.partidasActivas.get(nombreRemitente); // Usa 'estado'


        if (oponentes == null || oponentes.isEmpty()) {
            remitente.salida.writeUTF("No tienes ninguna partida activa.");
            return;
        }

        StringBuilder sb = new StringBuilder("--- Tus Partidas Activas ---\n");
        String focoActual = remitente.getOponenteEnFoco();

        for (String oponente : oponentes) {
            String JuegoId = crearJuegoId(nombreRemitente, oponente);
            TableroGato tablero = estado.tablerosPartidas.get(JuegoId);

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

    private void enfocarPartida(String mensaje, UnCliente remitente) throws IOException {
        String nombreRemitente = remitente.getNombreUsuario();
        String oponente = mensaje.substring(13).trim();

        if (oponente.equalsIgnoreCase(nombreRemitente)) {
            remitente.salida.writeUTF("No puedes jugar contra ti mismo.");
            return;
        }
        Set<String> oponentes = estado.partidasActivas.get(nombreRemitente); // Usa 'estado'

        if (oponentes == null || !oponentes.contains(oponente)) {
            remitente.salida.writeUTF("No tienes una partida activa con '" + oponente + "'.");
            return;
        }

        remitente.setOponenteEnFoco(oponente);
        remitente.salida.writeUTF("Ahora estás enfocado en la partida con '" + oponente + "'.");
        String JuegoId = crearJuegoId(nombreRemitente, oponente);
        TableroGato tablero = estado.tablerosPartidas.get(JuegoId);


        if (tablero != null) {
            remitente.salida.writeUTF(tablero.mostrarTablero());
            if (tablero.getTurno().equals(nombreRemitente)) {
                remitente.salida.writeUTF("Es tu turno.");
            } else {
                remitente.salida.writeUTF("Es el turno de '" + oponente + "'.");
            }
        }
    }


    private String crearJuegoId(String j1, String j2) {
        String[] nombres = {j1, j2};
        Arrays.sort(nombres);
        return nombres[0] + "-" + nombres[1];
    }

    private void procesarMovimientoGato(String mensaje, UnCliente remitente) throws IOException {
        String nombreRemitente = remitente.getNombreUsuario();


        String nombreOponente = remitente.getOponenteEnFoco();
        if (nombreOponente == null) {
            remitente.salida.writeUTF("No estas enfocado en ninguna partida. Usa /juego-focus <oponente> para elegir una.");
            return;
        }
        UnCliente clienteOponente = estado.getCliente(nombreOponente); // Usa 'estado'
        if (clienteOponente == null) {
            remitente.salida.writeUTF("Tu oponente '" + nombreOponente + "' ya no está conectado. La partida termino.");
            manejadorInvitaciones.finalizarPartida(nombreRemitente, nombreOponente);
            return;
        }
        String gameId = crearJuegoId(nombreRemitente, nombreOponente);
        TableroGato tablero = estado.tablerosPartidas.get(gameId); // Usa 'estado'

        if (tablero == null || tablero.isTerminado()) {
            remitente.salida.writeUTF("No hay ninguna partida activa con '" + nombreOponente + "' o ya ha terminado.");
            return;
        }

        if (!tablero.getTurno().equals(nombreRemitente)) {
            remitente.salida.writeUTF("No es tu turno en la partida con '" + nombreOponente + "'.");
            return;
        }

        String[] partes = mensaje.split(" ");
        if (partes.length != 3) {
            remitente.salida.writeUTF("Comando invalido. Usa: /gato [fila] [columna]");
            return;
        }

        try {
            int fila = Integer.parseInt(partes[1]);
            int col = Integer.parseInt(partes[2]);

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

    public void procesarAbandono(String usuarioAbandona, String oponenteGana) throws IOException {
        if (rankingBD != null) {
            rankingBD.actualizarResultados(oponenteGana, usuarioAbandona, false);
        }
        manejadorInvitaciones.finalizarPartida(usuarioAbandona, oponenteGana);
    }
}