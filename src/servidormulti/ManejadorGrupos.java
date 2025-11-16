package servidormulti;

import java.io.IOException;
import java.util.List;


public class ManejadorGrupos {
    private final GruposBD gruposBD;
    private final EstadoServidor estado;

    public ManejadorGrupos(GruposBD gruposBD, EstadoServidor estado) {
        this.gruposBD = gruposBD;
        this.estado = estado;
    }


    public boolean procesarComandoGrupo(String mensaje, UnCliente remitente) throws IOException {
        String[] partes = mensaje.split(" ", 2);
        String comando = partes[0].toLowerCase();
        String argumento = (partes.length > 1) ? partes[1].trim().toLowerCase() : "";


        if (comando.equals("/lista-grupos")) {
            remitente.salida.writeUTF(gruposBD.getListaGrupos());
            return true;
        }

        if (remitente.getNombreUsuario() == null) {
            return false;
        }
        switch (comando) {
            case "/crear-grupo":
                if (argumento.isEmpty()) {
                    remitente.salida.writeUTF("Uso: /crear-grupo [nombre]");
                    return true;
                }
                crearGrupo(argumento, remitente);
                return true;
            case "/borrar-grupo":
                if (argumento.isEmpty()) {
                    remitente.salida.writeUTF("Uso: /borrar-grupo [nombre]");
                    return true;
                }
                borrarGrupo(argumento, remitente);
                return true;
            case "/unirse-grupo":
                if (argumento.isEmpty()) {
                    remitente.salida.writeUTF("Uso: /unirse-grupo [nombre]");
                    return true;
                }
                cambiarGrupo(argumento, remitente, false);
                return true;
            case "/abandonar-grupo":
                abandonarGrupo(remitente);
                return true;
            default:
                return false;
        }
    }

    private void crearGrupo(String nombreGrupo, UnCliente remitente) throws IOException {
        if (nombreGrupo.equalsIgnoreCase(Constantes.NOMBRE_GRUPO_TODOS)) {
            remitente.salida.writeUTF("No puedes crear un grupo llamado 'todos'.");
            return;
        }

        boolean exito = gruposBD.crearGrupo(nombreGrupo, remitente.getNombreUsuario());
        if (exito) {
            remitente.salida.writeUTF("Grupo '" + nombreGrupo + "' creado.");
            cambiarGrupo(nombreGrupo, remitente, false);
        } else {
            remitente.salida.writeUTF("Error: El grupo '" + nombreGrupo + "' ya existe.");
        }
    }

    private void borrarGrupo(String nombreGrupo, UnCliente remitente) throws IOException {
        if (nombreGrupo.equalsIgnoreCase(Constantes.NOMBRE_GRUPO_TODOS)) {
            remitente.salida.writeUTF("No puedes borrar el grupo 'todos'.");
            return;
        }

        int idGrupoBorrado = gruposBD.borrarGrupo(nombreGrupo, remitente.getNombreUsuario());

        if (idGrupoBorrado != -1) {
            remitente.salida.writeUTF("Grupo '" + nombreGrupo + "' borrado.");
            for (UnCliente cliente : estado.clientes.values()) {
                if (cliente.getIdGrupoActual() == idGrupoBorrado) {
                    cliente.salida.writeUTF("El grupo '" + nombreGrupo + "' ha sido borrado por su creador.");
                    cambiarGrupo(Constantes.NOMBRE_GRUPO_TODOS, cliente, true);
                }
            }
        } else {
            remitente.salida.writeUTF("Error al borrar el grupo (no existe o no tienes permisos).");
        }
    }

    private void abandonarGrupo(UnCliente remitente) throws IOException {
        if (remitente.getIdGrupoActual() == Constantes.ID_GRUPO_TODOS) { // 1 es 'todos'
            remitente.salida.writeUTF("No puedes abandonar el grupo 'todos'.");
            return;
        }

        String nombreGrupoAnterior = remitente.getNombreGrupoActual();
        gruposBD.salirDeGrupo(remitente.getNombreUsuario(), remitente.getIdGrupoActual());

        remitente.salida.writeUTF("Has abandonado el grupo '" + nombreGrupoAnterior + "'.");
        cambiarGrupo(Constantes.NOMBRE_GRUPO_TODOS, remitente, false);
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

        long ultimoVisto = gruposBD.getUltimoMensajeVisto(remitente.getNombreUsuario(), idGrupoNuevo);

        List<String> mensajesNuevos = gruposBD.getMensajesNuevos(
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

    public void unirseGrupoSinHistorial(String nombreGrupo, UnCliente remitente) throws IOException {
        int idGrupoNuevo = gruposBD.getGrupoId(nombreGrupo);
        if (idGrupoNuevo == -1) {
            remitente.salida.writeUTF("Error: El grupo '" + nombreGrupo + "' no existe.");
            return;
        }
        gruposBD.unirseAGrupo(remitente.getNombreUsuario(), idGrupoNuevo);
        remitente.setGrupoActual(idGrupoNuevo, nombreGrupo);
        remitente.salida.writeUTF("Te has unido al grupo: '" + nombreGrupo + "'.");
        remitente.salida.writeUTF("No hay mensajes nuevos en este grupo.");
        actualizarEstadoLectura(remitente);
    }

    public void actualizarEstadoLectura(UnCliente cliente) {
        if (cliente.getNombreUsuario() == null) return;

        long maxId = gruposBD.getMaxMensajeId(cliente.getIdGrupoActual());
        if (maxId > 0) {
            long ultimoVisto = gruposBD.getUltimoMensajeVisto(cliente.getNombreUsuario(), cliente.getIdGrupoActual());
            if (maxId > ultimoVisto) {
                gruposBD.actualizarUltimoMensajeVisto(cliente.getNombreUsuario(), cliente.getIdGrupoActual(), maxId);
            }
        }
    }
}