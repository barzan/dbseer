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

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by dyoon on 9/26/15.
 * denStream implementation for online clustering of transactions
 */
public class DenStream
{
	private boolean isInitialized;
	private double lambda;
	private double epsilon;
	private double epsilon2;
	private int minPts;
	private int minPts2;
	private double mu;
	private double beta;
	private long timestamp;
	private long tp;

	ArrayList<MicroCluster> pMicroClusters; // potential MCs
	ArrayList<MicroCluster> oMicroClusters; // outlier MCs

	public DenStream(double lambda, double epsilon, int minPts, double mu, double beta, double epsilon2, int minPts2)
	{
		this.lambda = lambda;
		this.epsilon = epsilon;
		this.epsilon2 = epsilon2;
		this.minPts = minPts;
		this.minPts2 = minPts2;
		this.mu = mu;
		this.beta = beta;
		this.isInitialized = false;
		this.timestamp = 0;
		this.tp = (long)Math.ceil(1 / lambda * Math.log((beta * mu) / (beta * mu - 1)));

		pMicroClusters = new ArrayList<MicroCluster>();
		oMicroClusters = new ArrayList<MicroCluster>();
	}

	public void reset(double lambda, double epsilon, int minPts, double mu, double beta, double epsilon2, int minPts2)
	{
		this.lambda = lambda;
		this.epsilon = epsilon;
		this.epsilon2 = epsilon2;
		this.minPts = minPts;
		this.minPts2 = minPts2;
		this.mu = mu;
		this.beta = beta;
		this.isInitialized = true;
		this.timestamp = 0;
		this.tp = (long)Math.ceil(1 / lambda * Math.log((beta * mu) / (beta * mu - 1)));

		pMicroClusters = new ArrayList<MicroCluster>();
		oMicroClusters = new ArrayList<MicroCluster>();
	}

	public synchronized void initialDBSCAN(Collection<Transaction> transactions)
	{
		if (isInitialized)
		{
			System.err.println("DenStream has already been initialized.");
			return;
		}

		Collection<Transaction> actualTransactions = new ArrayList<Transaction>();
		for (Transaction t : transactions)
		{
			if (!t.isNoRowsReadWritten())
			{
				actualTransactions.add(t);
			}
		}

		System.out.println("Initial DBSCAN started");
		for (Transaction t : actualTransactions)
		{
			if (t.getClassification() != Transaction.CLASSIFIED)
			{
				t.setClassification(Transaction.CLASSIFIED);
				ArrayList<Transaction> neighbors = getNeighbors(actualTransactions, t, this.epsilon);

				if (neighbors.size() >= this.minPts)
				{
					MicroCluster mc = new MicroCluster(t, timestamp, lambda);
					expandCluster(mc, actualTransactions, neighbors);
					pMicroClusters.add(mc);
				}
				else
				{
					t.setClassification(Transaction.UNCLASSIFIED);
				}
			}
		}
		isInitialized = true;
		System.out.println("Initial DBSCAN finished");
	}

	public synchronized void train(Transaction t)
	{
		if (!isInitialized)
		{
			System.err.println("DenStream needs to be initialized first before training on new transactions");
			return;
		}

		timestamp++;
		boolean isMerged = false;
		System.out.println("Train a transaction");

		// try to merge transaction with p-micro-cluster
		if (pMicroClusters.size() > 0)
		{
			MicroCluster mc = nearestCluster(t, pMicroClusters);
			MicroCluster mcCopy = mc.copy();
			mcCopy.insert(t, timestamp);
			if (mcCopy.getRadius(timestamp) <= epsilon)
			{
				mc.insert(t, timestamp);
				isMerged = true;
			}
		}

		// try to merge transaction with o-micro-cluster
		if (!isMerged && oMicroClusters.size() > 0)
		{
			MicroCluster mc = nearestCluster(t, pMicroClusters);
			MicroCluster mcCopy = mc.copy();
			mcCopy.insert(t, timestamp);
			if (mcCopy.getRadius(timestamp) <= epsilon)
			{
				mc.insert(t, timestamp);
				isMerged = true;
				if (mc.getWeight(timestamp) > beta * mu)
				{
					oMicroClusters.remove(mc);
					pMicroClusters.add(mc);
				}
			}
		}

		// create a new o-micro-cluster if transaction is not merged with any existing p/o-micro-cluster.
		if (!isMerged)
		{
			oMicroClusters.add(new MicroCluster(t, timestamp, lambda));
		}

		// periodically remove faded clusters
		if (timestamp % tp == 0)
		{
			ArrayList<MicroCluster> removalCandidates = new ArrayList<MicroCluster>();
			for (MicroCluster mc : pMicroClusters)
			{
				if (mc.getWeight(timestamp) < beta * mu)
				{
					removalCandidates.add(mc);
				}
			}
			pMicroClusters.removeAll(removalCandidates);

			for (MicroCluster mc : oMicroClusters)
			{
				long t0 = mc.getCreationTimestamp();
				double ksi1 = Math.pow(2, (-lambda * (timestamp - t0 + tp))) - 1;
				double ksi2 = Math.pow(2, -lambda * tp) - 1;
				double ksi = ksi1 / ksi2;
				if (mc.getWeight(timestamp) < ksi)
				{
					removalCandidates.add(mc);
				}
			}
			oMicroClusters.removeAll(removalCandidates);
		}
	}

	private ArrayList<Transaction> getNeighbors(Collection<Transaction> transactions, Transaction t, double epsilon)
	{
		ArrayList<Transaction> neighbors = new ArrayList<Transaction>();
		for (Transaction neighborT : transactions)
		{
			if (neighborT.getClassification() != Transaction.CLASSIFIED)
			{
				if (t.getEuclideanDistance(neighborT) < epsilon)
				{
					neighbors.add(neighborT);
				}
			}
		}
		return neighbors;
	}

	private void expandCluster(MicroCluster mc, Collection<Transaction> transactions, ArrayList<Transaction> neighbors)
	{
		for (Transaction neighbor : neighbors)
		{
			if (neighbor.getClassification() != Transaction.CLASSIFIED)
			{
				neighbor.setClassification(Transaction.CLASSIFIED);
				mc.insert(neighbor, timestamp);
				ArrayList<Transaction> neighbors2 = getNeighbors(transactions, neighbor, epsilon);
				if (neighbors2.size() >= minPts)
				{
					expandCluster(mc, transactions, neighbors2);
				}
			}
		}
	}

	private MicroCluster nearestCluster(Transaction t, ArrayList<MicroCluster> mcs)
	{
		MicroCluster min = null;
		double minDistance = 0;
		for (MicroCluster mc : mcs)
		{
			if (min == null)
			{
				min = mc;
			}
			double distance = mc.getDistance(t);
			distance -= mc.getRadius(timestamp);
			if (distance < minDistance)
			{
				minDistance = distance;
				min = mc;
			}
		}
		return min;
	}

	// perform DBSCAN on micro-clusters and return the final cluster.
	public synchronized ArrayList<DenStreamCluster> getClusters()
	{
		if (!isInitialized)
		{
			System.err.println("DenStream needs to be initialized first.");
			return null;
		}

		ArrayList<DenStreamCluster> clusters = new ArrayList<DenStreamCluster>();

		// initialize mcs
		for (MicroCluster mc : pMicroClusters)
		{
			mc.setCovered(false);
		}

//		for (int i = 0; i < pMicroClusters.size() - 1; ++i)
//		{
//			for (int j = i + 1; j < pMicroClusters.size(); ++j)
//			{
//				MicroCluster mc1 = pMicroClusters.get(i);
//				MicroCluster mc2 = pMicroClusters.get(j);
//				double dist = mc1.getDistance(mc2);
//				System.out.print(dist + " ");
//			}
//			System.out.println();
//		}

		for (MicroCluster mc : pMicroClusters)
		{
			if (!mc.isCovered())
			{
				mc.setCovered(true);
				ArrayList<MicroCluster> neighbors = getNeighbors(pMicroClusters, mc, epsilon2);
				System.out.println("# Neighbor = " + neighbors.size());
				if (neighbors.size() >= minPts2)
				{
					DenStreamCluster cluster = new DenStreamCluster();
					expandCluster(cluster, pMicroClusters, neighbors);
					clusters.add(cluster);
				}
				else
				{
					mc.setCovered(false);
				}
			}
		}

		return clusters;
	}

	private ArrayList<MicroCluster> getNeighbors(ArrayList<MicroCluster> mcs, MicroCluster mc, double epsilon)
	{
		ArrayList<MicroCluster> neighbors = new ArrayList<MicroCluster>();
		for (MicroCluster neighborMC : mcs)
		{
			if (!neighborMC.isCovered())
			{
				double dist = mc.getDistance(neighborMC);
				System.out.println(dist + " : " + epsilon2);
				if (dist < epsilon2)
				{
					neighbors.add(neighborMC);
				}
			}
		}
		return neighbors;
	}

	private void expandCluster(DenStreamCluster cluster, ArrayList<MicroCluster> mcs, ArrayList<MicroCluster> neighbors)
	{
		for (MicroCluster neighbor : neighbors)
		{
			if (!neighbor.isCovered())
			{
				neighbor.setCovered(true);
				cluster.addCluster(neighbor);
				ArrayList<MicroCluster> neighbors2 = getNeighbors(mcs, neighbor, epsilon2);
				if (neighbors2.size() >= minPts2)
				{
					expandCluster(cluster, mcs, neighbors2);
				}
			}
		}
	}

	public boolean isInitialized()
	{
		return isInitialized;
	}
}
