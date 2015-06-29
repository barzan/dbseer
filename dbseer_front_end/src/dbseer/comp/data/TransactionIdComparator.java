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
