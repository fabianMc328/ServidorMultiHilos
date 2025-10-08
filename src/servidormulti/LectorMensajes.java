package servidormulti;

import java.io.DataInputStream;
import java.io.IOException;

public class LectorMensajes {
    private final DataInputStream entrada;

    public LectorMensajes(DataInputStream entrada) {
        this.entrada = entrada;
    }

    public String leer() throws IOException {
        return entrada.readUTF();
    }
}
