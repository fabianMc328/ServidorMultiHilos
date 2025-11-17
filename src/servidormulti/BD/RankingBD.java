package servidormulti.BD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RankingBD {

    private void actualizarPuntaje(String usuario, int puntosGanados, int esVictoria, int esEmpate, int esDerrota) {

        String sql = "INSERT INTO ranking_gato (usuario, puntos, partidas_jugadas, victorias, empates, derrotas) " +
                "VALUES (?, ?, 1, ?, ?, ?) " +
                "ON CONFLICT(usuario) DO UPDATE SET " +
                "puntos = puntos + excluded.puntos, " +
                "partidas_jugadas = partidas_jugadas + 1, " +
                "victorias = victorias + excluded.victorias, " +
                "empates = empates + excluded.empates, " +
                "derrotas = derrotas + excluded.derrotas";

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, usuario);
            ps.setInt(2, puntosGanados);
            ps.setInt(3, esVictoria);
            ps.setInt(4, esEmpate);
            ps.setInt(5, esDerrota);



            ps.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error al actualizar puntaje para " + usuario);
            e.printStackTrace();
        }
    }

    private void actualizarH2H(String j1, String j2, String resultadoJ1) {
        String jugador1 = (j1.compareTo(j2) < 0) ? j1 : j2;
        String jugador2 = (j1.compareTo(j2) < 0) ? j2 : j1;

        String campoAActualizar;
        int v1_inicial = 0;
        int v2_inicial = 0;
        int empate_inicial = 0;

        if (resultadoJ1.equals("empate")) {
            campoAActualizar = "empates = empates + 1";
            empate_inicial = 1;
        } else if ((resultadoJ1.equals("victoria") && j1.equals(jugador1)) || (resultadoJ1.equals("derrota") && j2.equals(jugador1))) {
            campoAActualizar = "victorias_jugador1 = victorias_jugador1 + 1";
            v1_inicial = 1;
        } else {
            campoAActualizar = "victorias_jugador2 = victorias_jugador2 + 1";
            v2_inicial = 1;
        }

        String sql = "INSERT INTO h2h_gato (jugador1, jugador2, victorias_jugador1, victorias_jugador2, empates) " +
                "VALUES (?, ?, ?, ?, ?) " +
                "ON CONFLICT(jugador1, jugador2) DO UPDATE SET " +
                campoAActualizar;

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, jugador1);
            ps.setString(2, jugador2);
            ps.setInt(3, v1_inicial);
            ps.setInt(4, v2_inicial);
            ps.setInt(5, empate_inicial);

            ps.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error al actualizar H2H entre " + j1 + " y " + j2);
            e.printStackTrace();
        }
    }

    public void actualizarResultados(String jugadorA, String jugadorB, boolean empate) {
        if (empate) {

            actualizarPuntaje(jugadorA, 1, 0, 1, 0);
            actualizarPuntaje(jugadorB, 1, 0, 1, 0);
            actualizarH2H(jugadorA, jugadorB, "empate");
        } else {

            actualizarPuntaje(jugadorA, 2, 1, 0, 0);
            actualizarPuntaje(jugadorB, 0, 0, 0, 1);
            actualizarH2H(jugadorA, jugadorB, "victoria");
        }
    }

    public String getRankingGeneral() {
        String sql = "SELECT usuario, puntos, victorias, empates, derrotas " +
                "FROM ranking_gato " +
                "ORDER BY puntos DESC, victorias DESC " +
                "LIMIT 20";

        StringBuilder sb = new StringBuilder("--- RANKING GENERAL (TOP 20) ---\n");

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            int pos = 1;
            while (rs.next()) {
                sb.append(String.format("%d. %s - %d Pts (V:%d, E:%d, D:%d)\n",
                        pos++,
                        rs.getString("usuario"),
                        rs.getInt("puntos"),
                        rs.getInt("victorias"),
                        rs.getInt("empates"),
                        rs.getInt("derrotas")
                ));
            }

            if (pos == 1) {
                return "Aún no hay datos en el ranking. ¡Juega una partida!";
            }
            return sb.toString();

        } catch (SQLException e) {
            e.printStackTrace();
            return "Error al consultar el ranking.";
        }
    }

    public String getH2H(String j1, String j2) {
        String jugador1 = (j1.compareTo(j2) < 0) ? j1 : j2;
        String jugador2 = (j1.compareTo(j2) < 0) ? j2 : j1;

        String sql = "SELECT * FROM h2h_gato WHERE jugador1 = ? AND jugador2 = ?";

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, jugador1);
            ps.setString(2, jugador2);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int v1 = rs.getInt("victorias_jugador1");
                int v2 = rs.getInt("victorias_jugador2");
                int empates = rs.getInt("empates");

                int victoriasJ1 = (j1.equals(jugador1)) ? v1 : v2;
                int victoriasJ2 = (j2.equals(jugador1)) ? v1 : v2;
                double total = victoriasJ1 + victoriasJ2 + empates;

                if (total == 0) {
                    return "No se han enfrentado (error de datos).";
                }

                double porcJ1 = (victoriasJ1 / total) * 100.0;
                double porcJ2 = (victoriasJ2 / total) * 100.0;

                return String.format("--- H2H: %s vs %s ---\n" +
                                "Total Partidas: %d\n" +
                                "%s: %d victorias (%.1f%%)\n" +
                                "%s: %d victorias (%.1f%%)\n" +
                                "Empates: %d",
                        j1, j2, (int)total, j1, victoriasJ1, porcJ1, j2, victoriasJ2, porcJ2, empates);

            } else {
                return "Aún no se han enfrentado.";
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return "Error al consultar H2H.";
        }
    }
}