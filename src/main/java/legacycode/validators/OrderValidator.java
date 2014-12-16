package legacycode.validators;

import legacycode.SbsOrderDao;
import legacycode.order.Order;

/**
 *
 * @author mcendrowicz
 */
public class OrderValidator {
	
	private final SbsOrderDao sbsDao;
	
	public OrderValidator(final SbsOrderDao sbsDao) {
		this.sbsDao = sbsDao;
	}
	
	public void assertValidOrder(final String orderId) {
		Order order = sbsDao.findOrderById(orderId);
		if (order == null || !"PENDING".equals(order.getStatus())) {
			throw new IllegalArgumentException("No pending oder with id: " + orderId + "!");
		}
	}

}
