package servidormulti.comandos;

import servidormulti.bd.RankingBD;
import servidormulti.UnCliente;

import java.io.IOException;

public class ComandoRanking implements Comando {

    private final RankingBD rankingBD;

    public ComandoRanking(RankingBD rankingBD) {
        this.rankingBD = rankingBD;
    }

    @Override
    public void ejecutar(UnCliente remitente, String[] argumentos) throws IOException {
        if (remitente.getNombreUsuario() == null) {
            remitente.salida.writeUTF("Debes iniciar sesion para usar este comando.");
            return;
        }

        String ranking = rankingBD.getRankingGeneral();
        remitente.salida.writeUTF(ranking);
    }
}