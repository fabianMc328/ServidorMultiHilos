package servidormulti;

import clientemulti.ParaMandar;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class UnCliente implements Runnable {

    final DataOutputStream salida;
    final DataInputStream entrada;
    private final String clienteId;
    private String nombreUsuario = null; //
    boolean registrado = false;

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
                        int contador = ServidorMulti.contadoresDeMensajes.get(clienteId);
                        contador++;
                        ServidorMulti.contadoresDeMensajes.put(clienteId, contador);

                        if (contador >= 3) {
                            salida.writeUTF("Has enviado 3 mensajes. Por favor, regístrate usando [Register] o [Login]");

                            while (!registrado) {
                                String comando = entrada.readUTF();
                                boolean exito = ParaRegistroOlogin(comando);

                                if (!exito) {
                                    salida.writeUTF("Inténtalo de nuevo. Usa [Register] o [Login]");
                                }
                            }

                            salida.writeUTF("¡Ahora puedes enviar mensajes normalmente!");
                            continue;
                        }
                    }

                    manejador.procesar(mensaje);
                }

            } catch (IOException ex) {
                System.out.println("Cliente desconectado o error: " + ex.getMessage());
                break;
            }
        }
    }

    public boolean ParaRegistroOlogin(String mensaje) {
        try {
            if (mensaje.equalsIgnoreCase("Register")) {
                String usuario = entrada.readUTF();
                String contra = entrada.readUTF();

                ManejadorUsuarios manejador = new ManejadorUsuarios();
                boolean exito = manejador.RegistrarUsuario(usuario, contra);

                if (exito) {
                    salida.writeUTF("Usuario registrado correctamente.");
                    this.nombreUsuario = usuario;
                    this.registrado = true;
                    ServidorMulti.clientes.put(usuario, this);
                    return true;
                } else {
                    salida.writeUTF("El usuario ya existe.");
                }

            } else if (mensaje.equalsIgnoreCase("Login")) {
                String usuario = entrada.readUTF();
                String contra = entrada.readUTF();

                ManejadorUsuarios manejador = new ManejadorUsuarios();
                boolean exito = manejador.VerificarUsuario(usuario, contra);

                if (exito) {
                    salida.writeUTF("Sesión iniciada correctamente.");
                    this.nombreUsuario = usuario;
                    this.registrado = true;
                    ServidorMulti.clientes.put(usuario, this);
                    return true;
                } else {
                    salida.writeUTF("El usuario no existe o contraseña incorrecta.");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    // GETTERS
    public String getClienteId() {
        return clienteId;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }
}


