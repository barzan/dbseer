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

package dbseer.comp.process.live;

import dbseer.gui.DBSeerGUI;
import dbseer.gui.panel.DBSeerLiveMonitorPanel;
import dbseer.old.middleware.MiddlewareSocket;

/**
 * Created by dyoon on 5/17/15.
 */
public class LiveMonitor implements Runnable
{
	private boolean run = true;
	private int numTransactionType = 0;
	private LiveMonitorInfo monitor;
	private MiddlewareSocket socket;
	private DBSeerLiveMonitorPanel monitorPanel;


	public LiveMonitor()
	{
//		DBSeerGUI.mainFrame.resetLiveMonitoring();
		monitor = DBSeerGUI.liveMonitorInfo;
		socket = DBSeerGUI.middlewareSocket;
		monitorPanel = DBSeerGUI.liveMonitorPanel;
	}

	@Override
	public void run()
	{
		while (this.run)
		{
			long sleepTime = 0;

			while (sleepTime < DBSeerGUI.liveMonitorRefreshRate * 1000)
			{
				try
				{
					Thread.sleep(250);
					if (!this.run)
					{
						break;
					}
				}
				catch (InterruptedException e)
				{
					return;
				}
				sleepTime += 250;
			}

			if (!this.run)
			{
				break;
			}

//			synchronized (LiveMonitorInfo.LOCK)
			{
//				if (DBSeerGUI.isLiveMonitoring)
				{
					// obsolete?
//					try
//					{
//						socket.updateLiveMonitor(monitor);
//					}
//					catch (SocketException e)
//					{
//						DBSeerExceptionHandler.handleException(new Exception("The connection to the middleware has been closed."));
//						try
//						{
//							socket.disconnect();
//						}
//						catch (Exception e1)
//						{
//							DBSeerExceptionHandler.handleException(e1);
//						}
//						return;
//					}
//					catch (EOFException e)
//					{
//						DBSeerExceptionHandler.handleException(new Exception("The connection to the middleware has been closed."));
//						try
//						{
//							socket.disconnect();
//						}
//						catch (Exception e1)
//						{
//							DBSeerExceptionHandler.handleException(e1);
//						}
//						return;
//					}
//					catch (Exception e)
//					{
//						DBSeerExceptionHandler.handleException(e);
//						try
//						{
//							socket.disconnect();
//						}
//						catch (Exception e1)
//						{
//							DBSeerExceptionHandler.handleException(e1);
//						}
//						return;
//					}
//					if (!StreamClustering.getDBSCAN().isInitializing())
//					{
						monitorPanel.setTotalNumberOfTransactions(monitor.getGlobalTransactionCount());
						monitorPanel.setCurrentTPS(monitor.getCurrentTPS());
						numTransactionType = monitor.getNumTransactionTypes();
						for (int i = 0; i < numTransactionType; ++i)
						{
							monitorPanel.setCurrentTPS(monitor.getCurrentTimestamp(), i, monitor.getCurrentTPS(i));
							monitorPanel.setCurrentAverageLatency(monitor.getCurrentTimestamp(), i, monitor.getCurrentAverageLatency(i));
						}
//						if (StreamClustering.getDBSCAN().isInitialized())
						{
							monitorPanel.updateTransactionNames();
						}
//					}
				}
			}
		}
	}

	public void stopMonitoring()
	{
		this.run = false;
	}
}
