package clientemulti;
import servidormulti.Constantes; // Importado

import java.io.IOException;
import java.net.Socket;
public class ClienteMulti {

    public static void main(String[] args) throws IOException {
        Socket s = new Socket(Constantes.HOST_SERVIDOR, Constantes.PUERTO_SERVIDOR);

        ParaMandar paraMandar = new ParaMandar(s);
        Thread hiloParaMandar = new Thread(paraMandar);
        hiloParaMandar.start();

        ParaRecibir paraRecibir = new ParaRecibir(s);
        Thread hiloParaRecibir = new Thread(paraRecibir);
        hiloParaRecibir.start();
    }

}