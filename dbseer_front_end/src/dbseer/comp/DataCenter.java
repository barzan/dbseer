package dbseer.comp;

import dbseer.comp.data.*;
import dbseer.gui.DBSeerConstants;
import dbseer.gui.DBSeerGUI;
import dbseer.gui.user.DBSeerTransactionSample;
import dbseer.gui.user.DBSeerTransactionSampleList;
import dbseer.gui.xml.XStreamHelper;
import matlabcontrol.MatlabInvocationException;
import matlabcontrol.MatlabProxy;

import javax.swing.*;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by dyoon on 2014. 7. 4..
 *
 * processes raw dataset from the middleware.
 *
 */
public class DataCenter
{
	private boolean doDBSCAN;
	private String rawPath;
	private String processedPath;
	private String datasetName;

	private SystemMonitor monitor;

	private Map<Integer, Transaction> transactionMap;
	private Map<Integer, Statement> statementMap;
	private Set<String> globalTableSet;
	private String[] globalTableList;
	private Map<String, Integer> globalTableMap;
	private ArrayList<Cluster> clusters;
	private ArrayList<Transaction> actualTransactions;

	public DataCenter(String path, String name, boolean doDBSCAN)
	{
		this.doDBSCAN = doDBSCAN;
		this.rawPath = path + File.separator + name;
		this.processedPath = this.rawPath;
//		if (doDBSCAN)
//		{
//			this.processedPath = this.rawPath + File.separator + "processed";
//		}
//		else
//		{
//			this.processedPath = this.rawPath + File.separator + "processed_no_dbscan";
//		}
		this.datasetName = name;
		monitor = new SystemMonitor();
		transactionMap = new HashMap<Integer, Transaction>();
		statementMap = new HashMap<Integer, Statement>();
		globalTableSet = new HashSet<String>();
		globalTableMap = new HashMap<String, Integer>();
		clusters = new ArrayList<Cluster>();
		actualTransactions = new ArrayList<Transaction>();
	}

	public DataCenter(String fullPath, boolean doDBSCAN)
	{
		this.doDBSCAN = doDBSCAN;
		this.rawPath = fullPath;
//		if (doDBSCAN)
//		{
//			this.processedPath = this.rawPath + File.separator + "processed";
//		}
//		else
//		{
//			this.processedPath = this.rawPath + File.separator + "processed_no_dbscan";
//		}
		this.processedPath = this.rawPath;

		File rawPathFile = new File(fullPath);
		if (rawPathFile.getName() != null)
			this.datasetName = rawPathFile.getName();
		else
			this.datasetName ="";

		monitor = new SystemMonitor();
		transactionMap = new HashMap<Integer, Transaction>();
		statementMap = new HashMap<Integer, Statement>();
		globalTableSet = new HashSet<String>();
		globalTableMap = new HashMap<String, Integer>();
		clusters = new ArrayList<Cluster>();
		actualTransactions = new ArrayList<Transaction>();
	}

	public void printAll()
	{
		Iterator it = transactionMap.entrySet().iterator();

		while (it.hasNext())
		{
			Map.Entry<Integer, Transaction> entry = (Map.Entry<Integer, Transaction>) it.next();
			List<Statement> statements = entry.getValue().getStatements();

			entry.getValue().printAll();
			for (Statement stmt : statements)
			{
				System.out.print("\t");
				stmt.printAll();
			}
		}
	}

	public boolean parseLogs()
	{
		try
		{
			if (!parseMonitorLogs()) return false;
			if (!parseTransactionLogs()) return false;
			if (!parseStatementLogs()) return false;
			if (!parseQueryLogs()) return false;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		DBSeerGUI.status.setText("Processing Dataset: Log parsing completed.");
		return true;
	}

	public boolean processDataset()
	{
		prepareTransactionClustering();
		if (doDBSCAN)
		{
			performDBSCAN();
		}
		else
		{
			assignSingleCluster();
		}
		writeHeader();
		writeTransactionInfo();
//		writePageInfo();
		return true;
	}

	private void assignSingleCluster()
	{
		Cluster cluster = new Cluster();
		cluster.setId(0);

		clusters.clear();
		actualTransactions.clear();

		Transaction[] transactions = transactionMap.values().toArray(new Transaction[transactionMap.values().size()]);

		for (Transaction transaction : transactions)
		{
			if (!transaction.isNoRowsReadWritten())
			{
				actualTransactions.add(transaction);
			}
		}

		for (Transaction transaction : actualTransactions)
		{
			transaction.setCluster(cluster);
		}

		clusters.add(cluster);
	}

	private boolean writeTransactionInfo()
	{
		DBSeerGUI.status.setText("Processing Dataset: Writing transaction information.");
		List<MonitorLog> monitorLogs = monitor.getLogs();
		long startTime = monitor.getStartTimestamp();
		long endTime = monitor.getEndTimestamp();

		long[][] counts = new long[monitorLogs.size()+60][clusters.size()];
		double[][] latencies = new double[monitorLogs.size()+60][clusters.size()];
		long[] totalCounts = new long[monitorLogs.size()+60];
		double[] totalLatency = new double[monitorLogs.size()+60];
		long[][] statementCounts = new long[monitorLogs.size()+60][4*globalTableList.length]; // table * {select, update, insert, delete}
		ArrayList<Double>[][] latenciesAtTime = (ArrayList<Double>[][])new ArrayList[monitorLogs.size()+60][clusters.size()];

		File countFile = new File(processedPath + File.separator + "trans_count");
		File avgLatencyFile = new File(processedPath + File.separator + "avg_latency");
		File statementFile = new File(processedPath + File.separator + "stmt_count");

		PrintWriter countWriter = null;
		PrintWriter avgLatencyWriter = null;
		PrintWriter statementWriter = null;

		try
		{
			if (!countFile.getParentFile().exists())
			{
				countFile.getParentFile().mkdirs();
			}
			if (!countFile.exists())
			{
				countFile.createNewFile();
			}

			if (!statementFile.getParentFile().exists())
			{
				statementFile.getParentFile().mkdirs();
			}
			if (!statementFile.exists())
			{
				statementFile.createNewFile();
			}

			if (!avgLatencyFile.getParentFile().exists())
			{
				avgLatencyFile.getParentFile().mkdirs();
			}
			if (!avgLatencyFile.exists())
			{
				avgLatencyFile.createNewFile();
			}

			countWriter = new PrintWriter(new BufferedWriter(new FileWriter(countFile)));
			avgLatencyWriter = new PrintWriter(new BufferedWriter(new FileWriter(avgLatencyFile)));
			statementWriter = new PrintWriter(new BufferedWriter(new FileWriter(statementFile)));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		XStreamHelper xmlHelper = new XStreamHelper();
		for (int i = 0; i < clusters.size(); ++i)
		{
			String file = processedPath + File.separator + "transaction_" + (clusters.get(i).getId()+1) + ".stmt";
			String sampleFile = processedPath + File.separator + "transaction_" + (clusters.get(i).getId()+1) + ".xml";
			File txStatements = new File(file);
			PrintWriter txStatementsWriter = null;

			try
			{
				txStatementsWriter = new PrintWriter(new BufferedWriter(new FileWriter(txStatements)));
			}
			catch (IOException e)
			{
				JOptionPane.showMessageDialog(null, e.getMessage(), "Error while processing sample transactions.", JOptionPane.ERROR_MESSAGE);
				return false;
			}

			List<Transaction> transactionList = clusters.get(i).getTransactions();

			DBSeerTransactionSampleList sampleList = new DBSeerTransactionSampleList();
			ArrayList<DBSeerTransactionSample> samples = sampleList.getSamples();

			for (long t = startTime; t <= endTime; ++t)
			{
				txStatementsWriter.printf("%d,", t - startTime);
				List<Transaction> executingTransactions = new ArrayList<Transaction>();
				for (Transaction tx : transactionList)
				{
					if (tx.getEndTime() == t)
					{
						tx.updateQueryMinMaxOffset();
						txStatementsWriter.printf("%d,%d,%d,%d,%d,", tx.getId(), tx.getMinStatementOffset(), tx.getMaxStatementOffset(),
							tx.getMinQueryOffset(), tx.getMaxQueryOffset());
						executingTransactions.add(tx);
					}
				}
				txStatementsWriter.println();
				if (executingTransactions.size() > 0)
				{
					Transaction sampleTx = executingTransactions.get(0);
					DBSeerTransactionSample sample = new DBSeerTransactionSample((int) (t - startTime), sampleTx.getEntireStatement());
					samples.add(sample);
				}
			}
			txStatementsWriter.flush();
			txStatementsWriter.close();

			try
			{
				xmlHelper.toXML(sampleList, sampleFile);
			}
			catch (FileNotFoundException e)
			{
				JOptionPane.showMessageDialog(null, e.getMessage(), "Error while processing sample transactions.", JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}

		// write list of tables to statement count file.
		for (String table : globalTableList)
		{
			statementWriter.print(table + ",");
		}
		statementWriter.println();

		for (Transaction t : actualTransactions)
		{
			long theTime = t.getEndTime();

			// summarize latency
			int clusterId = t.getCluster().getId();

			totalCounts[(int)(theTime - startTime)]++;
			totalLatency[(int)(theTime - startTime)] += t.getLatency();

			counts[(int)(theTime - startTime)][clusterId]++;
			latencies[(int)(theTime - startTime)][clusterId] += t.getLatency();
			if (latenciesAtTime[(int)(theTime - startTime)][clusterId] == null)
			{
				latenciesAtTime[(int)(theTime - startTime)][clusterId] = new ArrayList<Double>();
			}
			latenciesAtTime[(int)(theTime - startTime)][clusterId].add((double) t.getLatency());

			long[] selects = t.getNumSelect();
			long[] updates = t.getNumUpdate();
			long[] inserts = t.getNumInsert();
			long[] deletes = t.getNumDelete();

			// count statements
			for (long i = t.getStartTime(); i <= t.getEndTime(); ++i)
			{
				for (int j = 0; j < selects.length; ++j)
				{
					statementCounts[(int)(i - startTime)][j*4] += selects[j];
					statementCounts[(int)(i - startTime)][j*4+1] += updates[j];
					statementCounts[(int)(i - startTime)][j*4+2] += inserts[j];
					statementCounts[(int)(i - startTime)][j*4+3] += deletes[j];
				}
			}
		}

		String gap = "   "; // three whitespaces

		for (int i = 0; i < monitorLogs.size(); ++i)
		{
			MonitorLog log = monitorLogs.get(i);

			countWriter.print(gap);
			avgLatencyWriter.print(gap);

			countWriter.printf("%.16e", (double)log.getTimestamp());
			avgLatencyWriter.printf("%.16e", (double)log.getTimestamp());

			for (int j = 0;j < clusters.size();j++)
			{
				countWriter.print(gap);
				countWriter.printf("%.16e", (double) counts[i][j]);
				avgLatencyWriter.print(gap);
				if (counts[i][j] == 0)
					avgLatencyWriter.printf("%.16e", 0.0);
				else
					// divide by 1000 to convert into seconds.
					avgLatencyWriter.printf("%.16e", (latencies[i][j] / (double) counts[i][j]) / 1000.0);
			}

			statementWriter.printf("%.16e,", (double)log.getTimestamp());
			for (int j = 0; j < statementCounts[i].length; ++j)
			{
				statementWriter.printf("%d,", statementCounts[i][j]);
			}

			// write total count & avg latency
//			countWriter.print(gap);
//			avgLatencyWriter.print(gap);
//
//			countWriter.printf("%.16e", (double) totalCounts[i]);
//			if (totalCounts[i] == 0 )
//			{
//				avgLatencyWriter.printf("%.16e", 0.0);
//			}
//			else
//			{
//				avgLatencyWriter.printf("%.16e", (totalLatency[i] / (double) totalCounts[i]) / 1000.0);
//			}
//
			countWriter.println();
			avgLatencyWriter.println();
			statementWriter.println();
		}

		countWriter.flush();
		countWriter.close();
		avgLatencyWriter.flush();
		avgLatencyWriter.close();
		statementWriter.flush();
		statementWriter.close();

		if (!writeLatencyPercentile(latenciesAtTime, monitorLogs.size(), clusters.size()))
		{
			return false;
		}

		return true;
	}

	private boolean writeLatencyPercentile(ArrayList<Double>[][] latencies, int logSize, int varSize)
	{
		DBSeerGUI.status.setText("Processing Dataset: Writing percentile latencies.");
		MatlabProxy      proxy                 = DBSeerGUI.proxy;
		List<MonitorLog> monitorLogs           = monitor.getLogs();
		File             percentileLatencyFile = new File(processedPath + File.separator + "prctile_latencies.mat");

		if (proxy == null)
		{
			JOptionPane.showMessageDialog(null, "MatlabProxy uninitialized.", "Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}

		if (!percentileLatencyFile.getParentFile().exists())
		{
			percentileLatencyFile.getParentFile().mkdirs();
		}

		try
		{
			proxy.eval("latenciesPCtile = zeros(" + logSize + "," + (varSize+1) + ",8);");

			for (int i = 0; i < logSize; ++i)
			{
				proxy.eval("latenciesPCtile(" + (i+1) + ",1) = " + monitorLogs.get(i).getTimestamp() + ";");
				for (int j = 0; j < varSize; ++j)
				{
					if (latencies[i][j] == null)
					{
						proxy.eval("latenciesPCtile(" + (i+1) + "," + (j+2) + ",:) = prctile([], [10, 25, 50, 75, 90, 95, 99, 99.9]);");
					}
					else
					{
						int index = 0;
						double[] latencyValues = new double[latencies[i][j].size()];
						for (Double d : latencies[i][j])
						{
							latencyValues[index] = d.doubleValue() / 1000.0; // converting to seconds...
							++index;
						}
						proxy.setVariable("latencies", latencyValues);
						proxy.eval("latenciesPCtile(" + (i + 1) + "," + (j + 2) + ",:) = prctile(latencies, [10, 25, 50, 75, 90, 95, 99, 99.9]);");
					}
				}
			}
			proxy.eval("save('" + percentileLatencyFile.getAbsolutePath() + "', 'latenciesPCtile');");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return true;
	}

	private boolean writeHeader()
	{
		int         interruptIndex   = 0;
		int         memoryUsageIndex = 0;
		int         swapFreeIndex    = 0;
		int         memoryFreeIndex  = 0;
		int         virtualIndex     = 0;
		int         fileSystemIndex  = 0;
		int         swapIndex        = 0;
		int         pagingIndex      = 0;
		int         procsIndex       = 0;
		int         diskReadCount    = 0;
		int         diskWriteCount   = 0;
		int         ioReadCount      = 0;
		int         ioWriteCount     = 0;
		int         netRecvCount     = 0;
		int         netSendCount     = 0;
		int         utilCount        = 0;
		PrintWriter writer           = null;
		File        file             = new File(processedPath + File.separator + "dataset_header.m");

		List<Integer>        diskIndexes  = new ArrayList<Integer>();
		List<Integer>        ioIndexes    = new ArrayList<Integer>();
		List<Integer>        utilIndexes  = new ArrayList<Integer>();
		List<Integer>        netIndexes   = new ArrayList<Integer>();

		Set<Integer>         cpuUsrSet    = new LinkedHashSet<Integer>();
		Set<Integer>         cpuSysSet    = new LinkedHashSet<Integer>();
		Set<Integer>         cpuWaiSet    = new LinkedHashSet<Integer>();
		Set<Integer>         cpuIdlSet    = new LinkedHashSet<Integer>();
		Set<Integer>         cpuSiqSet    = new LinkedHashSet<Integer>();
		Set<Integer>         cpuHiqSet    = new LinkedHashSet<Integer>();

		Set<Integer>         netSendSet   = new LinkedHashSet<Integer>();
		Set<Integer>         netRecvSet   = new LinkedHashSet<Integer>();
		Map<String, Integer> interruptMap = new TreeMap<String, Integer>();

		DBSeerGUI.status.setText("Processing Dataset: Writing header file.");
		try
		{
			if (!file.getParentFile().exists())
			{
				file.getParentFile().mkdirs();
			}
			if (!file.exists())
			{
				file.createNewFile();
			}
			writer = new PrintWriter(new BufferedWriter(new FileWriter(file)));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		// write columns
		writer.print("columns = struct(");
		String[] headers = monitor.getHeaders();
		String[] metaHeaders = monitor.getMetaHeaders();

		// get location of interrupt/dsk/io/util columns
		for (int i = 0; i < metaHeaders.length; ++i)
		{
			if (metaHeaders[i].equalsIgnoreCase("interrupts"))
			{
				interruptIndex = i;
			}
			else if (metaHeaders[i].contains("dsk"))
			{
				diskIndexes.add(i);
			}
			else if (metaHeaders[i].contains("io"))
			{
				ioIndexes.add(i);
			}
			else if (metaHeaders[i].contains("net"))
			{
				netIndexes.add(i);
			}
			else if (metaHeaders[i].matches("[a-z]d[a-z][0-9]*"))
			{
				utilIndexes.add(i);
			}
			else if (metaHeaders[i].equalsIgnoreCase("memory usage"))
			{
				memoryUsageIndex = i;
			}
			else if (metaHeaders[i].equalsIgnoreCase("swap"))
			{
				swapIndex = i;
			}
			else if (metaHeaders[i].equalsIgnoreCase("virtual memory"))
			{
				virtualIndex = i;
			}
			else if (metaHeaders[i].equalsIgnoreCase("filesystem"))
			{
				fileSystemIndex = i;
			}
			else if (metaHeaders[i].equalsIgnoreCase("paging"))
			{
				pagingIndex = i;
			}
			else if (metaHeaders[i].equalsIgnoreCase("procs"))
			{
				procsIndex = i;
			}
		}

		// get interrupt columns.
		while (interruptIndex < headers.length)
		{
			if (headers[interruptIndex].matches("[0-9]+"))
			{
				interruptMap.put(headers[interruptIndex], interruptIndex+1);
				++interruptIndex;
			}
			else
			{
				break;
			}
		}

		// write 'columns' variable.
		for (int i = 0; i < headers.length; ++i)
		{
			String header = headers[i];

			// store cpu & network info separately
			if (header.equalsIgnoreCase("usr"))
			{
				writer.print("'cpu" + cpuUsrSet.size() + "_usr'," + (i + 1));
				cpuUsrSet.add(i + 1);
			}
			else if (header.equalsIgnoreCase("sys"))
			{
				writer.print("'cpu" + cpuSysSet.size() + "_sys'," + (i + 1));
				cpuSysSet.add(i + 1);
			}
			else if (header.equalsIgnoreCase("idl"))
			{
				writer.print("'cpu" + cpuIdlSet.size() + "_idl'," + (i + 1));
				cpuIdlSet.add(i + 1);
			}
			else if (header.equalsIgnoreCase("wai"))
			{
				writer.print("'cpu" + cpuWaiSet.size() + "_wai'," + (i + 1));
				cpuWaiSet.add(i + 1);
			}
			else if (header.equalsIgnoreCase("hiq"))
			{
				writer.print("'cpu" + cpuHiqSet.size() + "_hiq'," + (i + 1));
				cpuHiqSet.add(i + 1);
			}
			else if (header.equalsIgnoreCase("siq"))
			{
				writer.print("'cpu" + cpuSiqSet.size() + "_siq'," + (i + 1));
				cpuSiqSet.add(i + 1);
			}
			else if (header.equalsIgnoreCase("send"))
			{
				writer.print("'net" + netSendSet.size() + "_send'," + (i + 1));
				netSendSet.add(i + 1);
			}
			else if (header.equalsIgnoreCase("recv"))
			{
				writer.print("'net" + netRecvSet.size() + "_recv'," + (i + 1));
				netRecvSet.add(i+1);
			}
			else if (header.matches("[0-9]+")) // interrupts
			{
				writer.print("'interrupts_" + header + "'," + (i + 1));
			}
			else if (header.matches("[0-9]+m")) // 1m, 5m, 15m..
			{
				writer.print("'load_" + header + "'," + (i + 1));
			}
			else if (header.equalsIgnoreCase("#aio"))
			{
				writer.print("'aio'," + (i+1));
			}
			else if (i >= memoryUsageIndex && i <= memoryUsageIndex + 3)
			{
				writer.print("'memory_" + header + "'," + (i + 1));
			}
			else if (i >= swapIndex && i <= swapIndex + 1)
			{
				writer.print("'swap_" + header + "'," + (i + 1));
			}
			else if (i >= virtualIndex && i <= virtualIndex + 3)
			{
				writer.print("'virtual_" + header + "'," + (i + 1));
			}
			else if (i >= fileSystemIndex && i <= fileSystemIndex + 1)
			{
				writer.print("'filesystem_" + header + "'," + (i + 1));
			}
			else if (i >= pagingIndex && i <= pagingIndex + 1)
			{
				writer.print("'paging_" + header + "'," + (i + 1));
			}
			else if (i >= procsIndex && i <= procsIndex + 2)
			{
				writer.print("'procs_" + header + "'," + (i + 1));
			}
			else if (header.equalsIgnoreCase("read"))
			{
				if (diskIndexes.contains(i))
				{
					writer.print("'dsk_" + header);
					if (diskReadCount > 0) writer.print(diskReadCount + "',");
					else writer.print("',");
					writer.print(i+1);
					++diskReadCount;
				}
				else if (ioIndexes.contains(i))
				{
					writer.print("'io_" + header);
					if (ioReadCount > 0) writer.print(ioReadCount + "',");
					else writer.print("',");
					writer.print(i+1);
					++ioReadCount;
				}
			}
			else if (header.equalsIgnoreCase("writ"))
			{
				if (diskIndexes.contains(i-1))
				{
					writer.print("'dsk_" + header);
					if (diskWriteCount > 0) writer.print(diskWriteCount + "',");
					else writer.print("',");
					writer.print(i+1);
					++diskWriteCount;
				}
				else if (ioIndexes.contains(i-1))
				{
					writer.print("'io_" + header);
					if (ioWriteCount > 0) writer.print(ioWriteCount + "',");
					else writer.print("',");
					writer.print(i+1);
					++ioWriteCount;
				}
			}
			else if (header.equalsIgnoreCase("util"))
			{
				writer.print("'" + header);
				if (utilCount > 0) writer.print(utilCount + "',");
				else writer.print("',");
				writer.print(i+1);
				++utilCount;
			}
			else
			{
				writer.print("'" + headers[i].replaceAll("\\.","").replaceAll(" ", "_") + "'," + (i + 1)); // remove dots & spaces
			}

			if (i == headers.length - 1) writer.println(");");
			else writer.print(",");
		}

		// write 'interrupts' variable
		writer.print("interrupts = struct(");
		Iterator<Map.Entry<String,Integer>> it = interruptMap.entrySet().iterator();
		while (it.hasNext())
		{
			Map.Entry<String, Integer> entry = it.next();
			writer.print("'i" + entry.getKey() + "'," + entry.getValue());

			if (it.hasNext()) writer.print(",");
			else writer.println(");");
		}

		// write 'metadata' variable
		writer.print("metadata = struct(");
		writer.print("'cpu_siq',[");
		writeMetadataVector(writer, cpuSiqSet.iterator());

		writer.print("'cpu_usr',[");
		writeMetadataVector(writer, cpuUsrSet.iterator());

		writer.print("'cpu_idl',[");
		writeMetadataVector(writer, cpuIdlSet.iterator());

		writer.print("'cpu_wai',[");
		writeMetadataVector(writer, cpuWaiSet.iterator());

		writer.print("'cpu_sys',[");
		writeMetadataVector(writer, cpuSysSet.iterator());

		writer.print("'cpu_hiq',[");
		writeMetadataVector(writer, cpuHiqSet.iterator());

		writer.print("'net_send',[");
		writeMetadataVector(writer, netSendSet.iterator());

		writer.print("'net_recv',[");
		writeMetadataVector(writer, netRecvSet.iterator());

		writer.println("'interrupts',interrupts, 'num_net'," + netRecvSet.size() + ",'num_cpu'," +
				cpuUsrSet.size() + ");");

		writer.println("header = struct('dbms','mysql','columns',columns,'metadata',metadata);");

		writer.print("extra  = struct('disk',[");
		for (int i = 0; i < diskIndexes.size(); ++i)
		{
			writer.print(diskIndexes.get(i) + 1);
			if (i == diskIndexes.size() - 1) writer.print("],");
			else writer.print(" ");
		}
		writer.print("'io',[");
		for (int i = 0; i < ioIndexes.size(); ++i)
		{
			writer.print(ioIndexes.get(i) + 1);
			if (i == ioIndexes.size() - 1) writer.print("],");
			else writer.print(" ");
		}
		writer.print("'util',[");
		for (int i = 0; i < utilIndexes.size(); ++i)
		{
			writer.print(utilIndexes.get(i) + 1);
			if (i == utilIndexes.size() - 1) writer.print("]");
			else writer.print(" ");
		}
		writer.println(");");

		writer.flush();
		writer.close();

		return true;
	}

	private void writeMetadataVector(PrintWriter writer, Iterator<Integer> it)
	{
		while (it.hasNext())
		{
			int value = it.next().intValue();
			writer.print(value);

			if (it.hasNext()) writer.print(" ");
			else writer.print("],");
		}
	}

	private boolean parseMonitorLogs()
	{
		DBSeerGUI.status.setText("Processing Dataset: Parsing monitor logs.");
		File monitorPath = new File(this.rawPath);
		File monitorFile = null;

		File[] files = monitorPath.listFiles();

		if (files == null)
		{
			return false;
		}

		for (File file : files)
		{
			if (file.getName().contains(".csv") && file.getName().contains("log_exp"))
			{
				monitorFile = file;
				break;
			}
		}

		File processFile = new File(processedPath + File.separator + "monitor");
		if (!processFile.getParentFile().exists())
		{
			processFile.getParentFile().mkdirs();
		}
		if (!processFile.exists())
		{
			try
			{
				processFile.createNewFile();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}

		if (monitorFile != null)
		{
			if (!monitor.parseMonitorFile(monitorFile, processFile))
			{
				JOptionPane.showMessageDialog(null, "Failed to parse monitoring logs.", "Error", JOptionPane.ERROR_MESSAGE);
			    return false;
			}
		}

		return true;
	}

	private boolean parseTransactionLogs()
	{
		DBSeerGUI.status.setText("Processing Dataset: Parsing transaction logs.");
		File rawFile = new File(this.rawPath + File.separator + "allLogs-t.txt");

//		if (!rawFile.exists())
//		{
//			JOptionPane.showMessageDialog(null, "Failed to parse transaction logs. File does not exist.", "Error", JOptionPane.ERROR_MESSAGE);
//			return false;
//		}

		RandomAccessFile file = null;

		try
		{
			file = new RandomAccessFile(rawFile, "r");
		}
		catch (FileNotFoundException e)
		{
			JOptionPane.showMessageDialog(null, "Failed to parse transaction logs. File does not exist.",
					"Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}

		try
		{
			String line = null;
//			BufferedReader br = new BufferedReader(new FileReader(file));

			while ((line = file.readLine()) != null)
			{
				Transaction transaction = new Transaction();
				String[] columns = line.split(",");

				if (columns.length < 6)
				{
					continue;
				}

				// 0 - id, 1 - port, 2 - user, 3 - start timestamp, 4 - end timestamp, 5 - latency
				Integer id = new Integer(Integer.parseInt(columns[0]));
				transaction.setId(id.intValue());
				transaction.setPort(Integer.parseInt(columns[1]));
				transaction.setUser(columns[2]);
				transaction.setStartTime(Long.parseLong(columns[3]));
				transaction.setEndTime(Long.parseLong(columns[4]));
				transaction.setLatency(Long.parseLong(columns[5]));

				if (transactionMap.put(id, transaction) != null)
				{
//					System.out.println("Duplicate transaction id: " + id.intValue());
				}
			}
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return true;
	}

	private boolean parseStatementLogs()
	{
		DBSeerGUI.status.setText("Processing Dataset: Parsing statement logs.");
		File rawFile = new File(this.rawPath + File.separator + "allLogs-s.txt");

//		if (!file.exists())
//		{
//			JOptionPane.showMessageDialog(null, "Failed to parse statement logs. File does not exist.", "Error", JOptionPane.ERROR_MESSAGE);
//			return false;
//		}

		RandomAccessFile file = null;

		try
		{
			file = new RandomAccessFile(rawFile, "r");
		}
		catch (FileNotFoundException e)
		{
			JOptionPane.showMessageDialog(null, "Failed to parse transaction logs. File does not exist.",
					"Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}

		try
		{
			String line = null;
//			BufferedReader br = new BufferedReader(new FileReader(file));

			long offset = file.getFilePointer();
			while ((line = file.readLine()) != null)
			{
				Statement stmt = new Statement();

				String[] columns = line.split(",");

				if (columns.length < 6)
				{
					continue;
				}

				Integer transactionId = Integer.parseInt(columns[0]);
				int id = Integer.parseInt(columns[2]);
				stmt.setId(id);
				stmt.setStartTime(Long.parseLong(columns[3]));
				stmt.setEndTime(Long.parseLong(columns[4]));
				stmt.setLatency(Long.parseLong(columns[5]));
				stmt.setFileOffset(offset);

				Transaction transaction = transactionMap.get(transactionId);

				if (transaction == null)
				{
				//	System.out.println("No mapping transaction id: " + transactionId + " for statement - ignoring...");
					continue;
				}
				transaction.addStatement(stmt);

				if (statementMap.put(id, stmt) != null)
				{
//					System.out.println("Duplicate statement with id: " + id);
				}
				offset = file.getFilePointer();
			}
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		return true;
	}

	private boolean parseQueryLogs()
	{
		DBSeerGUI.status.setText("Processing Dataset: Parsing query logs.");
//		File file = new File(this.rawPath + File.separator + "allLogs-q.txt");
		RandomAccessFile file;

		try
		{
			file = new RandomAccessFile(this.rawPath + File.separator + "allLogs-q.txt", "r");
		}
		catch (FileNotFoundException e)
		{
			JOptionPane.showMessageDialog(null, "Failed to parse query logs. File does not exist.", "Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}

//		if (!file.exists())
//		{
//			JOptionPane.showMessageDialog(null, "Failed to parse query logs. File does not exist.", "Error", JOptionPane.ERROR_MESSAGE);
//			return false;
//		}

		long count = 0;
		SQLStatementParser parser = new SQLStatementParser();

		try
		{
			long offset = file.getFilePointer();
			String line = file.readLine();

			while (line != null)
			{
				int commaIndex = line.indexOf(",");
				if (commaIndex == -1)
				{
					offset = file.getFilePointer();
					line = file.readLine();
					continue;
				}
				int id = Integer.parseInt(line.substring(0,commaIndex));
				String statement = line.substring(commaIndex+1);
				statement = statement.replaceAll("\0", "\n");
				Statement stmt = statementMap.get(id);

				if (stmt == null)
				{
					offset = file.getFilePointer();
					line = file.readLine();
					continue;
				}

				List<MonitorLog> logs = monitor.getLogs(stmt.getStartTime(), stmt.getEndTime() + 1);

//				String statement = line.substring(line.indexOf(",") + 1);

				if (statement.isEmpty())
				{
					offset = file.getFilePointer();
					line = file.readLine();
					continue;
				}

				stmt.setContent(statement);
				stmt.setQueryOffset(offset);
				int mode = parser.parseStatement(statement);

				if (statement.toLowerCase().contains("for update"))
				{
					mode = DBSeerConstants.STATEMENT_UPDATE;
				}

				// ignore this statement for now (OLTPBenchmark runs it at the end of the benchmark.)
				if (statement.toLowerCase().contains("select * from global_variables"))
				{
					mode = DBSeerConstants.STATEMENT_NONE;
				}

				stmt.setMode(mode);

				for (String table : parser.getTables())
				{
					stmt.addTable(table);
					globalTableSet.add(table);
				}

				if (logs == null)
				{
					offset = file.getFilePointer();
					line = file.readLine();
					continue;
				}

				for (MonitorLog log : logs)
				{
					if (log != null)
					{
						if (mode == DBSeerConstants.STATEMENT_READ)
						{
							log.incrementReadStatement();
						}
						else if (mode == DBSeerConstants.STATEMENT_INSERT)
						{
							log.incrementInsertStatement();
						}
						else if (mode == DBSeerConstants.STATEMENT_UPDATE)
						{
							log.incrementUpdateStatement();
						}
						else if (mode == DBSeerConstants.STATEMENT_DELETE)
						{
							log.incrementDeleteStatement();
						}
					}
				}

				++count;
				if (count % 10000 == 0)
				{
//					System.out.println (count + " queries processed.");
					DBSeerGUI.status.setText("Processing Dataset: " + count + " queries processed.");
				}

				offset = file.getFilePointer();
				line = file.readLine();
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return false;
		}

		return true;
	}

	private void prepareTransactionClustering()
	{
//		System.out.println("Preparing for transaction clustering.");
		DBSeerGUI.status.setText("Processing Dataset: Preparing for transaction clustering.");
		globalTableList = globalTableSet.toArray(new String[globalTableSet.size()]);
		for (int i = 0; i < globalTableList.length; ++i)
		{
			globalTableMap.put(globalTableList[i], i);
		}

		for (Transaction transaction : transactionMap.values())
		{
			transaction.setNumTable(globalTableList.length);
			List<Statement> statements = transaction.getStatements();

			for (Statement statement : statements)
			{
				Set<String> tables = statement.getTables();
				for (String table : tables)
				{
					int idx = globalTableMap.get(table);
					switch (statement.getMode())
					{
						case DBSeerConstants.STATEMENT_READ:
							transaction.addSelect(idx);
							break;
						case DBSeerConstants.STATEMENT_INSERT:
							transaction.addInsert(idx);
							break;
						case DBSeerConstants.STATEMENT_UPDATE:
							transaction.addUpdate(idx);
							break;
						case DBSeerConstants.STATEMENT_DELETE:
							transaction.addDelete(idx);
							break;
						default:
							break;
					}
				}
			}
		}
	}

	public void performDBSCAN()
	{
//		System.out.println("Starting DBSCAN");
		DBSeerGUI.status.setText("Processing Dataset: Performing DBSCAN.");

		clusters.clear();
		actualTransactions.clear();

		Transaction[] transactions = transactionMap.values().toArray(new Transaction[transactionMap.values().size()]);

		if (transactions.length == 0)
		{
//			System.out.println("DBSCAN terminates: no transactions.");
			return;
		}

		for (Transaction transaction : transactions)
		{
			if (!transaction.isNoRowsReadWritten())
			{
				actualTransactions.add(transaction);
			}
		}

		double eps = Math.sqrt(Transaction.DIFF_SCALE);
		//double eps = Transaction.DIFF_SCALE / 10;
		int minTransactions = globalTableList.length + 1;

//		System.out.println("transaction count = " + transactions.length);
//		System.out.println("actual transaction count = " + actualTransactions.size());
//		System.out.println("eps = " + eps);
//		System.out.println("minPts = " + minTransactions);

		transactions = actualTransactions.toArray(new Transaction[actualTransactions.size()]);

		for (Transaction transaction : transactions)
		{
			if (transaction.getClassification() == Transaction.UNCLASSIFIED)
			{
				Cluster expandedCluster = expandCluster(transactions, transaction, eps, minTransactions);
				if (expandedCluster != null)
				{
					expandedCluster.setId(clusters.size());
					clusters.add(expandedCluster);
				}
			}
		}

		// handle noises with K-NN
		// assign them to the closest cluster.
		for (Transaction t : transactions)
		{
			if (t.getClassification() == Transaction.NOISE)
			{
				assignToClusterKNN(t, clusters.size() * 2);
			}
		}

		// Sort clusters in descending order in # of transactions.
		Collections.sort(clusters, Collections.reverseOrder(new ClusterSizeComparator()));

//		System.out.println("DBSCAN complete");
		DBSeerGUI.status.setText("Processing Dataset: DBSCAN has completed.");
//		printClusterAccAnalysisTPCC();
	}

	private Transaction[] findNeighbors(Transaction[] transactions, Transaction source, double eps)
	{
		ArrayList<Double> distList = new ArrayList<Double>();
		ArrayList<Transaction> neighbors = new ArrayList<Transaction>();

		double dist = 0.0;

		for (Transaction transaction : transactions)
		{
			if ( (dist = source.getEuclideanDistance(transaction)) < eps)
			{
				neighbors.add(transaction);
			}
			distList.add(dist);
		}
		return neighbors.toArray(new Transaction[neighbors.size()]);
	}

	private Cluster expandCluster(Transaction[] transactions, Transaction source, double eps, int minTransactions)
	{
		Transaction[] neighbors = findNeighbors(transactions, source, eps);
		Queue<Transaction> neighborsToExpand = new LinkedList<Transaction>();

		if (neighbors.length < minTransactions || clusters.size() >= DBSeerConstants.DBSCAN_MAX_CLUSTERS)
		{
			source.setClassification(Transaction.NOISE);
			return null;
		}
		else
		{
			Cluster cluster = new Cluster();

			cluster.addTransaction(source);

			for (Transaction neighbor : neighbors)
			{
				neighborsToExpand.add(neighbor);
			}

			Transaction neighbor = null;

			while ((neighbor = neighborsToExpand.poll()) != null)
			{
				neighbors = findNeighbors(transactions, neighbor, eps);
				if (neighbors.length >= minTransactions)
				{
					for (Transaction neighborToExpand : neighbors)
					{
						if (neighborToExpand.getClassification() < Transaction.CLASSIFIED)
						{
							if (neighborToExpand.getClassification() == Transaction.UNCLASSIFIED)
							{
								neighborsToExpand.add(neighborToExpand);
							}
							cluster.addTransaction(neighborToExpand);
						}
					}
				}
			}
			return cluster;
		}
	}

	private void assignToClusterKNN(Transaction source, int k)
	{
		int[] clusterCount = new int[clusters.size()];

		for (int i = 0; i < clusterCount.length; ++i)
		{
			clusterCount[i] = 0;
		}

		double dist = 0;
		ArrayList<TransactionDistance> distances = new ArrayList<TransactionDistance>();

		for (Transaction t : actualTransactions)
		{
			if (t.getClassification() == Transaction.NOISE) continue;
			dist = source.getEuclideanDistance(t);
			TransactionDistance distance = new TransactionDistance(t, dist);
			distances.add(distance);
		}
		Collections.sort(distances);

		k = (distances.size() < k) ? distances.size() : k;
		for (int i = 0; i < k; ++i)
		{
			clusterCount[distances.get(i).getTransaction().getCluster().getId()]++;
		}

		int maxClusterCount = 0;
		int maxClusterIdx = 0;

		for (int i = 1; i < clusterCount.length; ++i)
		{
			if (maxClusterCount <  clusterCount[i])
			{
				maxClusterIdx = i;
				maxClusterCount = clusterCount[i];
			}
		}

		Cluster clusterToAssign = clusters.get(maxClusterIdx);
		clusterToAssign.addTransaction(source);
	}

	private void writePageInfo()
	{
//		System.out.println("Writing Page Info...");
		DBSeerGUI.status.setText("Processing Dataset: Writing page information.");
		File pageFile = new File(processedPath + File.separator + "page_info.m");

		double[] transactionMix = new double[clusters.size()];
		int[][] allOperationCount = new int[clusters.size()][globalTableList.length];
		double[] allOperationAvg = new double[globalTableList.length];
		int[][] writeOperationCount = new int[clusters.size()][globalTableList.length];
		double[] writeOperationAvg = new double[globalTableList.length];
		double sum = 0;

		PrintWriter pageWriter = null;

		try
		{
			pageWriter = new PrintWriter(new BufferedWriter(new FileWriter(pageFile)));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		for (int i = 0; i < clusters.size(); ++i)
		{
			Cluster c = clusters.get(i);
			transactionMix[i] = c.getTransactions().size();
			sum += transactionMix[i];

			for (Transaction t : c.getTransactions())
			{
				long[] numSelect = t.getNumSelect();
				long[] numInsert = t.getNumInsert();
				long[] numUpdate = t.getNumUpdate();
				long[] numDelete = t.getNumDelete();

				for (int j = 0; j < globalTableList.length; ++j)
				{
					writeOperationCount[i][j] += (int)(numInsert[j] + numUpdate[j] + numDelete[j]);
					allOperationCount[i][j] += (int)(numSelect[j] + numInsert[j] + numUpdate[j] + numDelete[j]);
				}
			}
		}

		for (int i = 0; i < clusters.size(); ++i)
		{
			transactionMix[i] = transactionMix[i] / sum;
		}

		pageWriter.print("clusteredPageFreq = [");
		for (int i = 0; i < clusters.size(); ++i)
		{
			for (int j = 0; j < globalTableList.length; ++j)
			{
				allOperationAvg[j] += (double)allOperationCount[i][j] * transactionMix[i];
				writeOperationAvg[j] += (double)writeOperationCount[i][j] * transactionMix[i];
				pageWriter.print((double) writeOperationCount[i][j] / (double) actualTransactions.size() + " ");
			}
			if (i != clusters.size()-1)
			{
				pageWriter.print(";");
			}
		}
		pageWriter.println("];");

		pageWriter.print("clusteredPageMix = [");
		for (int j = 0; j < globalTableList.length; ++j)
		{
			pageWriter.print(allOperationAvg[j] / actualTransactions.size() + " ");
		}
		pageWriter.println("];");

		pageWriter.flush();
		pageWriter.close();
	}

	private void printClusterAccAnalysisTPCC()
	{
		System.out.println();
		System.out.println("--- Classification Accuracy Analysis ---");
		System.out.println();
		System.out.println("Cluster size = " + clusters.size());
		int idx = 0;
		for (Cluster c : clusters)
		{
			System.out.println("cluster: " + idx + " (" + c.getTransactions().size() + ")");
			int numTransactions = c.getTransactions().size();
			List<Transaction> list = c.getTransactions();

			String type = "";

			for (Statement s : list.get(0).getStatements())
			{
				if (s.getContent().contains("UPDATE WAREHOUSE"))
				{
					System.out.print("(Payment) ");
					type = "payment";
					break;
				}
				else if (s.getContent().contains("SELECT COUNT(DISTINCT (S_I_ID))"))
				{
					System.out.print("(StockLevel) ");
					type = "stocklevel";
					break;
				}
				else if (s.getContent().contains("SELECT O_ID"))
				{
					System.out.print("(OrderStatus) ");
					type = "orderstatus";
					break;
				}
				else if (s.getContent().contains("INSERT INTO NEW_ORDER"))
				{
					System.out.print("(NewOrder) ");
					type = "neworder";
					break;
				}
				else if (s.getContent().contains("SELECT SUM(OL_AMOUNT)"))
				{
					System.out.print("(Delivery) ");
					type = "delivery";
					break;
				}
			}

			int matchCount = 0;

			for (int i = 0; i < list.size(); ++i)
			{
				boolean match = false;
				System.out.print(list.get(i).getId() + " ");
				Transaction t = list.get(i);

				for (Statement s : t.getStatements())
				{
					if (type.equalsIgnoreCase("payment") && s.getContent().contains("UPDATE WAREHOUSE")) match = true;
					else if (type.equalsIgnoreCase("stocklevel") && s.getContent().contains("SELECT COUNT(DISTINCT (S_I_ID))")) match = true;
					else if (type.equalsIgnoreCase("orderstatus") && s.getContent().contains("SELECT O_ID")) match = true;
					else if (type.equalsIgnoreCase("neworder") && s.getContent().contains("INSERT INTO NEW_ORDER")) match = true;
					else if (type.equalsIgnoreCase("delivery") && s.getContent().contains("SELECT SUM(OL_AMOUNT)")) match = true;

					if (match) break;
				}

				if (match) ++matchCount;
				//else System.out.println("\nmisclassification = " + t.getId());
			}
			System.out.println();
			System.out.println("Correct Classification = " + matchCount);
			System.out.println("Classification Accuracy = " + ((double)matchCount/(double)list.size()) * 100.0 + "%");
			++idx;
		}

		System.out.println("noises:");
		int noiseCount = 0;
		Transaction[] transactions = actualTransactions.toArray(new Transaction[actualTransactions.size()]);
		for (Transaction t : transactions)
		{
			if (t.getClassification() == Transaction.NOISE)
			{
				System.out.print(t.getId() + " ");
				++noiseCount;
			}
		}
		System.out.println();
		System.out.println("# noises = " + noiseCount);
	}
}

