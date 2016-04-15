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

package dbseer.comp.live;

import com.google.common.primitives.Doubles;
import dbseer.comp.clustering.Cluster;
import dbseer.comp.clustering.StreamClustering;
import dbseer.comp.process.live.LiveMonitorInfo;
import dbseer.comp.data.Transaction;
import dbseer.comp.data.TransactionMap;
import dbseer.gui.DBSeerConstants;
import dbseer.gui.DBSeerGUI;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * Created by dyoon on 10/1/15.
 */
public class LiveTransactionProcessor implements Runnable
{
	private TransactionMap map;

	private File transactionCountFile;
	private File avgLatencyFile;

	private PrintWriter transactionCountWriter;
	private PrintWriter avgLatencyWriter;
	private PrintWriter monitorWriter;

	private HashMap<Integer, PrintWriter> percentileLatencyWriter;
	private HashMap<Integer, ArrayList<Double>> latencyMap;
	private static final double[] percentiles = {10.0, 25, 50, 75, 90, 95, 99, 99.9};
	private LiveMonitorInfo monitor;

	private boolean terminate;

	private final int SYS_LOG_RETRY = 15;
	private final int SYS_LOG_WAIT_TIME = 200;

	public LiveTransactionProcessor(TransactionMap map, PrintWriter monitorWriter)
	{
		this.map = map;
		this.transactionCountFile = new File(DBSeerGUI.userSettings.getDBSeerRootPath() + File.separator + DBSeerConstants.LIVE_DATASET_PATH + File.separator + "trans_count");
		this.avgLatencyFile = new File(DBSeerGUI.userSettings.getDBSeerRootPath() + File.separator + DBSeerConstants.LIVE_DATASET_PATH + File.separator + "avg_latency");
		this.monitorWriter = monitorWriter;
		this.percentileLatencyWriter = new HashMap<Integer, PrintWriter>();
		this.latencyMap = new HashMap<Integer, ArrayList<Double>>();
		this.monitor = DBSeerGUI.liveMonitorInfo;
		this.terminate = false;
	}

	public void setTerminate(boolean terminate)
	{
		this.terminate = terminate;
	}

	@Override
	public void run()
	{
		try
		{
			this.transactionCountWriter = new PrintWriter(new FileWriter(this.transactionCountFile, true));
			this.avgLatencyWriter = new PrintWriter(new FileWriter(this.avgLatencyFile, true));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		long time;
		// wait for transactions to come in
		while (true)
		{
			time = map.getMinEndTime();
			if (time != Long.MAX_VALUE)
			{
				break;
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
			if (terminate)
			{
				break;
			}
		}

		String gap = "   ";
		double totalCount = 0;
		double currentCount = 0;
		double[] count = new double[DBSeerConstants.MAX_NUM_TABLE];
		double[] latencySum = new double[DBSeerConstants.MAX_NUM_TABLE];
		int maxClusterId = 0;
		long transCount = 0;

		// start processing transactions
		while (true)
		{
			long maxTime, maxClusterEndTime;
			maxTime = map.getMaxEndTime();
			if (!StreamClustering.getDBSCAN().isInitialized() && transCount < DBSeerConstants.DBSCAN_INIT_PTS)
			{
				transCount = map.getCount();
				monitor.setGlobalTransactionCount(transCount);
				try
				{
					Thread.sleep(250);
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
//			synchronized (StreamClustering.LOCK)
			try
			{
				StreamClustering.LOCK.lockInterruptibly();
				{
					maxClusterEndTime = StreamClustering.getDBSCAN().getMaxEndTime();
				}
				StreamClustering.LOCK.unlock();
				while (time < maxTime && time < maxClusterEndTime)
				{
					currentCount = 0;
					Set<Transaction> transactions = map.pollTransactions(time);

					// if no transactions for the time, skip to the next timestamp.
					if (transactions.isEmpty())
					{
						++time;
						continue;
					}

					// if sys log not available for the time, also skip to the next timestamp;
					if (map.getMinSysLogTime() != Long.MAX_VALUE && map.getMinSysLogTime() > time)
					{
						++time;
						continue;
					}


					boolean monitorLogFound = true;
					String monitorLog;
					while ((monitorLog = map.getSysLog(time)) == null)
					{
						if (time < map.getLastSysLogTime())
						{
							monitorLogFound = false;
							break;
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

					if (!monitorLogFound)
					{
						++time;
						continue;
					}

					monitorWriter.println(monitorLog);
					monitorWriter.flush();

					for (Transaction t : transactions)
					{
						Cluster c = t.getCluster();
						// if cluster is null, skip
						if (c == null)
						{
							continue;
						}

						int cId = c.getId();
						long latency = t.getLatency();

						// ignore outliers
						if (cId >= 0)
						{
							latencySum[cId] += latency;
							++count[cId];
							++totalCount;
							++currentCount;

							ArrayList<Double> latencyList = latencyMap.get(cId);
							if (latencyList == null)
							{
								latencyList = new ArrayList<Double>();
								latencyMap.put(cId, latencyList);
							}
							latencyList.add((double)latency/1000.0);
						}
						if (cId > maxClusterId)
						{
							maxClusterId = cId;
						}
					}

					// update live monitor
//					int numTrans = maxClusterId + 1;
					int numTrans = StreamClustering.getDBSCAN().getAllClusters().size();
					synchronized (LiveMonitorInfo.LOCK)
					{
						monitor.setCurrentTimestamp(time);
						monitor.setNumTransactionTypes(numTrans);
						monitor.setGlobalTransactionCount(totalCount);
						for (int i = 0; i < numTrans; ++i)
						{
							monitor.setCurrentTPS(i, count[i]);
							if (count[i] == 0)
							{
								monitor.setCurrentAverageLatency(i, 0.0);
							}
							else
							{
								monitor.setCurrentAverageLatency(i, latencySum[i] / count[i]);
							}
						}
					}

					transactionCountWriter.print(gap);
					avgLatencyWriter.print(gap);

					transactionCountWriter.printf("%.16e", (double)time);
					avgLatencyWriter.printf("%.16e", (double)time);

					for (int i = 0; i < numTrans; ++i)
					{
						transactionCountWriter.print(gap);
						transactionCountWriter.printf("%.16e", count[i]);
						avgLatencyWriter.print(gap);
						if (count[i] == 0.0)
						{
							avgLatencyWriter.printf("%.16e", 0.0);
						}
						else
						{
							avgLatencyWriter.printf("%.16e", (latencySum[i] / (double)count[i] / 1000.0));
						}
						count[i] = 0;
						latencySum[i] = 0;

						// write percentile
						PrintWriter writer = percentileLatencyWriter.get(i);
						ArrayList<Double> latencyList = latencyMap.get(i);
						if (latencyList == null)
						{
							latencyList = new ArrayList<Double>();
							latencyMap.put(i, latencyList);
						}
						if (writer == null)
						{
							try
							{
								writer = new PrintWriter(new FileOutputStream(
										String.format("%s%03d", DBSeerGUI.userSettings.getDBSeerRootPath() + File.separator + DBSeerConstants.LIVE_DATASET_PATH + File.separator + "prctile_latency_", i), true));
							}
							catch (FileNotFoundException e)
							{
								e.printStackTrace();
							}
							percentileLatencyWriter.put(i, writer);
						}

						double[] latencies = Doubles.toArray(latencyList);
						writer.printf("%d,", time);
						for (double p : percentiles)
						{
							Percentile percentile = new Percentile(p);
							percentile.setData(latencies);
							double val = percentile.evaluate();
							if (Double.isNaN(val)) val = 0.0;
							writer.printf("%f,", val);
						}
						writer.println();
						writer.flush();
					}

					transactionCountWriter.println();
					avgLatencyWriter.println();
					transactionCountWriter.flush();
					avgLatencyWriter.flush();


//				System.out.print((maxClusterId + 1) + ": ");
//				for (int i = 0; i <= maxClusterId; ++i)
//				{
//					System.out.print(count[i] + ", ");
//					count[i] = 0;
//				}
//				System.out.println();
//				ArrayList<Cluster> clusters = (ArrayList<Cluster>)StreamClustering.getDBSCAN().getCurrentClusters();
//				for (int i = 0; i < clusters.size(); ++i)
//				{
//					Cluster c1 = clusters.get(i);
//					for (int j = 0; j < clusters.size(); ++j)
//					{
//						Cluster c2 = clusters.get(j);
//						System.out.print(c1.getClusterDistance(c2) + " ");
//					}
//					System.out.println();
//				}
//				System.out.println("----");
					// is it correct to set it here?
					DBSeerGUI.isLiveDataReady = true;

					++time;
				}

				if (terminate)
				{
					break;
				}

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
