package servidormulti.comandos;

import servidormulti.Servicios.GeneradorAyuda;
import servidormulti.Servicios.ManejadorGrupos;
import servidormulti.Servicios.ManejadorUsuarios;
import servidormulti.UnCliente;

import java.io.IOException;
public class ComandoCerrarSesion implements Comando {

    private final ManejadorUsuarios manejadorUsuarios;
    private final ManejadorGrupos manejadorGrupos;
    private final GeneradorAyuda generadorAyuda;

    public ComandoCerrarSesion(ManejadorUsuarios manejadorUsuarios, ManejadorGrupos manejadorGrupos, GeneradorAyuda generadorAyuda) {
        this.manejadorUsuarios = manejadorUsuarios;
        this.manejadorGrupos = manejadorGrupos;
        this.generadorAyuda = generadorAyuda;
    }

    @Override
    public void ejecutar(UnCliente remitente, String[] argumentos) throws IOException {
        if (remitente.getNombreUsuario() == null) {
            remitente.salida.writeUTF("No puedes cerrar sesion si no has iniciado sesion.");
            generadorAyuda.enviarListaDeComandos(remitente);
            return;
        }


        manejadorGrupos.actualizarEstadoLectura(remitente);
        manejadorUsuarios.cerrarSesion(remitente);

        remitente.salida.writeUTF("Sesion cerrada correctamente. Has vuelto a ser un Invitado.");
        generadorAyuda.enviarListaDeComandos(remitente);
    }
}