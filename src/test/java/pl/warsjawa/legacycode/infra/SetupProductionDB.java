package pl.warsjawa.legacycode.infra;

import org.h2.tools.RunScript;
import org.h2.tools.Server;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

/**
 * <b>Legacy tool</b> to setup production like environment on the local workstation for <b>manual</b> "testing"
 * after deploy.
 *
 * Not to be used in unit tests.
 */
public class SetupProductionDB {

    public static void main(String[] args) throws SQLException, InterruptedException {
        Server.createTcpServer("-tcpAllowOthers").start();
        Connection conn = DriverManager.getConnection("jdbc:h2:/tmp/payments", "prod", "topsecret");
        Server.createWebServer().start();
        InputStream initScriptStream = SetupProductionDB.class.getClassLoader().getResourceAsStream("initProdDB.sql");
        InputStreamReader initScriptReader = new InputStreamReader(initScriptStream);
        RunScript.execute(conn, initScriptReader);
        TimeUnit.DAYS.sleep(1);
    }
}
