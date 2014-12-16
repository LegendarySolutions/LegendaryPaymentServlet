package legacycode;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import legacycode.order.Order;
import legacycode.payment.Payment;
import legacycode.transaction.Transaction;
import legacycode.validators.OrderValidator;
import legacycode.validators.SignatureValidator;
import legacycode.validators.TransactionValidator;

public class PaymentServlet extends HttpServlet {

	private final PaymentService paymentService;
	private final SignatureValidator signatureValidator;
	private final SbsOrderDao sbsDao;
	private final OrderValidator orderValidator;
	private final TransactionValidator transactionValidator;
	private final EMailService emailService;

	public PaymentServlet() {
		paymentService = new PaymentService();
		signatureValidator = new SignatureValidator();
		sbsDao = SbsOrderDao.getInstance();
		orderValidator = new OrderValidator(sbsDao);
		transactionValidator = new TransactionValidator(paymentService);
		emailService = new EMailService();
	}

	public PaymentServlet(PaymentService paymentService, SignatureValidator signatureValidator, SbsOrderDao sbsDao,
			OrderValidator orderValidator, TransactionValidator transactionValidator, EMailService emailService) {
		this.paymentService = paymentService;
		this.signatureValidator = signatureValidator;
		this.sbsDao = sbsDao;
		this.orderValidator = orderValidator;
		this.transactionValidator = transactionValidator;
		this.emailService = emailService;
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
	
	protected void handle(HttpServletResponse resp, String amount, String status, String payload, String timestamp, String md5) throws IOException {
		try {
			signatureValidator.assertValidRequest(amount, status, payload, timestamp, md5);
		} catch (IllegalArgumentException iaex) {
			resp.sendError(HttpServletResponse.SC_FORBIDDEN, iaex.getMessage());
			return;
		}

		try {
			PaymentMethodResolver pmr = new PaymentMethodResolver(payload);
			boolean isSBS = pmr.isSBS;
			String orderId = pmr.orderId;
			
			if (isSBS) {
				orderValidator.assertValidOrder(orderId);
				handleOrder(orderId, status, amount);
			} else {
				transactionValidator.assertValidTransaction(orderId);
				handleTransaction(orderId, status, amount);
			}
		} catch (IllegalArgumentException iaex) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, iaex.getMessage());
			return;
		}
		
		resp.getOutputStream().print("OK");
	}

	protected void handleOrder(String orderId, String status, String amount) {
		Order order = sbsDao.findOrderById(orderId);
		if ("OK".equals(status)) {
			makeOrderPaid(order, amount);
		} else {
			cancelOrderOrMakeExpired(order, status);
		}
	}

	protected void cancelOrderOrMakeExpired(Order order, String status) {
		order.setStatus(status);
		sbsDao.save(order);
		emailService.cancelledOrExpiredOrderNotification(order, status);
	}
	
	protected void makeOrderPaid(Order order, String amount) {
		final Integer requiredAmount = order.getTotalPrice();
		final Integer incomingAmount = Integer.parseInt(amount);
		
		if (requiredAmount > incomingAmount) {
			throw new IllegalArgumentException("Not enough amount!");
		} else {
			order.setStatus("PAID");
			sbsDao.save(order);
			int surplus = incomingAmount - requiredAmount;
			emailService.paidOrderNotification(order, surplus);
		}
	}
	
	protected void handleTransaction(String orderId, String status, String amount) {
		Transaction transaction = paymentService.findTransactionById(orderId);
		Payment payment = paymentService.findPaymentById(transaction.getPaymentId());
		if ("OK".equals(status)) {
			completeTransaction(transaction, payment, amount);
		} else {
			cancelTransaction(transaction, payment, status, orderId);
		}
	}

	protected void cancelTransaction(Transaction transaction, Payment payment, String status, String orderId) throws IllegalArgumentException {
		if ("COMPLETED".equals(payment.getState())) {
			throw new IllegalArgumentException("Illegal operation (" + status + ") for completed payment: " + orderId + "!");
		} else {
			payment.setState("CANCELLED");
			paymentService.updatePayment(payment);
			emailService.cancelledTransactionNotification(transaction, payment);
		}
	}

	protected void completeTransaction(Transaction transaction, Payment payment, String amount) {
		final Integer requiredAmount = payment.getAmount();
		final Integer incomingAmount = Integer.parseInt(amount);
		
		if (requiredAmount > incomingAmount) {
			throw new IllegalArgumentException("Not enough amount!");
		}
		
		paymentService.setInactiveTransaction(transaction);
		payment.setState("COMPLETED");
		paymentService.updatePayment(payment);
		
		emailService.completedTransactionNotification(transaction, payment);
	}
	
	class PaymentMethodResolver {
		final Pattern sbsPattern = Pattern.compile(".*order_id:(\\d{4}).*");
		final Pattern transPattern = Pattern.compile("^(\\d{5,7}(S|K|G)).*");
		
		boolean isSBS = false;
		String orderId = null;
		
		PaymentMethodResolver(final String payload) {
			Matcher sbsMatcher = sbsPattern.matcher(payload);
			Matcher transMatcher = transPattern.matcher(payload);
			if (sbsMatcher.matches()) {
				isSBS = true;
				orderId = sbsMatcher.group(1);
			} else if (transMatcher.matches()) {
				isSBS = false;
				orderId = transMatcher.group(1);
			}
			if (orderId == null) {
				throw new IllegalArgumentException("Unrecognized format of payload!");
			}
		}
	}

}
