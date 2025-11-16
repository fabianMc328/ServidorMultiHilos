package servidormulti.BD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class GruposBD {
    public boolean crearGrupo(String nombreGrupo, String creador) {
        String sql = "INSERT INTO grupos (nombre_grupo, creador_usuario) VALUES (?, ?)";
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nombreGrupo);
            ps.setString(2, creador);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public int borrarGrupo(String nombreGrupo, String solicitante) {
        if (nombreGrupo.equalsIgnoreCase("todos")) {
            return -1;
        }

        int idGrupoBorrado = -1;
        String sqlCheck = "SELECT id_grupo, creador_usuario FROM grupos WHERE nombre_grupo = ?";

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement psCheck = conn.prepareStatement(sqlCheck)) {

            psCheck.setString(1, nombreGrupo);
            ResultSet rs = psCheck.executeQuery();

            if (rs.next()) {

                if (solicitante.equals(rs.getString("creador_usuario"))) {
                    idGrupoBorrado = rs.getInt("id_grupo");

                    String sqlDelete = "DELETE FROM grupos WHERE id_grupo = ?";
                    try (PreparedStatement psDelete = conn.prepareStatement(sqlDelete)) {
                        psDelete.setInt(1, idGrupoBorrado);
                        psDelete.executeUpdate();
                        return idGrupoBorrado;
                    }

                } else {
                    return -1;
                }
            } else {
                return -1;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int getGrupoId(String nombreGrupo) {
        String sql = "SELECT id_grupo FROM grupos WHERE nombre_grupo = ?";
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nombreGrupo);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("id_grupo");
            }
            return -1;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public String getListaGrupos() {
        String sql = "SELECT nombre_grupo FROM grupos";
        StringBuilder sb = new StringBuilder("--- Grupos Disponibles ---\n");
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                sb.append("- ").append(rs.getString("nombre_grupo")).append("\n");
            }
            return sb.toString();
        } catch (SQLException e) {
            e.printStackTrace();
            return "Error al obtener lista de grupos.";
        }
    }
    public void unirseAGrupo(String usuario, int idGrupo) {
        String sql = "INSERT INTO membresias_grupo (id_usuario, id_grupo) VALUES (?, ?) ON DUPLICATE KEY UPDATE id_grupo=id_grupo";
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, usuario);
            ps.setInt(2, idGrupo);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void salirDeGrupo(String usuario, int idGrupo) {
        if (idGrupo == 1) return;

        String sql = "DELETE FROM membresias_grupo WHERE id_usuario = ? AND id_grupo = ?";
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, usuario);
            ps.setInt(2, idGrupo);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public long guardarMensaje(int idGrupo, String remitente, String contenido) {
        String sql = "INSERT INTO mensajes_grupo (id_grupo, usuario_remitente, contenido) VALUES (?, ?, ?)";
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, idGrupo);
            ps.setString(2, remitente);
            ps.setString(3, contenido);
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getLong(1);
            }
            return -1;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }
    public long getUltimoMensajeVisto(String usuario, int idGrupo) {
        String sql = "SELECT ultimo_mensaje_visto_id FROM estado_lectura WHERE id_usuario = ? AND id_grupo = ?";
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, usuario);
            ps.setInt(2, idGrupo);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getLong("ultimo_mensaje_visto_id");
            }
            return 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public List<String> getMensajesNuevos(int idGrupo, long ultimoMensajeVistoId, String usuario, String clienteId) {
        List<String> mensajes = new ArrayList<>();

        String sql = "SELECT usuario_remitente, contenido, timestamp FROM mensajes_grupo " +
                "WHERE id_grupo = ? AND id_mensaje > ? " +
                "AND usuario_remitente != ? " +
                "AND usuario_remitente != ? " +
                "ORDER BY timestamp ASC";

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idGrupo);
            ps.setLong(2, ultimoMensajeVistoId);
            ps.setString(3, usuario); // ej: 'fabian'
            ps.setString(4, "Invitado-" + clienteId); // ej: 'Invitado-0'

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String linea = String.format("[%s] %s DICE: %s",
                        rs.getTimestamp("timestamp").toString(),
                        rs.getString("usuario_remitente"),
                        rs.getString("contenido"));
                mensajes.add(linea);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return mensajes;
    }

    public void actualizarUltimoMensajeVisto(String usuario, int idGrupo, long ultimoMensajeId) {
        if (ultimoMensajeId <= 0) return;

        String sql = "INSERT INTO estado_lectura (id_usuario, id_grupo, ultimo_mensaje_visto_id) VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE ultimo_mensaje_visto_id = ?";

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, usuario);
            ps.setInt(2, idGrupo);
            ps.setLong(3, ultimoMensajeId);
            ps.setLong(4, ultimoMensajeId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public long getMaxMensajeId(int idGrupo) {
        String sql = "SELECT MAX(id_mensaje) AS max_id FROM mensajes_grupo WHERE id_grupo = ?";
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idGrupo);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getLong("max_id");
            }
            return 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }
}