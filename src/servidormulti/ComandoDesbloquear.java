package servidormulti;

import java.io.IOException;

public class ComandoDesbloquear implements Comando {

    private final BloqueosBD bloqueosBD;
    private final GeneradorAyuda generadorAyuda;

    public ComandoDesbloquear(BloqueosBD bloqueosBD, GeneradorAyuda generadorAyuda) {
        this.bloqueosBD = bloqueosBD;
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
            remitente.salida.writeUTF("Uso: /desbloquear <usuario>");
            return;
        }

        String aDesbloquear = argumentos[0];
        boolean exito = bloqueosBD.desbloquearUsuario(remitente.getNombreUsuario(), aDesbloquear);
        remitente.salida.writeUTF(exito ? "Has desbloqueado a " + aDesbloquear : "Ese usuario no estaba bloqueado.");
    }
}