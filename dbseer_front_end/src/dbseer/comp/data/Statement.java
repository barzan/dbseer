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

import dbseer.gui.DBSeerConstants;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by dyoon on 2014. 7. 5..
 */
public class Statement
{
	private int id;
	private int txId;
	private long startTime;
	private long endTime;
	private long latency;
	private int mode;
	private Set<String> tables;
	private String content;
	private long fileOffset;

	private long queryOffset;
	private Transaction transaction = null;

	public Statement()
	{
		mode = DBSeerConstants.STATEMENT_NONE;
		tables = new HashSet<String>();
	}

	public void printAll()
	{
		System.out.print("Statement: (");
		System.out.print(id + ", ");
		System.out.print(startTime + ", ");
		System.out.print(endTime + ", ");
		System.out.print(latency + ")");
		System.out.println();
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

	public int getMode()
	{
		return mode;
	}

	public void setMode(int mode)
	{
		this.mode = mode;
	}

	public Set<String> getTables()
	{
		return tables;
	}

	public void addTable(String table)
	{
		tables.add(table);
	}

	public String getContent()
	{
		return content;
	}

	public void setContent(String content)
	{
		this.content = content;
	}

	public long getFileOffset()
	{
		return fileOffset;
	}

	public void setFileOffset(long fileOffset)
	{
		this.fileOffset = fileOffset;
	}
	public long getQueryOffset()
	{
		return queryOffset;
	}

	public void setQueryOffset(long queryOffset)
	{
		this.queryOffset = queryOffset;
	}

	public int getTxId()
	{
		return txId;
	}

	public void setTxId(int txId)
	{
		this.txId = txId;
	}

	public Transaction getTransaction()
	{
		return transaction;
	}

	public void setTransaction(Transaction transaction)
	{
		this.transaction = transaction;
	}
}

