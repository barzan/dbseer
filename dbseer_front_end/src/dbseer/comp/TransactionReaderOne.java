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

package dbseer.comp;

import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.StringTokenizer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Dong Young Yoon on 3/25/16.
 */
public class TransactionReaderOne implements TransactionReader
{
	// old implementation
	public static final int NOT_STARTED = 0;
	public static final int FETCHING = 1;
	public static final int FETCHED = 2;
	public static final int DONE = 3;

	private int fetchStatus = 0;

	private String transactionFilePath;
	private String queryFilePath;
	private String statementFilePath;
	private String statementOffsetPath;
	private String lastUser;
	private String transaction;
	private String currentUser;

	private int time;
	private int txIndex;

	private long lastLatency;
	private long lastId;

	private long currentLatency;
	private long currentId;

	private RandomAccessFile transactionFile = null;
	private RandomAccessFile queryFile = null;
	private RandomAccessFile statementFile = null;
	private RandomAccessFile statementOffsetFile = null;

	private ArrayList<Long> txIds;
	private ArrayList<Long> minOffsets;
	private ArrayList<Long> maxOffsets;
	private ArrayList<Long> minQueryOffsets;
	private ArrayList<Long> maxQueryOffsets;

	private TransactionFetchThread fetchThread;
	private final Lock lock = new ReentrantLock();
	private final Condition isFetched = lock.newCondition();

	public TransactionReaderOne()
	{
	}

	public TransactionReaderOne(String transactionFilePath, String queryFilePath, String statementFilePath, String statementOffsetPath, int time)
	{
		this.transactionFilePath = transactionFilePath;
		this.queryFilePath = queryFilePath;
		this.statementFilePath = statementFilePath;
		this.statementOffsetPath = statementOffsetPath;
		this.time = time;

//		this.fetchThread = new TransactionFetchThread(this);
	}

	public boolean initialize()
	{
		txIndex = 0;
		txIds = new ArrayList<Long>();
		minOffsets = new ArrayList<Long>();
		maxOffsets = new ArrayList<Long>();
		minQueryOffsets = new ArrayList<Long>();
		maxQueryOffsets = new ArrayList<Long>();

		try
		{
			transactionFile = new RandomAccessFile(new File(transactionFilePath), "r");
			queryFile = new RandomAccessFile(new File(queryFilePath), "r");
			statementFile = new RandomAccessFile(new File(statementFilePath), "r");
			statementOffsetFile = new RandomAccessFile(new File(statementOffsetPath), "r");

			int t = 1;

			while (t < time)
			{
				statementOffsetFile.readLine();
				++t;
			}
			String offsets = statementOffsetFile.readLine();

			StringTokenizer tokenizer = new StringTokenizer(offsets, ",");

			String timeToken = tokenizer.nextToken();
			if (Integer.parseInt(timeToken) != this.time - 1)
			{
				JOptionPane.showMessageDialog(null, "An error has occurred while initializing a statement reader",
						"Error", JOptionPane.ERROR_MESSAGE);
				return false;
			}
			String txIdString, minOffsetString, maxOffsetString, offset;
			while (tokenizer.hasMoreTokens())
			{
				txIdString = tokenizer.nextToken();
				if (txIdString.isEmpty()) break;
				txIds.add(Long.parseLong(txIdString));

				minOffsetString = tokenizer.nextToken();
				if (minOffsetString.isEmpty()) break;
				minOffsets.add(Long.parseLong(minOffsetString));

				maxOffsetString = tokenizer.nextToken();
				if (maxOffsetString.isEmpty()) break;
				maxOffsets.add(Long.parseLong(maxOffsetString));

				offset = tokenizer.nextToken();
				if (offset.isEmpty()) break;
				minQueryOffsets.add(Long.parseLong(offset));

				offset = tokenizer.nextToken();
				if (offset.isEmpty()) break;
				maxQueryOffsets.add(Long.parseLong(offset));
			}
		}
		catch (FileNotFoundException e)
		{
//			e.printStackTrace();
			return false;
		}
		catch (IOException e)
		{
//			e.printStackTrace();
			return false;
		}
		fetchStatus = FETCHING;
		fetchThread = new TransactionFetchThread(this);
		fetchThread.start();
		return true;
	}

	public String getLastTransactionUser()
	{
		return lastUser;
	}

	public long getLastTransactionId()
	{
		return lastId;
	}

	public long getLastTransactionLatency()
	{
		return lastLatency;
	}

	public String getNextTransaction()
	{
		String currentTransaction = "";
		lock.lock();
		if (fetchStatus == DONE)
		{
			lock.unlock();
			return "";
		}
		try
		{
			while (fetchStatus == NOT_STARTED || fetchStatus == FETCHING)
			{
				isFetched.await();
			}
			currentTransaction = transaction;
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		finally
		{
			lock.unlock();
		}

		currentId = lastId;
		currentLatency = lastLatency;
		currentUser = lastUser;

		fetchThread = new TransactionFetchThread(this);
		fetchThread.start();

		return currentTransaction;
	}

	public void fetchTransaction()
	{
		lock.lock();
		fetchStatus = FETCHING;
		if (txIndex == txIds.size())
		{
			transaction = "";
			fetchStatus = DONE;
			isFetched.signal();
			lock.unlock();
			return;
		}

		long txId = txIds.get(txIndex);
		long minOffset = minOffsets.get(txIndex);
		long maxOffset = maxOffsets.get(txIndex);
		long minQueryOffset = minQueryOffsets.get(txIndex);
		long maxQueryOffset = maxQueryOffsets.get(txIndex);
		long currentQueryOffset = minQueryOffset;
		long currentOffset = minOffset;
		String content = "";

		lastLatency = 0;
		lastId = txId;
		lastUser = "";

		ArrayList<Long> statementIds = new ArrayList<Long>();
//		PriorityQueue<QueryOffset> queryOffsetIndex = queryOffsetMap.get(queryFilePath);

		try
		{
			transactionFile.seek(0);
			String txInfo = transactionFile.readLine();

			while (txInfo != null)
			{
				String[] columns = txInfo.split(",");
				if (txId == Long.parseLong(columns[0]))
				{
					lastUser = columns[2];
					lastLatency = Long.parseLong(columns[5]);
					break;
				}
				txInfo = transactionFile.readLine();
			}

			statementFile.seek(minOffset);
			while (currentOffset <= maxOffset)
			{
				String stmt = statementFile.readLine();
				String[] columns = stmt.split(",");
				currentOffset = statementFile.getFilePointer();

				long currentTxId = Long.parseLong(columns[0]);
				long currentStmtId = Long.parseLong(columns[2]);

				if (currentTxId != txId)
				{
					continue;
				}

				statementIds.add(currentStmtId);
			}

			if (statementIds.isEmpty())
			{
				transaction = "";
				fetchStatus = DONE;
				isFetched.signal();
				lock.unlock();
				return;
//				return "";
			}

			Collections.sort(statementIds);

			long nextId;

//			QueryOffset[] queryOffsetIndexArray = queryOffsetIndex.toArray(new QueryOffset[0]);
//			Arrays.sort(queryOffsetIndexArray, new QueryOffsetComparator());

			queryFile.seek(minQueryOffset);

			for (int i = 0; i < statementIds.size(); ++i)
			{
				nextId = statementIds.get(i).longValue();

				String line = queryFile.readLine();
				currentQueryOffset = queryFile.getFilePointer();
				long id = Long.parseLong(line.substring(0,line.indexOf(",")));
				while (id != nextId && currentQueryOffset < maxQueryOffset)
				{
					line = queryFile.readLine();
					currentQueryOffset = queryFile.getFilePointer();
					id = Long.parseLong(line.substring(0,line.indexOf(",")));
				}

				if (id == nextId)
				{
					String statementContent = line.substring(line.indexOf(",")+1);
					statementContent = statementContent.replaceAll("\0", "\n");
					if (statementContent.contains("commit") || statementContent.contains("COMMIT") || statementContent.contains("SET SESSION") ||
							statementContent.contains("rollback") || statementContent.contains("ROLLBACK"))
					{
						continue;
					}
					content += statementContent;
				}
				else
				{
					queryFile.seek(minQueryOffset);
				}
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			++txIndex;
			transaction = content;
			fetchStatus = FETCHED;
			isFetched.signal();
			lock.unlock();
		}
	}

	public synchronized int getFetchStatus()
	{
		return fetchStatus;
	}

	public String getCurrentUser()
	{
		return currentUser;
	}

	public long getCurrentLatency()
	{
		return currentLatency;
	}

	public long getCurrentId()
	{
		return currentId;
	}
}
