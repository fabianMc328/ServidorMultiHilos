package servidormulti;
import java.io.File;
import java.io.IOException;
import static servidormulti.ServidorMulti.clientes;

public class ManejadorMensajes {

    private final UnCliente remitente;
    public ManejadorMensajes(UnCliente remitente) {
        this.remitente = remitente;
    }

    public void procesar(String mensaje) throws IOException {
        if (mensaje.startsWith("@")) {
            String[] partes = mensaje.split(" ");

            if (partes[0].contains(",")) {
                String[] destinos = partes[0].split(",");

                for (String destino : destinos) {
                    String nombre = destino.replace("@", "");
                    UnCliente cliente = clientes.get(nombre);
                    if (cliente != null) {
                        cliente.salida.writeUTF(nombreMensaje(mensaje));
                    }
                }

            } else {
                String destino = partes[0].substring(1);
                UnCliente cliente = clientes.get(destino);
                if (cliente != null) {
                    cliente.salida.writeUTF(nombreMensaje(mensaje));
                }
            }

        } else {
            for (UnCliente cliente : clientes.values()) {
                if (cliente != remitente) {
                    cliente.salida.writeUTF(mensaje);
                }
            }
        }
    }

    private String nombreMensaje(String mensaje) {
        return Thread.currentThread().getName() + " DICE: " + mensaje;
    }


}
