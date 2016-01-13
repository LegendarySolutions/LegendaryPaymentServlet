package legacycode.customerdata;

public class CustomerData {

	private final String email;
	private final String fullName;

	public CustomerData(String email, String fullName) {
		this.email = email;
		this.fullName = fullName;
	}

	public String getEmail() {
		return email;
	}

	public String getFullName() {
		return fullName;
	}

}
