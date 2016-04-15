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

package dbseer.comp.process.system.dstat;

import dbseer.comp.process.live.LiveLogProcessor;
import dbseer.comp.process.system.SystemLogProcessor;

import java.io.PrintWriter;
import java.util.*;

/**
 * Created by Dong Young Yoon on 1/3/16.
 */
public class DstatSystemLogProcessor extends SystemLogProcessor
{
	private boolean epochWritten;
	private String[] metaHeaders;
	private String[] headers;

	public DstatSystemLogProcessor(String dir)
	{
		super(dir);
		this.epochWritten = false;
	}

	public DstatSystemLogProcessor(String dir, LiveLogProcessor liveLogProcessor)
	{
		super(dir, liveLogProcessor);
		this.epochWritten = false;
	}

	@Override
	public void handle(String log) throws Exception
	{
		// skip first few lines
		if (log.isEmpty() || log.contains("Dstat") || log.contains("Author") || log.contains("Host") || log.contains("Cmdline") || log.contains("Module"))
		{
			return;
		}

		if (log.contains("epoch") && !epochWritten)
		{
			sysWriter.write(log);
			sysWriter.write("\n");
			sysWriter.flush();

			if (log.contains("load avg"))
			{
				log = log.replaceAll("\"", "");
				metaHeaders = log.split(",");
			}
			else
			{
				log = log.replaceAll("\"", "");
				headers = log.split(",");
			}

			if (headers != null && metaHeaders != null)
			{
				writeHeader();
				epochWritten = true;
			}
		}
		else
		{
			if (log.contains("epoch"))
			{
				return;
			}
			if (liveLogProcessor.getTxStartTime() == 0)
			{
				return;
			}
			else if (liveLogProcessor.getTxStartTime() > 0 && liveLogProcessor.getSysStartTime() == 0)
			{
				String[] data = log.split(",", 2);
				long timestamp = (long) Double.parseDouble(data[0]);
				liveLogProcessor.setSysStartTime(timestamp+1);
				return;
			}
			sysWriter.write(log);
			sysWriter.write("\n");
			sysWriter.flush();
		}
	}

	private boolean writeHeader() throws Exception
	{
		int         interruptIndex   = 0;
		int         memoryUsageIndex = 0;
		int         swapFreeIndex    = 0;
		int         memoryFreeIndex  = 0;
		int         virtualIndex     = 0;
		int         fileSystemIndex  = 0;
		int         swapIndex        = 0;
		int         pagingIndex      = 0;
		int         procsIndex       = 0;
		int         diskReadCount    = 0;
		int         diskWriteCount   = 0;
		int         ioReadCount      = 0;
		int         ioWriteCount     = 0;
		int         netRecvCount     = 0;
		int         netSendCount     = 0;
		int         utilCount        = 0;
		PrintWriter writer           = headerWriter;

		List<Integer>        diskIndexes  = new ArrayList<Integer>();
		List<Integer>        ioIndexes    = new ArrayList<Integer>();
		List<Integer>        utilIndexes  = new ArrayList<Integer>();
		List<Integer> netIndexes   = new ArrayList<Integer>();

		Set<Integer> cpuUsrSet    = new LinkedHashSet<Integer>();
		Set<Integer>         cpuSysSet    = new LinkedHashSet<Integer>();
		Set<Integer>         cpuWaiSet    = new LinkedHashSet<Integer>();
		Set<Integer>         cpuIdlSet    = new LinkedHashSet<Integer>();
		Set<Integer>         cpuSiqSet    = new LinkedHashSet<Integer>();
		Set<Integer>         cpuHiqSet    = new LinkedHashSet<Integer>();

		Set<Integer>         netSendSet   = new LinkedHashSet<Integer>();
		Set<Integer>         netRecvSet   = new LinkedHashSet<Integer>();
		Map<String, Integer> interruptMap = new TreeMap<String, Integer>();

		// write columns
		writer.print("columns = struct(");

		// get location of interrupt/dsk/io/util columns
		for (int i = 0; i < metaHeaders.length; ++i)
		{
			if (metaHeaders[i].equalsIgnoreCase("interrupts"))
			{
				interruptIndex = i;
			}
			else if (metaHeaders[i].contains("dsk"))
			{
				diskIndexes.add(i);
			}
			else if (metaHeaders[i].contains("io"))
			{
				ioIndexes.add(i);
			}
			else if (metaHeaders[i].contains("net"))
			{
				netIndexes.add(i);
			}
			else if (metaHeaders[i].matches("[a-z]d[a-z][0-9]*"))
			{
				utilIndexes.add(i);
			}
			else if (metaHeaders[i].equalsIgnoreCase("memory usage"))
			{
				memoryUsageIndex = i;
			}
			else if (metaHeaders[i].equalsIgnoreCase("swap"))
			{
				swapIndex = i;
			}
			else if (metaHeaders[i].equalsIgnoreCase("virtual memory"))
			{
				virtualIndex = i;
			}
			else if (metaHeaders[i].equalsIgnoreCase("filesystem"))
			{
				fileSystemIndex = i;
			}
			else if (metaHeaders[i].equalsIgnoreCase("paging"))
			{
				pagingIndex = i;
			}
			else if (metaHeaders[i].equalsIgnoreCase("procs"))
			{
				procsIndex = i;
			}
		}

		// get interrupt columns.
		while (interruptIndex < headers.length)
		{
			if (headers[interruptIndex].matches("[0-9]+"))
			{
				interruptMap.put(headers[interruptIndex], interruptIndex+1);
				++interruptIndex;
			}
			else
			{
				break;
			}
		}

		// write 'columns' variable.
		for (int i = 0; i < headers.length; ++i)
		{
			String header = headers[i];

			// store cpu & network info separately
			if (header.equalsIgnoreCase("usr"))
			{
				writer.print("'cpu" + cpuUsrSet.size() + "_usr'," + (i + 1));
				cpuUsrSet.add(i + 1);
			}
			else if (header.equalsIgnoreCase("sys"))
			{
				writer.print("'cpu" + cpuSysSet.size() + "_sys'," + (i + 1));
				cpuSysSet.add(i + 1);
			}
			else if (header.equalsIgnoreCase("idl"))
			{
				writer.print("'cpu" + cpuIdlSet.size() + "_idl'," + (i + 1));
				cpuIdlSet.add(i + 1);
			}
			else if (header.equalsIgnoreCase("wai"))
			{
				writer.print("'cpu" + cpuWaiSet.size() + "_wai'," + (i + 1));
				cpuWaiSet.add(i + 1);
			}
			else if (header.equalsIgnoreCase("hiq"))
			{
				writer.print("'cpu" + cpuHiqSet.size() + "_hiq'," + (i + 1));
				cpuHiqSet.add(i + 1);
			}
			else if (header.equalsIgnoreCase("siq"))
			{
				writer.print("'cpu" + cpuSiqSet.size() + "_siq'," + (i + 1));
				cpuSiqSet.add(i + 1);
			}
			else if (header.equalsIgnoreCase("send"))
			{
				writer.print("'net" + netSendSet.size() + "_send'," + (i + 1));
				netSendSet.add(i + 1);
			}
			else if (header.equalsIgnoreCase("recv"))
			{
				writer.print("'net" + netRecvSet.size() + "_recv'," + (i + 1));
				netRecvSet.add(i+1);
			}
			else if (header.matches("[0-9]+")) // interrupts
			{
				writer.print("'interrupts_" + header + "'," + (i + 1));
			}
			else if (header.matches("[0-9]+m")) // 1m, 5m, 15m..
			{
				writer.print("'load_" + header + "'," + (i + 1));
			}
			else if (header.equalsIgnoreCase("#aio"))
			{
				writer.print("'aio'," + (i+1));
			}
			else if (i >= memoryUsageIndex && i <= memoryUsageIndex + 3)
			{
				writer.print("'memory_" + header + "'," + (i + 1));
			}
			else if (i >= swapIndex && i <= swapIndex + 1)
			{
				writer.print("'swap_" + header + "'," + (i + 1));
			}
			else if (i >= virtualIndex && i <= virtualIndex + 3)
			{
				writer.print("'virtual_" + header + "'," + (i + 1));
			}
			else if (i >= fileSystemIndex && i <= fileSystemIndex + 1)
			{
				writer.print("'filesystem_" + header + "'," + (i + 1));
			}
			else if (i >= pagingIndex && i <= pagingIndex + 1)
			{
				writer.print("'paging_" + header + "'," + (i + 1));
			}
			else if (i >= procsIndex && i <= procsIndex + 2)
			{
				writer.print("'procs_" + header + "'," + (i + 1));
			}
			else if (header.equalsIgnoreCase("read"))
			{
				if (diskIndexes.contains(i))
				{
					writer.print("'dsk_" + header);
					if (diskReadCount > 0) writer.print(diskReadCount + "',");
					else writer.print("',");
					writer.print(i+1);
					++diskReadCount;
				}
				else if (ioIndexes.contains(i))
				{
					writer.print("'io_" + header);
					if (ioReadCount > 0) writer.print(ioReadCount + "',");
					else writer.print("',");
					writer.print(i+1);
					++ioReadCount;
				}
			}
			else if (header.equalsIgnoreCase("writ"))
			{
				if (diskIndexes.contains(i-1))
				{
					writer.print("'dsk_" + header);
					if (diskWriteCount > 0) writer.print(diskWriteCount + "',");
					else writer.print("',");
					writer.print(i+1);
					++diskWriteCount;
				}
				else if (ioIndexes.contains(i-1))
				{
					writer.print("'io_" + header);
					if (ioWriteCount > 0) writer.print(ioWriteCount + "',");
					else writer.print("',");
					writer.print(i+1);
					++ioWriteCount;
				}
			}
			else if (header.equalsIgnoreCase("util"))
			{
				writer.print("'" + header);
				if (utilCount > 0) writer.print(utilCount + "',");
				else writer.print("',");
				writer.print(i+1);
				++utilCount;
			}
			else
			{
				writer.print("'" + headers[i].replaceAll("\\.","").replaceAll(" ", "_") + "'," + (i + 1)); // remove dots & spaces
			}

			if (i == headers.length - 1) writer.println(");");
			else writer.print(",");
		}

		// write 'interrupts' variable
		writer.print("interrupts = struct(");
		Iterator<Map.Entry<String,Integer>> it = interruptMap.entrySet().iterator();
		while (it.hasNext())
		{
			Map.Entry<String, Integer> entry = it.next();
			writer.print("'i" + entry.getKey() + "'," + entry.getValue());

			if (it.hasNext()) writer.print(",");
			else writer.println(");");
		}

		// write 'metadata' variable
		writer.print("metadata = struct(");
		writer.print("'cpu_siq',[");
		writeMetadataVector(writer, cpuSiqSet.iterator());

		writer.print("'cpu_usr',[");
		writeMetadataVector(writer, cpuUsrSet.iterator());

		writer.print("'cpu_idl',[");
		writeMetadataVector(writer, cpuIdlSet.iterator());

		writer.print("'cpu_wai',[");
		writeMetadataVector(writer, cpuWaiSet.iterator());

		writer.print("'cpu_sys',[");
		writeMetadataVector(writer, cpuSysSet.iterator());

		writer.print("'cpu_hiq',[");
		writeMetadataVector(writer, cpuHiqSet.iterator());

		writer.print("'net_send',[");
		writeMetadataVector(writer, netSendSet.iterator());

		writer.print("'net_recv',[");
		writeMetadataVector(writer, netRecvSet.iterator());

		writer.println("'interrupts',interrupts, 'num_net'," + netRecvSet.size() + ",'num_cpu'," +
				cpuUsrSet.size() + ");");

		writer.println(String.format("header = struct('name','%s','dbms','mysql','columns',columns,'metadata',metadata);", this.name));

		writer.print("extra  = struct('disk',[");
		for (int i = 0; i < diskIndexes.size(); ++i)
		{
			writer.print(diskIndexes.get(i) + 1);
			if (i == diskIndexes.size() - 1) writer.print("],");
			else writer.print(" ");
		}
		writer.print("'io',[");
		for (int i = 0; i < ioIndexes.size(); ++i)
		{
			writer.print(ioIndexes.get(i) + 1);
			if (i == ioIndexes.size() - 1) writer.print("],");
			else writer.print(" ");
		}
		writer.print("'util',[");
		for (int i = 0; i < utilIndexes.size(); ++i)
		{
			writer.print(utilIndexes.get(i) + 1);
			if (i == utilIndexes.size() - 1) writer.print("]");
			else writer.print(" ");
		}
		writer.println(");");

		writer.flush();
		writer.close();

		return true;
	}

	private void writeMetadataVector(PrintWriter writer, Iterator<Integer> it)
	{
		while (it.hasNext())
		{
			int value = it.next().intValue();
			writer.print(value);

			if (it.hasNext()) writer.print(" ");
			else writer.print("],");
		}
	}
}
