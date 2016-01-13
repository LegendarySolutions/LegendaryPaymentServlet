package legacycode.order;

import legacycode.customerdata.CustomerData;

public class Order {

	private String id;
	private int totalPrice;
	private String status = "NEW";
	private CustomerData customerData;
	
	// default constructor to keep the project compilable
	public Order() {
		
	}
	
	/**
	 * New constructor
	 * 
	 * @param id
	 * @param totalPrice
	 * @param status
	 * @param customerData 
	 */
	public Order(String id, int totalPrice, String status, CustomerData customerData) {
		this.id = id;
		this.totalPrice = totalPrice;
		this.status = status;
		this.customerData = customerData;
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getStatus() {
		return status;
	}
	
	public void setStatus(String status) {
		this.status = status;
	}

	public int getTotalPrice() {
		return totalPrice;
	}

	public void setTotalPrice(int totalPrice) {
		this.totalPrice = totalPrice;
	}

	public CustomerData getCustomerData() {
		return customerData;
	}

	public void setCustomerData(CustomerData data) {
		this.customerData = data;
	}

}
