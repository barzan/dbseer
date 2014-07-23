package dbseer.comp.data;

import java.util.Comparator;

/**
 * Created by dyoon on 2014. 7. 10..
 */
public class ClusterSizeComparator implements Comparator<Cluster>
{
	@Override
	public int compare(Cluster c1, Cluster c2)
	{
		int s1 = c1.getTransactions().size();
		int s2 = c2.getTransactions().size();

		if (s1 < s2) return -1;
		else if (s2 < s1) return 1;
		return 0;
	}
}
