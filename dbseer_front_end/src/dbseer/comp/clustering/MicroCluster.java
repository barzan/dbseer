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

import dbseer.comp.data.Transaction;
import dbseer.gui.DBSeerConstants;

import java.util.ArrayList;

/**
 * Created by dyoon on 9/25/15.
 * Micro cluster in denStream algorithm.
 */
public class MicroCluster extends Cluster
{
	private long lastEditTime = -1;
	private long creationTimestamp = -1;

	private double lambda;
	private double weight = 0;
	private boolean covered = false; // used in DBSCAN

	private ArrayList<Transaction> tx = new ArrayList<Transaction>();

	public MicroCluster(Transaction t, long creationTimestamp, double lambda)
	{
		tx.add(t);
		this.N = 1;
		this.weight = 1;
		long[] numSelect = t.getNumSelect();
		long[] numInsert = t.getNumInsert();
		long[] numDelete = t.getNumDelete();
		long[] numUpdate = t.getNumUpdate();
		int numTable = StreamClustering.getTableCount();

		for (int i = 0; i < numTable; ++i)
		{
			this.LS[i*4] = numSelect[i];
			this.LS[i*4+1] = numInsert[i];
			this.LS[i*4+2] = numDelete[i];
			this.LS[i*4+3] = numUpdate[i];

			for (int j = 0; j < 4; ++j)
			{
				this.SS[i * 4 + j] = Math.pow(this.LS[i * 4 + j], 2);
			}
		}
		this.creationTimestamp = creationTimestamp;
		this.lastEditTime = creationTimestamp;
		this.lambda = lambda;
	}

	public MicroCluster(double[] center, long creationTimestamp, double lambda)
	{
		this.LS = center;
		this.creationTimestamp = creationTimestamp;
		this.lastEditTime = creationTimestamp;
		this.lambda = lambda;
	}

	public MicroCluster copy()
	{
		MicroCluster copy = new MicroCluster(this.LS.clone(), this.creationTimestamp, this.lambda);

		copy.weight = this.weight + 1;
		copy.N = this.N;
		copy.SS = this.SS.clone();
		copy.LS = this.LS.clone();
		copy.lastEditTime = this.lastEditTime;

		return copy;
	}

	public void insert(Transaction t, long timestamp)
	{
		tx.add(t);
		++weight;
		lastEditTime = timestamp;

		this.insert(t);
	}

	private double[] calcCF2(long dt)
	{
		double[] cf2 = new double[SS.length];
		for (int i = 0; i < StreamClustering.getTableCount() * 4; ++i)
		{
			cf2[i] = Math.pow(2, -lambda * dt) * SS[i];
		}
		return cf2;
	}

	private double[] calcCF1(long dt)
	{
		double[] cf1 = new double[LS.length];
		for (int i = 0; i < StreamClustering.getTableCount() * 4; ++i)
		{
			cf1[i] = Math.pow(2, -lambda * dt) * SS[i];
		}
		return cf1;
	}

	public double getWeight(long timestamp)
	{
		long dt = timestamp - lastEditTime;
		return (N * Math.pow(2, -lambda * dt));
	}

	public double getRadius(long timestamp)
	{
		long dt = timestamp - lastEditTime;
		double[] cf1 = calcCF1(dt);
		double[] cf2 = calcCF2(dt);
		double w = getWeight(timestamp);
		double CF1 = 0;
		double CF2 = 0;
		for (int i = 0; i < StreamClustering.getTableCount() * 4; ++i)
		{
			CF1 += cf1[i];
			CF2 += cf2[i];
		}
		return Math.sqrt( (CF2/w) - Math.pow(CF1/w, 2) );
	}

	public double getDistance(MicroCluster mc)
	{
		double distance = 0.0;
		double[] center = this.LS;
		double[] point = mc.LS;
		int maxIdx = StreamClustering.getTableCount();
		for (int i = 0; i < maxIdx; ++i)
		{
			double val1 = center[i] / this.N;
			double val2 = point[i] / mc.N;
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

	public long getCreationTimestamp()
	{
		return creationTimestamp;
	}

	public boolean isCovered()
	{
		return covered;
	}

	public void setCovered(boolean covered)
	{
		this.covered = covered;
	}
}
