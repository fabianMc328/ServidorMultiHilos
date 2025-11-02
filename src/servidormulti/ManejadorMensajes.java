package servidormulti;

import java.io.IOException;
import static servidormulti.ServidorMulti.clientes;

public class ManejadorMensajes {
    private final BloqueosBD bloqueosBD;
    private final UsuariosBD usuariosBD;
    final ManejadorInvitaciones manejadorInvitaciones;
    private final RankingBD rankingBD;
    private final GruposBD gruposBD;
    private final ManejadorGrupos manejadorGrupos;

    public ManejadorMensajes(BloqueosBD bloqueosBD, UsuariosBD usuariosBD, RankingBD rankingBD, GruposBD gruposBD, ManejadorGrupos manejadorGrupos) {
        this.bloqueosBD = bloqueosBD;
        this.usuariosBD = usuariosBD;
        this.rankingBD = rankingBD;
        this.manejadorInvitaciones = new ManejadorInvitaciones();
        this.gruposBD = gruposBD;
        this.manejadorGrupos = manejadorGrupos;
    }

    public void procesar(String mensaje, UnCliente remitente) throws IOException {
        String nombreRemitente = remitente.getNombreUsuario() != null ? remitente.getNombreUsuario() : "Invitado-" + remitente.getClienteId();

        if (mensaje.startsWith("/invitar ")) {
            if (remitente.getNombreUsuario() == null) {
                remitente.salida.writeUTF("Debes iniciar sesión para enviar invitaciones.");
                return;
            }
            String invitado = mensaje.substring(9).trim();
            manejadorInvitaciones.enviarInvitacion(nombreRemitente, invitado);
            return;
        }
        if (mensaje.startsWith("/aceptar ")) {
            if (remitente.getNombreUsuario() == null) {
                remitente.salida.writeUTF("Debes iniciar sesión para aceptar invitaciones.");
                return;
            }
            String invitador = mensaje.substring(9).trim();
            manejadorInvitaciones.aceptarInvitacion(nombreRemitente, invitador);
            return;
        }
        if (mensaje.startsWith("/rechazar ")) {
            if (remitente.getNombreUsuario() == null) {
                remitente.salida.writeUTF("Debes iniciar sesión para rechazar invitaciones.");
                return;
            }
            String invitador = mensaje.substring(10).trim();
            manejadorInvitaciones.rechazarInvitacion(nombreRemitente, invitador);
            return;
        }
        if (mensaje.equalsIgnoreCase("/ranking")) {
            if (remitente.getNombreUsuario() == null) {
                remitente.salida.writeUTF("Debes iniciar sesión para ver el ranking.");
                return;
            }
            String ranking = rankingBD.getRankingGeneral();
            remitente.salida.writeUTF(ranking);
            return;
        }
        if (mensaje.startsWith("/h2h ")) {
            if (remitente.getNombreUsuario() == null) {
                remitente.salida.writeUTF("Debes iniciar sesión para ver estadísticas H2H.");
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
        if (ServidorMulti.partidasActivas.containsKey(nombreRemitente)) {
            String oponente = ServidorMulti.partidasActivas.get(nombreRemitente);
            UnCliente clienteOponente = ServidorMulti.clientes.get(oponente);
            if (clienteOponente != null) {
                if (mensaje.startsWith("/gato ")) {
                    procesarMovimientoGato(mensaje, remitente, clienteOponente);
                } else {
                    String texto = "[Juego Gato] " + nombreRemitente + ": " + mensaje;
                    clienteOponente.salida.writeUTF(texto);
                    remitente.salida.writeUTF(texto);
                }
            } else {
                remitente.salida.writeUTF("Tu oponente ya no está conectado. La partida terminó.");
                if(remitente.getNombreUsuario() != null) {
                    procesarAbandono(oponente, nombreRemitente);
                }
            }
            return;
        }
        if (mensaje.startsWith("/bloquear ")) {
            bloquearUsuario(mensaje, remitente);
            return;
        } else if (mensaje.startsWith("/desbloquear ")) {
            desbloquearUsuario(mensaje, remitente);
            return;
        }
        if (mensaje.startsWith("/")) {
            manejadorGrupos.procesarComandoGrupo(mensaje, remitente);
            return;
        }
        if (mensaje.startsWith("@")) {
            procesarMensajePrivado(mensaje, remitente, nombreRemitente);
        } else {
            procesarMensajeGrupo(mensaje, remitente, nombreRemitente);
        }
    }

    private void bloquearUsuario(String mensaje, UnCliente remitente) throws IOException {
        if (remitente.getNombreUsuario() == null) {
            remitente.salida.writeUTF("Debes iniciar sesión para bloquear usuarios.");
            return;
        }
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
        if (remitente.getNombreUsuario() == null) {
            remitente.salida.writeUTF("Debes iniciar sesión para desbloquear usuarios.");
            return;
        }
        String aDesbloquear = mensaje.substring(12).trim();
        boolean exito = bloqueosBD.desbloquearUsuario(remitente.getNombreUsuario(), aDesbloquear);
        remitente.salida.writeUTF(exito ? "Has desbloqueado a " + aDesbloquear : "Ese usuario no estaba bloqueado.");
    }

    private void procesarMensajePrivado(String mensaje, UnCliente remitente, String nombreRemitente) throws IOException {
        if (remitente.getNombreUsuario() == null) {
            remitente.salida.writeUTF("Debes iniciar sesión para enviar mensajes privados.");
            return;
        }
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


            UnCliente clienteDestino = clientes.get(nombreLimpio);

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

        for (UnCliente clienteDestino : clientes.values()) {

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

    private void procesarMovimientoGato(String mensaje, UnCliente remitente, UnCliente oponente) throws IOException {
        String nombreRemitente = remitente.getNombreUsuario();
        String nombreOponente = oponente.getNombreUsuario();

        TableroGato tablero = ServidorMulti.tablerosPartidas.get(nombreRemitente);

        if (tablero == null || tablero.isTerminado()) {
            remitente.salida.writeUTF("No hay ninguna partida activa o ya ha terminado.");
            return;
        }

        char simboloRemitente = ServidorMulti.simbolosJugadores.get(nombreRemitente);

        if (tablero.getTurno() != simboloRemitente) {
            remitente.salida.writeUTF("No es tu turno.");
            return;
        }

        String[] partes = mensaje.split(" ");
        if (partes.length != 3) {
            remitente.salida.writeUTF("Comando inválido. Usa: /gato [fila] [columna]");
            return;
        }

        try {
            int fila = Integer.parseInt(partes[1]);
            int col = Integer.parseInt(partes[2]);

            boolean exito = tablero.hacerMovimiento(fila, col);
            if (!exito) {
                remitente.salida.writeUTF("Movimiento inválido. (Posición ocupada o fuera de rango [0-2]).");
                return;
            }


            String estadoTablero = "\n" + tablero.mostrarTablero();
            remitente.salida.writeUTF("Tu movimiento:" + estadoTablero);
            oponente.salida.writeUTF("Movimiento de " + nombreRemitente + ":" + estadoTablero);

            if (tablero.verificarGanador()) {
                tablero.setTerminado(true);
                remitente.salida.writeUTF("¡Felicidades, has ganado!");
                oponente.salida.writeUTF("¡" + nombreRemitente + " ha ganado la partida!");

                rankingBD.actualizarResultados(nombreRemitente, nombreOponente, false);

                manejadorInvitaciones.finalizarPartida(nombreRemitente, nombreOponente);

            } else if (tablero.tableroCompleto()) {
                tablero.setTerminado(true);
                remitente.salida.writeUTF("¡La partida ha terminado en empate!");
                oponente.salida.writeUTF("¡La partida ha terminado en empate!");

                rankingBD.actualizarResultados(nombreRemitente, nombreOponente, true);

                manejadorInvitaciones.finalizarPartida(nombreRemitente, nombreOponente);

            } else {

                tablero.cambiarTurno();
                remitente.salida.writeUTF("Turno de " + nombreOponente + ".");
                oponente.salida.writeUTF("Es tu turno.");
            }

        } catch (NumberFormatException e) {
            remitente.salida.writeUTF("Comando inválido. Fila y columna deben ser números (0, 1, o 2).");
        }
    }

    public void procesarAbandono(String usuarioAbandona, String oponenteGana) throws IOException {
        if (rankingBD != null) {
            rankingBD.actualizarResultados(oponenteGana, usuarioAbandona, false);
        }
        manejadorInvitaciones.finalizarPartida(usuarioAbandona, oponenteGana);
    }
}