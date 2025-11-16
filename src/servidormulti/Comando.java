package servidormulti;

import java.io.IOException;

public interface Comando {

    void ejecutar(UnCliente remitente, String[] argumentos) throws IOException;
}