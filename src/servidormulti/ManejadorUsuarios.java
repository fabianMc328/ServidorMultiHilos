package servidormulti;


import java.io.*;

public class ManejadorUsuarios {



    public ManejadorUsuarios() {


    }
public synchronized boolean RegistrarUsuario(String usuario, String contraseña){
ArchivosUsuarios arch = new ArchivosUsuarios();
boolean siFueRegistrado = arch.escribirUsuarios(usuario,contraseña);

if(siFueRegistrado){
    return true;
}else{return false;}


}//llave del metodo principal

public synchronized boolean VerificarUsuario(String usuario,String contra){
        ArchivosUsuarios arch = new ArchivosUsuarios();
        if (arch.VerificarLogin(usuario,contra)) {
            return true;

        }else{return false;}
}
}
