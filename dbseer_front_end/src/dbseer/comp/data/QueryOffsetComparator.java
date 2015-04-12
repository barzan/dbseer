package dbseer.comp.data;

import java.util.Comparator;

/**
 * Created by dyoon on 15. 1. 4..
 */
public class QueryOffsetComparator implements Comparator<QueryOffset>
{
	@Override
	public int compare(QueryOffset q1, QueryOffset q2)
	{
		if (q1.getId() < q2.getId()) return 1;
		else if (q1.getId() > q2.getId()) return -1;
		return 0;
	}
}
