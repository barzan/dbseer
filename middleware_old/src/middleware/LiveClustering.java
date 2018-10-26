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
import java.util.ArrayList;

/**
 * Responsible for clustering incoming transactions.
 *
 * Created by dyoon on 5/16/15.
 */
public class LiveClustering
{
  private ArrayList<Integer> clusterSizes;
  private ArrayList<LiveTransactionLocation> centroids;

  public LiveClustering()
  {
    centroids = new ArrayList<LiveTransactionLocation>();
    clusterSizes = new ArrayList<Integer>();
  }

  public synchronized int clusterTransaction(LiveTransaction transaction)
  {
    LiveTransactionLocation currentLocation = transaction.getLocation();

    // first transaction
    if (centroids.size() == 0)
    {
      centroids.add(currentLocation);
      clusterSizes.add(1);

      return 0;
    }

    double minDistance = Double.POSITIVE_INFINITY;
    int minDistanceCentroidIndex = -1;
    int idx = 0;
    for (LiveTransactionLocation centroid : centroids)
    {
      double distance = currentLocation.getDistance(centroid);
      if (distance < minDistance)
      {
        minDistance = distance;
        minDistanceCentroidIndex = idx;
      }
      ++idx;
    }

    // cluster found.
    if (minDistance < LiveTransactionLocation.EPS)
    {
      // add current transaction to the cluster.
      LiveTransactionLocation centroid = centroids.get(minDistanceCentroidIndex);
      int currentClusterSize = clusterSizes.get(minDistanceCentroidIndex);

      centroid.multiply(currentClusterSize);
      centroid.add(currentLocation);
      centroid.divide(currentClusterSize+1);
      clusterSizes.set(minDistanceCentroidIndex, currentClusterSize+1);

      return minDistanceCentroidIndex;
    }
    // transaction is far from every cluster. Let's create a new cluster.
    else
    {
      int index = 0;
      for (LiveTransactionLocation centroid : centroids)
      {
        double distance = currentLocation.getDistance(centroid);
        ++index;
      }
      int newIndex = centroids.size();
      centroids.add(currentLocation);
      clusterSizes.add(1);

      return newIndex;
    }
  }

  public synchronized void removeCluster(int index)
  {
    if (index + 1 >= clusterSizes.size())
    {
      return;
    }
    clusterSizes.remove(index);
    centroids.remove(index);
  }
}
