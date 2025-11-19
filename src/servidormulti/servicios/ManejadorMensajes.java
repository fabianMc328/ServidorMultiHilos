package servidormulti.Servicios;

import servidormulti.BD.BloqueosBD;
import servidormulti.BD.GruposBD;
import servidormulti.BD.RankingBD;
import servidormulti.BD.UsuariosBD;
import servidormulti.EstadoServidor;
import servidormulti.UnCliente;

import java.io.IOException;


public class ManejadorMensajes {

    private final BloqueosBD bloqueosBD;
    private final UsuariosBD usuariosBD;
    final ManejadorInvitaciones manejadorInvitaciones;
    private final RankingBD rankingBD;
    private final GruposBD gruposBD;
    private final ManejadorGrupos manejadorGrupos;
    private final ManejadorUsuarios manejadorUsuarios;
    private final EstadoServidor estado;

    private final ProcesadorComandos procesadorComandos;
    private final GeneradorAyuda generadorAyuda;

    public ManejadorMensajes(BloqueosBD bloqueosBD, UsuariosBD usuariosBD, RankingBD rankingBD,
                             GruposBD gruposBD, ManejadorGrupos manejadorGrupos,
                             ManejadorUsuarios manejadorUsuarios, ManejadorInvitaciones manejadorInvitaciones,
                             EstadoServidor estado, GeneradorAyuda generadorAyuda) {

        this.bloqueosBD = bloqueosBD;
        this.usuariosBD = usuariosBD;
        this.rankingBD = rankingBD;
        this.manejadorInvitaciones = manejadorInvitaciones;
        this.gruposBD = gruposBD;
        this.manejadorGrupos = manejadorGrupos;
        this.manejadorUsuarios = manejadorUsuarios;
        this.estado = estado;
        this.generadorAyuda = generadorAyuda;
        this.procesadorComandos = new ProcesadorComandos(
                manejadorUsuarios,
                manejadorGrupos,
                rankingBD,
                generadorAyuda,
                manejadorInvitaciones,
                estado,
                bloqueosBD,
                usuariosBD
        );

    }


    public void procesar(String mensaje, UnCliente remitente) throws IOException {
        String nombreRemitente = remitente.getNombreUsuario() != null ? remitente.getNombreUsuario() : "Invitado-" + remitente.getClienteId();

        if (mensaje.startsWith("@")) {
            if (remitente.getNombreUsuario() == null) {
                remitente.salida.writeUTF("Debes iniciar sesion para enviar mensajes privados.");
                generadorAyuda.enviarListaDeComandos(remitente);
                return;
            }
            procesarMensajePrivado(mensaje, remitente, nombreRemitente);
            return;
        }

        if (mensaje.startsWith("/")) {
            procesadorComandos.ejecutar(mensaje, remitente);
            return;
        }
        procesarMensajeGrupo(mensaje, remitente, nombreRemitente);

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

    public void procesarAbandono(String usuarioAbandona, String oponenteGana) throws IOException {
        if (rankingBD != null) {
            rankingBD.actualizarResultados(oponenteGana, usuarioAbandona, false);
        }
        manejadorInvitaciones.finalizarPartida(usuarioAbandona, oponenteGana);
    }
}