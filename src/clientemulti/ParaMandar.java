package clientemulti;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ParaMandar implements Runnable {
    final BufferedReader teclado = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
    final DataOutputStream salida;

    public static final String Register = "/register";
    public static final String Login = "/login";

    // Esta variable se actualiza desde ParaRecibir
    public static volatile boolean estaLogueado = false;

    public ParaMandar(Socket s) throws IOException {
        this.salida = new DataOutputStream(s.getOutputStream());
    }

    @Override
    public void run() {
        try {
            while (true) {
                String mensaje = teclado.readLine();
                if (mensaje != null && !mensaje.isEmpty()) {

                    // Limpiamos espacios accidentales (ej: "/login ")
                    String mensajeLimpio = mensaje.trim();

                    // Verificamos si es un comando de autenticación
                    if (mensajeLimpio.equalsIgnoreCase(Register) || mensajeLimpio.equalsIgnoreCase(Login)) {

                        // --- AQUÍ ESTÁ EL ARREGLO ---
                        // Si ya estás logueado, te avisa y NO hace nada más.
                        if (estaLogueado) {
                            System.out.println(">> [ERROR LOCAL] Ya has iniciado sesión. No puedes usar " + mensajeLimpio + " de nuevo.");
                            System.out.println(">> Si quieres cambiar de cuenta, usa: /cerrar-sesion");
                            continue; // Reinicia el ciclo, no pide datos ni manda nada.
                        }
                        // ---------------------------

                        ParaRegistroOlogin(mensajeLimpio);
                        continue;
                    }

                    // Si no es login/register, se manda normal
                    salida.writeUTF(mensaje);
                }
            }
        } catch (IOException e) {
            System.out.println("Error enviando mensaje: " + e.getMessage());
        }
    }

    public void ParaRegistroOlogin(String mensaje) {
        try {
            // Pedimos los datos solo si NO estamos logueados
            String usuario = leer("Ingresa el usuario: ");
            String contra =  leer("Ingresa la contrasena: ");

            // Enviamos el bloque completo al servidor
            salida.writeUTF(mensaje);
            salida.writeUTF(usuario);
            salida.writeUTF(contra);
        } catch (IOException ex) {
            System.out.println("Error enviando credenciales: " + ex.getMessage());
        }
    }

    private String leer(String cadena) throws IOException {
        System.out.print(cadena);
        return teclado.readLine();
    }
}