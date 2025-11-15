package servidormulti;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties; // Importado

public class ConexionBD {
    public static void main(String[] args) {
        testConnection();
    }
    private static HikariDataSource ds;

    static {
        // --- INICIO DE MODIFICACIÓN ---
        // Lee las propiedades desde el archivo db.properties
        Properties props = new Properties();
        try (InputStream input = ConexionBD.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (input == null) {
                System.err.println("Error: No se encontró el archivo db.properties en el classpath.");
                throw new RuntimeException("No se encontró db.properties");
            }
            props.load(input);
        } catch (IOException ex) {
            System.err.println("Error leyendo db.properties");
            ex.printStackTrace();
            throw new RuntimeException("Error leyendo db.properties", ex);
        }

        HikariConfig config = new HikariConfig();
        // Usa los valores del archivo de propiedades
        config.setJdbcUrl(props.getProperty("db.url"));
        config.setUsername(props.getProperty("db.user"));
        config.setPassword(props.getProperty("db.password"));
        // --- FIN DE MODIFICACIÓN ---

        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        ds = new HikariDataSource(config);
    }

    public static Connection getConnection() throws SQLException {
        return ds.getConnection();
    }
    public static void testConnection() {
        try (Connection conn = getConnection()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println("Conexion exitosa a la base de datos!");
            }
        } catch (SQLException e) {
            System.err.println("Error conectando a la base de datos:");
            e.printStackTrace();
        }
    }
}