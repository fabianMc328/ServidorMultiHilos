package clientemulti;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
public class ParaMandar implements Runnable{
    final BufferedReader teclado = new BufferedReader(new InputStreamReader(System.in));
    final DataOutputStream salida ;
    public static final String Register = "Register";
    public static final String Login = "Login";
    public ParaMandar(Socket s) throws IOException {
        this.salida = new DataOutputStream(s.getOutputStream());
    }

    @Override
    public void run() {
        try {
            while (true) {
                String mensaje = teclado.readLine();
                if (mensaje != null && !mensaje.isEmpty()) {
                    if (mensaje.equalsIgnoreCase(Register) ||  mensaje.equalsIgnoreCase(Login)) {
                     ParaRegistroOlogin(mensaje);
                     continue;
                    }




                        salida.writeUTF(mensaje);
                    }
                }
            }
         catch (IOException e) {
            System.out.println("Error enviando mensaje: " + e.getMessage());
        }
    }

    public void ParaRegistroOlogin(String mensaje) {

        try {
String usuario = leer("Ingresa el usuario: ");
String contra =  leer("Ingresa la contrasena: ");
salida.writeUTF(mensaje);
salida.writeUTF(usuario);
salida.writeUTF(contra);


        }catch (IOException ex) {
            System.out.println("Error enviando mensaje: " + ex.getMessage());
        }

    }

    private String leer(String cadena) throws IOException {
        System.out.print(cadena);
        return teclado.readLine();
    }




}

