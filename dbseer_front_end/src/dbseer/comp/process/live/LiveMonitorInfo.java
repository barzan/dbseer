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

package dbseer.comp.process.live;

import dbseer.comp.MatlabFunctions;

/**
 * class that stores live monitoring statistics
 *
 * Created by dyoon on 5/17/15.
 */
public class LiveMonitorInfo
{
	private static int MAX_TABLE = 200;

	private volatile int numTransactionTypes;
	private volatile long currentTimestamp;

	private volatile double globalTransactionCount;
	private String[] transactionTypeNames;
	private volatile double[] currentTPS;
	private volatile double[] currentAverageLatencies;
	private volatile double[] totalTransactionCounts;

	public static Object LOCK = new Object();

	public LiveMonitorInfo()
	{
		numTransactionTypes = 0;
		transactionTypeNames = new String[MAX_TABLE];
		currentTPS = new double[MAX_TABLE];
		currentAverageLatencies = new double[MAX_TABLE];
		totalTransactionCounts = new double[MAX_TABLE];
	}

	public void reset()
	{
		currentTimestamp = 0;
		numTransactionTypes = 0;
		globalTransactionCount = 0;
		transactionTypeNames = new String[MAX_TABLE];
		currentTPS = new double[MAX_TABLE];
		currentAverageLatencies = new double[MAX_TABLE];
		totalTransactionCounts = new double[MAX_TABLE];
	}

	public synchronized int getNumTransactionTypes()
	{
		return numTransactionTypes;
	}

	public synchronized void setNumTransactionTypes(int numTransactionTypes)
	{
		this.numTransactionTypes = numTransactionTypes;
	}

	public void setCurrentTPS(int i, double tps)
	{
		currentTPS[i] = tps;
	}

	public void setCurrentAverageLatency(int i, double latency)
	{
		currentAverageLatencies[i] = latency;
	}

	public void setTotalTransactionCount(int i, double count)
	{
		totalTransactionCounts[i] = count;
	}

	public double getTotalTransactionCount()
	{
		double sum = 0;
		for (int i = 0; i < numTransactionTypes; ++i)
		{
			sum += totalTransactionCounts[i];
		}
		return sum;
	}

	public double getCurrentTPS()
	{
		double sum = 0;
		for (int i = 0; i < numTransactionTypes; ++i)
		{
			sum += currentTPS[i];
		}
		return sum;
	}

	public double getCurrentTPS(int i)
	{
		return currentTPS[i];
	}

	public double getGlobalTransactionCount()
	{
		return globalTransactionCount;
	}

	public void setGlobalTransactionCount(double globalTransactionCount)
	{
		this.globalTransactionCount = globalTransactionCount;
	}

	public double getCurrentAverageLatency(int i)
	{
		return currentAverageLatencies[i];
	}

	public long getCurrentTimestamp()
	{
		return currentTimestamp;
	}

	public void setCurrentTimestamp(long currentTimestamp)
	{
		this.currentTimestamp = currentTimestamp;
	}
}
