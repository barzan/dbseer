package dbseer.comp.data;

import java.util.Comparator;

/**
 * Created by dyoon on 2014. 8. 6..
 */
public class TransactionComparatorByEndTime implements Comparator<Transaction>
{
	@Override
	public int compare(Transaction t1, Transaction t2)
	{
		long myTime = t1.getEndTime();
		long otherTime = t2.getEndTime();

		if ( myTime < otherTime ) return -1;
		else if ( myTime > otherTime ) return 1;
		else return 0;
	}
}
