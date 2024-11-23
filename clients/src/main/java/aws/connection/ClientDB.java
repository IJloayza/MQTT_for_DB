package aws.connection;

import java.sql.Statement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ClientDB {

    private static final String NOM_BASE_DE_DADES = "proj";
    private static final String CADENA_DE_CONNEXIO = "jdbc:mysql://localhost:3306/" + NOM_BASE_DE_DADES;
    private static final String USER = "root";
    private static final String CONTRASEÑA = "XXXXXX";
    private static Connection conn = null;

    public static void connect() throws SQLException {
        if (conn != null) return;   // is connected
        conn = DriverManager.getConnection(CADENA_DE_CONNEXIO + USER + CONTRASEÑA);
    }

    public static void disconnect() throws SQLException {
        if (conn == null) return; // disconnected
        conn.close();
        conn = null;
    }

    public static int addUID(String uid) throws SQLException {
        String sql = String.format(
                // Query to insert in table
                "INSERT INTO alumno (UID) VALUES ('%s')",
                uid);
        int filasAfectadas;
        Statement st = null;
        try {
            st = conn.createStatement();
            filasAfectadas = st.executeUpdate(sql);
        } finally {
            
            if (st != null) {
                st.close();
            }
        }
        return filasAfectadas;
    }
}