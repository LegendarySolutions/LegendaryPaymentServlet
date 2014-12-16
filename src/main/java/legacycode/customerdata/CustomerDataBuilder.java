package legacycode.customerdata;

/**
 *
 * @author mcendrowicz
 */
public class CustomerDataBuilder {
	
	private String email;
	private String fullName;
	
	public static CustomerDataBuilder aCustomerData() {
		return new CustomerDataBuilder();
	}
	
	public CustomerDataBuilder withEmail(String email) {
		this.email = email;
		return this;
	}
	
	public CustomerDataBuilder withFullName(String fullName) {
		this.fullName = fullName;
		return this;
	}
	
	public CustomerData build() {
		return new CustomerData(email, fullName);
	}
}
