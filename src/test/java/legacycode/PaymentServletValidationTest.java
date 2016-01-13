package legacycode;

import java.io.IOException;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletResponse;

import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetup;
import legacycode.fixtures.DummyServletOutputStream;
import legacycode.fixtures.OrderFixture;
import legacycode.fixtures.TransactionFixture;
import legacycode.validators.OrderValidator;
import legacycode.validators.SignatureValidator;
import legacycode.validators.TransactionValidator;
import pl.wkr.fluentrule.api.FluentExpectedException;

import static legacycode.payment.PaymentBuilder.aPayment;
import static legacycode.transaction.TransactionBuilder.aTransaction;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.matches;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(MockitoJUnitRunner.class)
public class PaymentServletValidationTest {

//    @Rule
//    public FluentExpectedException fluentEx = FluentExpectedException.none();
	
	@Mock
    private HttpServletResponse response;
    @Mock
    private SignatureValidator signatureValidator;
	@Mock
	private PaymentService paymentService;
	@Mock
	private SbsOrderDao sbsOrderDao;
	@InjectMocks
	private OrderValidator orderValidator;
	@InjectMocks
	private TransactionValidator transactionValidator;
	@Spy
	private EMailService emailService;

    private PaymentServlet sut;
	
	private GreenMail greenMail;

    @Before
    public void init() {
		greenMail = new GreenMail(new ServerSetup(9988, "127.0.0.1", ServerSetup.PROTOCOL_SMTP));
		greenMail.start();
        sut = new PaymentServlet(paymentService, signatureValidator, sbsOrderDao, orderValidator, transactionValidator, emailService);
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(response);
		greenMail.stop();
    }

    @Test
    public void shouldKeepValidationErrorReasonInResponse() throws IOException {
        //given
        willThrow(new IllegalArgumentException("MD5 signature do not match!")).given(signatureValidator).assertValidRequest("", "", "", "", "");
        //when
        sut.handle(response, "", "", "", "", "");
        //then
        verify(response).sendError(HttpServletResponse.SC_FORBIDDEN, "MD5 signature do not match!");
    }

    @Test
    public void shouldDoBusinessLogicOnValidRequest() throws IOException {
        //when
        sut.handle(response, "", "", "", "100000", "5f142f02085b27c938897385782563f6");
        //then
        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Unrecognized format of payload!");
        verify(signatureValidator).assertValidRequest("", "", "", "100000", "5f142f02085b27c938897385782563f6");
    }
	
	@Test
	public void shouldFailOnNotExistingOrder() throws IOException {
		//given
		given(sbsOrderDao.findOrderById(matches("\\d{4}"))).willReturn(OrderFixture.noOrder());
		//when
		sut.handle(response, "", "", "order_id:6666", "100000", "5f142f02085b27c938897385782563f6");
		//then
		verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "No pending oder with id: 6666!");
	}
	
	@Test
	public void shouldFailOnNotPendingOrder() throws IOException {
		//given
		given(sbsOrderDao.findOrderById(matches("\\d{4}"))).willReturn(OrderFixture.expiredOrder());
		//when
		sut.handle(response, "", "", "order_id:6666", "100000", "5f142f02085b27c938897385782563f6");
		//then
		verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "No pending oder with id: 6666!");
	}
	
	@Test
	public void shouldCancelOrder() throws IOException, InterruptedException, MessagingException {
		//given
		given(sbsOrderDao.findOrderById(matches("\\d{4}"))).willReturn(OrderFixture.pendingWithCustomerData());
		given(response.getOutputStream()).willReturn(new DummyServletOutputStream());
		//when
		sut.handle(response, "100", "CANCELLED", "order_id:6666", "100000", "5f142f02085b27c938897385782563f6");
		//then
		verify(response, only()).getOutputStream();
		//and
		assertThat(greenMail.waitForIncomingEmail(1)).as("One email received").isTrue();
		MimeMessage firstMessage = greenMail.getReceivedMessages()[0];
		assertThat(firstMessage.getSubject()).isEqualTo("Order #6666 has been CANCELLED!");
	}
	
	@Test
	public void shouldExpireOrder() throws IOException, InterruptedException, MessagingException {
		//given
		given(sbsOrderDao.findOrderById(matches("\\d{4}"))).willReturn(OrderFixture.pendingWithCustomerData());
		given(response.getOutputStream()).willReturn(new DummyServletOutputStream());
		//when
		sut.handle(response, "100", "EXPIRED", "order_id:6666", "100000", "5f142f02085b27c938897385782563f6");
		//then
		verify(response, only()).getOutputStream();
		//and
		assertThat(greenMail.waitForIncomingEmail(1)).as("One email received").isTrue();
		MimeMessage firstMessage = greenMail.getReceivedMessages()[0];
		assertThat(firstMessage.getSubject()).isEqualTo("Order #6666 has been EXPIRED!");
	}
	
	@Test
	public void shouldCompleteOrder() throws IOException, InterruptedException, MessagingException {
		//given
		given(sbsOrderDao.findOrderById(matches("\\d{4}"))).willReturn(OrderFixture.pendingWithCustomerData());
		given(response.getOutputStream()).willReturn(new DummyServletOutputStream());
		//when
		sut.handle(response, "101", "OK", "order_id:6666", "100000", "5f142f02085b27c938897385782563f6");
		//then
		verify(response, only()).getOutputStream();
		//and
		assertThat(greenMail.waitForIncomingEmail(1)).as("One email received").isTrue();
		MimeMessage firstMessage = greenMail.getReceivedMessages()[0];
		assertThat(firstMessage.getSubject()).isEqualTo("Order #6666 has been successfully processed!");
		assertThat(GreenMailUtil.getBody(firstMessage)).contains("surplus");
	}
	
	@Test
	public void shouldFailOnNotEnoughAmount_O() throws IOException {
		//given
		given(sbsOrderDao.findOrderById(matches("\\d{4}"))).willReturn(OrderFixture.pending_1000_Order());
		//when
		sut.handle(response, "100", "OK", "order_id:6666", "100000", "5f142f02085b27c938897385782563f6");
		//then
		verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Not enough amount!");
	}
	
	@Test
	public void shouldFailOnNotExistingTransaction() throws IOException {
		//given
		given(paymentService.findTransactionById(matches("\\d{5,7}S|K|G"))).willReturn(TransactionFixture.noTransaction());
		//when
		sut.handle(response, "100", "OK", "11111S", "100000", "5f142f02085b27c938897385782563f6");
		//then
		verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "No active transaction with transaction_id: 11111S!");
	}
	
	@Test
	public void shouldFailOnNotActiveTransaction() throws IOException {
		//given
		given(paymentService.findTransactionById(matches("\\d{5,7}S|K|G"))).willReturn(TransactionFixture.notActive());
		//when
		sut.handle(response, "100", "OK", "11111S", "100000", "5f142f02085b27c938897385782563f6");
		//then
		verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "No active transaction with transaction_id: 11111S!");
	}
	
	@Test
	public void shouldFailOnMultipleTransactions() throws IOException {
		//given
		given(paymentService.findTransactionById(matches("\\d{5,7}S|K|G"))).willReturn(aTransaction()
				.withActive(true)
				.withId("30000")
				.withPaymentId("somePID")
				.build());
		given(paymentService.findTransactionsByPaymentId("somePID")).willReturn(TransactionFixture.randomTransactions());
		//when
		sut.handle(response, "100", "OK", "11111S", "100000", "5f142f02085b27c938897385782563f6");
		//then
		verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Multiple active transactions detected for payment: somePID!");
	}
	
	@Test
	public void shouldCompleteValidTransaction() throws IOException, InterruptedException, MessagingException {
		//given
		given(paymentService.findTransactionById(matches("\\d{5,7}S|K|G"))).willReturn(aTransaction()
				.withActive(true)
				.withId("30000")
				.withPaymentId("somePID")
				.withContactEmail("xyz")
				.withContactPerson("abc")
				.build());
		given(paymentService.findTransactionsByPaymentId("somePID")).willReturn(anyList());
		given(paymentService.findPaymentById("somePID")).willReturn(aPayment()
				.withId("somePID")
				.withState("someMagicState")
				.withAmount(1000)
				.build());
		given(response.getOutputStream()).willReturn(new DummyServletOutputStream());
		//when
		sut.handle(response, "1000", "OK", "11111S", "100000", "5f142f02085b27c938897385782563f6");
		//then
		verify(response, only()).getOutputStream();
		//and
		assertThat(greenMail.waitForIncomingEmail(1)).as("One email received").isTrue();
		MimeMessage firstMessage = greenMail.getReceivedMessages()[0];
		assertThat(firstMessage.getSubject()).isEqualTo("Payment #somePID has been successfully processed!");
	}
	
	@Test
	public void shouldFailOnNotEnoughAmount_T() throws IOException {
		//given
		given(paymentService.findTransactionById(matches("\\d{5,7}S|K|G"))).willReturn(aTransaction()
				.withActive(true)
				.withId("30000")
				.withPaymentId("somePID")
				.build());
		given(paymentService.findTransactionsByPaymentId("somePID")).willReturn(anyList());
		given(paymentService.findPaymentById("somePID")).willReturn(aPayment()
				.withId("somePID")
				.withState("someMagicState")
				.withAmount(1000)
				.build());
		//when
		sut.handle(response, "100", "OK", "11111S", "100000", "5f142f02085b27c938897385782563f6");
		//then
		verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Not enough amount!");
	}
	
	@Test
	public void shouldFailOnCancellingCompletedPayment() throws IOException {
		//given
		given(paymentService.findTransactionById(matches("\\d{5,7}S|K|G"))).willReturn(aTransaction()
				.withActive(true)
				.withId("30000")
				.withPaymentId("somePID")
				.build());
		given(paymentService.findTransactionsByPaymentId("somePID")).willReturn(anyList());
		given(paymentService.findPaymentById("somePID")).willReturn(aPayment()
				.withId("somePID")
				.withState("COMPLETED")
				.withAmount(1000)
				.build());
		//when
		sut.handle(response, "100", "CANCELLED", "11111S", "100000", "5f142f02085b27c938897385782563f6");
		//then
		verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Illegal operation (CANCELLED) for completed payment: 11111S!");
	}
	
}