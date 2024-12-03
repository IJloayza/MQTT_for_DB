package aws.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ClientDB {

    private static final String NOM_BASE_DE_DADES = "sistemas_ciclos";
    private static final String URL = "jdbc:postgresql://192.168.34.100:5432/" + NOM_BASE_DE_DADES;
    private static final String USER = "aplicacions";
    private static final String PASSWORD = "admin";
    private static Connection conn = null;

    public static void connect() throws SQLException {
        if (conn != null) return;   // is connected
        conn = DriverManager.getConnection(URL + USER + PASSWORD);
    }

    public static void disconnect() throws SQLException {
        if (conn == null) return; // disconnected
        conn.close();
        conn = null;
    }

    public static boolean isValidUid(String uid) throws SQLException {
    String queryUid = "SELECT COUNT(*) FROM usuarios WHERE uid = ?";
    
    // Manejo automático de recursos con try-with-resources
    try (PreparedStatement ps = conn.prepareStatement(queryUid)) {
        // Asignar el parámetro
        ps.setString(1, uid);

        // Ejecutar la consulta y procesar el resultado
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1) > 0; // Devuelve true si el conteo es mayor que 0
            }
        }
    }
    return false;
}

    public static int addUID(String uid) throws SQLException {
    // Usa una consulta parametrizada para evitar inyecciones SQL
    String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    String sql = "INSERT INTO fecha (uid, horario) VALUES (?, ?)";

    // Manejo automático de recursos con try-with-resources
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
        // Asignar parámetro
        ps.setString(1, uid);
        ps.setString(2, date);
        // Ejecutar la consulta y devolver filas afectadas
        return ps.executeUpdate();
        }
    }
}