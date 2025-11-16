package servidormulti.comandos;

import servidormulti.BD.BloqueosBD;
import servidormulti.Servicios.GeneradorAyuda;
import servidormulti.UnCliente;
import servidormulti.BD.UsuariosBD;

import java.io.IOException;

public class ComandoBloquear implements Comando {

    private final BloqueosBD bloqueosBD;
    private final UsuariosBD usuariosBD;
    private final GeneradorAyuda generadorAyuda;

    public ComandoBloquear(BloqueosBD bloqueosBD, UsuariosBD usuariosBD, GeneradorAyuda generadorAyuda) {
        this.bloqueosBD = bloqueosBD;
        this.usuariosBD = usuariosBD;
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
            remitente.salida.writeUTF("Uso: /bloquear <usuario>");
            return;
        }

        String aBloquear = argumentos[0];
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
}