package legacycode.transaction;

public class Transaction {

	private String id;
	private String paymentId;
	private boolean active = true;
	private String contactEmail;
	private String contactPerson;
	
	public Transaction() {
		
	}

	public Transaction(String id, String paymentId, boolean active, String contactEmail, String contactPerson) {
		this.id = id;
		this.paymentId = paymentId;
		this.active = active;
		this.contactEmail = contactEmail;
		this.contactPerson = contactPerson;
	}
	
	public String getContactEmail() {
		return contactEmail;
	}

	public void setContactEmail(String contactEmail) {
		this.contactEmail = contactEmail;
	}

	public String getContactPerson() {
		return contactPerson;
	}

	public void setContactPerson(String contactPerson) {
		this.contactPerson = contactPerson;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPaymentId() {
		return paymentId;
	}

	public void setPaymentId(String paymentId) {
		this.paymentId = paymentId;
	}
}
