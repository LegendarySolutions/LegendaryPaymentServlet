package pl.warsjawa.legacycode.infra;

import static java.lang.String.format;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;

import org.h2.tools.RunScript;
import org.h2.tools.Server;

/**
 * <b>Legacy tool</b> to setup production like environment on the local workstation for <b>manual</b> "testing" after deploy.
 *
 * Not to be used in unit tests.
 */
public class SetupDB {

    public static void main(String[] args) {
        
        try {
        
            Server.createTcpServer("-tcpAllowOthers").start();
            Connection conn = DriverManager.getConnection(format("jdbc:h2:%s//payments", System.getProperty("java.io.tmpdir")), "prod", "topsecret");
            Server.createWebServer().start();
            InputStream initScriptStream = SetupDB.class.getClassLoader().getResourceAsStream("initDB.sql");
            InputStreamReader initScriptReader = new InputStreamReader(initScriptStream);
            RunScript.execute(conn, initScriptReader);
            
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
