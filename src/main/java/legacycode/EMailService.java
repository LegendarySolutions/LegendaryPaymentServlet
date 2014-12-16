package legacycode;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import legacycode.customerdata.CustomerData;
import legacycode.order.Order;
import legacycode.payment.Payment;
import legacycode.transaction.Transaction;

/**
 *
 * @author mcendrowicz
 */
public class EMailService {

	public void cancelledOrExpiredOrderNotification(Order order, String status) {
		final String orderId = order.getId();
		final CustomerData customerData = order.getCustomerData();
		StringBuilder subject = new StringBuilder();
		subject.append("Order #").append(orderId).append(" has been ").append(status).append("!");

		StringBuilder body = new StringBuilder();
		body.append("Hello ").append(customerData.getFullName()).append(",\n");
		body.append("your payment for order #").append(orderId).append(" has been ").append(status).append("!");

		sendEmail(customerData.getEmail(), subject.toString(), body.toString());
	}

	public void paidOrderNotification(Order order, int surplus) {
		final String orderId = order.getId();
		final CustomerData customerData = order.getCustomerData();
		StringBuilder subject = new StringBuilder();
		subject.append("Order #").append(orderId).append(" has been successfully processed!");

		StringBuilder body = new StringBuilder();
		body.append("Hello ").append(customerData.getFullName()).append(",\n");
		body.append("your payment for order #").append(orderId).append(" has been successfully processed!").append("\n");

		if (surplus > 0) {
			body.append("We have registered surplus of ").append(surplus).append("USD on your account.").append("\n");
			sendEmail("admin@oursystem.com", "Order #" + orderId + " has surplus of " + surplus, "");
		}
		body.append("Thanks!");

		sendEmail(customerData.getEmail(), subject.toString(), body.toString());
	}

	public void cancelledTransactionNotification(Transaction transaction, Payment payment) {
		final String paymentId = payment.getId();
		final String contactEmail = transaction.getContactEmail();
		final String contactPerson = transaction.getContactPerson();

		StringBuilder subject = new StringBuilder();
		subject.append("Payment #").append(paymentId).append(" has been cancelled!");

		StringBuilder body = new StringBuilder();
		body.append("Hello ").append(contactPerson).append(",\n");
		body.append("your payment #").append(paymentId).append(" has been cancelled!");

		sendEmail(contactEmail, subject.toString(), body.toString());
	}

	public void completedTransactionNotification(Transaction transaction, Payment payment) {
		final String paymentId = payment.getId();
		final String contactEmail = transaction.getContactEmail();
		final String contactPerson = transaction.getContactPerson();

		StringBuilder subject = new StringBuilder();
		subject.append("Payment #").append(paymentId).append(" has been successfully processed!");

		StringBuilder body = new StringBuilder();
		body.append("Hello ").append(contactPerson).append(",\n");
		body.append("your payment #").append(paymentId).append(" has been successfully processed!").append("\n").append("Thanks!");

		sendEmail(contactEmail, subject.toString(), body.toString());
	}

	private void sendEmail(String address, String subject, String body) {
		try {
			Session session = Session.getInstance(new Properties());
			Message msg = new MimeMessage(session);
			msg.setRecipient(Message.RecipientType.TO, new InternetAddress(address));
			msg.setSubject(subject);
			msg.setText(body);
			msg.saveChanges();
			Transport transport = session.getTransport("smtp");
			transport.connect("127.0.0.1", 9988, "", "");
			transport.sendMessage(msg, msg.getAllRecipients());

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
