package servidormulti.bd;
import java.sql.*;

public class UsuariosBD {
    public boolean registrarUsuario(String usuario, String contra) {
        if (existeUsuario(usuario)) return false;
        String sql = "INSERT INTO usuarios (usuario, contra, status) VALUES (?, ?, ?)";
        try (Connection conn = ConexionBD.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, usuario); ps.setString(2, contra); ps.setString(3, "noRegistrado"); ps.executeUpdate(); return true;
        } catch (SQLException e) {
            System.out.println("Error en base de datos.");
            return false;
        }
    }

    public boolean existeUsuario(String usuario) {
        String sql = "SELECT usuario FROM usuarios WHERE usuario = ?";
        try (Connection conn = ConexionBD.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, usuario); ResultSet rs = ps.executeQuery(); return rs.next();
        } catch (SQLException e) {
            System.out.println("Error en base de datos.");
            return false;
        }
    }

    public boolean verificarLogin(String usuario, String contra) {
        String sql = "SELECT usuario FROM usuarios WHERE usuario = ? AND contra = ?";
        try (Connection conn = ConexionBD.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, usuario); ps.setString(2, contra); ResultSet rs = ps.executeQuery(); return rs.next();
        } catch (SQLException e) {
            System.out.println("Error en base de datos.");
            return false;
        }
    }
}