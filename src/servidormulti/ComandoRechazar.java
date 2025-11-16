package servidormulti;

import java.io.IOException;

public class ComandoRechazar implements Comando {

    private final ManejadorInvitaciones manejadorInvitaciones;
    private final GeneradorAyuda generadorAyuda;

    public ComandoRechazar(ManejadorInvitaciones manejadorInvitaciones, GeneradorAyuda generadorAyuda) {
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
            remitente.salida.writeUTF("Uso: /rechazar <usuario>");
            return;
        }

        String invitador = argumentos[0];
        String nombreRemitente = remitente.getNombreUsuario();
        manejadorInvitaciones.rechazarInvitacion(nombreRemitente, invitador);
    }
}