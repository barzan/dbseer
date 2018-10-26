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

package dbseer.comp.data;

import dbseer.comp.clustering.Cluster;
import dbseer.comp.clustering.StreamClustering;
import dbseer.gui.DBSeerConstants;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by dyoon on 2014. 7. 5..
 */
public class Transaction
{
	public static final int UNCLASSIFIED = 0;
	public static final int NOISE = 1;
	public static final int CLASSIFIED = 2;
	
	public static final double DIFF_SCALE = 10000.0;
	private static int idCount = 0;

	private int id;
	private int classification;
	private int type;
	private Cluster cluster;

	private String serverName;
	private String hostName;

	private long startTime;
	private long endTime;
	private long latency; // in milliseconds
	private int port;
	private int maxIdx;
	private String user;
	private List<Statement> statements;
	private Map<Integer, Statement> statementMap;
	private long[] numRowsRead;
//	private long[] numRowsWritten;
	private long[] numRowsInserted;
	private long[] numRowsUpdated;
	private long[] numRowsDeleted;

	private long[] numSelect;
	private long[] numInsert;
	private long[] numUpdate;
	private long[] numDelete;

	private long minStatementOffset;
	private long maxStatementOffset;
	private long lastStatementId;

	private long minQueryOffset;
	private long maxQueryOffset;

	private List<Integer> tableAccessed;
	private List<Integer> typeAccessed;

	boolean visited; // used in DBSCAN
	boolean assignedToCluster;

	public static volatile int numTable;

	public Transaction()
	{
		cluster = null;
		classification = Transaction.UNCLASSIFIED;
		statements = new ArrayList<Statement>();
		statementMap = new HashMap<>();
		visited = false;
		assignedToCluster = false;

		tableAccessed = new ArrayList<Integer>();
		typeAccessed = new ArrayList<Integer>();

		minStatementOffset = Long.MAX_VALUE;
		maxStatementOffset = Long.MIN_VALUE;
		lastStatementId = 0;
		maxIdx = 0;
		id = idCount++;
	}

	public int getType()
	{
		return type;
	}

	public void setType(int type)
	{
		this.type = type;
	}


	public void printAll()
	{
		System.out.print("Transaction: (");
		System.out.print(id + ", ");
		System.out.print(startTime + ", ");
		System.out.print(endTime + ", ");
		System.out.print(latency + ")");
		System.out.println();
		for (int i = 0; i < numRowsRead.length; ++i)
		{
//			System.out.print("(" + numRowsRead[i] + ", ");
//			System.out.print(numRowsInserted[i] + ", ");
//			System.out.print(numRowsUpdated[i] + ", ");
//			System.out.print(numRowsDeleted[i] + "), ");

			System.out.print("(" + numSelect[i] + ", ");
			System.out.print(numInsert[i] + ", ");
			System.out.print(numUpdate[i] + ", ");
			System.out.print(numDelete[i] + "), ");

		}
		System.out.println();
	}

	public boolean contains(long time)
	{
		if (time >= startTime && time <= endTime)
		{
			return true;
		}
		return false;
	}

	public String getEntireStatement()
	{
		String stmt = "";
		for (Statement s : statements)
		{
			String content = s.getContent();
			if (content == null)
			{
				return null;
			}
			if (content.contains("commit") || content.contains("COMMIT") || content.contains("SET SESSION") ||
					content.contains("rollback") || content.contains("ROLLBACK"))
			{
				continue;
			}
			stmt = stmt + content + "\n";
		}
		return stmt;
	}

	public Map<Integer, Statement> getStatementMap()
	{
		return statementMap;
	}

	public void addStatement(Statement stmt)
	{
		stmt.setTxId(this.id);
		long offset = stmt.getFileOffset();
		if (offset > maxStatementOffset)
		{
			maxStatementOffset = offset;
		}
		if (offset < minStatementOffset)
		{
			minStatementOffset = offset;
		}
		statements.add(stmt);
		statementMap.put(stmt.getId(), stmt);
	}

	public void updateQueryMinMaxOffset()
	{
		if (statements.isEmpty())
		{
			return;
		}

		minQueryOffset = statements.get(0).getQueryOffset();
		maxQueryOffset = statements.get(0).getQueryOffset();

		for (Statement s : statements)
		{
			long offset = s.getQueryOffset();
			if (offset > maxQueryOffset)
			{
				maxQueryOffset = offset;
			}
			if (offset < minQueryOffset)
			{
				minQueryOffset = offset;
			}
		}
	}

	public List<Statement> getStatements()
	{
		return statements;
	}

	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id = id;
	}

	public long getStartTime()
	{
		return startTime;
	}

	public void setStartTime(long startTime)
	{
		this.startTime = startTime;
	}

	public long getEndTime()
	{
		return endTime;
	}

	public void setEndTime(long endTime)
	{
		this.endTime = endTime;
	}

	public long getLatency()
	{
		return latency;
	}

	public void setLatency(long latency)
	{
		this.latency = latency;
	}

	public int getPort()
	{
		return port;
	}

	public void setPort(int port)
	{
		this.port = port;
	}

	public String getUser()
	{
		return user;
	}

	public void setUser(String user)
	{
		this.user = user;
	}

	public void setNumTable(int num)
	{
		numRowsRead = new long[num];
		numRowsInserted = new long[num];
		numRowsUpdated = new long[num];
		numRowsDeleted = new long[num];

		numSelect = new long[num];
		numInsert = new long[num];
		numUpdate = new long[num];
		numDelete = new long[num];

		for (int i = 0; i < num; ++i)
		{
			numRowsRead[i] = 0;
			numRowsInserted[i] = 0; // initialization
			numRowsUpdated[i] = 0;
			numRowsDeleted[i] = 0;

			numSelect[i] = 0;
			numInsert[i] = 0;
			numUpdate[i] = 0;
			numDelete[i] = 0;
		}
	}

	public void setRows(int idx, long numRead, long numInserted, long numUpdated, long numDeleted)
	{
		numRowsRead[idx] = numRead;
		numRowsInserted[idx] = numInserted;
		numRowsUpdated[idx] = numUpdated;
		numRowsDeleted[idx] = numDeleted;
	}

	public void addRows(int idx, long numRead, long numInserted, long numUpdated, long numDeleted)
	{
		numRowsRead[idx] += numRead;
		numRowsInserted[idx] += numInserted;
		numRowsUpdated[idx] += numUpdated;
		numRowsDeleted[idx] += numDeleted;
	}

	public void addSelect(int idx)
	{
		if (idx > maxIdx)
		{
			maxIdx = idx;
		}
		numSelect[idx]++;
		tableAccessed.add(idx);
		typeAccessed.add(DBSeerConstants.STATEMENT_READ);
	}

	public void addInsert(int idx)
	{
		if (idx > maxIdx)
		{
			maxIdx = idx;
		}
		numInsert[idx]++;
		tableAccessed.add(idx);
		typeAccessed.add(DBSeerConstants.STATEMENT_INSERT);
	}

	public void addUpdate(int idx)
	{
		if (idx > maxIdx)
		{
			maxIdx = idx;
		}
		numUpdate[idx]++;
		tableAccessed.add(idx);
		typeAccessed.add(DBSeerConstants.STATEMENT_UPDATE);
	}

	public void addDelete(int idx)
	{
		if (idx > maxIdx)
		{
			maxIdx = idx;
		}
		numDelete[idx]++;
		tableAccessed.add(idx);
		typeAccessed.add(DBSeerConstants.STATEMENT_DELETE);
	}

	public double[] toDoubleArray()
	{
		int numTable = Transaction.numTable;
		double[] val = new double[4 * numTable];

		for (int i = 0; i < numTable; ++i)
		{
			val[i * 4] = numSelect[i];
			val[i * 4 + 1] = numInsert[i];
			val[i * 4 + 2] = numDelete[i];
			val[i * 4 + 3] = numUpdate[i];
		}
		return val;
	}

	public long[] getNumRowsRead()
	{
		return numRowsRead;
	}

	public long[] getNumRowsInserted()
	{
		return numRowsInserted;
	}

	public long[] getNumRowsUpdated()
	{
		return numRowsUpdated;
	}

	public long[] getNumRowsDeleted()
	{
		return numRowsDeleted;
	}

	public long[] getNumSelect()
	{
		return numSelect;
	}

	public long[] getNumInsert()
	{
		return numInsert;
	}

	public long[] getNumUpdate()
	{
		return numUpdate;
	}

	public long[] getNumDelete()
	{
		return numDelete;
	}

	public List<Integer> getTableAccessed()
	{
		return tableAccessed;
	}

	public List<Integer> getTypeAccessed()
	{
		return typeAccessed;
	}

	public boolean isNoRowsReadWritten()
	{
		for (int i = 0; i <= maxIdx; ++i)
		{
//			if (numRowsRead[i] != 0 || numRowsInserted[i] != 0 ||
//					numRowsUpdated[i] != 0 || numRowsDeleted[i] != 0)
			if (numSelect[i] != 0 || numInsert[i] != 0 ||
					numUpdate[i] != 0 || numDelete[i] != 0)
			{
				return false;
			}
		}
		return true;
	}

	public double getEuclideanDistance(Transaction other)
	{
		long[] otherNumRowsRead = other.getNumRowsRead();
		long[] otherNumRowsInserted = other.getNumRowsInserted();
		long[] otherNumRowsUpdated = other.getNumRowsUpdated();
		long[] otherNumRowsDeleted = other.getNumRowsDeleted();

		long[] otherNumSelect = other.getNumSelect();
		long[] otherNumInsert = other.getNumInsert();
		long[] otherNumUpdate = other.getNumUpdate();
		long[] otherNumDelete = other.getNumDelete();

//		List<Integer> otherTableAccessed = other.getTableAccessed();
//		List<Integer> otherTypeAccessed = other.getTypeAccessed();
//
//		int shortLength = tableAccessed.size() < otherTableAccessed.size() ? tableAccessed.size() : otherTableAccessed.size();
//		int longLength = tableAccessed.size() > otherTableAccessed.size() ? tableAccessed.size() : otherTableAccessed.size();

		double scale = 1.0;
		double distance = 0.0;

		int maxIndex = (maxIdx > other.maxIdx) ? maxIdx : other.maxIdx;

		for (int i = 0; i <= maxIndex; ++i)
		{
			if (numSelect[i] == 0 || otherNumSelect[i] == 0) scale = DIFF_SCALE;
			else scale = 1.0;
			distance += Math.pow(numSelect[i] - otherNumSelect[i], 2.0) * scale;
			if (numInsert[i] == 0 || otherNumInsert[i] == 0) scale = DIFF_SCALE;
			else scale = 1.0;
			distance += Math.pow(numInsert[i] - otherNumInsert[i], 2.0) * scale;
			if (numUpdate[i] == 0 || otherNumUpdate[i] == 0) scale = DIFF_SCALE;
			else scale = 1.0;
			distance += Math.pow(numUpdate[i] - otherNumUpdate[i], 2.0) * scale;
			if (numDelete[i] == 0 || otherNumDelete[i] == 0) scale = DIFF_SCALE;
			else scale = 1.0;
			distance += Math.pow(numDelete[i] - otherNumDelete[i], 2.0) * scale;

//			long numRead = numSelect[i];
//			long numReadOther = otherNumSelect[i];
//			long numWrite = numDelete[i] + numInsert[i] + numUpdate[i];
//			long numWriteOther = otherNumDelete[i] + otherNumInsert[i] + otherNumUpdate[i];
//			long readDist = numRead - numReadOther;
//			long writeDist = numWrite - numWriteOther;
//			distance += readDist * readDist;
//			distance += writeDist * writeDist;

		}
		distance = Math.sqrt(distance);
//		if (distance > 2.44 && distance < 2.45)
//		{
//			System.out.println("2.44 = " + this.getId() + ", " + other.getId());
//		}
		return distance;
	}

	public boolean isVisited()
	{
		return visited;
	}

	public void setVisited(boolean visited)
	{
		this.visited = visited;
	}

	public boolean isAssignedToCluster()
	{
		return assignedToCluster;
	}

	public void setAssignedToCluster(boolean assignedToCluster)
	{
		this.assignedToCluster = assignedToCluster;
	}

	public int getClassification()
	{
		return classification;
	}

	public void setClassification(int classification)
	{
		this.classification = classification;
	}

	public Cluster getCluster()
	{
		return cluster;
	}

	public void setCluster(Cluster cluster)
	{
		this.cluster = cluster;
	}

	public long getMinStatementOffset()
	{
		return minStatementOffset;
	}

	public void setMinStatementOffset(long minStatementOffset)
	{
		this.minStatementOffset = minStatementOffset;
	}

	public long getMaxStatementOffset()
	{
		return maxStatementOffset;
	}

	public void setMaxStatementOffset(long maxStatementOffset)
	{
		this.maxStatementOffset = maxStatementOffset;
	}

	public long getMinQueryOffset()
	{
		return minQueryOffset;
	}

	public void setMinQueryOffset(long minQueryOffset)
	{
		this.minQueryOffset = minQueryOffset;
	}

	public long getMaxQueryOffset()
	{
		return maxQueryOffset;
	}

	public long getLastStatementId()
	{
		return lastStatementId;
	}

	public String getServerName()
	{
		return serverName;
	}

	public void setServerName(String serverName)
	{
		this.serverName = serverName;
	}

	public String getHostName()
	{
		return hostName;
	}

	public void setHostName(String hostName)
	{
		this.hostName = hostName;
	}

	public void setLastStatementId(long lastStatementId)
	{
		this.lastStatementId = lastStatementId;
	}
}
