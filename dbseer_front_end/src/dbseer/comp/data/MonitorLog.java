package dbseer.comp.data;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by dyoon on 2014. 7. 4..
 */
public class MonitorLog
{
	private static Map<String, Integer> headers = new HashMap<String, Integer>();
	private double[] fields = null;
	private long numReadStatements = 0;
	private long numInsertStatements = 0;
	private long numUpdateStatements = 0;
	private long numDeleteStatements = 0;
//	private long timestamp = 0;

	public MonitorLog(String log)
	{
		String[] columns = log.split(",");

		fields = new double[columns.length];
		numReadStatements = 0;
		numInsertStatements = 0;
		numUpdateStatements = 0;
		numDeleteStatements = 0;

		for (int i = 0; i < columns.length; ++i)
		{
			try
			{
				fields[i] = Double.parseDouble(columns[i]);
			}
			catch (NumberFormatException e)
			{
				fields[i] = 0;
			}
		}
	}

	public static void setHeaders(String[] header)
	{
		for (int i = 0; i < header.length; ++i)
		{
			headers.put(header[i], new Integer(i));
		}
	}

	public int length()
	{
		return fields.length;
	}

	public double get(int i)
	{
		return fields[i];
	}

	public Double get(String fieldName)
	{
		Integer index = headers.get(fieldName);

		if (index == null) return null;

		return new Double(fields[index.intValue()]);
	}

	public double[] getAll()
	{
		return fields;
	}

	public long getTimestamp()
	{
		return (long)fields[0];
	}

	public void incrementReadStatement()
	{
		++numReadStatements;
	}

	public long getNumReadStatements()
	{
		return numReadStatements;
	}

	public void incrementInsertStatement()
	{
		++numInsertStatements;
	}

	public long getNumInsertStatements()
	{
		return numInsertStatements;
	}

	public void incrementUpdateStatement()
	{
		++numUpdateStatements;
	}

	public long getNumUpdateStatements()
	{
		return numUpdateStatements;
	}

	public void incrementDeleteStatement()
	{
		++numDeleteStatements;
	}

	public long getNumDeleteStatements()
	{
		return numDeleteStatements;
	}
}
