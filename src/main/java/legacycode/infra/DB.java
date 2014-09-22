package legacycode.infra;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.h2.tools.RunScript;
import org.h2.tools.Server;

public class DB {

    public static Connection getConnection() throws SQLException {
        String connectionUrl = String.format("jdbc:h2:tcp://localhost:9092/%s/payments", System.getProperty("java.io.tmpdir"));
        return DriverManager.getConnection(connectionUrl, "prod", "topsecret");
    }

    public static void start() {

        try {
        
            Server.createTcpServer("-tcpAllowOthers").start();
            Server.createWebServer().start();
            
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void runScript(String script) {
    
        Connection conn = null;
    
        try {

            conn = getConnection();
            InputStream initScriptStream = DB.class.getClassLoader().getResourceAsStream(script);
            InputStreamReader initScriptReader = new InputStreamReader(initScriptStream);

            System.out.println("Running: " + script);
            
            RunScript.execute(conn, initScriptReader);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                }
            }
        }
    }

    public static void runSql(String sql) {

        Connection conn = null;
        
        try {
            
            conn = getConnection();
            
            System.out.println("Running: " + sql);

            RunScript.execute(conn, new StringReader(sql));
            
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                }
            }
        }
        
    }

}
