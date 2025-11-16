package servidormulti;

import servidormulti.BD.BloqueosBD;
import servidormulti.BD.GruposBD;
import servidormulti.BD.RankingBD;
import servidormulti.BD.UsuariosBD;
import servidormulti.Servicios.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServidorMulti {

    private static final EstadoServidor estado = new EstadoServidor();


    public static void main(String[] args) throws IOException {


        UsuariosBD usuariosBD = new UsuariosBD();
        BloqueosBD bloqueosBD = new BloqueosBD();
        RankingBD rankingBD = new RankingBD();
        GruposBD gruposBD = new GruposBD();
        GeneradorAyuda generadorAyuda = new GeneradorAyuda();



        ManejadorGrupos manejadorGrupos = new ManejadorGrupos(gruposBD, estado);
        ManejadorUsuarios manejadorUsuarios = new ManejadorUsuarios(usuariosBD, estado);
        ManejadorInvitaciones manejadorInvitaciones = new ManejadorInvitaciones(bloqueosBD, estado);

        ManejadorMensajes manejadorMensajes = new ManejadorMensajes(
                bloqueosBD,
                usuariosBD,
                rankingBD,
                gruposBD,
                manejadorGrupos,
                manejadorUsuarios,
                manejadorInvitaciones,
                estado,
                generadorAyuda
        );


        System.out.println("Servidor de chat iniciado...");
        try (ServerSocket servidorSocket = new ServerSocket(Constantes.PUERTO_SERVIDOR)) {
            int contadorId = 0;
            while (true) {
                Socket s = servidorSocket.accept();
                String clienteId = String.valueOf(contadorId++);

                UnCliente unCliente = new UnCliente(s, clienteId, manejadorUsuarios, manejadorMensajes, manejadorGrupos, estado);
                estado.agregarCliente(clienteId, unCliente);

                new Thread(unCliente).start();
                System.out.println("Se conectó un nuevo cliente con ID temporal: " + clienteId);
            }
        }
    }
}