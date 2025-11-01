package servidormulti;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ManejadorGrupos {
    private final GruposBD gruposBD;
    private final Map<String, UnCliente> clientes;


    public ManejadorGrupos(GruposBD gruposBD, Map<String, UnCliente> clientes) {
        this.gruposBD = gruposBD;
        this.clientes = clientes;
    }


    public void procesarComandoGrupo(String mensaje, UnCliente remitente) throws IOException {
        String[] partes = mensaje.split(" ", 2);
        String comando = partes[0].toLowerCase();
        String argumento = (partes.length > 1) ? partes[1].trim().toLowerCase() : "";

        if (remitente.getNombreUsuario() == null && !comando.equals("/lista-grupos")) {
            remitente.salida.writeUTF("Debes iniciar sesión para crear, borrar o unirte a grupos.");
            return;
        }
        switch (comando) {
            case "/crear-grupo":
                if (argumento.isEmpty()) {
                    remitente.salida.writeUTF("Uso: /crear-grupo [nombre]");
                    return;
                }
                crearGrupo(argumento, remitente);
                break;
            case "/borrar-grupo":
                if (argumento.isEmpty()) {
                    remitente.salida.writeUTF("Uso: /borrar-grupo [nombre]");
                    return;
                }
                borrarGrupo(argumento, remitente);
                break;
            case "/unirse-grupo":
                if (argumento.isEmpty()) {
                    remitente.salida.writeUTF("Uso: /unirse-grupo [nombre]");
                    return;
                }
                cambiarGrupo(argumento, remitente);
                break;
            case "/abandonar-grupo":
                abandonarGrupo(remitente);
                break;
            case "/lista-grupos":
                remitente.salida.writeUTF(gruposBD.getListaGrupos());
                break;
            default:
                remitente.salida.writeUTF("Comando desconocido. Comandos de grupo: /crear-grupo, /borrar-grupo, /unirse-grupo, /abandonar-grupo, /lista-grupos");
                break;
        }
    }

    private void crearGrupo(String nombreGrupo, UnCliente remitente) throws IOException {
        if (nombreGrupo.equalsIgnoreCase("todos")) {
            remitente.salida.writeUTF("No puedes crear un grupo llamado 'todos'.");
            return;
        }

        boolean exito = gruposBD.crearGrupo(nombreGrupo, remitente.getNombreUsuario());
        if (exito) {
            remitente.salida.writeUTF("Grupo '" + nombreGrupo + "' creado.");
            cambiarGrupo(nombreGrupo, remitente);
        } else {
            remitente.salida.writeUTF("Error: El grupo '" + nombreGrupo + "' ya existe.");
        }
    }

    public void cambiarGrupo(String nombreGrupo, UnCliente remitente) throws IOException {
        cambiarGrupo(nombreGrupo, remitente, false);
    }

    public void cambiarGrupo(String nombreGrupo, UnCliente remitente, boolean forzar) throws IOException {
        int idGrupoNuevo = gruposBD.getGrupoId(nombreGrupo);
        if (idGrupoNuevo == -1) {
            remitente.salida.writeUTF("El grupo '" + nombreGrupo + "' no existe.");
            return;
        }

        if (!forzar && remitente.getIdGrupoActual() == idGrupoNuevo) {
            remitente.salida.writeUTF("Ya te encuentras en el grupo '" + nombreGrupo + "'.");
            return;
        }
        actualizarEstadoLectura(remitente);
        gruposBD.unirseAGrupo(remitente.getNombreUsuario(), idGrupoNuevo);
        remitente.setGrupoActual(idGrupoNuevo, nombreGrupo);
        remitente.salida.writeUTF("Te has unido al grupo: '" + nombreGrupo + "'.");


        long ultimoVisto = gruposBD.obtenerUltimoMensajeVisto(remitente.getNombreUsuario(), idGrupoNuevo);


        List<String> mensajesNuevos = gruposBD.obtenerMensajesNuevos(
                idGrupoNuevo,
                ultimoVisto,
                remitente.getNombreUsuario(),
                remitente.getClienteId()
        );


        if (mensajesNuevos.isEmpty()) {
            remitente.salida.writeUTF("No hay mensajes nuevos en este grupo.");
        } else {
            remitente.salida.writeUTF("--- Mostrando " + mensajesNuevos.size() + " mensajes no leídos ---");
            for (String msg : mensajesNuevos) {
                remitente.salida.writeUTF(msg);
            }
            remitente.salida.writeUTF("--- Fin de mensajes no leídos ---");
        }


        actualizarEstadoLectura(remitente);
    }
    private void borrarGrupo(String nombreGrupo, UnCliente remitente) throws IOException {
        if (nombreGrupo.equalsIgnoreCase("todos")) {
            remitente.salida.writeUTF("No puedes borrar el grupo 'todos'.");
            return;
        }


        int idGrupoBorrado = gruposBD.borrarGrupo(nombreGrupo, remitente.getNombreUsuario());

        if (idGrupoBorrado != -1) {
            remitente.salida.writeUTF("Grupo '" + nombreGrupo + "' borrado.");
            for (UnCliente cliente : clientes.values()) {
                if (cliente.getIdGrupoActual() == idGrupoBorrado) {
                    cliente.salida.writeUTF("El grupo '" + nombreGrupo + "' ha sido borrado por su creador.");
                    cambiarGrupo("todos", cliente, true);
                }
            }
        } else {
            remitente.salida.writeUTF("Error al borrar el grupo (no existe o no tienes permisos).");
        }
    }
    private void abandonarGrupo(UnCliente remitente) throws IOException {
        if (remitente.getIdGrupoActual() == 1) { // 1 es 'todos'
            remitente.salida.writeUTF("No puedes abandonar el grupo 'todos'.");
            return;
        }

        String nombreGrupoAnterior = remitente.getNombreGrupoActual();
        gruposBD.salirDeGrupo(remitente.getNombreUsuario(), remitente.getIdGrupoActual());

        remitente.salida.writeUTF("Has abandonado el grupo '" + nombreGrupoAnterior + "'.");
        cambiarGrupo("todos", remitente);
    }
    public void actualizarEstadoLectura(UnCliente cliente) {
        if (cliente.getNombreUsuario() == null) return;

        long maxId = gruposBD.getMaxMensajeId(cliente.getIdGrupoActual());
        if (maxId > 0) {
            long ultimoVisto = gruposBD.obtenerUltimoMensajeVisto(cliente.getNombreUsuario(), cliente.getIdGrupoActual());
            if (maxId > ultimoVisto) {
                gruposBD.actualizarUltimoMensajeVisto(cliente.getNombreUsuario(), cliente.getIdGrupoActual(), maxId);
            }
        }
    }
}