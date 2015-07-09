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
 * Transaction class for live monitoring.
 *
 * Created by dyoon on 5/15/15.
 */
public class LiveTransaction
{
  public static final int MAX_TABLE = 100;

  private int id;
  private int maxTableId;
  private long latency;

  private ArrayList<LiveQuery> queries;
  private LiveTransactionLocation location;

  public LiveTransaction(int id)
  {
    this.id = id;
    maxTableId = 0;
    latency = 0;
    queries = new ArrayList<LiveQuery>();
    location = new LiveTransactionLocation();
  }

  public synchronized void addQuery(LiveQuery query)
  {
    queries.add(query);
  }

  public synchronized String getEntireStatements()
  {
    String statement = "";
    for (LiveQuery q : queries)
    {
      statement += q.getStatement();
      statement += "\n";
    }
    return statement;
  }

  public ArrayList<LiveQuery> getQueries()
  {
    return queries;
  }

  public LiveTransactionLocation getLocation()
  {
    return location;
  }

  public void addStatement(int tableId, int type)
  {
    if (tableId > location.maxTableId)
    {
      location.maxTableId = tableId;
    }
    switch (type)
    {
      case LiveTransactionLocation.SELECT:
      {
        location.numSelect[tableId]++;
        break;
      }
      case LiveTransactionLocation.UPDATE:
      {
        location.numUpdate[tableId]++;
        break;
      }
      case LiveTransactionLocation.DELETE:
      {
        location.numDelete[tableId]++;
        break;
      }
      case LiveTransactionLocation.INSERT:
      {
        location.numInsert[tableId]++;
        break;
      }
      default:
        break;
    }
  }

  public void printTransactionStat()
  {
    System.out.println("tx = ");
    for (int i = 0;i <location.maxTableId;++i)
    {
      System.out.print(String.format("%.0f,%.0f,%.0f,%.0f,",
          location.numSelect[i], location.numUpdate[i], location.numDelete[i], location.numInsert[i]));
    }
    System.out.println();
  }

  public int getMaxTableId()
  {
    return maxTableId;
  }

  public long getLatency()
  {
    return latency;
  }

  public void setLatency(long latency)
  {
    this.latency = latency;
  }
}
