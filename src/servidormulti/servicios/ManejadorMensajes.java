package servidormulti.servicios;

import servidormulti.bd.*;
import servidormulti.UnCliente;
import servidormulti.EstadoServidor;
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
                manejadorUsuarios, manejadorGrupos, rankingBD, generadorAyuda,
                manejadorInvitaciones, estado, bloqueosBD, usuariosBD
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
            boolean comandoEjecutado = procesadorComandos.ejecutar(mensaje, remitente);
            if (!comandoEjecutado) {
                boolean comandoDeGrupo = manejadorGrupos.procesarComandoGrupo(mensaje, remitente);
                if (!comandoDeGrupo) {
                    remitente.salida.writeUTF("Comando desconocido.");
                    generadorAyuda.enviarListaDeComandos(remitente);
                }
            }
            return;
        }

        if (remitente.getOponenteEnFoco() != null) {
            String mensajeAutomatico = "@" + remitente.getOponenteEnFoco() + " " + mensaje;
            procesarMensajePrivado(mensajeAutomatico, remitente, nombreRemitente);
            return;
        }

        procesarMensajeGrupo(mensaje, remitente, nombreRemitente);
    }

    private void procesarMensajePrivado(String mensaje, UnCliente remitente, String nombreRemitente) throws IOException {
        String[] partes = mensaje.split(" ", 2);
        if (partes.length < 2) {
            remitente.salida.writeUTF("Formato: @usuario mensaje");
            return;
        }
        String todosLosDestinos = partes[0].substring(1);
        String mensajePrivado = partes[1];
        String[] destinosIndividuales = todosLosDestinos.split(",");

        for (String destino : destinosIndividuales) {
            String nombreLimpio = destino.trim();
            if (nombreLimpio.isEmpty()) continue;
            if (nombreLimpio.equalsIgnoreCase(nombreRemitente)) {
                remitente.salida.writeUTF("No puedes enviarte mensajes a ti mismo.");
                continue;
            }

            UnCliente clienteDestino = estado.getCliente(nombreLimpio);
            if (clienteDestino != null) {
                boolean bloqueado = bloqueosBD.estaBloqueado(nombreRemitente, nombreLimpio) || bloqueosBD.estaBloqueado(nombreLimpio, nombreRemitente);
                if (bloqueado) {
                    remitente.salida.writeUTF("No puedes enviar mensaje a " + nombreLimpio + " (bloqueo activo).");
                    continue;
                }
                try {
                    String etiqueta = (remitente.getOponenteEnFoco() != null && remitente.getOponenteEnFoco().equalsIgnoreCase(nombreLimpio))
                            ? "[JUEGO]" : "(Privado)";

                    clienteDestino.salida.writeUTF(etiqueta + " " + nombreRemitente + ": " + mensajePrivado);
                    if (remitente.getOponenteEnFoco() != null) {
                        remitente.salida.writeUTF("[TÚ->JUEGO]: " + mensajePrivado);
                    }

                } catch (IOException e) {
                    System.out.println("No se pudo enviar mensaje privado a " + nombreLimpio);
                }
            } else {
                remitente.salida.writeUTF("El usuario '" + nombreLimpio + "' no está conectado.");
            }
        }
    }

    private void procesarMensajeGrupo(String mensaje, UnCliente remitente, String nombreRemitente) throws IOException {
        if (remitente.getNombreUsuario() == null && remitente.getIdGrupoActual() != 1) {
            remitente.salida.writeUTF("Solo puedes chatear en 'todos' sin registrarte.");
            return;
        }

        gruposBD.guardarMensaje(remitente.getIdGrupoActual(), nombreRemitente, mensaje);
        String mensajeCompleto = String.format("[%s] %s DICE: %s", remitente.getNombreGrupoActual(), nombreRemitente, mensaje);

        for (UnCliente clienteDestino : estado.clientes.values()) {
            if (clienteDestino == remitente) continue;
            if (clienteDestino.getIdGrupoActual() == remitente.getIdGrupoActual()) {
                boolean bloqueado = false;
                String nombreDestino = clienteDestino.getNombreUsuario();
                String nombreOrigen = remitente.getNombreUsuario();

                if (nombreOrigen != null && nombreDestino != null) {
                    if (bloqueosBD.estaBloqueado(nombreDestino, nombreOrigen) || bloqueosBD.estaBloqueado(nombreOrigen, nombreDestino)) {
                        bloqueado = true;
                    }
                }

                if (!bloqueado) {
                    try {
                        clienteDestino.salida.writeUTF(mensajeCompleto);
                    } catch (IOException e) {
                        System.out.println("Aviso: No se pudo entregar mensaje a un cliente (posible desconexión).");
                    }
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