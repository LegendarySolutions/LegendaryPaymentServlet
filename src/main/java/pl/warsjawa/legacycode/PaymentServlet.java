package pl.warsjawa.legacycode;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Legacy servlet to handle changes in payments.
 * <p>
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

            String pDesc = req.getParameter("desc");
            String pAmount = req.getParameter("amount");
            String pStatus = req.getParameter("status");

            if (pStatus.equals("OK")) {
                Pattern orderPattern = Pattern.compile("Order: (\\d\\d\\d\\d\\d)");
                Matcher matcher = orderPattern.matcher(pDesc);
                if (matcher.matches()) {
                    String orderId = matcher.group(1);
                    System.out.println("Retrieved orderId: " + orderId);

                    responseText = doIt(responseText, conn, pStatus, orderId, pAmount);
                } else {
                    orderPattern = Pattern.compile(".*(\\d\\d\\d\\d\\d).*");
                    matcher = orderPattern.matcher(pDesc);
                    if (matcher.matches()) {
                        String orderId = matcher.group(1);
                        System.out.println("Greedily retrieved orderId: " + orderId);

                        responseText = doIt(responseText, conn, pStatus, orderId, pAmount);
                    } else {
                        System.out.println("Unrecognized payment description");
                        responseText = "ERROR";
                        //TODO: WORKSHOP: Send mail to admin
                    }
                }

            } else {
                System.out.println("Wrong status");
                responseText = "ERROR";
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

    //TODO: WORKSHOP: Inline it to make it even worse (with some subtle modification in one branch to make refactoring harder)
    private String doIt(String responseText, Connection conn, String pStatus, String orderId, String pAmount) throws SQLException {
        Statement statement = conn.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT * FROM orders WHERE orderId = " + orderId);

        if (resultSet.next()) {
            String oldStatus = resultSet.getString("status");
            if (oldStatus.equals("PAID")) {
                System.out.println("Already paid. Ignoring.");
                responseText = "ALREADY PAID";
            } else if (oldStatus.equals("NEW") && pStatus.equals("OK")) {
                if (new Integer(pAmount) == resultSet.getInt("amount")) {
                    System.out.println("Paid");
                    responseText = "PAID";
                    Statement updateStatement = conn.createStatement();
                    updateStatement.executeUpdate("UPDATE orders SET status = 'PAID' WHERE orderId = " + orderId);
                    //TODO: WORKSHOP: Send email to user
                } else if (new Integer(pAmount) < resultSet.getInt("amount")) {
                    System.out.println("Not enough");
                    responseText = "NOT ENOUGH";
                    //TODO: WORKSHOP: Send email to user
                } else {
                    System.out.println("Paid with surplus");
                    responseText = "PAID SURPLUS";
                    Statement updateStatement = conn.createStatement();
                    updateStatement.executeUpdate("UPDATE orders SET status = 'PAID' WHERE orderId = " + orderId);
                    //TODO: WORKSHOP: Send email to user
                    //TODO: Handle surplus manually
                }
            } else if (oldStatus.equals("CANCELLED") && pStatus.equals("OK")) {
                System.out.println("Wrong transition error");
                responseText = "ERROR";
            } else {
                System.out.println("Other error");
                responseText = "ERROR";
                //TODO: WORKSHOP: Send email to admin
            }
        } else {
            System.out.println("Unknown order");
            responseText = "ERROR";
            //TODO: WORKSHOP: Send email to admin
        }
        return responseText;
    }
}
