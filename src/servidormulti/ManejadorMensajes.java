package servidormulti;

import java.io.IOException;
import static servidormulti.ServidorMulti.clientes;

public class ManejadorMensajes {

    private final UnCliente remitente;
    private final BloqueosBD bloqueosBD = ServidorMulti.bloqueosBD;
    private final UsuariosBD usuariosBD = new UsuariosBD();

    public ManejadorMensajes(UnCliente remitente) {
        this.remitente = remitente;
    }

    public void procesar(String mensaje) throws IOException {
        String nombreRemitente = remitente.getNombreUsuario();


        if (mensaje.startsWith("/bloquear ")) {
            String aBloquear = mensaje.substring(10).trim();

            if (aBloquear.equalsIgnoreCase(nombreRemitente)) {
                remitente.salida.writeUTF("No puedes bloquearte a ti mismo.");
                return;
            }

            if (!usuariosBD.existeUsuario(aBloquear)) {
                remitente.salida.writeUTF("El usuario '" + aBloquear + "' no existe.");
                return;
            }

            boolean exito = bloqueosBD.bloquearUsuario(nombreRemitente, aBloquear);
            remitente.salida.writeUTF(exito ? "Has bloqueado a " + aBloquear : "Ya estaba bloqueado o ocurrió un error.");
            return;
        }


        if (mensaje.startsWith("/desbloquear ")) {
            String aDesbloquear = mensaje.substring(12).trim();

            boolean exito = bloqueosBD.desbloquearUsuario(nombreRemitente, aDesbloquear);
            remitente.salida.writeUTF(exito ? "Has desbloqueado a " + aDesbloquear : "Ese usuario no estaba bloqueado.");
            return;
        }


        if (mensaje.startsWith("@")) {
            String[] partes = mensaje.split(" ");

            if (partes[0].contains(",")) {
                String[] destinos = partes[0].split(",");

                for (String destino : destinos) {
                    String nombre = destino.replace("@", "");
                    UnCliente cliente = clientes.get(nombre);
                    if (cliente != null) {
                        if (bloqueosBD.estaBloqueado(nombre, nombreRemitente)) {
                            remitente.salida.writeUTF("No puedes enviar mensaje a " + nombre + " porque te tiene bloqueado.");
                            continue;
                        }
                        cliente.salida.writeUTF(nombreMensaje(mensaje));
                    } else {
                        remitente.salida.writeUTF("El usuario '" + nombre + "' no está conectado.");
                    }
                }

            } else {
                String destino = partes[0].substring(1);
                UnCliente cliente = clientes.get(destino);
                if (cliente != null) {
                    if (bloqueosBD.estaBloqueado(destino, nombreRemitente)) {
                        remitente.salida.writeUTF("No puedes enviar mensaje a " + destino + " porque te tiene bloqueado.");
                        return;
                    }
                    cliente.salida.writeUTF(nombreMensaje(mensaje));
                } else {
                    remitente.salida.writeUTF("El usuario '" + destino + "' no está conectado.");
                }
            }

        } else {

            if (!mensaje.startsWith("@") && !mensaje.startsWith("/") && mensaje.trim().length() > 0) {
                for (UnCliente cliente : clientes.values()) {
                    if (cliente != remitente) {
                        if (bloqueosBD.estaBloqueado(cliente.getNombreUsuario(), nombreRemitente)) {
                            continue;
                        }
                        cliente.salida.writeUTF(nombreMensaje(mensaje));
                    }
                }

            }
        }
    }

    private String nombreMensaje(String mensaje) {
        return remitente.getNombreUsuario() + " DICE: " + mensaje;
    }

}

