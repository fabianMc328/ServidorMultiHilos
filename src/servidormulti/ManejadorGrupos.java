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


    }
    private void borrarGrupo(String nombreGrupo, UnCliente remitente) throws IOException {
        if (nombreGrupo.equalsIgnoreCase("todos")) {
            remitente.salida.writeUTF("No puedes borrar el grupo 'todos'.");
            return;
        }
    }
}