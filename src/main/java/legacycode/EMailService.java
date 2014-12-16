package legacycode;

import java.io.StringWriter;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.log.NullLogChute;

import legacycode.order.Order;
import legacycode.payment.Payment;
import legacycode.transaction.Transaction;

/**
 *
 * @author mcendrowicz
 */
public class EMailService {
	
	private static final VelocityEngine velocityEngine = new VelocityEngine();
	
	static {
		try {
			Properties velocityProperties = new Properties();
			velocityProperties.setProperty(Velocity.RUNTIME_LOG_LOGSYSTEM_CLASS, NullLogChute.class.getName());
			velocityEngine.init(velocityProperties);
		} catch (Exception ex) {
			throw new ExceptionInInitializerError(ex);
		}
	}
	
	public void cancelledOrExpiredOrderNotification(Order order, String status) {
		try {
			VelocityContext context = getVelocityContext(order, 0, status);
			final String subject = getOrderNotificationSubject(context, false);
			final String body = getOrderNotificationBody(context, false);
			
			sendEmail(order.getCustomerData().getEmail(), subject, body);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void paidOrderNotification(Order order, int surplus) {
		try {
			VelocityContext context = getVelocityContext(order, surplus, null);
			final String subject = getOrderNotificationSubject(context, true);
			final String body = getOrderNotificationBody(context, true);
			
			sendEmail(order.getCustomerData().getEmail(), subject, body);
			
			if (surplus > 0) {
				sendEmail("admin@oursystem.com", "Order #" + order.getId() + " has surplus of " + surplus, "");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private String getOrderNotificationSubject(VelocityContext context, boolean isPaid) throws Exception {
		Template subjectTemplate = null;
		if (isPaid) {
			subjectTemplate = velocityEngine.getTemplate("templates/order-paid-subject.vm");
		} else {
			subjectTemplate = velocityEngine.getTemplate("templates/order-not-paid-subject.vm");
		}
		StringWriter subjectWriter = new StringWriter();
		subjectTemplate.merge(context, subjectWriter);
		
		return subjectWriter.toString();
	}
	
	private String getOrderNotificationBody(VelocityContext context, boolean isPaid) throws Exception {
		Template bodyTemplate = null;
		if (isPaid) {
			bodyTemplate = velocityEngine.getTemplate("templates/order-paid-body.vm");
		} else {
			bodyTemplate = velocityEngine.getTemplate("templates/order-not-paid-body.vm");
		}
		StringWriter bodyWriter = new StringWriter();
		bodyTemplate.merge(context, bodyWriter);
		
		return bodyWriter.toString();
	}

	private VelocityContext getVelocityContext(Order order, int surplus, String status) {
		VelocityContext context = new VelocityContext();
		context.put("orderId", order.getId());
		context.put("userName", order.getCustomerData().getFullName());
		context.put("surplus", surplus);
		context.put("status", status);
		
		return context;
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
		System.out.println("SUBJECT:");
		System.out.println(subject);
		System.out.println("BODY:");
		System.out.println(body);
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
