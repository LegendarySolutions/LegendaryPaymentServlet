package legacycode.validators;

import java.util.List;

import legacycode.PaymentService;
import legacycode.transaction.Transaction;

/**
 *
 * @author mcendrowicz
 */
public class TransactionValidator {
	
	private final PaymentService paymentService;
	
	public TransactionValidator(final PaymentService paymentService) {
		this.paymentService = paymentService;
	}
	
	public void assertValidTransaction(final String orderId) {
		Transaction transaction = paymentService.findTransactionById(orderId);
		
		if (transaction == null || !transaction.isActive()) {
			throw new IllegalArgumentException("No active transaction with transaction_id: " + orderId + "!");
		}
		
		List<Transaction> transactions = paymentService.findTransactionsByPaymentId(transaction.getPaymentId());
		for (Transaction t : transactions) {
			// only one active transaction is allowed!
            if (t.isActive() && !t.getId().equals(transaction.getId())) {
				throw new IllegalArgumentException("Multiple active transactions detected for payment: " + transaction.getPaymentId() + "!");
			}
		}
	}

}
