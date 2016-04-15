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
import dbseer.gui.DBSeerGUI;
import dbseer.gui.user.DBSeerDataSet;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by dyoon on 9/29/15.
 */
public class IncrementalDBSCAN
{
	private int clusterId;
	private int outlierId;
	private int initPts;
	private int minPts;
	private double epsilon;
	private volatile boolean initialized;
	private volatile boolean initializing;

	private Collection<Cluster> allClusters = new ArrayList<Cluster>();
	private Collection<Cluster> clusters = new ArrayList<Cluster>();
	private Collection<Cluster> outliers = new ArrayList<Cluster>();

	private long maxEndTime;

	public IncrementalDBSCAN(int minPts, double epsilon, int initPts)
	{
		this.clusterId = 0;
		this.outlierId = -1;
		this.initPts = initPts;
		this.minPts = minPts;
		this.epsilon = epsilon;
		this.initialized = false;
		this.initializing = false;
		this.maxEndTime = -1;
	}

	public synchronized void initialDBSCAN(Collection<Transaction> transactions)
	{
		if (initialized)
		{
			System.err.println("Incremental DBSCAN has already been initialized.");
			return;
		}

		this.initializing = true;

//		System.out.println("Initial DBSCAN Started");
		Collection<Transaction> actualTransactions = new ArrayList<Transaction>();
		for (Transaction t : transactions)
		{
			if (!t.isNoRowsReadWritten() && t.getEntireStatement() != null)
			{
				actualTransactions.add(t);
			}
		}

		for (Transaction t : actualTransactions)
		{
			if (t.getClassification() != Transaction.CLASSIFIED)
			{
				t.setClassification(Transaction.CLASSIFIED);
				ArrayList<Transaction> neighbors = getNeighbors(actualTransactions, t);

				if (neighbors.size() >= this.minPts)
				{
					Cluster c = new Cluster(clusterId++);
					expandCluster(c, actualTransactions, neighbors);
					c.insert(t);
					clusters.add(c);
					allClusters.add(c);
				}
				else
				{
					t.setClassification(Transaction.UNCLASSIFIED);
				}
			}
			if (t.getEndTime() > this.maxEndTime)
			{
				this.maxEndTime = t.getEndTime();
			}
		}
		// reset live dataset.
		DBSeerDataSet dataset = DBSeerGUI.liveDataset;
		dataset.clearTransactionTypes();
		int count = 0;

		for (Cluster c : allClusters)
		{
			dataset.addTransactionType("Type " + (++count));
		}

		initialized = true;
		this.initializing = false;
//		System.out.println("Initial DBSCAN Finished");
	}

	public synchronized Collection<Cluster> getCurrentClusters()
	{
		ArrayList<Cluster> currentClusters = new ArrayList<Cluster>();
		for (Cluster c : clusters)
		{
			currentClusters.add(c.copy());
		}
		return currentClusters;
	}

	public synchronized Collection<Cluster> getAllClusters()
	{
		ArrayList<Cluster> clusters = new ArrayList<Cluster>();
		for (Cluster c : allClusters)
		{
			clusters.add(c.copy());
		}
		return clusters;
	}

	public synchronized String[] getTransactionSamples(int index)
	{
		ArrayList<Cluster> clusters = (ArrayList<Cluster>)allClusters;
		if (clusters.size() <= index)
		{
			return null;
		}
		return clusters.get(index).getTransactionSamples();
	}

	public synchronized void train(Transaction t)
	{
		if (t.getEntireStatement() == null)
		{
			return;
		}

		// find the closest cluster
		Cluster closest = findClosestCluster(t, clusters);

		// minimum-distance cluster has been found.
		if (closest != null)
		{
			double dist = closest.getDistance(t);

			// if it fits into an existing cluster, add it.
			if (dist <= this.epsilon && closest.getSize() >= this.minPts)
			{
				closest.insert(t);
			}
			// if not, look at outliers.
			else
			{
				Cluster closestOutlier = findClosestCluster(t, outliers);
				if (closestOutlier != null)
				{
					dist = closestOutlier.getDistance(t);
					// if it fits into an existing outlier cluster, add it.
					if (dist <= this.epsilon)
					{
						closest.insert(t);
					}
					// if not, create a new outlier cluster
					else
					{
						addNewOutlierCluster(t);
					}
				}
				// there is no outlier clusters.. so add new one.
				else
				{
					addNewOutlierCluster(t);
				}

				// check outlier clusters and see if any of them can become a valid cluster.
				ArrayList<Cluster> promotionList = new ArrayList<Cluster>();
				for (Cluster c : outliers)
				{
					if (c.getSize() >= this.minPts)
					{
						promotionList.add(c);
					}
				}
				DBSeerDataSet dataset = DBSeerGUI.liveDataset;
				int count = dataset.getNumTransactionTypes();
				// promote the outlier clusters with more than minPts transactions.
				for (Cluster c : promotionList)
				{
					c.setId(clusterId++);
					clusters.add(c);
					allClusters.add(c);
					outliers.remove(c);

					// add it to live dataset.
					dataset.addTransactionType("Type " + (++count));
				}
			}
		}
		// there is no cluster at all for some reason... just add an outlier cluster for now.
		else
		{
			addNewOutlierCluster(t);
		}

		if (t.getEndTime() > this.maxEndTime)
		{
			this.maxEndTime = t.getEndTime();
		}
	}

	private Cluster findClosestCluster(Transaction t, Collection<Cluster> clc)
	{
		Cluster minCluster = null;
		double minDist = 0.0;

		// find the closest cluster
		for (Cluster c : clc)
		{
			double dist = c.getDistance(t);
			if (minCluster == null)
			{
				minCluster = c;
				minDist = dist;
			}
			else if (dist < minDist)
			{
				minCluster = c;
				minDist = dist;
			}
		}
		return minCluster;
	}

	private void addNewOutlierCluster(Transaction t)
	{
		Cluster c = new Cluster(outlierId--);
		c.insert(t);
		this.outliers.add(c);
	}

	private ArrayList<Transaction> getNeighbors(Collection<Transaction> transactions, Transaction t)
	{
		ArrayList<Transaction> neighbors = new ArrayList<Transaction>();
		for (Transaction neighborT : transactions)
		{
			if (neighborT.getClassification() != Transaction.CLASSIFIED)
			{
				if (t.getEuclideanDistance(neighborT) <= this.epsilon)
				{
					neighbors.add(neighborT);
				}
			}
		}
		return neighbors;
	}

	private void expandCluster(Cluster c, Collection<Transaction> transactions, ArrayList<Transaction> neighbors)
	{
		for (Transaction neighbor : neighbors)
		{
			if (neighbor.getClassification() != Transaction.CLASSIFIED)
			{
				neighbor.setClassification(Transaction.CLASSIFIED);
				c.insert(neighbor);
				ArrayList<Transaction> neighbors2 = getNeighbors(transactions, neighbor);
				if (neighbors2.size() >= minPts)
				{
					expandCluster(c, transactions, neighbors2);
				}
			}
		}
	}

	public int getInitPts()
	{
		return initPts;
	}

	public boolean isInitialized()
	{
		return initialized;
	}

	public boolean isInitializing()
	{
		return initializing;
	}

	public long getMaxEndTime()
	{
		return maxEndTime;
	}
}
