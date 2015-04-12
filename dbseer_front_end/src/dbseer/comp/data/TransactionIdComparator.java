package dbseer.comp.data;

import java.util.Comparator;

/**
 * Created by dyoon on 15. 1. 1..
 */
public class TransactionIdComparator implements Comparator<Transaction>
{
	@Override
	public int compare(Transaction t1, Transaction t2)
	{
		int i1 = t1.getId();
		int i2 = t2.getId();

		if (i1 < i2) return -1;
		else if (i2 < i1) return 1;
		return 0;
	}
}
