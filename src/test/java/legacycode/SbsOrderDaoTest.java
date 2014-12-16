package legacycode;

import java.sql.SQLException;

import org.junit.*;

import legacycode.customerdata.CustomerData;
import legacycode.infra.DB;
import legacycode.order.Order;

import static org.assertj.core.api.Assertions.assertThat;

public class SbsOrderDaoTest {

    private SbsOrderDao sut = SbsOrderDao.getInstance();

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
		DB.runSql("drop table ORDERS");
	}
	
    @Test
    public void shouldFindOrderById() throws Exception {
		// given
        Order order = new Order();
        order.setId("234");
        order.setStatus("NEW");
        order.setTotalPrice(123);
        order.setCustomerData(new CustomerData("jdoe@mail.com", "John Doe"));
        //when
		sut.save(order);
		//then
        assertThat(sut.findOrderById("123")).isNull();
        assertThat(sut.findOrderById("234")).isNotNull();
    }
	
	@Test
	public void shouldNotFindOrderAndBehaveCorrectly() throws Exception {
		assertThat(sut.findOrderById("234")).isNull();
	}
}
