package pl.warsjawa.legacycode;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import pl.warsjawa.legacycode.infra.MailServer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.icegreen.greenmail.util.GreenMailUtil;

public class DebugServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        Connection connection = null;

        try {

            connection = getConnection();

            Map<String, Object> json = new HashMap<>();

            json.put("orders", executeQuery(connection, "select * from ORDERS"));
            json.put("payments", executeQuery(connection, "select * from PAYMENT"));
            json.put("transactions", executeQuery(connection, "select * from TRANSACTION"));
            json.put("sent_emails", extractEmails());

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            resp.getOutputStream().println(gson.toJson(json));

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    // ignore
                }
            }
        }

    }

    private Object extractEmails() {

        try {
            List<Object> mails = new ArrayList<Object>();

            for (MimeMessage email : MailServer.sentEmails()) {
                
                Map<String, Object> mail = new HashMap<>();
                mail.put("subject", email.getSubject());
                mail.put("recipient", email.getRecipients(RecipientType.TO)[0].toString());
                mail.put("body", GreenMailUtil.getBody(email));
                
                mails.add(mail);
            }

            return mails;
            
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Object> executeQuery(Connection connection, String sql) throws SQLException {

        List<Object> result = new ArrayList<>();
        ResultSet rs = connection.createStatement().executeQuery(sql);
        while (rs.next()) {
            Map<String, Object> json = new HashMap<String, Object>();
            int columnCount = rs.getMetaData().getColumnCount();
            for (int i = 1; i <= columnCount; i++) {
                String name = rs.getMetaData().getColumnLabel(i);
                json.put(name.toLowerCase(), rs.getObject(name));
            }
            result.add(json);
        }
        return result;
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:h2:tcp://localhost:9092//tmp/payments", "prod", "topsecret");
    }

}
