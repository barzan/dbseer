package dbseer.comp.data;

/**
 * Created by dyoon on 2014. 7. 8..
 */
public class TransactionDistance implements Comparable<TransactionDistance>
{
	private Transaction transaction;
	private double distance;

	public TransactionDistance(Transaction transaction, double distance)
	{
		this.transaction = transaction;
		this.distance = distance;
	}

	@Override
	public int compareTo(TransactionDistance other)
	{
		if (this.distance < other.getDistance()) return -1;
		else if (this.distance > other.getDistance()) return 1;
		else return 0;
	}

	public Transaction getTransaction()
	{
		return transaction;
	}

	public void setTransaction(Transaction transaction)
	{
		this.transaction = transaction;
	}

	public double getDistance()
	{
		return distance;
	}

	public void setDistance(double distance)
	{
		this.distance = distance;
	}
}
