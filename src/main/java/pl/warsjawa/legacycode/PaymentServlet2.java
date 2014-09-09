package pl.warsjawa.legacycode;

import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class PaymentServlet2 extends HttpServlet{

    private static final String secret = "15c84df6-bfa3-46c1-8929-a5dedaeab4a4";
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String amount = req.getParameter("amount");
        String status = req.getParameter("status");     // OK | CANCELLED | EXPIRED
        String payload = req.getParameter("payload");
        String timestamp = req.getParameter("ts");
        String md5 = req.getParameter("md5");           // md5(amount + status + payload + timestamp + secret)
    
        try {
            
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(amount.getBytes());
            digest.update(status.getBytes());
            digest.update(payload.getBytes());
            digest.update(timestamp.getBytes());
            digest.update(secret.getBytes());
            
            String expectedMd5 = new BigInteger(1, digest.digest()).toString(16);
            System.out.println("Expected MD5: " + expectedMd5);
            
            if(!expectedMd5.equals(md5)){
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "MD5 signature did not match!");
                return;
            }
            
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
     
        boolean isSBS; 
        String orderId = null;
        
        Pattern sbsPattern = Pattern.compile(".*order_id:(\\d{4}).*");      
        Matcher sbsMatcher = sbsPattern.matcher(payload);
        if(sbsMatcher.matches()){
            isSBS = true;
            orderId = sbsMatcher.group(1); 
        }
        
        Pattern xyzPattern = Pattern.compile(".*(\\d{5,7}S|K|G).*");
        Matcher xyzMatcher = xyzPattern.matcher(payload);
        if(xyzMatcher.matches()){
            isSBS = false;
            orderId = xyzMatcher.group(1);
        }
        
        if(orderId == null){
            
            // Neither SBS nor XYZ. Reject.
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
            
            if(order.getTotalPrice() == Integer.parseInt(amount)){
                
                order.setStatus("PAID");
                sbsDao.save(order);

                String email = order.getCustomerData().getEmail();
                
                sendEmail(email, "Order #" + orderId + " has been successfully processed!", 
                        "Hello " + order.getCustomerData().getFullName() + ",\n your payment for order #" + orderId + " has been successfully processed!\n Thanks!");

                
            } else if(order.getTotalPrice() > Integer.parseInt(amount)) {
                
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Not enough amount!");
                return;
                
            } else {
                
                int surplus = Integer.parseInt(amount) - order.getTotalPrice();
                order.setStatus("PAID");
                sbsDao.save(order);
                
                // TODO: save surplus to customer account

                String email = order.getCustomerData().getEmail();
                
                sendEmail(email, "Order #" + orderId + " has been successfully processed!", 
                        "Hello " + order.getCustomerData().getFullName() + ",\n your payment for order #" + orderId + " has been successfully processed!\n"
                      + "We have registered surplus of " + surplus + "USD on your account.\n Thanks!");
                
                sendEmail("admin@oursystem.com", "Order #" + orderId + " has surplus of " + surplus, "");
            }
            
        } else {
            
            List<Payment> payments = paymentService.findByTransactionId(orderId);
            
            Payment activePayment = null;
            
            for (Payment payment : payments) {
                if(payment.isActive()) {
                    if(activePayment != null && activePayment != payment){
                        throw new IllegalStateException("Multiple active payments for order!");
                    }
                    activePayment = payment;
                }
            }
            
            if(activePayment == null){
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "No active payment for transaction_id: " + orderId + "!");
                return;
            }
            
            paymentService.markAsCompleted(activePayment);
            
            sendEmail(payment.getContactEmail(), "Order #" + orderId + " has been successfully processed!", 
                    "Hello " + order.getCustomerPerson() + ",\n your payment for order #" + orderId + " has been successfully processed!\n Thanks!");
            
        }
        
    }

    private void sendEmail(String email, String subject, String body) throws MessagingException, AddressException, NoSuchProviderException {
        
        Session session = Session.getInstance(null);
        Message msg = new MimeMessage(session);
        msg.setRecipient(RecipientType.TO, new InternetAddress(email));
        msg.setSubject(subject);
        msg.setText(body);
        msg.saveChanges();
        Transport transport = session.getTransport("smtp");
        transport.connect("localhost", 25, "", "");
        transport.sendMessage(msg, msg.getAllRecipients());
    }
    
}