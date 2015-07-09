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

package middleware;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.NavigableSet;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Created by dyoon on 5/16/15.
 */
public class LiveAggregateProcessor implements Runnable
{
  private static final long PROCESS_INTERVAL = 3000;
  private static final String LIVE_LOG_DIR = "./live/";

  private int numTransactionTypes;
  private long lastProcessedTimestamp;
  private ConcurrentSkipListMap<Long, LiveAggregate> aggregateMap;
  private LiveAggregateGlobal globalAggregate;

  private File avgLatencyFile;
  private File transactionCountFile;

//  public LiveAggregateProcessor(ConcurrentSkipListMap<Long, LiveAggregate> aggregateMap, long startTime, SharedData sharedData)
  public LiveAggregateProcessor(ConcurrentSkipListMap<Long, LiveAggregate> aggregateMap, long startTime, LiveAggregateGlobal globalAggregate)
  {
    this.numTransactionTypes = 0;
    this.lastProcessedTimestamp = startTime - 1;
    this.aggregateMap = aggregateMap;
    this.globalAggregate = globalAggregate;
  }

  @Override
  public void run()
  {
//    File liveLogDir = new File(LIVE_LOG_DIR);
//    if (!liveLogDir.exists())
//    {
//      liveLogDir.mkdirs();
//    }
//
//    avgLatencyFile = new File(LIVE_LOG_DIR + "avg_latency");
//    transactionCountFile = new File(LIVE_LOG_DIR + "trans_count");
//
//    try
//    {
//      if (avgLatencyFile.exists())
//      {
//        avgLatencyFile.delete();
//        avgLatencyFile.createNewFile();
//      }
//      if (transactionCountFile.exists())
//      {
//        transactionCountFile.delete();
//        transactionCountFile.createNewFile();
//      }
//    }
//    catch (IOException e)
//    {
//      e.printStackTrace();
//    }

    while (true)
    {
      long sleepTime = 0;
      try
      {
        while (sleepTime < PROCESS_INTERVAL)
        {
          Thread.sleep(250);
          sleepTime += 250;
          if (Thread.currentThread().isInterrupted())
          {
            return;
          }
        }
      }
      catch (InterruptedException e)
      {
//        e.printStackTrace();
        return;
      }

      long currentTimestamp = System.currentTimeMillis() / 1000L;

      long processFrom = lastProcessedTimestamp + 1;
      long processTo = currentTimestamp - 1;

      if (processFrom > processTo)
      {
        continue;
      }

      for (long time = processFrom;time <= processTo; ++time)
      {
        LiveAggregate aggregate = aggregateMap.get(time);
//        String avgLatency = "";
//        String transCount = "";
//
//        avgLatency += "\t" + time;
//        transCount += "\t" + time;

        if (aggregate == null)
        {
          for (int i = 0; i < numTransactionTypes; ++i)
          {
            globalAggregate.addTransactionCount(i, 0);
            globalAggregate.setCurrentAverageLatency(i, 0);
          }
        }
        else
        {
          if (numTransactionTypes < aggregate.numTransactionType)
          {
            numTransactionTypes = aggregate.numTransactionType;
          }
          for (int i = 0; i < numTransactionTypes; ++i)
          {
            double latency = (aggregate.transactionCounts[i] == 0) ? 0 : (aggregate.totalLatencies[i] / aggregate.transactionCounts[i]);
            globalAggregate.addTransactionCount(i, aggregate.transactionCounts[i]);
            globalAggregate.setCurrentAverageLatency(i, latency);
          }
        }

//        try
//        {
//          Files.append(avgLatency, avgLatencyFile, Charsets.UTF_8);
//          Files.append(transCount, transactionCountFile, Charsets.UTF_8);
//        }
//        catch (IOException e)
//        {
//          e.printStackTrace();
//        }
        aggregateMap.remove(time);
      }

      // remove older entries.
      ConcurrentNavigableMap<Long, LiveAggregate> oldMap = aggregateMap.headMap(processFrom);
      NavigableSet<Long> oldKeySet = oldMap.keySet();

      for (Long l : oldKeySet)
      {
        aggregateMap.remove(l);
      }
      lastProcessedTimestamp = processTo;
    }
  }
}
