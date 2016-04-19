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

package dbseer.comp.process.transaction;

import com.google.common.primitives.Doubles;
import dbseer.comp.clustering.IncrementalDBSCAN;
import dbseer.comp.process.live.LiveLogProcessor;
import dbseer.comp.process.live.LiveMonitorInfo;
import dbseer.comp.data.Transaction;
import dbseer.gui.DBSeerConstants;
import dbseer.gui.DBSeerGUI;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Dong Young Yoon on 1/2/16.
 */
public class TransactionLogWriter
{
	private static final double[] percentiles = {10.0, 25, 50, 75, 90, 95, 99, 99.9};

	private String dir;
	private boolean isInitialized;
	private IncrementalDBSCAN dbscan;
//	private PrintWriter tpsWriter;
//	private PrintWriter latencyWriter;
//	private HashMap<Integer, PrintWriter> prctileLatencyWriter;
//	private HashMap<Integer, ArrayList<Double>> latencyMap;

	private HashMap<String, TransactionWriter> writers;

	private boolean isDBSCANInitialized;
	private boolean isWritingStarted;
	private ArrayList<Transaction> initialTransactions;
	private ExecutorService dbscanInitializer;

	private LiveMonitorInfo monitor;
	private String[] servers;
	private int numServer;
	private HashMap<String, Integer> serverIndex;
	private LiveLogProcessor liveLogProcessor;
	private int maxType;

	public TransactionLogWriter(String dir)
	{
		this.dir = dir;
		this.isInitialized = false;
		this.isDBSCANInitialized = false;
		this.isWritingStarted = false;
		this.dbscan = new IncrementalDBSCAN(DBSeerConstants.DBSCAN_MIN_PTS, Math.sqrt(Transaction.DIFF_SCALE)/5, DBSeerGUI.settings.dbscanInitPts);
		DBSeerGUI.dbscan = this.dbscan;
		this.initialTransactions = new ArrayList<Transaction>();
	}

	public TransactionLogWriter(String dir, String[] servers, LiveMonitorInfo monitor, LiveLogProcessor liveLogProcessor)
	{
		this.dir = dir;
		this.servers = servers;
		this.isInitialized = false;
		this.isDBSCANInitialized = false;
		this.isWritingStarted = false;
		this.dbscan = new IncrementalDBSCAN(DBSeerConstants.DBSCAN_MIN_PTS, Math.sqrt(Transaction.DIFF_SCALE)/5, DBSeerGUI.settings.dbscanInitPts);
		DBSeerGUI.dbscan = this.dbscan;
		this.initialTransactions = new ArrayList<Transaction>();
		this.monitor = monitor;
		this.writers = new HashMap<String, TransactionWriter>();
		this.serverIndex = new HashMap<String, Integer>();
		this.liveLogProcessor = liveLogProcessor;
	}

	public void initialize() throws Exception
	{
		int index = 0;
		this.maxType = 0;
		for (String server : servers)
		{
			File logDir = new File(this.dir + File.separator + server);
			if (!logDir.exists())
			{
				logDir.mkdirs();
			}

			File tpsFile = new File(this.dir + File.separator + server + File.separator + "trans_count");
			File latencyFile = new File(this.dir + File.separator + server + File.separator + "avg_latency");

			PrintWriter tpsWriter = new PrintWriter(new FileWriter(tpsFile, false));
			PrintWriter latencyWriter = new PrintWriter(new FileWriter(latencyFile, false));
			HashMap<Integer, PrintWriter> prctileLatencyWriter = new HashMap<Integer, PrintWriter>();
			HashMap<Integer, ArrayList<Double>> latencyMap = new HashMap<Integer, ArrayList<Double>>();
			HashMap<Integer, PrintWriter> transactionSampleWriter = new HashMap<Integer, PrintWriter>();

			TransactionWriter writer = new TransactionWriter(tpsWriter, latencyWriter, prctileLatencyWriter, transactionSampleWriter, latencyMap);
			writers.put(server, writer);
			serverIndex.put(server, index++);
		}
		this.numServer = servers.length;

		this.isInitialized = true;
		this.isWritingStarted = false;
	}

	// write tps, latency, percentile latency logs for DBSeer use.
	public void writeLog(long timestamp, Collection<Transaction> transactions) throws Exception
	{
		if (!this.isInitialized)
		{
			throw new Exception("TransactionLogWriter not initialized.");
		}

		double totalCount = 0;
		double[][] count = new double[numServer][DBSeerConstants.MAX_NUM_TABLE];
		double[][] latencySum = new double[numServer][DBSeerConstants.MAX_NUM_TABLE];
		String gap = "   ";

		if (!dbscan.isInitialized())
		{
			initialTransactions.addAll(transactions);

			if (initialTransactions.size() > dbscan.getInitPts() && !dbscan.isInitializing())
			{
				dbscanInitializer = Executors.newSingleThreadExecutor();
				dbscanInitializer.submit(new Runnable()
				{
					@Override
					public void run()
					{
						dbscan.initialDBSCAN(initialTransactions);
					}
				});
			}
		}

		for (Transaction t : transactions)
		{
			if (dbscan != null && dbscan.isInitialized())
			{
				if (liveLogProcessor.getTxStartTime() == 0)
				{
					liveLogProcessor.setTxStartTime(timestamp);
				}
				dbscan.train(t);
			}

			int type;
			if (t.getCluster() == null)
			{
				type = 0;
			}
			else
			{
				type = t.getCluster().getId();
			}

			if (type > maxType)
			{
				maxType = type;
			}

			// if not outlier;
			if (type >= 0)
			{
				String server = t.getServerName();
				int index = serverIndex.get(server);
				latencySum[index][type] += t.getLatency();
				count[index][type]++;
				totalCount++;

				ArrayList<Double> latencyList = writers.get(server).getLatencyMap().get(type);
				if (latencyList == null)
				{
					latencyList = new ArrayList<Double>();
					writers.get(server).getLatencyMap().put(type, latencyList);
				}
				latencyList.add((double)t.getLatency());

				// write sample
				HashMap<Integer, Integer> countMap = writers.get(server).getTransactionSampleCountMap();
				Integer sampleCount = countMap.get(type);
				if (sampleCount == null)
				{
					countMap.put(type, 1);
				}
				else
				{
					int countVal = sampleCount.intValue();
					if (countVal < DBSeerConstants.MAX_TRANSACTION_SAMPLE)
					{
						HashMap<Integer, PrintWriter> sampleWriters = writers.get(server).getTransactionSampleWriter();
						PrintWriter sampleWriter = sampleWriters.get(type);
						if (sampleWriter == null)
						{
							sampleWriter = new PrintWriter(new FileOutputStream(
									String.format("%s%d", this.dir + File.separator + server + File.separator + "tx_sample_", type), false));
							sampleWriters.put(type, sampleWriter);
						}
						sampleWriter.print(t.getEntireStatement());
						sampleWriter.println("---");
						sampleWriter.flush();
						countVal++;
						countMap.put(type, countVal);
					}
				}
			}
		}

		// update live monitor
		if (monitor != null)
		{
			monitor.setCurrentTimestamp(timestamp);
			monitor.setNumTransactionTypes(maxType + 1);
			monitor.setGlobalTransactionCount(totalCount);

			for (int i = 0; i <= maxType; ++i)
			{
				double countSum = 0;
				double latencySumSum = 0;
				for (int j = 0; j < numServer; ++j)
				{
					countSum += count[j][i];
					latencySumSum += latencySum[j][i];
				}
				monitor.setCurrentTPS(i, countSum);
				if (countSum == 0)
				{
					monitor.setCurrentAverageLatency(i, 0.0);
				}
				else
				{
					monitor.setCurrentAverageLatency(i, latencySumSum / countSum);
				}
			}
		}

		if (timestamp < liveLogProcessor.getSysStartTime() || liveLogProcessor.getSysStartTime() == 0)
		{
			return;
		}

		for (String server : servers)
		{
			TransactionWriter writer = writers.get(server);
			PrintWriter tpsWriter = writer.getTpsWriter();
			PrintWriter latencyWriter = writer.getLatencyWriter();

			HashMap<Integer, PrintWriter> prctileLatencyWriter = writer.getPrctileLatencyWriter();
			HashMap<Integer, ArrayList<Double>> latencyMap = writer.getLatencyMap();

			tpsWriter.print(gap);
			latencyWriter.print(gap);

			tpsWriter.printf("%.16e", (double) timestamp);
			latencyWriter.printf("%.16e", (double) timestamp);

			int index = serverIndex.get(server);

			for (int i = 0; i <= maxType; ++i)
			{
				tpsWriter.print(gap);
				tpsWriter.printf("%.16e", count[index][i]);

				latencyWriter.print(gap);
				if (count[index][i] == 0.0)
				{
					latencyWriter.printf("%.16e", 0.0);
				}
				else
				{
					latencyWriter.printf("%.16e", (latencySum[index][i] / count[index][i]) / 1000.0);
				}

				// write percentile
				PrintWriter prctileWriter = prctileLatencyWriter.get(i);
				ArrayList<Double> latencyList = latencyMap.get(i);
				if (latencyList == null)
				{
					latencyList = new ArrayList<Double>();
					latencyMap.put(i, latencyList);
				}
				if (prctileWriter == null)
				{
					prctileWriter = new PrintWriter(new FileOutputStream(
							String.format("%s%03d", this.dir + File.separator + server + File.separator + "prctile_latency_", i), false));
					prctileLatencyWriter.put(i, prctileWriter);
				}
				double[] latencies = Doubles.toArray(latencyList);
				prctileWriter.printf("%d,", timestamp);
				for (double p : percentiles)
				{
					Percentile percentile = new Percentile(p);
					percentile.setData(latencies);
					double val = percentile.evaluate();
					if (Double.isNaN(val)) val = 0.0;
					prctileWriter.printf("%f,", val / 1000.0);
				}
				prctileWriter.println();
				prctileWriter.flush();
				latencyList.clear();
			}

			tpsWriter.println();
			latencyWriter.println();
			tpsWriter.flush();
			latencyWriter.flush();
			isWritingStarted = true;
		}
	}

	public boolean isWritingStarted()
	{
		return isWritingStarted;
	}
}
