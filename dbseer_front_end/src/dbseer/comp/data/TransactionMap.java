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

import com.google.common.collect.HashMultimap;

import java.util.HashMap;
import java.util.Set;

/**
 * Created by dyoon on 10/1/15.
 */
public class TransactionMap
{
	private HashMultimap<Long, Transaction> map;
	private HashMap<Long, String> syslogMap;
	private long minEndTime;
	private long maxEndTime;
	private long count = 0;
	private volatile long minSysLogTime;
	private volatile long lastSysLogTime;

	public TransactionMap()
	{
		map = HashMultimap.create();
		syslogMap = new HashMap<Long, String>();
		minEndTime = Long.MAX_VALUE;
		maxEndTime = Long.MIN_VALUE;
		minSysLogTime = Long.MAX_VALUE;
		lastSysLogTime = 0;
	}

	public synchronized void addSysLog(long time, String log)
	{
		if (time < minSysLogTime) minSysLogTime = time;
		lastSysLogTime = time;
		syslogMap.put(time, log);
	}

	public synchronized String getSysLog(long time)
	{
		return syslogMap.get(time);
	}

	public synchronized void add(Transaction t)
	{
		long endTime = t.getEndTime();
		map.put(endTime, t);
		if (endTime < minEndTime)
		{
			minEndTime = endTime;
		}
		if (endTime > maxEndTime)
		{
			maxEndTime = endTime;
		}
		++count;
	}

	public synchronized long getCount()
	{
		return map.size();
	}

	public synchronized void clear()
	{
		syslogMap.clear();
		minEndTime = Long.MAX_VALUE;
		maxEndTime = Long.MIN_VALUE;
	}

	public synchronized boolean isMapEmpty()
	{
		return map.isEmpty();
	}

	public synchronized Set<Transaction> pollTransactions(long time)
	{
		return map.removeAll(time);
	}

	public synchronized long getMinEndTime()
	{
		return minEndTime;
	}

	public synchronized long getMaxEndTime()
	{
		return maxEndTime;
	}

	public long getMinSysLogTime()
	{
		return minSysLogTime;
	}

	public long getLastSysLogTime()
	{
		return lastSysLogTime;
	}
}
