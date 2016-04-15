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

import dbseer.comp.data.Statement;
import dbseer.comp.data.Transaction;
import dbseer.gui.DBSeerGUI;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by dyoon on 9/24/15.
 */
public class StreamClustering
{
	private static IncrementalDBSCAN dbscan;

	public static Map<Integer, Transaction> trxMap = new ConcurrentHashMap<Integer, Transaction>();
	public static Queue<Transaction> trxQueue = new ArrayBlockingQueue<Transaction>(32 * 1024);
	public static Map<Integer, Statement> stmtMap = new ConcurrentHashMap<Integer, Statement>();
	public static int maxTrxId = Integer.MIN_VALUE;
	public static int maxStmtId = Integer.MIN_VALUE;
	public static long maxStatementId = -1;

	public static Queue<Statement> delayedStatements = new LinkedList<Statement>();
	public static ArrayList<String> delayedQueries = new ArrayList<String>();
	public static Set<String> tableSet = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());

	// double lambda, double epsilon, int minPts, double mu, double beta, double epsilon2, int minPts2
	public static DenStream denstream = new DenStream(0.25, 15, 2, 10, 0.2, 10, 2);
	public static ReentrantLock LOCK = new ReentrantLock();

	private StreamClustering()
	{
	}

	public static void clearMapAndQueues()
	{
		trxMap.clear();
		stmtMap.clear();
		delayedStatements.clear();
		delayedQueries.clear();

		DBSeerGUI.stmtLogQueue.clear();
		DBSeerGUI.trxLogQueue.clear();
		DBSeerGUI.queryLogQueue.clear();
		DBSeerGUI.sysLogQueue.clear();

		maxTrxId = Integer.MIN_VALUE;
		maxStmtId = Integer.MIN_VALUE;
		maxStatementId = -1;
	}

	public static void setDBSCAN(IncrementalDBSCAN dbscan)
	{
		StreamClustering.dbscan = dbscan;
	}

	public static IncrementalDBSCAN getDBSCAN()
	{
		return dbscan;
	}

	public static void addTrx(Transaction t)
	{
		if (maxTrxId < t.getId())
		{
			maxTrxId = t.getId();
		}
		trxMap.put(t.getId(), t);
	}

	public static void addStmt(Statement s)
	{
		if (maxStmtId < s.getId())
		{
			maxStmtId = s.getId();
		}
		stmtMap.put(s.getId(), s);
	}

	public static void addTable(String table)
	{
		tableSet.add(table);
	}

	public static int getTableIndex(String table)
	{
		return Arrays.asList(tableSet.toArray()).indexOf(table);
	}

	public static int getTableCount()
	{
		return tableSet.size();
	}

	public static synchronized long getMaxStatementId()
	{
		return maxStatementId;
	}

	public static synchronized void setMaxStatementId(long maxStatementId)
	{
		StreamClustering.maxStatementId = maxStatementId;
	}

	public static boolean isDelayedLogsExist()
	{
		boolean result;
		result = !delayedQueries.isEmpty() || !delayedStatements.isEmpty();
		return result;
	}
}
