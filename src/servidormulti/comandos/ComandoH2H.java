package servidormulti.comandos;

import servidormulti.servicios.GeneradorAyuda;
import servidormulti.bd.RankingBD;
import servidormulti.UnCliente;
import servidormulti.bd.UsuariosBD;

import java.io.IOException;

public class ComandoH2H implements Comando {

    private final RankingBD rankingBD;
    private final UsuariosBD usuariosBD;
    private final GeneradorAyuda generadorAyuda;

    public ComandoH2H(RankingBD rankingBD, UsuariosBD usuariosBD, GeneradorAyuda generadorAyuda) {
        this.rankingBD = rankingBD;
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
            remitente.salida.writeUTF("Uso: /h2h <oponente>");
            return;
        }

        String oponente = argumentos[0];
        if (oponente.equalsIgnoreCase(remitente.getNombreUsuario())) {
            remitente.salida.writeUTF("No puedes compararte contigo mismo.");
            return;
        }
        if (!usuariosBD.existeUsuario(oponente)) {
            remitente.salida.writeUTF("El usuario '" + oponente + "' no existe.");
            return;
        }
        String h2hStats = rankingBD.getH2H(remitente.getNombreUsuario(), oponente);
        remitente.salida.writeUTF(h2hStats);
    }
}