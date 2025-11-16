package servidormulti;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


public class ProcesadorComandos {
    private final Map<String, Comando> comandosDisponibles = new HashMap<>();
    private final GeneradorAyuda generadorAyuda;

    public ProcesadorComandos(
            ManejadorUsuarios manejadorUsuarios,
            ManejadorGrupos manejadorGrupos,
            RankingBD rankingBD,
            GeneradorAyuda generadorAyuda


    ) {
        this.generadorAyuda = generadorAyuda;
        comandosDisponibles.put("/ranking", new ComandoRanking(rankingBD));
        comandosDisponibles.put("/cerrar-sesion", new ComandoCerrarSesion(manejadorUsuarios, manejadorGrupos, generadorAyuda));

    }


    public boolean ejecutar(String mensaje, UnCliente remitente) throws IOException {
        String[] partes = mensaje.split(" ", 2);
        String nombreComando = partes[0].toLowerCase();

        Comando comando = comandosDisponibles.get(nombreComando);

        if (comando != null) {
            String[] argumentos;
            if (partes.length > 1) {
                argumentos = partes[1].split(" ");
            } else {
                argumentos = new String[0];
            }

            comando.ejecutar(remitente, argumentos);
            return true;
        }

        return false;
    }

    public void enviarAyuda(UnCliente remitente) throws IOException {
        generadorAyuda.enviarListaDeComandos(remitente);
    }
}