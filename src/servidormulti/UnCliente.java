package servidormulti;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class UnCliente implements Runnable {

    final DataOutputStream salida;
    final DataInputStream entrada;
    private final String clienteId;
    public UnCliente(Socket socket, String clienteId) throws IOException {
        this.clienteId = clienteId;
        salida = new DataOutputStream(socket.getOutputStream());
        entrada = new DataInputStream(socket.getInputStream());
    }

    @Override
    public void run() {
        String mensaje;
        LectorMensajes lector = new LectorMensajes(entrada);
        ManejadorMensajes manejador = new ManejadorMensajes(this);

        while (true) {
            try {
                mensaje = entrada.readUTF();
                if (mensaje != null && !mensaje.isEmpty()) {

 int contadorMnessager = ServidorMulti.contadoresDeMensajes.get(this.clienteId);
 contadorMnessager++;
 ServidorMulti.contadoresDeMensajes.put(this.clienteId, contadorMnessager);

                    if (contadorMnessager >= 3) {
                        salida.writeUTF("Has enviado 3 mensajes. Por favor, registrate.");
                    }




                    manejador.procesar(mensaje);

                }
      /*
if(mensaje.equalsIgnoreCase("logear")) {
    String usuario = entrada.readUTF();
    String contra = entrada.readUTF();

    ManejadorUsuarios c = new ManejadorUsuarios();
    boolean registrado = c.RegistrarUsuario(usuario, contra);

    if (registrado) {
        salida.writeUTF("Usuario registrado correctamente.");
    } else {
        salida.writeUTF("El usuario ya existe.");
    }
}else{

    if (mensaje.equalsIgnoreCase("verificar")) {
        String usuario = entrada.readUTF();
        String contra = entrada.readUTF();
        ManejadorUsuarios c = new ManejadorUsuarios();
         boolean siEsta = c.VerificarUsuario(usuario, contra);

         if (siEsta) {
             salida.writeUTF("el usuario existe");
         }else{salida.writeUTF("el usuario no existe.");}


    }else{

*/
            } catch (IOException ex) {

            }
        }
    }
}

