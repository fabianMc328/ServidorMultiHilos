package servidormulti;

import java.io.*;

public class ArchivosUsuarios {
    private final File ArchivosUsuarios = new File("usuarios.txt");

    public ArchivosUsuarios() {
        try {
            if (!ArchivosUsuarios.exists()) {
                ArchivosUsuarios.createNewFile();

            }
        } catch (Exception e) {
            System.out.println("Error al crear el archivo de usuarios");
        }

    }

    public boolean escribirUsuarios(String usuario, String contra) {

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

        try (PrintWriter escribir = new PrintWriter(new FileWriter(ArchivosUsuarios, true))){
            escribir.println(usuario + ":" + contra);
            return true;
        }catch (Exception e){
            System.out.println("Error al registrar a nuevo usuario");
            return false;
        }


    }
    public boolean VerificarLogin(String usuario, String contra) {

        try {
            BufferedReader LecturaDeArchivo = new BufferedReader(new FileReader(ArchivosUsuarios));
            String linea;
            while ((linea = LecturaDeArchivo.readLine()) != null){
                String[]partes = linea.split(":");
                if(partes[0].equals(usuario) && partes[1].equals(contra)){
                    return true;

                }

            }
        }catch (Exception e){
            System.out.println("Error al Verificar login");
        }
        return false;
    }//llave del metodo


}