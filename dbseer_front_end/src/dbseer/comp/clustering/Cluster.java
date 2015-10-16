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

package dbseer.comp.clustering;

import com.google.common.collect.EvictingQueue;
import dbseer.comp.data.Transaction;
import dbseer.gui.DBSeerConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dyoon on 2014. 7. 6..
 */
public class Cluster
{
	private int id;
	private List<Transaction> transactions;
	private EvictingQueue<String> transactionExamples;

	protected long N; // Number of points in the cluster.
	protected double[] LS; // Linear sum of all points in the cluster.
	protected double[] SS; // Squared sum of all points in the cluster.

	public Cluster()
	{
		this.id = -1;
		this.transactions = new ArrayList<Transaction>();
		this.LS = new double[4 * DBSeerConstants.MAX_NUM_TABLE];
		this.SS = new double[4 * DBSeerConstants.MAX_NUM_TABLE];
		this.transactionExamples = EvictingQueue.create(DBSeerConstants.MAX_TRANSACTION_SAMPLE);
	}

	public Cluster(int id)
	{
		this.id = id;
		this.transactions = new ArrayList<Transaction>();
		this.LS = new double[4 * DBSeerConstants.MAX_NUM_TABLE];
		this.SS = new double[4 * DBSeerConstants.MAX_NUM_TABLE];
		this.transactionExamples = EvictingQueue.create(DBSeerConstants.MAX_TRANSACTION_SAMPLE);
	}

	public Cluster copy()
	{
		Cluster copy = new Cluster(this.id);
		copy.N = this.N;
		copy.LS = this.LS.clone();
		copy.SS = this.SS.clone();
		copy.transactions = (List<Transaction>) ((ArrayList<Transaction>) this.transactions).clone();

		return copy;
	}

	public void insert(Transaction t)
	{
		++N;
		long[] numSelect = t.getNumSelect();
		long[] numInsert = t.getNumInsert();
		long[] numDelete = t.getNumDelete();
		long[] numUpdate = t.getNumUpdate();
		int numTable = StreamClustering.getTableCount();

		for (int i = 0; i < numTable; ++i)
		{
			this.LS[i*4] += numSelect[i];
			this.LS[i*4+1] += numInsert[i];
			this.LS[i*4+2] += numDelete[i];
			this.LS[i*4+3] += numUpdate[i];

			this.SS[i*4] += numSelect[i] * numSelect[i];
			this.SS[i*4+1] += numInsert[i] * numInsert[i];
			this.SS[i*4+2] += numDelete[i] * numDelete[i];
			this.SS[i*4+3] += numUpdate[i] * numUpdate[i];
		}

		transactionExamples.add(t.getEntireStatement());
		t.setCluster(this);
	}

	public void addTransaction(Transaction transaction)
	{
		++N;
		transactions.add(transaction);
		transaction.setClassification(Transaction.CLASSIFIED);
		transaction.setCluster(this);
	}

	public double getDistance(Transaction t)
	{
		double distance = 0.0;
		double[] center = this.LS;
		double[] point = t.toDoubleArray();
		int maxIdx = StreamClustering.getTableCount();
		for (int i = 0; i < maxIdx * 4; ++i)
		{
			double val1 = center[i] / this.N;
			double val2 = point[i];
			double d = val1 - val2;
			if ( (val1 == 0 && val2 != 0) || (val1 != 0 && val2 == 0) )
			{
				d = d * d * Transaction.DIFF_SCALE;
			}
			else
			{
				d= d * d;
			}

			distance += d;
		}
		return Math.sqrt(distance);
	}

	public double getClusterDistance(Cluster c)
	{
		double distance = 0.0;
		double[] center = this.LS;
		double[] point = c.LS;
		int maxIdx = StreamClustering.getTableCount();
		for (int i = 0; i < maxIdx * 4; ++i)
		{
			double val1 = center[i] / this.N;
			double val2 = point[i] / c.N;
			double d = val1 - val2;
			if ( (val1 == 0 && val2 != 0) || (val1 != 0 && val2 == 0) )
			{
				d = d * d * Transaction.DIFF_SCALE;
			}
			else
			{
				d= d * d;
			}

			distance += d;
		}
		return Math.sqrt(distance);

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

	public long getSize()
	{
		return N;
	}

	public String[] getTransactionSamples()
	{
		String[] samples = transactionExamples.toArray(new String[transactionExamples.size()]);
		return samples;
	}
}
