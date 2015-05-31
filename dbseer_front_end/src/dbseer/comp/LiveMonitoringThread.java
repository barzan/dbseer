package dbseer.comp;

import dbseer.comp.data.LiveMonitor;
import dbseer.gui.DBSeerExceptionHandler;
import dbseer.gui.DBSeerGUI;
import dbseer.gui.panel.DBSeerLiveMonitorPanel;
import dbseer.middleware.MiddlewareSocket;

import java.io.EOFException;
import java.io.IOException;
import java.net.SocketException;

/**
 * Created by dyoon on 5/17/15.
 */
public class LiveMonitoringThread extends Thread
{
	private int numTransactionType = 0;
	private LiveMonitor monitor;
	private MiddlewareSocket socket;
	private DBSeerLiveMonitorPanel monitorPanel;

	public LiveMonitoringThread()
	{
//		DBSeerGUI.mainFrame.resetLiveMonitoring();
		monitor = DBSeerGUI.liveMonitor;
		socket = DBSeerGUI.middlewareSocket;
		monitorPanel = DBSeerGUI.liveMonitorPanel;
	}

	@Override
	public void run()
	{
		while (true)
		{
			long sleepTime = 0;

			while (sleepTime < DBSeerGUI.liveMonitorRefreshRate * 1000)
			{
				try
				{
					Thread.sleep(250);
				}
				catch (InterruptedException e)
				{
					return;
				}
				sleepTime += 250;
				if (this.isInterrupted())
				{
					return;
				}
			}

			synchronized (LiveMonitor.LOCK)
			{
				if (socket.isConnected() && socket.isLoggedIn())
				{
					try
					{
						socket.updateLiveMonitor(monitor);
					}
					catch (SocketException e)
					{
						DBSeerExceptionHandler.handleException(new Exception("The connection to the middleware has been closed."));
						try
						{
							socket.disconnect();
						}
						catch (Exception e1)
						{
							DBSeerExceptionHandler.handleException(e1);
						}
						return;
					}
					catch (EOFException e)
					{
						DBSeerExceptionHandler.handleException(new Exception("The connection to the middleware has been closed."));
						try
						{
							socket.disconnect();
						}
						catch (Exception e1)
						{
							DBSeerExceptionHandler.handleException(e1);
						}
						return;
					}
					catch (Exception e)
					{
						DBSeerExceptionHandler.handleException(e);
						try
						{
							socket.disconnect();
						}
						catch (Exception e1)
						{
							DBSeerExceptionHandler.handleException(e1);
						}
						return;
					}
					monitorPanel.setTotalNumberOfTransactions(monitor.getGlobalTransactionCount());
					monitorPanel.setCurrentTPS(monitor.getCurrentTPS());
					numTransactionType = monitor.getNumTransactionTypes();
					for (int i = 0; i < numTransactionType; ++i)
					{
						monitorPanel.setCurrentTPS(i, monitor.getCurrentTPS(i));
						monitorPanel.setCurrentAverageLatency(i, monitor.getCurrentAverageLatency(i));
					}
				}
			}
		}
	}
}
