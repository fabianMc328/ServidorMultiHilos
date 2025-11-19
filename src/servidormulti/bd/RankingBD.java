package servidormulti.bd;
import java.sql.*;

public class RankingBD {
    private void actualizarPuntaje(String usuario, int puntos, int vic, int emp, int der) {
        String sql = "INSERT INTO ranking_gato (usuario, puntos, partidas_jugadas, victorias, empates, derrotas) VALUES (?, ?, 1, ?, ?, ?) ON CONFLICT(usuario) DO UPDATE SET puntos = puntos + excluded.puntos, partidas_jugadas = partidas_jugadas + 1, victorias = victorias + excluded.victorias, empates = empates + excluded.empates, derrotas = derrotas + excluded.derrotas";
        try (Connection conn = ConexionBD.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, usuario); ps.setInt(2, puntos); ps.setInt(3, vic); ps.setInt(4, emp); ps.setInt(5, der); ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error BD al actualizar ranking: " + e.getMessage());
        }
    }

    private void actualizarH2H(String j1, String j2, String resJ1) {
        String jug1 = (j1.compareTo(j2) < 0) ? j1 : j2; String jug2 = (j1.compareTo(j2) < 0) ? j2 : j1;
        String upd = (resJ1.equals("empate")) ? "empates=empates+1" : ((resJ1.equals("victoria") && j1.equals(jug1)) || (resJ1.equals("derrota") && j2.equals(jug1))) ? "victorias_jugador1=victorias_jugador1+1" : "victorias_jugador2=victorias_jugador2+1";
        int v1=0,v2=0,em=0; if(upd.contains("victorias_jugador1")) v1=1; else if(upd.contains("victorias_jugador2")) v2=1; else em=1;
        String sql = "INSERT INTO h2h_gato (jugador1, jugador2, victorias_jugador1, victorias_jugador2, empates) VALUES (?, ?, ?, ?, ?) ON CONFLICT(jugador1, jugador2) DO UPDATE SET " + upd;
        try (Connection conn = ConexionBD.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, jug1); ps.setString(2, jug2); ps.setInt(3, v1); ps.setInt(4, v2); ps.setInt(5, em); ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error BD al actualizar historial: " + e.getMessage());
        }
    }

    public void actualizarResultados(String jA, String jB, boolean emp) {
        if (emp) { actualizarPuntaje(jA, 1, 0, 1, 0); actualizarPuntaje(jB, 1, 0, 1, 0); actualizarH2H(jA, jB, "empate"); }
        else { actualizarPuntaje(jA, 2, 1, 0, 0); actualizarPuntaje(jB, 0, 0, 0, 1); actualizarH2H(jA, jB, "victoria"); }
    }

    public String getRankingGeneral() {
        String sql = "SELECT usuario, puntos, victorias, empates, derrotas FROM ranking_gato ORDER BY puntos DESC, victorias DESC LIMIT 20";
        StringBuilder sb = new StringBuilder("--- RANKING GENERAL (TOP 20) ---\n");
        try (Connection conn = ConexionBD.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            int pos = 1; while (rs.next()) sb.append(String.format("%d. %s - %d Pts (V:%d, E:%d, D:%d)\n", pos++, rs.getString("usuario"), rs.getInt("puntos"), rs.getInt("victorias"), rs.getInt("empates"), rs.getInt("derrotas")));
            return sb.toString();
        } catch (SQLException e) { return "Error al leer ranking."; }
    }

    public String getH2H(String j1, String j2) {
        String jug1 = (j1.compareTo(j2) < 0) ? j1 : j2; String jug2 = (j1.compareTo(j2) < 0) ? j2 : j1;
        String sql = "SELECT * FROM h2h_gato WHERE jugador1 = ? AND jugador2 = ?";
        try (Connection conn = ConexionBD.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, jug1); ps.setString(2, jug2); ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int v1 = rs.getInt("victorias_jugador1"); int v2 = rs.getInt("victorias_jugador2"); int em = rs.getInt("empates");
                int victoriasJ1 = (j1.equals(jug1)) ? v1 : v2;
                int victoriasJ2 = (j2.equals(jug1)) ? v1 : v2;
                double total = victoriasJ1 + victoriasJ2 + em;

                if (total == 0) return "No se han enfrentado (error de datos).";

                double pct1 = (victoriasJ1 / total) * 100.0;
                double pct2 = (victoriasJ2 / total) * 100.0;

                return String.format("--- H2H: %s vs %s ---\nTotal Partidas: %d\n%s: %d victorias (%.1f%%)\n%s: %d victorias (%.1f%%)\nEmpates: %d",
                        j1, j2, (int)total, j1, victoriasJ1, pct1, j2, victoriasJ2, pct2, em);


            } return "Aún no se han enfrentado.";
        } catch (SQLException e) { return "Error al leer historial."; }
    }
}