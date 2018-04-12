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

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

/**
 * Aggregates of live statistics for a single timestamp.
 *
 * Created by dyoon on 5/16/15.
 */
public class LiveAggregate
{
  public int numTransactionType;
  public double[] transactionCounts;
  public double[] totalLatencies;
  public Multimap<Integer, Long> latencies;

  public LiveAggregate()
  {
    numTransactionType = 0;
    transactionCounts = new double[LiveTransaction.MAX_TABLE];
    totalLatencies = new double[LiveTransaction.MAX_TABLE];
    latencies = ArrayListMultimap.create();
  }

  public synchronized void addTransaction(int transactionType, LiveTransaction transaction)
  {
    if (numTransactionType < transactionType + 1)
    {
      numTransactionType = transactionType + 1;
    }
    transactionCounts[transactionType]++;
    totalLatencies[transactionType] += transaction.getLatency();
    latencies.put(transactionType, transaction.getLatency());
  }
}
