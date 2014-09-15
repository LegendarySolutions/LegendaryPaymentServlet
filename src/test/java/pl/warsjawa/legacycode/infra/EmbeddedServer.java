package pl.warsjawa.legacycode.infra;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

public class EmbeddedServer {
    
    Server server;

    public EmbeddedServer(Integer port) {
        server = new Server(port);
        WebAppContext context = new WebAppContext();
        context.setResourceBase("./src/main/webapp");
        context.setDescriptor("./WEB-INF/web.xml");
        server.setHandler(context);
    }

    public void start() {
        
        try {
            server.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    void awaitShutdown() {
        try {
            server.join();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void stop() {

        try {
            server.stop();
            awaitShutdown();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public static void main(String[] args) {
        
        SetupDB.main(args);
        MailServer.main(args);
        
        EmbeddedServer server = new EmbeddedServer(Integer.valueOf(envOr("PORT", "8080")));
        
        server.start();
        server.awaitShutdown();
        
    }
    
    private static String envOr(String port, String defaultValue) {
        String variable = System.getenv(port);
        if (variable == null) {
            return defaultValue;
        }
        return variable;
    }
}