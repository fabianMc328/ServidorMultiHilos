package servidormulti.bd;
import java.sql.*;

public class BloqueosBD {
    public boolean bloquearUsuario(String usuarioBloqueador, String usuarioBloqueado) {
        if (estaBloqueado(usuarioBloqueador, usuarioBloqueado)) return false;
        String sql = "INSERT INTO bloqueos (usuario_bloqueador, usuario_bloqueado) VALUES (?, ?)";
        try (Connection conn = ConexionBD.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, usuarioBloqueador); ps.setString(2, usuarioBloqueado); ps.executeUpdate(); return true;
        } catch (SQLException e) {
            System.out.println("Error en base de datos.");
            return false;
        }
    }

    public boolean desbloquearUsuario(String usuarioBloqueador, String usuarioBloqueado) {
        String sql = "DELETE FROM bloqueos WHERE usuario_bloqueador = ? AND usuario_bloqueado = ?";
        try (Connection conn = ConexionBD.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, usuarioBloqueador); ps.setString(2, usuarioBloqueado); int filas = ps.executeUpdate(); return filas > 0;
        } catch (SQLException e) {
            System.out.println("Error en base de datos.");
            return false;
        }
    }

    public boolean estaBloqueado(String usuarioBloqueador, String posibleRemitente) {
        String sql = "SELECT 1 FROM bloqueos WHERE usuario_bloqueador = ? AND usuario_bloqueado = ?";
        try (Connection conn = ConexionBD.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, usuarioBloqueador); ps.setString(2, posibleRemitente); ResultSet rs = ps.executeQuery(); return rs.next();
        } catch (SQLException e) {
            System.out.println("Error en base de datos.");
            return false;
        }
    }
}