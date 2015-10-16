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

package dbseer.comp.clustering;

import dbseer.comp.data.Transaction;

import java.util.Collection;

/**
 * Created by dyoon on 9/28/15.
 */
public class DenStreamRunner implements Runnable
{
	private DenStream denstream;
	private int initPoints = 1000;

	public DenStreamRunner()
	{
//		denstream = new DenStream(0.25, 16, 10, 10, 0.2, 0.9, 5);
		denstream = StreamClustering.denstream;
	}

	@Override
	public void run()
	{
		while (true)
		{
			if (!denstream.isInitialized())
			{
				if (StreamClustering.trxMap.values().size() > initPoints)
				{
					synchronized (StreamClustering.LOCK)
					{
						denstream.initialDBSCAN(StreamClustering.trxMap.values());
						StreamClustering.stmtMap.clear();
						StreamClustering.trxMap.clear();
					}
				}
				else
				{
					try
					{
						Thread.sleep(250);
					}
					catch (InterruptedException e)
					{
						e.printStackTrace();
					}
				}
			}
			else
			{
				Collection<Transaction> transactions = StreamClustering.trxMap.values();
				for (Transaction t : transactions)
				{
					synchronized (StreamClustering.LOCK)
					{
						denstream.train(t);
						StreamClustering.trxMap.values().remove(t);
					}
				}
				try
				{
					Thread.sleep(100);
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
		}
	}
}
