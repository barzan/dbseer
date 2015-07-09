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

import java.util.ArrayList;

/**
 * global aggregate for live monitoring.
 *
 * Created by dyoon on 5/17/15.
 */
public class LiveAggregateGlobal
{
  public int numTransactionType;
//  public double[] totalTransactionCounts;
//  public double[] currentAverageLatencies;
//  public double[] currentTransactionCounts;
//	public ArrayList<Double> totalTransactionCounts;
//  public ArrayList<Double> currentAverageLatencies;
//  public ArrayList<Double> currentTransactionCounts;
  public ArrayList<LiveTransactionStatistic> transactionStatistics;
  public double totalTransactionCount;
  private Object lock;

  public LiveAggregateGlobal()
  {
//    numTransactionType = 0;
//    totalTransactionCounts = new double[LiveTransaction.MAX_TABLE];
//    currentAverageLatencies = new double[LiveTransaction.MAX_TABLE];
//    currentTransactionCounts = new double[LiveTransaction.MAX_TABLE];
//    totalTransactionCounts = new ArrayList<Double>();
//    currentAverageLatencies = new ArrayList<Double>();
//    currentTransactionCounts = new ArrayList<Double>();
    lock = new Object();
    transactionStatistics = new ArrayList<LiveTransactionStatistic>();
    totalTransactionCount = 0;
  }

  public void addTransactionCount(int type, double count)
  {
//    if (numTransactionType < type + 1)
//    {
//      numTransactionType = type + 1;
//    }
//    currentTransactionCounts[type] = count;
//    totalTransactionCounts[type] += count;
    synchronized (lock)
    {
      while (transactionStatistics.size() < type + 1)
      {
        transactionStatistics.add(new LiveTransactionStatistic());
      }
      transactionStatistics.get(type).currentTransactionCounts = count;
      transactionStatistics.get(type).totalTransactionCounts += count;
      totalTransactionCount += count;
    }
  }

  public synchronized void addExample(int type, String example)
  {
    synchronized (lock)
    {
      while (transactionStatistics.size() < type + 1)
      {
        transactionStatistics.add(new LiveTransactionStatistic());
      }
      transactionStatistics.get(type).examples.add(example);
    }
  }

  public synchronized void setCurrentAverageLatency(int type, double latency)
  {
//    if (numTransactionType < type + 1)
//    {
//      numTransactionType = type + 1;
//    }
//    currentAverageLatencies[type] = latency;
    synchronized (lock)
    {
      while (transactionStatistics.size() < type + 1)
      {
        transactionStatistics.add(new LiveTransactionStatistic());
      }
      transactionStatistics.get(type).currentAverageLatency = latency;
    }
  }

  public synchronized int getNumTransactionType()
  {
    return transactionStatistics.size();
  }

  public synchronized void removeTransactionType(int index)
  {
    synchronized (lock)
    {
      transactionStatistics.remove(index);
    }
  }
}
