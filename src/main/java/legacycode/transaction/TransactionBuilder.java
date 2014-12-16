package legacycode.transaction;

/**
 *
 * @author mcendrowicz
 */
public class TransactionBuilder {
	
	private String id;
	private String paymentId;
	private boolean active;
	private String contactEmail;
	private String contactPerson;
	
	public static TransactionBuilder aTransaction() {
		return new TransactionBuilder();
	}
	
	public TransactionBuilder withId(String id) {
		this.id = id;
		return this;
	}
	
	public TransactionBuilder withPaymentId(String paymentId) {
		this.paymentId = paymentId;
		return this;
	}
	
	public TransactionBuilder withActive(boolean active) {
		this.active = active;
		return this;
	}
	
	public TransactionBuilder withContactEmail(String contactEmail) {
		this.contactEmail = contactEmail;
		return this;
	}
	
	public TransactionBuilder withContactPerson(String contactPerson) {
		this.contactPerson = contactPerson;
		return this;
	}
	
	public Transaction build() {
		return new Transaction(id, paymentId, active, contactEmail, contactPerson);
	}

}
