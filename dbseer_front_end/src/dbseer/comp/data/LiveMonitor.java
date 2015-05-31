package dbseer.comp.data;

import dbseer.comp.MatlabFunctions;

/**
 * class for live monitoring statistics
 *
 * Created by dyoon on 5/17/15.
 */
public class LiveMonitor
{
	private static int MAX_TABLE = 200;

	private int numTransactionTypes;

	private double globalTransactionCount;
	private String[] transactionTypeNames;
	private double[] currentTPS;
	private double[] currentAverageLatencies;
	private double[] totalTransactionCounts;

	public static Object LOCK = new Object();

	public LiveMonitor()
	{
		numTransactionTypes = 0;
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
}
