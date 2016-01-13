package legacycode.fixtures;

import legacycode.customerdata.CustomerData;
import legacycode.order.Order;

import static legacycode.order.OrderBuilder.anOrder;

/**
 *
 * @author mcendrowicz
 */
public class OrderFixture {
	
	public static Order noOrder() {
		return null;
	}
	
	public static Order newOrder() {
		return anOrder().withStatus("NEW").build();
	}
	
	public static Order pendingOrder() {
		return anOrder().withStatus("PENDING").build();
	}
	
	public static Order pending_1000_Order() {
		return anOrder().withStatus("PENDING").withTotalPrice(1000).build();
	}
	
	public static Order pendingWithCustomerData() {
		return anOrder().withTotalPrice(100).withId("6666").withStatus("PENDING").withCustomerData(new CustomerData("abc", "xyz")).build();
	}
	
	public static Order cancelledOrder() {
		return anOrder().withStatus("CANCELLED").build();
	}
	
	public static Order expiredOrder() {
		return anOrder().withStatus("EXPIRED").build();
	}

}
