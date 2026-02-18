package Java;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DatabaseManager — Singleton que centraliza la conexión JDBC a MySQL.
 *
 * Uso:
 *   Connection con = DatabaseManager.getInstance().getConnection();
 *
 * Cierre al salir:
 *   DatabaseManager.getInstance().cerrar();
 */
public class Databasemanager {

    // ── Datos de conexión ──────────────────────────────────────────────────────
    private static final String HOST     = "127.0.0.1";
    private static final int    PORT     = 3305;
    private static final String DATABASE = "veterinaria_db";
    private static final String USER     = "root";
    private static final String PASSWORD = "Travel270";

    private static final String URL = String.format(
        "jdbc:mysql://%s:%d/%s?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true",
        HOST, PORT, DATABASE
    );

    // ── Instancia única ────────────────────────────────────────────────────────
    private static Databasemanager instancia;
    private Connection conexion;

    // Constructor privado — nadie puede instanciar esta clase directamente
    private Databasemanager() {
        conectar();
    }

    /** Devuelve la única instancia del gestor de base de datos. */
    public static Databasemanager getInstance() {
        if (instancia == null) {
            instancia = new Databasemanager();
        }
        return instancia;
    }

    // ── Conexión ───────────────────────────────────────────────────────────────

    private void conectar() {
        try {
            // A partir de JDBC 4 el driver se carga automáticamente,
            // pero cargarlo explícitamente evita problemas en algunos entornos.
            Class.forName("com.mysql.cj.jdbc.Driver");
            conexion = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("✔ Conexión a MySQL establecida correctamente.");
        } catch (ClassNotFoundException e) {
            System.err.println("✘ Driver MySQL no encontrado. Añade mysql-connector-j.jar al classpath.");
            System.err.println("  Descarga: https://dev.mysql.com/downloads/connector/j/");
        } catch (SQLException e) {
            System.err.println("✘ Error al conectar con MySQL: " + e.getMessage());
            System.err.println("  Código SQL: " + e.getSQLState());
        }
    }

    /**
     * Devuelve la conexión activa. Si se cerró o perdió, la reconecta
     * automáticamente antes de devolverla.
     */
    public Connection getConnection() {
        try {
            if (conexion == null || conexion.isClosed()) {
                System.out.println("⚠ Reconectando a la base de datos...");
                conectar();
            }
        } catch (SQLException e) {
            System.err.println("✘ Error al verificar la conexión: " + e.getMessage());
        }
        return conexion;
    }

    /** Cierra la conexión de forma limpia. Llamar al finalizar la aplicación. */
    public void cerrar() {
        try {
            if (conexion != null && !conexion.isClosed()) {
                conexion.close();
                System.out.println("✔ Conexión a MySQL cerrada.");
            }
        } catch (SQLException e) {
            System.err.println("✘ Error al cerrar la conexión: " + e.getMessage());
        }
    }

    /** Indica si la conexión está activa en este momento. */
    public boolean isConectado() {
        try {
            return conexion != null && !conexion.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}