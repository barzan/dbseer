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
