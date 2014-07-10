package dbseer.comp.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dyoon on 2014. 7. 6..
 */
public class Cluster
{
	private int id;
	private List<Transaction> transactions;

	public Cluster()
	{
		id = -1;
		transactions = new ArrayList<Transaction>();
	}

	public void addTransaction(Transaction transaction)
	{
		transactions.add(transaction);
		transaction.setClassification(Transaction.CLASSIFIED);
		transaction.setCluster(this);
	}

	public List<Transaction> getTransactions()
	{
		return transactions;
	}

	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id = id;
	}
}
