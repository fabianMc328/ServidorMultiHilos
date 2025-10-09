package servidormulti;

import java.io.*;

public class ManejadorUsuarios {
    private final File ArchivosUsuarios= new File("Usuarios.txt");


    public ManejadorUsuarios() {

        try{
            if (!ArchivosUsuarios.exists()) {
                ArchivosUsuarios.createNewFile();

            }
        }catch (Exception e){
            System.out.println("Error al crear el archivo de usuarios");
        }



    }
public synchronized boolean RegistrarUsuario(String usuario, String contraseña){

        try {
            BufferedReader LecturaDeArchivo = new BufferedReader(new FileReader(ArchivosUsuarios));
String linea;
while ((linea = LecturaDeArchivo.readLine()) != null){
String[]partes = linea.split(":");
if (   (partes[0].length() > 0) &&  (partes[0].equals(usuario)) ){
    return false;

}
}

        }catch (Exception e){
            System.out.println("UPS, algo paso al leer el archivo");

        }
try {
    PrintWriter escribir = new PrintWriter(new FileWriter(ArchivosUsuarios, true));
escribir.write(usuario + ":" + contraseña);
return true;
}catch (Exception e){
    System.out.println("Error al registrar a nuevo usuario");
    return false;
}




}//llave del metodo principal
}
