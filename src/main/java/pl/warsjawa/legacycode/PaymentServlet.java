package pl.warsjawa.legacycode;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

/**
 * Legacy servlet to handle changes in payments.
 *
 * WARNING. This is legacy code for education purposes only. <b>DO NOT WRITE CODE LIKE THAT AT HOME!</br>
 */
public class PaymentServlet extends HttpServlet {

    //TODO: Change to POST - temporarily GET to make easier to call from a browser
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        System.out.println("Serving...");

        String responseText = null;

        try {
            Class.forName("org.h2.Driver");
            Connection conn = DriverManager.getConnection("jdbc:h2:tcp://localhost:9092//tmp/payments", "prod", "topsecret");

            String id = req.getParameter("id");
            String status = req.getParameter("status");

            Statement statement = conn.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM payments WHERE id = " + id);

            if (resultSet.next()) {
                String oldStatus = resultSet.getString("status");
                if (oldStatus.equals(status)) {
                    responseText = "THE SAME";
                } else if (oldStatus.equals("NEW") && status.equals("PAID")) {
                    responseText = "PAID";
                } else if (oldStatus.equals("PAID") && status.equals("NEW")) {
                    responseText = "ERROR";
                } else {
                    responseText = "ERROR";
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        PrintWriter writer = resp.getWriter();
        writer.println(responseText);
        writer.close();
    }
}
