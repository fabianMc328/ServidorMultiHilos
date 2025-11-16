package servidormulti.comandos;

import servidormulti.Servicios.GeneradorAyuda;
import servidormulti.Servicios.ManejadorInvitaciones;
import servidormulti.UnCliente;

import java.io.IOException;

public class ComandoAceptar implements Comando {

    private final ManejadorInvitaciones manejadorInvitaciones;
    private final GeneradorAyuda generadorAyuda;

    public ComandoAceptar(ManejadorInvitaciones manejadorInvitaciones, GeneradorAyuda generadorAyuda) {
        this.manejadorInvitaciones = manejadorInvitaciones;
        this.generadorAyuda = generadorAyuda;
    }

    @Override
    public void ejecutar(UnCliente remitente, String[] argumentos) throws IOException {
        if (remitente.getNombreUsuario() == null) {
            remitente.salida.writeUTF("Debes iniciar sesion para usar este comando.");
            generadorAyuda.enviarListaDeComandos(remitente);
            return;
        }
        if (argumentos.length == 0) {
            remitente.salida.writeUTF("Uso: /aceptar <usuario>");
            return;
        }

        String invitador = argumentos[0];
        String nombreRemitente = remitente.getNombreUsuario();
        manejadorInvitaciones.aceptarInvitacion(nombreRemitente, invitador);
    }
}