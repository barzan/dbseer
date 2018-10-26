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
 * Location of each LiveTransaction in the distance space.
 *
 * Created by dyoon on 5/16/15.
 */
public class LiveTransactionLocation
{
  public static final int NONE = 0;
  public static final int SELECT = 1;
  public static final int INSERT = 2;
  public static final int DELETE = 3;
  public static final int UPDATE = 4;

  public static final double DIFF_SCALE = 10000.0;
  public static final double EPS = 100.0;

  public double numSelect[];
  public double numInsert[];
  public double numDelete[];
  public double numUpdate[];

  public int maxTableId;

  public LiveTransactionLocation()
  {
    numSelect = new double[LiveTransaction.MAX_TABLE];
    numInsert = new double[LiveTransaction.MAX_TABLE];
    numDelete = new double[LiveTransaction.MAX_TABLE];
    numUpdate = new double[LiveTransaction.MAX_TABLE];

    maxTableId = 0;
  }

  public double getDistance(LiveTransactionLocation other)
  {
    int maxId = 0;

    if (this.maxTableId > other.maxTableId)
    {
      maxId = this.maxTableId;
    }
    else
    {
      maxId = other.maxTableId;
    }

    double distance = 0;
    double scale = 1;

    for (int i=0;i<maxId;++i)
    {
      if (this.numSelect[i] == 0 || other.numSelect[i] == 0) scale = DIFF_SCALE;
      else scale = 1;
      distance += Math.pow(this.numSelect[i] - other.numSelect[i], 2) * scale;

      if (this.numInsert[i] == 0 || other.numInsert[i] == 0) scale = DIFF_SCALE;
      else scale = 1;
      distance += Math.pow(this.numInsert[i] - other.numInsert[i], 2) * scale;

      if (this.numDelete[i] == 0 || other.numDelete[i] == 0) scale = DIFF_SCALE;
      else scale = 1;
      distance += Math.pow(this.numDelete[i] - other.numDelete[i], 2) * scale;

      if (this.numUpdate[i] == 0 || other.numUpdate[i] == 0) scale = DIFF_SCALE;
      else scale = 1;
      distance += Math.pow(this.numUpdate[i] - other.numUpdate[i], 2) * scale;
    }
    distance = Math.sqrt(distance);

    return distance;
  }

  public void multiply(double multiplier)
  {
    for (int i=0;i<maxTableId;++i)
    {
      this.numSelect[i] *= multiplier;
      this.numUpdate[i] *= multiplier;
      this.numDelete[i] *= multiplier;
      this.numInsert[i] *= multiplier;
    }
  }

  public void divide(double divider)
  {
    for (int i=0;i<maxTableId;++i)
    {
      this.numSelect[i] /= divider;
      this.numUpdate[i] /= divider;
      this.numDelete[i] /= divider;
      this.numInsert[i] /= divider;
    }
  }

  public void add(LiveTransactionLocation other)
  {
    if (other.maxTableId > this.maxTableId)
    {
      this.maxTableId = other.maxTableId;
    }
    for (int i=0;i<maxTableId;++i)
    {
      this.numSelect[i] += other.numSelect[i];
      this.numUpdate[i] += other.numUpdate[i];
      this.numDelete[i] += other.numDelete[i];
      this.numInsert[i] += other.numInsert[i];
    }
  }

  public String getString()
  {
    String str = "";
    for (int i=0;i<maxTableId;++i)
    {
      str += "{";
      str += this.numSelect[i] + ",";
      str += this.numInsert[i] + ",";
      str += this.numUpdate[i] + ",";
      str += this.numDelete[i] + "}";
      str += ",";
    }
    return str;
  }
}
