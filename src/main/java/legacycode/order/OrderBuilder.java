package legacycode.order;

import legacycode.customerdata.CustomerData;

/**
 *
 * @author mcendrowicz
 */
public class OrderBuilder {
	
	private String id;
	private int totalPrice;
	private String status;
	private CustomerData customerData;
	
	public static OrderBuilder anOrder() {
		return new OrderBuilder();
	}
	
	public OrderBuilder withId(String id) {
		this.id = id;
		return this;
	}
	
	public OrderBuilder withTotalPrice(int totalPrice) {
		this.totalPrice = totalPrice;
		return this;
	}
	
	public OrderBuilder withStatus(String status) {
		this.status = status;
		return this;
	}
	
	public OrderBuilder withCustomerData(CustomerData customerData) {
		this.customerData = customerData;
		return this;
	}
	
	public Order build() {
		return new Order(id, totalPrice, status, customerData);
	}

}
