/*
 * Copyright 2013 Barzan Mozafari
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dbseer.comp.data;

import dbseer.comp.clustering.Cluster;

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
