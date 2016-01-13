package legacycode.payment;

/**
 *
 * @author mcendrowicz
 */
public class PaymentBuilder {

	private String id;
	private String state;
	private Integer amount;

	public static PaymentBuilder aPayment() {
		return new PaymentBuilder();
	}

	public PaymentBuilder withId(String id) {
		this.id = id;
		return this;
	}

	public PaymentBuilder withState(String state) {
		this.state = state;
		return this;
	}

	public PaymentBuilder withAmount(Integer amount) {
		this.amount = amount;
		return this;
	}

	public Payment build() {
		return new Payment(id, state, amount);
	}

}
