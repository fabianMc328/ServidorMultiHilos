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

    }
}