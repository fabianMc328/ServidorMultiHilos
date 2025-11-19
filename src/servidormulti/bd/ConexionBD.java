package servidormulti.bd;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class ConexionBD {
    public static void main(String[] args) { testConnection(); }
    private static HikariDataSource ds;

    static {
        Properties props = new Properties();
        try (InputStream input = ConexionBD.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (input == null) throw new RuntimeException("Falta archivo db.properties");
            props.load(input);
        } catch (IOException ex) {
            System.out.println("Error leyendo configuracion de la base de datos.");
        }

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(props.getProperty("db.url"));
        config.setMaximumPoolSize(10);
        config.addDataSourceProperty("journal_mode", "WAL");

        ds = new HikariDataSource(config);

        try {
            crearTablasSiNoExisten();
            System.out.println("Base de datos SQLite lista.");
        } catch (SQLException e) {
            System.out.println("Error critico al crear las tablas.");
        }
    }

    public static Connection getConnection() throws SQLException { return ds.getConnection(); }

    private static void crearTablasSiNoExisten() throws SQLException {
        String sqlUsuarios = "CREATE TABLE IF NOT EXISTS usuarios (usuario TEXT PRIMARY KEY NOT NULL, contra TEXT NOT NULL, status TEXT);";
        String sqlBloqueos = "CREATE TABLE IF NOT EXISTS bloqueos (usuario_bloqueador TEXT NOT NULL, usuario_bloqueado TEXT NOT NULL, PRIMARY KEY (usuario_bloqueador, usuario_bloqueado));";
        String sqlGrupos = "CREATE TABLE IF NOT EXISTS grupos (id_grupo INTEGER PRIMARY KEY AUTOINCREMENT, nombre_grupo TEXT NOT NULL UNIQUE, creador_usuario TEXT);";
        String sqlGrupoTodos = "INSERT OR IGNORE INTO grupos (id_grupo, nombre_grupo, creador_usuario) VALUES (1, 'todos', NULL);";
        String sqlMembresias = "CREATE TABLE IF NOT EXISTS membresias_grupo (id_usuario TEXT NOT NULL, id_grupo INTEGER NOT NULL, PRIMARY KEY (id_usuario, id_grupo));";
        String sqlMensajes = "CREATE TABLE IF NOT EXISTS mensajes_grupo (id_mensaje INTEGER PRIMARY KEY AUTOINCREMENT, id_grupo INTEGER NOT NULL, usuario_remitente TEXT NOT NULL, contenido TEXT NOT NULL, timestamp DATETIME DEFAULT CURRENT_TIMESTAMP);";
        String sqlEstadoLectura = "CREATE TABLE IF NOT EXISTS estado_lectura (id_usuario TEXT NOT NULL, id_grupo INTEGER NOT NULL, ultimo_mensaje_visto_id INTEGER NOT NULL, PRIMARY KEY (id_usuario, id_grupo));";
        String sqlRanking = "CREATE TABLE IF NOT EXISTS ranking_gato (usuario TEXT PRIMARY KEY NOT NULL, puntos INTEGER DEFAULT 0, partidas_jugadas INTEGER DEFAULT 0, victorias INTEGER DEFAULT 0, empates INTEGER DEFAULT 0, derrotas INTEGER DEFAULT 0);";
        String sqlH2H = "CREATE TABLE IF NOT EXISTS h2h_gato (jugador1 TEXT NOT NULL, jugador2 TEXT NOT NULL, victorias_jugador1 INTEGER DEFAULT 0, victorias_jugador2 INTEGER DEFAULT 0, empates INTEGER DEFAULT 0, PRIMARY KEY (jugador1, jugador2));";

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(sqlUsuarios); stmt.execute(sqlBloqueos); stmt.execute(sqlGrupos);
            stmt.execute(sqlGrupoTodos); stmt.execute(sqlMembresias); stmt.execute(sqlMensajes);
            stmt.execute(sqlEstadoLectura); stmt.execute(sqlRanking); stmt.execute(sqlH2H);
        }
    }

    public static void testConnection() {
        try (Connection conn = getConnection()) {
            if (conn != null && !conn.isClosed()) System.out.println("Conexion exitosa a SQLite.");
        } catch (SQLException e) {
            System.out.println("Error al conectar con la base de datos.");
        }
    }
}