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

/**
 * Query class for live monitoring
 *
 * Created by dyoon on 5/15/15.
 */
public class LiveQuery
{
  private long id;
  private long start;
  private long end;
  private long latency;
  private String statement;

  public LiveQuery(long id, long start, long end, long latency, String statement)
  {
    this.id = id;
    this.start = start;
    this.end = end;
    this.latency = latency;
    this.statement = statement;
  }

  public long getId()
  {
    return id;
  }

  public long getStart()
  {
    return start;
  }

  public long getEnd()
  {
    return end;
  }

  public long getLatency()
  {
    return latency;
  }

  public String getStatement()
  {
    return statement;
  }
}
