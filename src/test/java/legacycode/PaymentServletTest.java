package legacycode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import legacycode.infra.DB;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PaymentServletTest {

    private PaymentServlet servlet;
    
    @Mock
    private HttpServletResponse response;

    @Mock
    private SignatureValidator validator;

    private List<Email> sentEmails = new ArrayList<>();
    private String responseText;

    @BeforeClass
    public static void setupDB(){
        DB.start();
    }
    
    @Before
    public void setUp() {
        DB.runScript("createDB.sql");
        servlet = new PaymentServlet(new PaymentService(), validator){
            
            @Override
            protected void sendEmail(Email mail) {
                sentEmails.add(mail);
            }
            
            @Override
            protected void printResponse(HttpServletResponse resp, String text) throws IOException {
                responseText = text;
            }
        };
    }
    
    @After
    public void verifyNoMoreInteractions(){
        Mockito.verifyNoMoreInteractions(response);
    }
    
    @Test
    public void characterization_test_1() throws Exception {
        
        // given:
        doThrow(new IllegalArgumentException("Exception Message"))
            .when(validator)
            .assertValidRequest(anyString(), anyString(), anyString(), anyString(), anyString());
        
        // when:
        servlet.handle(response, "", "", "", "", "");
        
        // then:
        assertForbidden("Exception Message");
        assertNoEmails();
    }
    
    @Test
    public void characterization_test_2() throws Exception {

        // given:
        
        // when:
        servlet.handle(response, "", "", "", "", "");

        // then:
        assertBadRequest("Unrecognized format of payload!");
        assertNoEmails();
    }

    @Test
    public void characterization_test_3() throws Exception {
        
        // given:
        
        // when:
        servlet.handle(response, "", "", "order_id:1234", "", "");
        
        // then:
        assertBadRequest("No pending oder with id: 1234!");
        assertNoEmails();
    }

    @Test
    public void characterization_test_4() throws Exception {

        // given:
        DB.runSql("insert into ORDERS(ID, STATUS) values (1234, 'COMPLETED')");
        
        // when:
        servlet.handle(response, "", "", "order_id:1234", "", "");
        
        // then:
        assertBadRequest("No pending oder with id: 1234!");
        assertNoEmails();
    }

    @Test
    public void characterization_test_5() throws Exception {
        
        // given:
        DB.runSql("insert into ORDERS(ID, STATUS, EMAIL, FULL_NAME) values (1234, 'PENDING', 'jdoe@mail.com', 'John Doe')");
        
        // when:
        servlet.handle(response, "100", "", "order_id:1234", "", "");
        
        // then:
        assertOk();
        Email emailToCustomer = new Email("jdoe@mail.com", "Order #1234 has been successfully processed!", "Hello John Doe,\n your payment for order #1234 has been successfully processed!\n"
                + "We have registered surplus of 100USD on your account.\n Thanks!");
        Email emailToAdmin = new Email("admin@oursystem.com", "Order #1234 has surplus of 100", "");
        assertSentEmails(emailToCustomer, emailToAdmin);
        assertOrderStatus("1234", "PAID");
    }

    @Test
    public void characterization_test_6() throws Exception {
        
        // given:
        DB.runSql("insert into ORDERS(ID, STATUS, EMAIL, FULL_NAME) values (1234, 'PENDING', 'jdoe@mail.com', 'John Doe')");
        
        // when:
        servlet.handle(response, "100", "CANCELLED", "order_id:1234", "", "");
        
        // then:
        assertOk();
        assertSentEmails(new Email("jdoe@mail.com", "Order #1234 has been CANCELLED!", "Hello John Doe,\n your payment for order #1234 has been CANCELLED!"));
        assertOrderStatus("1234", "CANCELLED");
    }

    @Test
    public void characterization_test_7() throws Exception {
        
        // given:
        DB.runSql("insert into ORDERS(ID, STATUS, EMAIL, FULL_NAME) values (1234, 'PENDING', 'jdoe@mail.com', 'John Doe')");
        
        // when:
        servlet.handle(response, "100", "EXPIRED", "order_id:1234", "", "");
        
        // then:
        assertOk();
        assertSentEmails(new Email("jdoe@mail.com", "Order #1234 has been EXPIRED!", "Hello John Doe,\n your payment for order #1234 has been EXPIRED!"));
        assertOrderStatus("1234", "EXPIRED");
    }

    @Test
    public void characterization_test_8() throws Exception {
        
        // given:
        DB.runSql("insert into ORDERS(ID, STATUS, EMAIL, FULL_NAME, TOTAL) values (1234, 'PENDING', 'jdoe@mail.com', 'John Doe', 100)");
        
        // when:
        servlet.handle(response, "100", "OK", "order_id:1234", "", "");
        
        // then:
        assertOk();
        assertSentEmails(new Email("jdoe@mail.com", "Order #1234 has been successfully processed!", "Hello John Doe,\n your payment for order #1234 has been successfully processed!\n Thanks!"));
        assertOrderStatus("1234", "PAID");
    }

    @Test
    public void characterization_test_9() throws Exception {
        
        // given:
        DB.runSql("insert into ORDERS(ID, STATUS, EMAIL, FULL_NAME, TOTAL) values (1234, 'PENDING', 'jdoe@mail.com', 'John Doe', 100)");
        
        // when:
        servlet.handle(response, "99", "OK", "order_id:1234", "", "");
        
        // then:
        assertBadRequest("Not enough amount!");
        assertNoEmails();
    }
    
    @Test
    public void characterization_test_10() throws Exception {
        
        // given:
        
        // when:
        servlet.handle(response, "99", "OK", "1234567K", "", "");
        
        // then:
        assertBadRequest("No active transaction with transaction_id: 1234567K!");
        assertNoEmails();
    }

    @Test
    public void characterization_test_11() throws Exception {
        
        // given:
        DB.runSql("insert into TRANSACTION(ID, ACTIVE) values ('1234567K', 'FALSE')");
        
        // when:
        servlet.handle(response, "99", "OK", "1234567K", "", "");
        
        // then:
        assertBadRequest("No active transaction with transaction_id: 1234567K!");
        assertNoEmails();
    }
    
    @Test
    public void characterization_test_12() throws Exception {
        
        // given:
        DB.runSql("insert into TRANSACTION(ID, ACTIVE, PAYMENT_ID, CONTACT_EMAIL, CONTACT_PERSON) values ('1234567K', 'TRUE', '123', 'jdoe@email.com', 'John Doe')");
        DB.runSql("insert into PAYMENT(ID) values ('123')");
        
        // when:
        servlet.handle(response, "99", "OK", "1234567K", "", "");
        
        // then:
        assertOk();
        assertSentEmails(new Email("jdoe@email.com", "Payment #123 has been successfully processed!", "Hello John Doe,\n your payment #123 has been successfully processed!\n Thanks!"));
        assertPaymentStatus("123", "COMPLETED");
    }
    
    @Test
    public void characterization_test_13() throws Exception {
        
        // given:
        DB.runSql("insert into TRANSACTION(ID, ACTIVE, PAYMENT_ID, CONTACT_EMAIL, CONTACT_PERSON) values ('1234567K', 'TRUE', '123', 'jdoe@email.com', 'John Doe')");
        DB.runSql("insert into TRANSACTION(ID, ACTIVE, PAYMENT_ID, CONTACT_EMAIL, CONTACT_PERSON) values ('1234567G', 'TRUE', '123', 'jdoe@email.com', 'John Doe')");
        
        // when:
        servlet.handle(response, "99", "OK", "1234567K", "", "");
        
        // then:
        assertBadRequest("Multiple active transactions detected for payment: 123!");
        assertNoEmails();        
    }
    
    @Test
    public void characterization_test_14() throws Exception {
        
        // given:
        DB.runSql("insert into TRANSACTION(ID, ACTIVE, PAYMENT_ID, CONTACT_EMAIL, CONTACT_PERSON) values ('1234567K', 'TRUE', '123', 'jdoe@email.com', 'John Doe')");
        DB.runSql("insert into TRANSACTION(ID, ACTIVE, PAYMENT_ID, CONTACT_EMAIL, CONTACT_PERSON) values ('1234567G', 'FALSE', '123', 'jdoe@email.com', 'John Doe')");
        DB.runSql("insert into PAYMENT(ID) values ('123')");
        
        // when:
        servlet.handle(response, "99", "OK", "1234567K", "", "");
        
        // then:
        assertOk();
        assertSentEmails(new Email("jdoe@email.com", "Payment #123 has been successfully processed!", "Hello John Doe,\n your payment #123 has been successfully processed!\n Thanks!"));
        assertPaymentStatus("123", "COMPLETED");
    }
    
    @Test
    public void characterization_test_15() throws Exception {
        
        // given:
        DB.runSql("insert into TRANSACTION(ID, ACTIVE, PAYMENT_ID, CONTACT_EMAIL, CONTACT_PERSON) values ('1234567K', 'TRUE', '123', 'jdoe@email.com', 'John Doe')");
        DB.runSql("insert into PAYMENT(ID) values ('123')");
        
        // when:
        servlet.handle(response, "99", "COMPLETED", "1234567K", "", "");
        
        // then:
        assertOk();
        assertSentEmails(new Email("jdoe@email.com", "Payment #123 has been cancelled!", "Hello John Doe,\n your payment #123 has been cancelled!"));
        assertPaymentStatus("123", "CANCELLED");
    }

    @Test
    public void characterization_test_16() throws Exception {
        
        // given:
        DB.runSql("insert into TRANSACTION(ID, ACTIVE, PAYMENT_ID, CONTACT_EMAIL, CONTACT_PERSON) values ('1234567K', 'TRUE', '123', 'jdoe@email.com', 'John Doe')");
        DB.runSql("insert into PAYMENT(ID, STATUS) values ('123', 'COMPLETED')");
        
        // when:
        servlet.handle(response, "99", "COMPLETED", "1234567K", "", "");
        
        // then:
        assertBadRequest("Illegal operation (COMPLETED) for completed payment: 1234567K!");
        assertNoEmails();
    }

    // --
    
    public void assertNoEmails() {
        assertThat(sentEmails).isEmpty();
    }
    
    public void assertSentEmails(Email... emails) {
        assertThat(sentEmails).containsExactly(emails);
    }
    
    public void assertOk() {
        assertThat(responseText).isEqualTo("OK");
    }
    
    public void assertBadRequest(String message) throws IOException {
        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, message);
        assertThat(responseText).isNull();
    }
    
    public void assertForbidden(String message) throws IOException {
        verify(response).sendError(HttpServletResponse.SC_FORBIDDEN, message);
        assertThat(responseText).isNull();
    }
    
    public void assertPaymentStatus(String paymentId, String status) {
        PaymentService service = new PaymentService();
        assertThat(service.findPaymentById(paymentId).getState()).isEqualTo(status);
    }
    
    private void assertOrderStatus(String orderId, String status) {
        SbsOrderDao dao = SbsOrderDao.getInstance();
        assertThat(dao.findOrderById(orderId).getStatus()).isEqualTo(status);
    }
    
}
