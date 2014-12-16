package legacycode;

import java.sql.SQLException;
import java.util.List;

import org.junit.*;

import legacycode.infra.DB;
import legacycode.transaction.Transaction;

import static org.assertj.core.api.Assertions.assertThat;


public class PaymentServiceTest {

    private final PaymentService paymentService = new PaymentService();;

    @BeforeClass
    public static void beforeClassExecution() {
        DB.start();
    }
	
	@AfterClass
	public static void afterClassExecution() throws SQLException {
		DB.getConnection().close();
	}
    
    @Before
    public void init() {
        DB.runScript("createDB.sql");
    }
	
	@After
	public void tearDown() throws SQLException {
		DB.runSql("drop table TRANSACTION");
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
