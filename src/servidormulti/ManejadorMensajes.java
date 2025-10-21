package servidormulti;

import java.io.IOException;
import static servidormulti.ServidorMulti.clientes;

public class ManejadorMensajes {
    private final BloqueosBD bloqueosBD;
    private final UsuariosBD usuariosBD;
    private final ManejadorInvitaciones manejadorInvitaciones;

    public ManejadorMensajes(BloqueosBD bloqueosBD, UsuariosBD usuariosBD) {
        this.bloqueosBD = bloqueosBD;
        this.usuariosBD = usuariosBD;
        this.manejadorInvitaciones = new ManejadorInvitaciones();
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

        if (ServidorMulti.partidasActivas.containsKey(nombreRemitente)) {
            String oponente = ServidorMulti.partidasActivas.get(nombreRemitente);
            UnCliente clienteOponente = ServidorMulti.clientes.get(oponente);

            if (clienteOponente != null) {
                String texto = "[Juego Gato] " + nombreRemitente + ": " + mensaje;
                clienteOponente.salida.writeUTF(texto);
                remitente.salida.writeUTF(texto);
            } else {
                remitente.salida.writeUTF("Tu oponente ya no está conectado. La partida terminó.");
                manejadorInvitaciones.finalizarPartida(nombreRemitente, oponente);
            }
            return;
        }

        if (mensaje.startsWith("/bloquear ")) {
            bloquearUsuario(mensaje, remitente);
        } else if (mensaje.startsWith("/desbloquear ")) {
            desbloquearUsuario(mensaje, remitente);
        } else if (mensaje.startsWith("@")) {
            procesarMensajePrivado(mensaje, remitente, nombreRemitente);
        } else {
            procesarMensajeGlobal(mensaje, remitente, nombreRemitente);
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
        String todosLosDestinos = partes[0].substring(1); // Quita la '@'
        String mensajePrivado = partes[1];
        String[] destinosIndividuales = todosLosDestinos.split(",");

        for (String destino : destinosIndividuales) {
            String nombreLimpio = destino.trim();
            if (nombreLimpio.isEmpty()) {
                continue;
            }

            UnCliente clienteDestino = clientes.get(nombreLimpio);

            if (clienteDestino != null) {
                if (bloqueosBD.estaBloqueado(nombreLimpio, remitente.getNombreUsuario())) {
                    remitente.salida.writeUTF("No puedes enviar mensaje a " + nombreLimpio + " porque te tiene bloqueado.");
                    continue;
                }
                clienteDestino.salida.writeUTF("(Privado) " + nombreRemitente + " DICE: " + mensajePrivado);
            } else {
                remitente.salida.writeUTF("El usuario '" + nombreLimpio + "' no está conectado.");
            }
        }
    }

    private void procesarMensajeGlobal(String mensaje, UnCliente remitente, String nombreRemitente) throws IOException {
        String mensajeCompleto = nombreRemitente + " DICE: " + mensaje;

        for (UnCliente cliente : clientes.values()) {
            if (cliente != remitente) {
                boolean puedeEnviar = true;
                if (remitente.getNombreUsuario() != null && cliente.getNombreUsuario() != null) {
                    if (bloqueosBD.estaBloqueado(cliente.getNombreUsuario(), remitente.getNombreUsuario())) {
                        puedeEnviar = false;
                    }
                }
                if (puedeEnviar) {
                    cliente.salida.writeUTF(mensajeCompleto);
                }
            }
        }
    }
}
