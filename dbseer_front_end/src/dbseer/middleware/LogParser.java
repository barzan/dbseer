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

package dbseer.middleware;

import dbseer.comp.SQLStatementParser;
import dbseer.comp.clustering.StreamClustering;
import dbseer.comp.data.Statement;
import dbseer.comp.data.Transaction;
import dbseer.comp.data.TransactionMap;
import dbseer.gui.DBSeerConstants;
import dbseer.gui.DBSeerGUI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Created by dyoon on 9/24/15.
 */
public class LogParser implements Runnable
{
	private TransactionMap tMap;
	private SQLStatementParser parser;
	private ArrayList<String> unprocessedQueryLogs;

	private boolean terminate;

	public LogParser(TransactionMap tMap)
	{
		this.tMap = tMap;
		this.unprocessedQueryLogs = StreamClustering.delayedQueries;
		this.parser = new SQLStatementParser();
		this.terminate = false;
	}

	@Override
	public void run()
	{
		String logData = null;
		boolean lockAcquired = false;

		while (true)
		{
			if (terminate)
			{
				break;
			}

			try
			{
				StreamClustering.LOCK.lockInterruptibly();
				lockAcquired = true;
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
//			synchronized (StreamClustering.LOCK)
			{
				try
				{
					// parse transaction logs first.
					logData = DBSeerGUI.trxLogQueue.poll(500, TimeUnit.MILLISECONDS);
					if (logData != null)
					{
						String[] logs = logData.split(System.getProperty("line.separator"));

						for (String log : logs)
						{
							Transaction transaction = new Transaction();
							String[] columns = log.split(",");

							if (columns.length < 6)
							{
								continue;
							}

							// 0 - id, 1 - port, 2 - user, 3 - start timestamp, 4 - end timestamp, 5 - latency, 6 - last stmt id
							Integer id = new Integer(Integer.parseInt(columns[0]));
							transaction.setId(id.intValue());
							transaction.setPort(Integer.parseInt(columns[1]));
							transaction.setUser(columns[2]);
							transaction.setStartTime(Long.parseLong(columns[3]));
							transaction.setEndTime(Long.parseLong(columns[4]));
							transaction.setLatency(Long.parseLong(columns[5]));
							transaction.setLastStatementId(Long.parseLong(columns[6]));
							transaction.setNumTable(DBSeerConstants.MAX_NUM_TABLE);

							StreamClustering.addTrx(transaction);
							tMap.add(transaction);
						}
					}

					// parse statements
					// process statements left in previous batch
					while (StreamClustering.delayedStatements.size() > 0)
					{
						Statement stmt = StreamClustering.delayedStatements.peek();

						// break if transaction not available yet.
						if (stmt.getTxId() > StreamClustering.maxTrxId)
						{
							break;
						}

						Transaction t = StreamClustering.trxMap.get(stmt.getTxId());

						if (t != null)
						{
							t.addStatement(stmt);
							stmt.setTransaction(t);
							StreamClustering.addStmt(stmt);
							StreamClustering.delayedStatements.poll();
						}
						else
						{
							if (StreamClustering.maxTrxId > stmt.getTxId())
							{
								StreamClustering.delayedStatements.poll();
								continue;
							}
							else
							{
								break;
							}
						}
					}

					if (StreamClustering.delayedStatements.size() > 0)
					{
						if (lockAcquired && StreamClustering.LOCK.isHeldByCurrentThread())
						{
							StreamClustering.LOCK.unlock();
						}
						lockAcquired = false;
						continue;
					}

					// now process statement log from the queue.
					logData = DBSeerGUI.stmtLogQueue.poll(500, TimeUnit.MILLISECONDS);
					if (logData != null)
					{
						String[] logs = logData.split(System.getProperty("line.separator"));

						for (String log : logs)
						{
							Statement stmt = new Statement();
							String[] columns = log.split(",");

							if (columns.length < 6)
							{
								continue;
							}

							Integer transactionId = Integer.parseInt(columns[0]);
							int id = Integer.parseInt(columns[2]);
							stmt.setId(id);
							stmt.setTxId(transactionId);
							stmt.setStartTime(Long.parseLong(columns[3]));
							stmt.setEndTime(Long.parseLong(columns[4]));
							stmt.setLatency(Long.parseLong(columns[5]));

							// if transaction not available
							if (transactionId.intValue() > StreamClustering.maxTrxId)
							{
								StreamClustering.delayedStatements.add(stmt);
							}
							else
							{
								Transaction t = StreamClustering.trxMap.get(transactionId);
								if (t != null)
								{
									t.addStatement(stmt);
									stmt.setTransaction(t);
									StreamClustering.addStmt(stmt);
								}
								else
								{
									StreamClustering.delayedStatements.add(stmt);
								}
							}
						}
					}

					boolean unprocessedQueryLogExist = false;
					while (unprocessedQueryLogs.size() > 0)
					{
						unprocessedQueryLogExist = true;
						String log = unprocessedQueryLogs.get(0);
						if (parseQueryLog(log))
						{
							unprocessedQueryLogs.remove(0);
						}
						else
						{
							break;
						}
					}

					if (unprocessedQueryLogExist && unprocessedQueryLogs.size() > 0)
					{
						if (lockAcquired)
						{
							StreamClustering.LOCK.unlock();
						}
						lockAcquired = false;
						continue;
					}

					// parse query log
					logData = DBSeerGUI.queryLogQueue.poll(500, TimeUnit.MILLISECONDS);
					if (logData != null)
					{
						String[] logs = logData.split(System.getProperty("line.separator"));

						for (int i = 0; i < logs.length; ++i)
						{
							String log = logs[i];

							if (!parseQueryLog(log))
							{
							 	unprocessedQueryLogs.addAll(Arrays.asList(Arrays.copyOfRange(logs, i, logs.length)));
								break;
							}
						}
					}
				}
				catch (InterruptedException e)
				{
					if (!terminate)
					{
						e.printStackTrace();
					}
				}
			}
			if (lockAcquired)
			{
				StreamClustering.LOCK.unlock();
			}
			lockAcquired = false;

			if (terminate &&
					DBSeerGUI.trxLogQueue.size() == 0 &&
					DBSeerGUI.stmtLogQueue.size() == 0 &&
					DBSeerGUI.queryLogQueue.size() == 0)
			{
				break;
			}

			try
			{
				Thread.sleep(250);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}

	private boolean parseQueryLog(String log)
	{
		int commaIndex = log.indexOf(",");
		if (commaIndex == -1)
		{
			return false;
		}

		int id = Integer.parseInt(log.substring(0, commaIndex));
		String statement = log.substring(commaIndex + 1);
		statement = statement.replaceAll("\0", "");
		Statement stmt = StreamClustering.stmtMap.get(id);
		int mode = parser.parseStatement(statement);

		if (parser.getTables().size() == 0 && !statement.toLowerCase().contains("commit") &&
				!statement.toLowerCase().contains("rollback") && !statement.toLowerCase().contains("set session"))
		{
			return true;
		}

		if (stmt == null)
		{
			if (id < StreamClustering.maxStmtId)
			{
				return true;
			}
			return false;
		}

		StreamClustering.stmtMap.remove(id);
		stmt.setContent(statement);

		if (statement.toLowerCase().contains("for update"))
		{
			mode = DBSeerConstants.STATEMENT_UPDATE;
		}

		// ignore this statement for now (OLTPBenchmark runs it at the end of the benchmark.)
		if (statement.toLowerCase().contains("select * from global_variables"))
		{
			mode = DBSeerConstants.STATEMENT_NONE;
		}

		for (String table : parser.getTables())
		{
			StreamClustering.addTable(table);
			stmt.addTable(table);

			int idx = StreamClustering.getTableIndex(table);
			Transaction transaction = stmt.getTransaction();
			switch (mode)
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

		StreamClustering.setMaxStatementId(id);
		return true;
	}

	public void setTerminate(boolean terminate)
	{
		this.terminate = terminate;
	}
}
