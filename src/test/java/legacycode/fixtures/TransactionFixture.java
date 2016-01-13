package legacycode.fixtures;

import java.util.ArrayList;
import java.util.List;

import legacycode.transaction.Transaction;

import static legacycode.transaction.TransactionBuilder.aTransaction;

/**
 *
 * @author mcendrowicz
 */
public class TransactionFixture {
	
	public static Transaction noTransaction() {
		return null;
	}
	
	public static Transaction notActive() {
		return aTransaction().withActive(false).build();
	}
	
	public static Transaction active30000() {
		return aTransaction().withActive(true).withId("30000").build();
	}
	
	public static List<Transaction> randomTransactions() {
		List<Transaction> transactions = new ArrayList<Transaction>();
		transactions.add(aTransaction().withActive(true).withId("30000").build());
		transactions.add(aTransaction().withActive(true).withId("40000").build());
		transactions.add(aTransaction().withActive(true).withId("50000").build());
		
		return transactions;
	}

}