package servidormulti;

import clientemulti.ParaMandar;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class UnCliente implements Runnable {

    final DataOutputStream salida;
    final DataInputStream entrada;
    private final String clienteId;
boolean registrado=false;
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
if (!registrado) {
    int contadorMnessager = ServidorMulti.contadoresDeMensajes.get(this.clienteId);
    contadorMnessager++;
    ServidorMulti.contadoresDeMensajes.put(this.clienteId, contadorMnessager);

    if (contadorMnessager >= 3) {
        salida.writeUTF("Has enviado 3 mensajes. Por favor, registrate usando [Register] o [Login]");


        while (!registrado) {
            String comando = entrada.readUTF();
            boolean exito = ParaRegistroOlogin(comando);

            if (exito) {
                registrado = true;
            } else {
                salida.writeUTF("\nInténtalo de nuevo. Por favor, registrate usando [Register] o [Login]");
            }

        }

        salida.writeUTF("¡Ahora puedes enviar mensajes normalmente!");
        continue;
    }
}

                    manejador.procesar(mensaje);

                }

            } catch (IOException ex) {

            }
        }
    }

    public boolean ParaRegistroOlogin(String mensaje) {
        try {
            if (mensaje.equalsIgnoreCase("Register")) {
                String usuario = entrada.readUTF();
                String contra = entrada.readUTF();

                ManejadorUsuarios c = new ManejadorUsuarios();
                boolean registrado = c.RegistrarUsuario(usuario, contra);

                if (registrado) {
                    salida.writeUTF("Usuario registrado correctamente.");
                    return true;
                } else {
                    salida.writeUTF("El usuario ya existe.");
                    return false;
                }
            } else {

                if (mensaje.equalsIgnoreCase("Login")) {
                    String usuario = entrada.readUTF();
                    String contra = entrada.readUTF();
                    ManejadorUsuarios c = new ManejadorUsuarios();
                    boolean siEsta = c.VerificarUsuario(usuario, contra);

                    if (siEsta) {
                        salida.writeUTF("Sesion inciciada correctamente.");
                        return true;
                    } else {
                        salida.writeUTF("el usuario no existe.");
                        return false;
                    }


                  }
               }
            }catch (Exception e) {

        }
        return false;
    }




    }
