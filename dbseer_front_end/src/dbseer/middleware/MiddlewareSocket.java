package dbseer.middleware;

import dbseer.comp.LiveMonitoringThread;
import dbseer.comp.data.LiveMonitor;
import dbseer.gui.DBSeerExceptionHandler;
import dbseer.gui.DBSeerGUI;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Arrays;

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
	private byte[] monitoringData;

	private String errorMessage = "";
	private boolean isConnected = false;
	private boolean isLoggedIn = false;
	private Object lock;

	public MiddlewareSocket()
	{
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
		if (DBSeerGUI.liveMonitoringThread != null)
		{
			DBSeerGUI.liveMonitoringThread.interrupt();
			try
			{
				DBSeerGUI.liveMonitoringThread.join();
			}
			catch (InterruptedException e)
			{
			}
		}
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
//			System.out.println("Login successful");
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
				DBSeerGUI.liveMonitoringThread = new LiveMonitoringThread();
				DBSeerGUI.liveMonitoringThread.start();
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
				if (DBSeerGUI.liveMonitoringThread != null)
				{
					DBSeerGUI.liveMonitoringThread.interrupt();
					try
					{
						DBSeerGUI.liveMonitoringThread.join();
					}
					catch (InterruptedException e)
					{
					}
				}
				return true;
			}

			int packetId = input.readInt();
			long packetLength = input.readLong();
			byte[] packetData = new byte[(int) packetLength];
			int readBytes = 0;

			while (readBytes < packetLength)
			{
				readBytes += input.read(packetData, readBytes, (int) packetLength - readBytes);
			}

			if (packetId == PACKET_STOP_MONITORING_SUCCESS)
			{
				if (DBSeerGUI.liveMonitoringThread != null)
				{
					DBSeerGUI.liveMonitoringThread.interrupt();
//					try
//					{
//						DBSeerGUI.liveMonitoringThread.join();
//					}
//					catch (InterruptedException e)
//					{
//					}
				}
				monitoringData = packetData;
				return true;
			}
			else if (packetId == PACKET_STOP_MONITORING_FAILURE)
			{
				errorMessage = "Failed to stop monitoring: " + new String(packetData);
			}
			else
			{
				errorMessage = "Start monitoring failed: invalid packet. (ID = " + packetId + ")";
			}
			return false;
		}
	}

	public synchronized boolean isMonitoring() throws IOException
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
				if (!DBSeerGUI.liveMonitoringThread.isAlive())
				{
					DBSeerGUI.liveMonitoringThread = new LiveMonitoringThread();
					DBSeerGUI.liveMonitoringThread.start();
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

	public synchronized boolean updateLiveMonitor(LiveMonitor monitor) throws IOException
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
				DBSeerGUI.liveMonitor.setNumTransactionTypes(newSize);
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

	public byte[] getMonitoringData()
	{
		return monitoringData;
	}
}
