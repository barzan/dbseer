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

package dbseer.old.middleware;

import dbseer.comp.data.TransactionMap;
import dbseer.comp.process.live.LiveMonitorInfo;
import dbseer.comp.process.live.LiveMonitor;
import dbseer.comp.clustering.ClusterRunnable;
import dbseer.comp.clustering.IncrementalDBSCAN;
import dbseer.comp.clustering.StreamClustering;
import dbseer.comp.data.Transaction;
import dbseer.comp.live.LiveSystemLogTailerListener;
import dbseer.comp.live.LiveTransactionProcessor;
import dbseer.gui.DBSeerConstants;
import dbseer.gui.DBSeerExceptionHandler;
import dbseer.gui.DBSeerGUI;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListener;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by dyoon on 2014. 6. 29..
 */
public class MiddlewareSocket
{
	private static final int MIDDLEWARE_CONNECT_TIMEOUT = 5000; // 5sec
	private static final int MIDDLEWARE_SOCKET_TIMEOUT = 0; // No timeout.
	private static final int PACKET_LOGIN_REQUEST = 100;
	private static final int PACKET_LOGIN_SUCCESS = 101;
	private static final int PACKET_LOGIN_FAILURE = 102;
	private static final int PACKET_LOGOUT = 103;
	private static final int PACKET_START_MONITORING = 200;
	private static final int PACKET_START_MONITORING_SUCCESS = 201;
	private static final int PACKET_START_MONITORING_FAILURE = 202;
	private static final int PACKET_STOP_MONITORING = 300;
	private static final int PACKET_STOP_MONITORING_SUCCESS = 301;
	private static final int PACKET_STOP_MONITORING_FAILURE = 302;
	private static final int PACKET_STOP_MONITORING_WITH_NO_DATA = 303;
	private static final int PACKET_ASK_MONITORING = 400;
	private static final int PACKET_IS_NOT_MONITORING = 401;
	private static final int PACKET_IS_MONITORING = 402;
	private static final int PACKET_REQUEST_LIVE_MONITORING = 500;
	private static final int PACKET_IS_NOT_LIVE_MONITORING = 501;
	private static final int PACKET_IS_LIVE_MONITORING = 502;
	private static final int PACKET_REQUEST_TRANSACTION_SAMPLE = 600;
	private static final int PACKET_TRANSACTION_SAMPLE_NOT_AVAILABLE = 601;
	private static final int PACKET_TRANSACTION_SAMPLE_AVAILABLE = 602;
	private static final int PACKET_REMOVE_TRANSACTION_TYPE = 700;
	private static final int PACKET_REMOVE_TRANSACTION_SUCCESS = 701;
	private static final int PACKET_REMOVE_TRANSACTION_FAILURE = 702;

	private static final int MAX_PACKET_LENGTH = 64 * 1024; // 64k
	private Socket socket;
	private DataInputStream input;
	private DataOutputStream output;

	private String id;
	private String password;
	private String ip;
	private int port;

	private String errorMessage = "";
	private boolean isConnected = false;
	private boolean isLoggedIn = false;
	private Object lock;

	private TransactionMap txMap;

//	private ExecutorService clusteringExecutor = null;
//	private ExecutorService transactionProcessorExecutor = null;
	private IncrementalLogReceiver logReceiver = null;
	private LogParser logParser = null;
	private LiveTransactionProcessor liveTransactionProcessor = null;
	private ClusterRunnable clusterRunnable = null;
	private Tailer sysLogTailer = null;

	private LiveMonitor liveMonitor = null;
	private Thread liveTransactionProcessorThread = null;
	private Thread clusteringThread = null;
	private Thread incrementalLogThread = null;
	private Thread logParserThread = null;
	private Thread sysLogTailerThread = null;

	private File logFile = null;

	public MiddlewareSocket()
	{
		txMap = new TransactionMap();
		socket = new Socket();
		lock = new Object();
	}

	public synchronized boolean connect(String ip, int port) throws IOException
	{
		this.ip = ip;
		this.port = port;
		socket = new Socket();
		InetSocketAddress address = new InetSocketAddress(ip, port);
		try
		{
			socket.connect(address, MIDDLEWARE_CONNECT_TIMEOUT);
			socket.setSoTimeout(MIDDLEWARE_SOCKET_TIMEOUT);
			input = new DataInputStream(socket.getInputStream());
			output = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
			isConnected = true;
		}
		catch (Exception e)
		{
			DBSeerExceptionHandler.handleException(e);
			return false;
		}
		return true;
	}

	public synchronized void disconnect() throws IOException
	{
		synchronized (this.lock)
		{
			output.writeInt(PACKET_LOGOUT);
			output.writeLong(0);
			output.flush();

			this.ip = "";
			this.port = 0;
			input.close();
			output.close();
			socket.close();
			input = null;
			output = null;
			socket = null;
		}
		isConnected = false;
		isLoggedIn = false;

		stopMonitoringProcesses(true);
		DBSeerGUI.liveMonitorPanel.reset();
		DBSeerGUI.middlewarePanel.setLogout();
		DBSeerGUI.middlewareStatus.setText("Middleware: Not Connected");
	}

	public synchronized boolean login(String id, String password) throws IOException, InterruptedException
	{
		this.id = id;
		this.password = password;

		synchronized (this.lock)
		{
			output.writeInt(PACKET_LOGIN_REQUEST);
			String data = "ID=" + id + " PW=" + password;
			output.writeLong(data.length());
			output.write(data.getBytes());
			output.flush();

			int packetId = input.readInt();
			long packetLength = input.readLong();
			byte[] packetData = new byte[MAX_PACKET_LENGTH];

			int readBytes = 0;

			while (readBytes < packetLength)
			{
				readBytes += input.read(packetData, readBytes, (int) packetLength - readBytes);
			}

			if (packetId == PACKET_LOGIN_SUCCESS)
			{
				isLoggedIn = true;
				return true;
			}
			else if (packetId == PACKET_LOGIN_FAILURE)
			{
				errorMessage = "Login failed: invalid credentials - " + new String(packetData);
			}
			else
			{
				errorMessage = "Login failed: invalid packet. (ID = " + packetId + ")";
			}
			isLoggedIn = false;
			return false;
		}
	}

	public synchronized boolean startMonitoring() throws IOException
	{
		if (output == null)
		{
			errorMessage = "Middleware not connected.";
			return false;
		}
		synchronized (this.lock)
		{
			output.writeInt(PACKET_START_MONITORING);
			output.writeLong(0);
			output.flush();

			int packetId = input.readInt();
			long packetLength = input.readLong();
			byte[] packetData = new byte[(int) packetLength];

			int readBytes = 0;

			while (readBytes < packetLength)
			{
				readBytes += input.read(packetData, readBytes, (int) packetLength - readBytes);
			}

			if (packetId == PACKET_START_MONITORING_SUCCESS)
			{
				// clear live dataset directory
				File liveDir = new File(DBSeerGUI.userSettings.getDBSeerRootPath() + File.separator + DBSeerConstants.LIVE_DATASET_PATH);
				if (liveDir.exists() && liveDir.isDirectory())
				{
					FileUtils.cleanDirectory(liveDir);
				}

				startMonitoringProcesses(true);

				return true;
			}
			else if (packetId == PACKET_START_MONITORING_FAILURE)
			{
				errorMessage = "Failed to start monitoring.";
			}
			else if (packetId == PACKET_LOGIN_FAILURE)
			{
				errorMessage = String.valueOf(packetData);
			}
			else
			{
				errorMessage = "Start monitoring failed: invalid packet. (ID = " + packetId + ")";
			}
			return false;
		}
	}

	public synchronized boolean stopMonitoring(boolean getData) throws IOException
	{
		if (output == null)
		{
			errorMessage = "Middleware not connected.";
			return false;
		}

		synchronized (this.lock)
		{
			if (getData)
			{
				output.writeInt(PACKET_STOP_MONITORING);
				output.writeLong(0);
				output.flush();
			}
			else
			{
				output.writeInt(PACKET_STOP_MONITORING_WITH_NO_DATA);
				output.writeLong(0);
				output.flush();
				return true;
			}

			int packetId = input.readInt();
			long packetLength = input.readLong();
//			byte[] packetData = new byte[8192];
//			int readBytes = 0;
//
//			logFile = new File(DBSeerGUI.userSettings.getDBSeerRootPath() + File.separator +
//					DBSeerConstants.LIVE_DATASET_PATH +
//					File.separator + "dstat_log.zip");
//
//			FileOutputStream fos = new FileOutputStream(logFile);
//
//			while (readBytes < packetLength)
//			{
//				int len;
//				if (packetLength - readBytes < 8192)
//				{
//					len = (int)(packetLength - readBytes);
//				}
//				else
//				{
//					len = 8192;
//				}
//				readBytes += input.read(packetData, 0, len);
//				fos.write(packetData, 0, len);
//			}
//			fos.close();

			if (packetId == PACKET_STOP_MONITORING_SUCCESS)
			{
				stopMonitoringProcesses(false);
				return true;
			}
			else if (packetId == PACKET_STOP_MONITORING_FAILURE)
			{
				errorMessage = "Failed to stop monitoring";
			}
			else
			{
				errorMessage = "Start monitoring failed: invalid packet. (ID = " + packetId + ")";
			}
			return false;
		}
	}

	public synchronized boolean isMonitoring(boolean reconnect) throws IOException, EOFException
	{
		if (output == null)
		{
			errorMessage = "Middleware not connected.";
			return false;
		}

		synchronized (this.lock)
		{
			output.writeInt(PACKET_ASK_MONITORING);
			output.writeLong(0);
			output.flush();

			int packetId = input.readInt();
			long packetLength = input.readLong();
			byte[] packetData = new byte[(int) packetLength];
			int readBytes = 0;

			while (readBytes < packetLength)
			{
				readBytes += input.read(packetData, readBytes, (int) packetLength - readBytes);
			}

			if (packetId == PACKET_IS_MONITORING)
			{
//				if (!DBSeerGUI.liveMonitor.isAlive())
//				{
//					DBSeerGUI.liveMonitor = new LiveMonitor();
//					DBSeerGUI.liveMonitor.start();
//				}
				if (reconnect)
				{
					startMonitoringProcesses(false);
				}
				return true;
			}
			else if (packetId == PACKET_IS_NOT_MONITORING)
			{
				return false;
			}
			else
			{
				errorMessage = "Start monitoring failed: invalid packet. (ID = " + packetId + ")";
			}
			return false;
		}
	}

	public synchronized boolean updateLiveMonitor(LiveMonitorInfo monitor) throws IOException
	{
		if (output == null)
		{
			errorMessage = "Middleware not connected.";
			return false;
		}

		synchronized (this.lock)
		{

			output.writeInt(PACKET_REQUEST_LIVE_MONITORING);
			output.writeLong(0);
			output.flush();

			int response = input.readInt();
			if (response == PACKET_IS_NOT_LIVE_MONITORING)
			{
				return true;
			}
			else
			{
				int numTransactionType = input.readInt();
				monitor.setNumTransactionTypes(numTransactionType);
				double totalTransactionCount = input.readDouble();
				monitor.setGlobalTransactionCount(totalTransactionCount);
				for (int i = 0; i < numTransactionType; ++i)
				{
					double tps = input.readDouble();
					double latency = input.readDouble();
					double count = input.readDouble();
					monitor.setCurrentTPS(i, tps);
					monitor.setCurrentAverageLatency(i, latency);
					monitor.setTotalTransactionCount(i, count);
				}
				return true;
			}
		}
	}

	public synchronized String requestTransactionSample(int type, int index) throws IOException
	{
		if (output == null || !isLoggedIn)
		{
			errorMessage = "Middleware not connected.";
			return null;
		}

		synchronized (this.lock)
		{

			output.writeInt(PACKET_REQUEST_TRANSACTION_SAMPLE);
			output.writeLong(0);
			output.writeInt(type);
			output.writeInt(index);
			output.flush();

			int response = input.readInt();
			if (response == PACKET_TRANSACTION_SAMPLE_NOT_AVAILABLE)
			{
				return null;
			}
			long len = input.readLong();
			byte[] data = new byte[(int) len];

			int readBytes = 0;

			while (readBytes < len)
			{
				readBytes += input.read(data, readBytes, (int) len - readBytes);
			}

			return new String(data);
		}
	}

	public synchronized boolean removeTransactionType(int type) throws IOException
	{
		if (output == null || !isLoggedIn)
		{
			errorMessage = "Middleware not connected.";
			return false;
		}

		synchronized (this.lock)
		{
			output.writeInt(PACKET_REMOVE_TRANSACTION_TYPE);
			output.writeLong(0);
			output.writeInt(type);
			output.flush();

			int response = input.readInt();
			if (response == PACKET_REMOVE_TRANSACTION_SUCCESS)
			{
				int newSize = input.readInt();
				DBSeerGUI.liveMonitorInfo.setNumTransactionTypes(newSize);
				return true;
			}
			else if (response == PACKET_REMOVE_TRANSACTION_FAILURE)
			{
				return false;
			}
			else
			{
				errorMessage = "Invalid response: " + response;
				return false;
			}
		}
	}

	private boolean startMonitoringProcesses(boolean newStart)
	{
		if (newStart)
		{
			DBSeerGUI.liveMonitorInfo.reset();
		}
		StreamClustering.LOCK = new ReentrantLock();
		logReceiver = new IncrementalLogReceiver(this.ip, DBSeerGUI.userSettings.getDBSeerRootPath() + File.separator + DBSeerConstants.LIVE_DATASET_PATH);
		incrementalLogThread = new Thread(logReceiver);
		incrementalLogThread.start();

		logParser = new LogParser(txMap);
		logParserThread = new Thread(logParser);
		logParserThread.start();

		if (newStart || StreamClustering.getDBSCAN() == null)
		{
			IncrementalDBSCAN dbscan = new IncrementalDBSCAN(DBSeerConstants.DBSCAN_MIN_PTS, Math.sqrt(Transaction.DIFF_SCALE)/5, DBSeerConstants.DBSCAN_INIT_PTS);
			StreamClustering.setDBSCAN(dbscan);
		}

		clusterRunnable = new ClusterRunnable();
		clusteringThread = new Thread(clusterRunnable);
		clusteringThread.start();

		File sysLogFile = new File(DBSeerGUI.userSettings.getDBSeerRootPath() + File.separator +
				DBSeerConstants.LIVE_DATASET_PATH + File.separator + "log_exp_1.csv");
		File monitorFile = new File(DBSeerGUI.userSettings.getDBSeerRootPath() + File.separator +
				DBSeerConstants.LIVE_DATASET_PATH + File.separator + "monitor");
		File datasetHeaderFile = new File(DBSeerGUI.userSettings.getDBSeerRootPath() + File.separator +
				DBSeerConstants.LIVE_DATASET_PATH + File.separator + "dataset_header.m");
		PrintWriter monitorWriter = null;
		try
		{
			monitorWriter = new PrintWriter(new FileWriter(monitorFile, true));
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return false;
		}

		liveTransactionProcessor = new LiveTransactionProcessor(txMap, monitorWriter);
		liveTransactionProcessorThread = new Thread(liveTransactionProcessor);
		liveTransactionProcessorThread.start();

		TailerListener tailerListener = new LiveSystemLogTailerListener(txMap, monitorWriter, datasetHeaderFile, newStart);
		sysLogTailer = new Tailer(sysLogFile, tailerListener, 1000);
		sysLogTailerThread = new Thread(sysLogTailer);
		sysLogTailerThread.start();

		liveMonitor = new LiveMonitor();
//		liveMonitor.start();

		if (newStart)
		{
			DBSeerGUI.isLiveDataReady = false;
			DBSeerGUI.liveDataset.clearTransactionTypes();
		}

		DBSeerGUI.isLiveMonitoring = true;

		return true;
	}

	private boolean stopMonitoringProcesses(boolean willResume)
	{
		try
		{
			if (incrementalLogThread != null)
			{
				logReceiver.setTerminate(true);
				incrementalLogThread.join();
			}
			if (logParserThread != null)
			{
				logParser.setTerminate(true);
				logParserThread.join();
			}
			if (sysLogTailerThread != null)
			{
				sysLogTailer.stop();
				sysLogTailerThread.join();
			}
			if (liveTransactionProcessorThread != null)
			{
				liveTransactionProcessor.setTerminate(true);
				liveTransactionProcessorThread.interrupt();
				liveTransactionProcessorThread.join();
			}
			if (clusteringThread != null)
			{
				clusterRunnable.setTerminate(true);
				clusteringThread.interrupt();
				clusteringThread.join();
			}
			if (liveMonitor != null)
			{
				liveMonitor.stopMonitoring();
//				liveMonitor.interrupt();
//				liveMonitor.join();
			}
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}

		txMap.clear();
		StreamClustering.clearMapAndQueues();

		try
		{
			Thread.sleep(500);
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}

		DBSeerGUI.isLiveMonitoring = false;
		DBSeerGUI.isLiveDataReady = false;

		return true;
	}

	public void setConnected(boolean isConnected)
	{
		this.isConnected = isConnected;
		if (!isConnected)
		{
			DBSeerGUI.middlewareStatus.setText("Middleware: Not Connected");
			DBSeerGUI.middlewarePanel.startMonitoringButton.setEnabled(false);
			DBSeerGUI.middlewarePanel.stopMonitoringButton.setEnabled(false);
		}
	}

	public String getErrorMessage()
	{
		return errorMessage;
	}

	public String getId()
	{
		return id;
	}

	public String getIp()
	{
		return ip;
	}

	public int getPort()
	{
		return port;
	}

	public boolean isLoggedIn()
	{
		return isLoggedIn;
	}

	public boolean isConnected()
	{
		return isConnected;
	}

	public File getLogFile()
	{
		return logFile;
	}
}
