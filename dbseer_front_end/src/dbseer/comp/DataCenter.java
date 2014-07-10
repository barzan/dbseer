package dbseer.comp;

import com.foundationdb.sql.parser.StatementNode;
import dbseer.comp.data.*;
import dbseer.gui.DBSeerConstants;

import java.io.*;
import java.lang.reflect.Array;
import java.util.*;

/**
 * Created by dyoon on 2014. 7. 4..
 *
 * processes raw dataset from the middleware.
 *
 */
public class DataCenter
{
	private String path;

	private SystemMonitor monitor;

	private Map<Integer, Transaction> transactionMap;
	private Map<Integer, Statement> statementMap;
	private Set<String> globalTableSet;
	private String[] globalTableList;
	private Map<String, Integer> globalTableMap;
	private ArrayList<Cluster> clusters;
	private ArrayList<Transaction> actualTransactions;

	public DataCenter(String path)
	{
		this.path = path;
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

	public boolean parseMonitorLogs()
	{
		File monitorPath = new File(this.path);
		File monitorFile = null;

		File[] files = monitorPath.listFiles();

		for (File file : files)
		{
			if (file.getName().contains(".csv") && file.getName().contains("log_exp"))
			{
				monitorFile = file;
			}
		}

		if (monitorFile != null)
		{
			if (!monitor.parseMonitorFile(monitorFile))
			{
			    return false;
			}
		}

		return true;
	}

	public boolean parseTransactionLogs()
	{
		File file = new File(this.path + File.separator + "allLogs-t.txt");

		if (!file.exists())
		{
			System.out.println("transaction log does not exist.");
			return false;
		}

		try
		{
			String line = null;
			BufferedReader br = new BufferedReader(new FileReader(file));

			while ((line = br.readLine()) != null)
			{
				Transaction transaction = new Transaction();
				String[] columns = line.split(",");

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
					System.out.println("Duplicate transaction id: " + id.intValue());
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

		return true;
	}

	public boolean parseStatementLogs()
	{
		File file = new File(this.path + File.separator + "allLogs-s.txt");

		if (!file.exists())
		{
			System.out.println("statement log does not exist.");
			return false;
		}

		try
		{
			String line = null;
			BufferedReader br = new BufferedReader(new FileReader(file));

			while ((line = br.readLine()) != null)
			{
				Statement stmt = new Statement();

				String[] columns = line.split(",");

				Integer transactionId = Integer.parseInt(columns[0]);
				int id = Integer.parseInt(columns[2]);
				stmt.setId(id);
				stmt.setStartTime(Long.parseLong(columns[3]));
				stmt.setEndTime(Long.parseLong(columns[4]));
				stmt.setLatency(Long.parseLong(columns[5]));

				Transaction transaction = transactionMap.get(transactionId);

				if (transaction == null)
				{
					System.out.println("No mapping transaction for statement");
					return false;
				}
				transaction.addStatement(stmt);

				if (statementMap.put(id, stmt) != null)
				{
					System.out.println("Duplicate statement with id: " + id);
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

		return true;
	}

	public boolean parseQueryLogs()
	{
		File file = new File(this.path + File.separator + "allLogs-q.txt");
		if (!file.exists())
		{
			System.out.println("query log does not exist.");
			return false;
		}

		SQLStatementParser parser = new SQLStatementParser();

		try
		{
			Scanner scanner = new Scanner(file);
			scanner.useDelimiter("\0");

			while (scanner.hasNext())
			{
				String line = scanner.next();
				String[] columns = line.split(",");

				String idColumn = columns[0].trim();
				if (idColumn.isEmpty()) continue;
				int id = Integer.parseInt(idColumn);
				Statement stmt = statementMap.get(id);

				if (stmt == null)
				{
					System.out.println("Statement with id: " + id + " is unavailable.");
					return false;
				}

				List<MonitorLog> logs = monitor.getLogs(stmt.getStartTime(), stmt.getEndTime() + 1);

				String statement = line.substring(line.indexOf(",") + 1);

				stmt.setContent(statement);
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
			}
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}

		return true;
	}

	public boolean prepareTransactionClustering()
	{
		globalTableList = globalTableSet.toArray(new String[globalTableSet.size()]);
		for (int i = 0; i < globalTableList.length; ++i)
		{
			globalTableMap.put(globalTableList[i], i);
		}

		for (Transaction transaction : transactionMap.values())
		{
			transaction.setNumTable(globalTableList.length);
			List<Statement> statements = transaction.getStatements();

			long numRead = 0;
			long numInserted = 0;
			long numUpdated = 0;
			long numDeleted = 0;
			long numRows = 0;
			long numStatements = 0;

			for (Statement statement : statements)
			{
				numRead = 0;
				numInserted = 0;
				numUpdated = 0;
				numDeleted = 0;

				List<MonitorLog> logs = monitor.getLogs(statement.getStartTime() - 1, statement.getEndTime() + 1);
				if (logs != null)
				{
					MonitorLog currentLog = null;
					MonitorLog previousLog = null;
					for (int i = 1; i < logs.size(); ++i)
					{
						currentLog = logs.get(i);
						previousLog = logs.get(i-1);

						if (statement.getMode() == DBSeerConstants.STATEMENT_READ)
						{
							numRows = (long)(currentLog.get("Innodb_rows_read").doubleValue() -
									previousLog.get("Innodb_rows_read").doubleValue());
							numStatements = currentLog.getNumReadStatements();
							if (numRows < numStatements)
							{
								numRows = 1; // at least 1 row is read/inserted/updated/deleted...
							}
							else
							{
								numRows = numRows / numStatements;
							}
							numRead += numRows;
						}
						else if (statement.getMode() == DBSeerConstants.STATEMENT_INSERT)
						{
							numInserted++;
						}
						else if (statement.getMode() == DBSeerConstants.STATEMENT_UPDATE)
						{
							numRows = (long)(currentLog.get("Innodb_rows_updated").doubleValue() -
									previousLog.get("Innodb_rows_updated").doubleValue());
							numStatements = currentLog.getNumUpdateStatements();
							if (numRows < numStatements)
							{
								numRows = 1;
							}
							else
							{
								numRows = numRows / numStatements;
							}
							numUpdated += numRows;
						}
						else if (statement.getMode() == DBSeerConstants.STATEMENT_DELETE)
						{
							numRows = (long)(currentLog.get("Innodb_rows_deleted").doubleValue() -
									previousLog.get("Innodb_rows_deleted").doubleValue());
							numStatements = currentLog.getNumDeleteStatements();
							if (numRows < numStatements)
							{
								numRows = 1;
							}
							else
							{
								numRows = numRows / numStatements;
							}
							numDeleted += numRows;
						}
						//System.out.println(numRead + " : " + numWritten);
					}

					Set<String> tables = statement.getTables();
					for (String table : tables)
					{
						int idx = globalTableMap.get(table);
						transaction.addRows(idx, numRead, numInserted, numUpdated, numDeleted);
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
//			System.out.print(transaction.getId() + ": ");
//			transaction.printRowsReadWritten();
		}
		for (String table : globalTableList)
		{
			System.out.print(table + " ");
		}
		System.out.println();
		return true;
	}

	public void performDBSCAN()
	{
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

		double eps = Transaction.DIFF_SCALE / 2;
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

		if (neighbors.length < minTransactions)
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
				else System.out.println("\nmisclassification = " + t.getId());
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
