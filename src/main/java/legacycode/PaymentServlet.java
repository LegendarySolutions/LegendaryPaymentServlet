package legacycode;

import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class PaymentServlet extends HttpServlet{

    private PaymentService paymentService;
    private SignatureValidator signatureValidator;
    
    public PaymentServlet() {
        this(new PaymentService(), new SignatureValidator());
    }

    public PaymentServlet(PaymentService paymentService, SignatureValidator signatureValidator) {
        this.paymentService = paymentService;
        this.signatureValidator = signatureValidator;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String amount = req.getParameter("amount");
        String status = req.getParameter("status");     // OK | CANCELLED | EXPIRED
        String payload = req.getParameter("payload");
        String timestamp = req.getParameter("ts");
        String md5 = req.getParameter("md5");           // md5(amount + status + payload + timestamp + secret)
    
        handle(resp, amount, status, payload, timestamp, md5);
    }

    protected void handle(HttpServletResponse resp, String amount, String status, String payload, String timestamp, String md5)
            throws IOException {
       
        try {
            
            signatureValidator.assertValidRequest(amount, status, payload, timestamp, md5);
            
        } catch(Exception e){
            
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, e.getMessage());
            return;
        }
        
        boolean isSBS = false; 
        String orderId = null;
        
        Pattern sbsPattern = Pattern.compile(".*order_id:(\\d{4}).*");      
        Matcher sbsMatcher = sbsPattern.matcher(payload);
        if(sbsMatcher.matches()){
            isSBS = true;
            orderId = sbsMatcher.group(1); 
        }
        
        Pattern transPattern = Pattern.compile("^(\\d{5,7}(S|K|G)).*");
        Matcher transMatcher = transPattern.matcher(payload);
        if(transMatcher.matches()){
            isSBS = false;
            orderId = transMatcher.group(1);
        }
        
        if(orderId == null){
            
            // Neither SBS nor Trans. Reject.
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unrecognized format of payload!");
            return;
        }
        
        if(isSBS){

            SbsOrderDao sbsDao = SbsOrderDao.getInstance();
            Order order = sbsDao.findOrderById(orderId);

            if(order == null || !"PENDING".equals(order.getStatus())){
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "No pending oder with id: " + orderId + "!");
                return;
            }

            if (status.equals("CANCELLED") || status.equals("EXPIRED")) {
                order.setStatus(status);
                sbsDao.save(order);

                String email = order.getCustomerData().getEmail();

                sendEmail(new Email(email, "Order #" + orderId + " has been " + status + "!", "Hello " + order.getCustomerData().getFullName() + ",\n your payment for order #" + orderId + " has been " + status + "!"));

            } else {    //OK
                if (order.getTotalPrice() == Integer.parseInt(amount)) {

                    order.setStatus("PAID");
                    sbsDao.save(order);

                    String email = order.getCustomerData().getEmail();

                    sendEmail(new Email(email, "Order #" + orderId + " has been successfully processed!", "Hello " + order.getCustomerData().getFullName() + ",\n your payment for order #" + orderId + " has been successfully processed!\n Thanks!"));


                } else if (order.getTotalPrice() > Integer.parseInt(amount)) {

                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Not enough amount!");
                    return;

                } else {

                    int surplus = Integer.parseInt(amount) - order.getTotalPrice();
                    order.setStatus("PAID");
                    sbsDao.save(order);

                    // TODO: save surplus to customer account

                    String email = order.getCustomerData().getEmail();

                    sendEmail(new Email(email, "Order #" + orderId + " has been successfully processed!", "Hello " + order.getCustomerData().getFullName() + ",\n your payment for order #" + orderId + " has been successfully processed!\n"
                            + "We have registered surplus of " + surplus + "USD on your account.\n Thanks!"));

                    sendEmail(new Email("admin@oursystem.com", "Order #" + orderId + " has surplus of " + surplus, ""));
                }
            }
            
        } else {
            
            Transaction transaction = paymentService.findTransactionById(orderId);
            
            if(transaction == null || !transaction.isActive()){
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "No active transaction with transaction_id: " + orderId + "!");
                return;
            }
            
            List<Transaction> transactions = paymentService.findTransactionsByPaymentId(transaction.getPaymentId());
            
            for (Transaction t : transactions) {
                
                // only one active transaction is allowed!
                if(t.isActive() && t.getId() != transaction.getId()){
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Multiple active transactions detected for payment: " + transaction.getPaymentId() + "!");
                    return;
                }
            }

            if ("OK".equals(status)) {

                paymentService.setInactiveTransaction(transaction);

                Payment payment = paymentService.findPaymentById(transaction.getPaymentId());
                payment.setState("COMPLETED");
                paymentService.updatePayment(payment);

                sendEmail(new Email(transaction.getContactEmail(), "Payment #" + payment.getId() + " has been successfully processed!", "Hello " + transaction.getContactPerson() + ",\n your payment #" + payment.getId() + " has been successfully processed!\n Thanks!"));
            } else {

                Payment payment = paymentService.findPaymentById(transaction.getPaymentId());
                if ("COMPLETED".equals(payment.getState())) {
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Illegal operation (" + status + ") for completed payment: " + orderId + "!");
                    return;

                } else {
                    payment.setState("CANCELLED");
                    paymentService.updatePayment(payment);

                    sendEmail(new Email(transaction.getContactEmail(), "Payment #" + payment.getId() + " has been cancelled!", "Hello " + transaction.getContactPerson() + ",\n your payment #" + payment.getId() + " has been cancelled!"));
                }
            }

        }
        
        printResponse(resp, "OK");
    }

    protected void printResponse(HttpServletResponse resp, String text) throws IOException {
        resp.getOutputStream().print(text);
    }
    
    protected void sendEmail(Email mail) {
        
        try {
            
            Session session = Session.getInstance(new Properties());
            Message msg = new MimeMessage(session);
            msg.setRecipient(RecipientType.TO, new InternetAddress(mail.getEmail()));
            msg.setSubject(mail.getSubject());
            msg.setText(mail.getBody());
            msg.saveChanges();
            Transport transport = session.getTransport("smtp");
            transport.connect("localhost", 9988, "", "");
            transport.sendMessage(msg, msg.getAllRecipients());
            
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
