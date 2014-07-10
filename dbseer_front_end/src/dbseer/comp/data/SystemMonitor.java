package dbseer.comp.data;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dyoon on 2014. 7. 4..
 */
public class SystemMonitor
{
	private File monitorFile = null;
	private List<MonitorLog> monitorLogs;
	private String[] headers = null;

	private long startTimestamp = 0;
	private long endTimestamp = 0;

	private static final int HEADER_LINES_TO_IGNORE = 5;

	public SystemMonitor()
	{
		monitorLogs = new ArrayList<MonitorLog>();
	}

	public boolean parseMonitorFile(File file)
	{
		monitorFile = file;

		try
		{
			BufferedReader br = new BufferedReader(new FileReader(monitorFile));
			String line = null;
			int linesSkipped = 0;

			// handle headers;

			while ( (line = br.readLine()) != null && linesSkipped < HEADER_LINES_TO_IGNORE)
			{
				linesSkipped++;
			}

			line = br.readLine();
			if (line == null)
			{
				System.out.println("Empty header");
				return false;
			}
			line = line.replaceAll("\"", ""); // remove all double quotations.
			headers = line.split(",");
			MonitorLog.setHeaders(headers);

			br.readLine(); // ignore first record.

			while ( (line = br.readLine()) != null )
			{
				MonitorLog log = new MonitorLog(line);
				monitorLogs.add(log);
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

		startTimestamp = monitorLogs.get(0).getTimestamp();
		endTimestamp = monitorLogs.get(monitorLogs.size()-1).getTimestamp();

		return true;
	}

	public MonitorLog getLog(long timestamp)
	{
		if (timestamp < startTimestamp || timestamp > endTimestamp)
		{
			return null;
		}

		long index = timestamp - startTimestamp;

		return monitorLogs.get((int)index);
	}

	public List<MonitorLog> getLogs(long startTime, long endTime)
	{
		if (startTime < startTimestamp || startTime > endTimestamp)
		{
			return null;
		}

		if (endTime < startTimestamp || endTime > endTimestamp + 1)
		{
			return null;
		}

		List<MonitorLog> logs = monitorLogs.subList((int)(startTime - startTimestamp), (int)(endTime - startTimestamp));

		return logs;
	}

	public String[] getHeaders()
	{
		return headers;
	}

	public List<MonitorLog> getLogs()
	{
		return monitorLogs;
	}

	public long getStartTimestamp()
	{
		return startTimestamp;
	}

	public long getEndTimestamp()
	{
		return endTimestamp;
	}
}
