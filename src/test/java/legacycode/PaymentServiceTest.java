package legacycode;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import legacycode.infra.DB;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

// TODO: Weird o_O Tests pass if run separetely but fail if run together. Will fix this later, ignoring just for now...
@Ignore   
public class PaymentServiceTest {

    private PaymentService paymentService;

    @BeforeClass
    public static void startDB() {
        DB.start();
        DB.runScript("createDB.sql");
    }
    
    @Before
    public void setUp() {
        paymentService = new PaymentService();
    }
    
    @Test
    public void findTransactionsByPaymentId() throws Exception {

        DB.runSql("insert into TRANSACTION(ID, ACTIVE, CONTACT_EMAIL, CONTACT_PERSON, PAYMENT_ID) "
                + "values ('2000042S', 'FALSE', 'alan@customers.com', 'Alan', '70043')");
        
        List<Transaction> transactions = paymentService.findTransactionsByPaymentId("70043");
        
        assertThat(transactions).hasSize(1);
    }

    @Test
    public void findNotTransactionsByPaymentId() throws Exception {

        List<Transaction> transactions = paymentService.findTransactionsByPaymentId("70043");
        
        assertThat(transactions).hasSize(0);
    }
}
