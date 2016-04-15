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

package dbseer.gui.user;

import org.apache.commons.io.input.ReversedLinesFileReader;

import java.io.File;
import java.io.IOException;

/**
 * Created by Dong Young Yoon on 4/7/16.
 */
public class DBSeerDataSetPath
{
	private String name;
	private String header;
	private String monitor;
	private String avgLatency;
	private String prcLatency;
	private String txCount;
	private String root;

	public DBSeerDataSetPath()
	{
		name = "";
		header = "";
		monitor = "";
		avgLatency = "";
		prcLatency = "";
		txCount = "";
		root = "";
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getRoot()
	{
		return root;
	}

	public void setRoot(String root)
	{
		this.root = root;
	}

	public String getHeader()
	{
		return header;
	}

	public void setHeader(String header)
	{
		this.header = header;
	}

	public String getMonitor()
	{
		return monitor;
	}

	public void setMonitor(String monitor)
	{
		this.monitor = monitor;
	}

	public String getAvgLatency()
	{
		return avgLatency;
	}

	public void setAvgLatency(String avgLatency)
	{
		this.avgLatency = avgLatency;
	}

	public String getPrcLatency()
	{
		return prcLatency;
	}

	public void setPrcLatency(String prcLatency)
	{
		this.prcLatency = prcLatency;
	}

	public String getTxCount()
	{
		return txCount;
	}

	public void setTxCount(String txCount)
	{
		this.txCount = txCount;
	}

	public int getNumTransactionType()
	{
		if (this.avgLatency.isEmpty())
		{
			return 0;
		}

		// use the last line of average latency file to get num tx types.
		File avgLatencyFile = new File(this.avgLatency);
		if (avgLatencyFile == null || !avgLatencyFile.exists() || avgLatencyFile.length() == 0)
		{
			return 0;
		}

		try
		{
			ReversedLinesFileReader reverseFileReader = new ReversedLinesFileReader(avgLatencyFile);
			String line = reverseFileReader.readLine(); // read last line.
			String[] tokens = line.trim().split("\\s+");
			reverseFileReader.close();
			return (tokens.length - 1);
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return 0;
		}
	}

	public boolean hasEmptyPath()
	{
		if (this.header.isEmpty() ||
				this.monitor.isEmpty() ||
				this.avgLatency.isEmpty() ||
//				this.prcLatency.isEmpty() ||
				this.txCount.isEmpty() ||
				this.root.isEmpty())
		{
			return true;
		}
		return false;
	}
}
