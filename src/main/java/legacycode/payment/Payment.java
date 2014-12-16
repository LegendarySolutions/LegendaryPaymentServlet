package legacycode.payment;

public class Payment {

	private String id;
	private String state;
	private Integer amount;
	
	public Payment() {
	}
	
	public Payment(String id, String state, Integer amount) {
		this.id = id;
		this.state = state;
		this.amount = amount;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getState() {
		return state;
	}

	public void setState(String newState) {
		this.state = newState;
	}

	public Integer getAmount() {
		return amount;
	}

	public void setAmount(Integer amount) {
		this.amount = amount;
	}
}
