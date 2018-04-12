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

import com.google.common.collect.EvictingQueue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Class that handles everything for live monitoring.
 *
 * Created by dyoon on 5/15/15.
 */
public class LiveMonitor
{
  private static final int MAX_SAMPLE = 30;

//  private ArrayList<EvictingQueue<String>> transactionSamplesList;
  private ConcurrentHashMap<Integer, LiveTransaction> transactionMap;
  private ConcurrentHashMap<String, Integer> tableMap;
  private ConcurrentSkipListMap<Long, LiveAggregate> aggregateMap;

  private Object monitorLock;

  private LiveClustering clustering;

  public LiveAggregateGlobal globalAggregate;

  public LiveMonitor()
  {
//    transactionSamplesList = new ArrayList<EvictingQueue<String>>();
    transactionMap = new ConcurrentHashMap<Integer, LiveTransaction>();
    tableMap = new ConcurrentHashMap<String, Integer>();
    monitorLock = new Object();
    aggregateMap = new ConcurrentSkipListMap<Long, LiveAggregate>();
    clustering = new LiveClustering();
    globalAggregate = new LiveAggregateGlobal();
  }

  public void addQuery(int txId, long queryId, long start, long end, long latency, String stmt)
  {
    LiveTransaction transaction;
    LiveQuery newQuery = new LiveQuery(queryId, start, end, latency, stmt);

    if (stmt.contains("commit") || stmt.contains("COMMIT") || stmt.contains("SET SESSION") ||
        stmt.contains("rollback") || stmt.contains("ROLLBACK") || stmt.contains("sql_mode"))
    {
      return;
    }

    if (transactionMap.containsKey(txId))
    {
      transaction = transactionMap.get(txId);
    }
    else
    {
      transaction = new LiveTransaction(txId);
      transactionMap.put(txId, transaction);
    }

    transaction.addQuery(newQuery);
  }

  public void endTransaction(int txId, String userId, long start, long end, long latency)
  {
    LiveTransaction transaction = transactionMap.get(txId);
    if (transaction == null)
    {
//      System.err.println(String.format("Live transaction with id %d not found. Something is wrong.", txId));
      return;
    }

    // perform clustering.
    transaction.setLatency(latency);
    List<LiveQuery> queries = transaction.getQueries();
    SQLStatementParser parser = new SQLStatementParser();

    for (LiveQuery query : queries)
    {
      int type = parser.parseStatement(query.getStatement());
      if (type == -1)
      {
        transactionMap.remove(txId);
        return;
      }
      List<String> tables = parser.getTables();
      for (String table : tables)
      {
        int tableId;
        if (!tableMap.containsKey(table))
        {
          tableId = tableMap.size();
          tableMap.put(table, tableId);
        }
        else
        {
          tableId = tableMap.get(table);
        }
//        System.out.println(String.format("%s - %d - %d", table, tableId, type));
        transaction.addStatement(tableId, type);
      }
    }

//    transaction.printTransactionStat();
    int transactionType = 0;
    synchronized (monitorLock)
    {
		  transactionType = clustering.clusterTransaction(transaction);
//      while (transactionSamplesList.size() < transactionType + 1)
//      {
//        transactionSamplesList.add(EvictingQueue.<String>create(MAX_SAMPLE));
//      }
//      EvictingQueue<String> transactionSamples = transactionSamplesList.get(transactionType);
//      transactionSamples.add(transaction.getEntireStatements());
      globalAggregate.addExample(transactionType, transaction.getEntireStatements());
    }

    // aggregate result.
    if (!aggregateMap.containsKey(end))  // aggregate does not exist for the timestamp.
    {
      aggregateMap.put(end, new LiveAggregate());
    }
    LiveAggregate aggregate = aggregateMap.get(end);
    aggregate.addTransaction(transactionType, transaction);

    transactionMap.remove(txId);
  }

  public ConcurrentSkipListMap<Long, LiveAggregate> getAggregateMap()
  {
    return aggregateMap;
  }

  public void removeTransactionType(int type)
  {
    synchronized (monitorLock)
    {
      globalAggregate.transactionStatistics.remove(type);
      clustering.removeCluster(type);
    }
  }

  public String[] getTransactionSamples(int type)
  {
    String[] samples = null;
    synchronized (monitorLock)
    {
      if (type + 1 > globalAggregate.transactionStatistics.size())
      {
        return null;
      }
//      EvictingQueue<String> transactionSamples = transactionSamplesList.get(type);
      EvictingQueue<String> transactionSamples = globalAggregate.transactionStatistics.get(type).examples;
      samples = new String[transactionSamples.size()];
      transactionSamples.toArray(samples);
    }
    return samples;
  }
}
