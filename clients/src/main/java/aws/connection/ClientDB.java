package aws.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ClientDB {

    private static final String NOM_BASE_DE_DADES = "sistema_ciclos";
    private static final String URL = "jdbc:postgresql://192.168.34.100:5432/" + NOM_BASE_DE_DADES;
    private static final String USER = "aplicacions";
    private static final String PASSWORD = "admin";
    private static Connection conn = null;

    public static void connect() throws SQLException {
        if (conn != null) return;   // is connected
        conn = DriverManager.getConnection(URL , USER , PASSWORD);
    }

    public static void disconnect() throws SQLException {
        if (conn == null) return; // disconnected
        conn.close();
        conn = null;
    }


    public static boolean addUID(String uid) throws SQLException {
    // Usa una consulta parametrizada para evitar inyecciones SQL
    String sql = "INSERT INTO FECHA (Fecha_hora, Estado, Uid_usuarios) VALUES (CURRENT_TIMESTAMP, ?::prf, ?)";

    // Manejo automático de recursos con try-with-resources
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
        
        // Asignar parámetrO
        ps.setString(1, "falta");
        ps.setString(2, uid);
        // Ejecutar la consulta y devolver filas afectadas
        int results = ps.executeUpdate();
        return results > 0;
        }
    }
}