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

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dyoon on 2014. 7. 4..
 */
public class SystemMonitor
{
	private File monitorFile = null;
	private File processedMonitorFile = null;
	private List<MonitorLog> monitorLogs;
	private String[] headers = null;
	private String[] metaHeaders = null;
	private PrintWriter writer = null;

	private long startTimestamp = 0;
	private long endTimestamp = 0;

	private static final int HEADER_LINES_TO_IGNORE = 4;

	public SystemMonitor()
	{
		monitorLogs = new ArrayList<MonitorLog>();
	}

	public boolean parseMonitorFile(File file, File processFile)
	{
		monitorFile = file;

		try
		{
			writer = new PrintWriter(new BufferedWriter(new FileWriter(processFile)));
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
				System.out.println("Empty metaheader");
				return false;
			}
			writer.println(line);
			line = line.replaceAll("\"", ""); // remove all double quotations.
			metaHeaders = line.split(",");

			line = br.readLine();
			if (line == null)
			{
				System.out.println("Empty header");
				return false;
			}
			writer.println(line);
			line = line.replaceAll("\"", ""); // remove all double quotations.
			headers = line.split(",");
			MonitorLog.setHeaders(headers);

			br.readLine(); // ignore first record.

			while ( (line = br.readLine()) != null )
			{
				writer.println(line);
				MonitorLog log = new MonitorLog(line);
				monitorLogs.add(log);
			}

			writer.flush();
			writer.close();
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
		if (startTime < startTimestamp || startTime > endTimestamp || startTime - startTimestamp >= monitorLogs.size())
		{
			return null;
		}

		if (endTime < startTimestamp)// || endTime > endTimestamp + 1)
		{
			return null;
		}

		long end = endTime - startTimestamp;

		if ((endTime-startTimestamp) > monitorLogs.size())
		{
			end = monitorLogs.size();
		}

		List<MonitorLog> logs = monitorLogs.subList((int)(startTime - startTimestamp), (int)(end));

		return logs;
	}

	public String[] getHeaders()
	{
		return headers;
	}

	public String[] getMetaHeaders()
	{
		return metaHeaders;
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
