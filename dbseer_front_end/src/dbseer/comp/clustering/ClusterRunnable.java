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
 * Created by dyoon on 9/29/15.
 */
public class ClusterRunnable implements Runnable
{
	private boolean terminate;

	public ClusterRunnable()
	{
		this.terminate = false;
	}

	@Override
	public void run()
	{
		IncrementalDBSCAN dbscan = StreamClustering.getDBSCAN();

		while (true)
		{
			if (terminate)
			{
				return;
			}
			if (!dbscan.isInitialized())
			{
				if (StreamClustering.trxMap.values().size() > dbscan.getInitPts() * 2)
				{
					ArrayList<Transaction> clusteringCandidates = new ArrayList<Transaction>();
					ArrayList<Transaction> readyToCluster = new ArrayList<Transaction>();

					try
					{
						StreamClustering.LOCK.lockInterruptibly();
						try
						{
							clusteringCandidates.addAll(StreamClustering.trxMap.values());
						}
						finally
						{
							StreamClustering.LOCK.unlock();
						}
					}
					catch (InterruptedException e)
					{
						if (!terminate)
						{
							e.printStackTrace();
						}
						else
						{
							return;
						}
					}
					for (Transaction t : clusteringCandidates)
					{
						if (t.getLastStatementId() <= StreamClustering.getMaxStatementId() &&
								t.getEntireStatement() != null)
						{
							readyToCluster.add(t);
						}
					}
					if (readyToCluster.size() > dbscan.getInitPts())
					{
						for (Transaction t : readyToCluster)
						{
							StreamClustering.trxMap.remove(t);
						}
						dbscan.initialDBSCAN(readyToCluster);
					}
				}
				else
				{
					try
					{
						Thread.sleep(250);
					}
					catch (InterruptedException e)
					{
						if (!terminate)
						{
							e.printStackTrace();
						}
						else
						{
							return;
						}
					}
				}
			}
			else
			{
				Collection<Transaction> transactions = StreamClustering.trxMap.values();
				for (Transaction t : transactions)
				{
//					synchronized (StreamClustering.LOCK)
					try
					{
						StreamClustering.LOCK.lockInterruptibly();
						try
						{
							if (t.getLastStatementId() <= StreamClustering.getMaxStatementId())
							{
								if (!t.isNoRowsReadWritten())
								{
									dbscan.train(t);
								}
								StreamClustering.trxMap.values().remove(t);
							}
						}
						finally
						{
							StreamClustering.LOCK.unlock();
						}
					}
					catch (InterruptedException e)
					{
						if (!terminate)
						{
							e.printStackTrace();
						}
						else
						{
							return;
						}
					}
				}
				try
				{
					Thread.sleep(100);
				}
				catch (InterruptedException e)
				{
					if (!terminate)
					{
						e.printStackTrace();
					}
					else
					{
						return;
					}
				}
			}
		}
	}

	public void setTerminate(boolean terminate)
	{
		this.terminate = terminate;
	}
}
