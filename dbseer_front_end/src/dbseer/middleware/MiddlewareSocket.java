package dbseer.middleware;

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
	private static final int MIDDLEWARE_SOCKET_TIMEOUT = 60000; // 1 MIN.
	private static final int PACKET_LOGIN_REQUEST = 100;
	private static final int PACKET_LOGIN_SUCCESS = 101;
	private static final int PACKET_LOGIN_FAILURE = 102;
	private static final int PACKET_START_MONITORING = 200;
	private static final int PACKET_START_MONITORING_SUCCESS = 201;
	private static final int PACKET_START_MONITORING_FAILURE = 202;
	private static final int PACKET_STOP_MONITORING = 300;
	private static final int PACKET_STOP_MONITORING_SUCCESS = 301;
	private static final int PACKET_STOP_MONITORING_FAILURE = 302;
	private static final int PACKET_ASK_MONITORING = 400;
	private static final int PACKET_IS_NOT_MONITORING = 401;
	private static final int PACKET_IS_MONITORING = 402;

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

	public MiddlewareSocket()
	{
		socket = new Socket();
	}

	public void connect(String ip, int port) throws IOException
	{
		this.ip = ip;
		this.port = port;
		socket = new Socket();
		InetSocketAddress address = new InetSocketAddress(ip, port);
		try
		{
			socket.connect(address, MIDDLEWARE_CONNECT_TIMEOUT);
			socket.setSoTimeout(MIDDLEWARE_SOCKET_TIMEOUT);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		input = new DataInputStream(socket.getInputStream());
		output = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
	}

	public boolean login(String id, String password) throws IOException
	{
		this.id = id;
		this.password = password;
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
			return true;
		}
		else if (packetId == PACKET_LOGIN_FAILURE)
		{
			errorMessage = "Login failed: invalid credentials.";
		}
		else
		{
			errorMessage = "Login failed: invalid packet. (ID = " + packetId + ")";
		}
		return false;
	}

	public boolean startMonitoring() throws IOException
	{
		if (output == null)
		{
			errorMessage = "Middleware not connected.";
			return false;
		}
		output.writeInt(PACKET_START_MONITORING);
		output.writeLong(0);
		output.flush();

		int packetId = input.readInt();
		long packetLength = input.readLong();
		byte[] packetData = new byte[(int)packetLength];

		int readBytes = 0;

		while (readBytes < packetLength)
		{
			readBytes += input.read(packetData, readBytes, (int) packetLength - readBytes);
		}

		if (packetId == PACKET_START_MONITORING_SUCCESS)
		{
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

	public boolean stopMonitoring() throws IOException
	{
		if (output == null)
		{
			errorMessage = "Middleware not connected.";
			return false;
		}
		output.writeInt(PACKET_STOP_MONITORING);
		output.writeLong(0);
		output.flush();

		int packetId = input.readInt();
		long packetLength = input.readLong();
		byte[] packetData = new byte[(int)packetLength];
		int readBytes = 0;

		while (readBytes < packetLength)
		{
			readBytes += input.read(packetData, readBytes, (int) packetLength - readBytes);
		}

		if (packetId == PACKET_STOP_MONITORING_SUCCESS)
		{
			monitoringData = packetData;
			return true;
		}
		else if (packetId == PACKET_STOP_MONITORING_FAILURE)
		{
			errorMessage = "Failed to stop monitoring.";
		}
		else
		{
			errorMessage = "Start monitoring failed: invalid packet. (ID = " + packetId + ")";
		}
		return false;
	}

	public boolean isMonitoring() throws IOException
	{
		if (output == null)
		{
			errorMessage = "Middleware not connected.";
			return false;
		}
		output.writeInt(PACKET_ASK_MONITORING);
		output.writeLong(0);
		output.flush();

		int packetId = input.readInt();
		long packetLength = input.readLong();
		byte[] packetData = new byte[(int)packetLength];
		int readBytes = 0;

		while (readBytes < packetLength)
		{
			readBytes += input.read(packetData, readBytes, (int) packetLength - readBytes);
		}

		if (packetId == PACKET_IS_MONITORING)
		{
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

	public byte[] getMonitoringData()
	{
		return monitoringData;
	}
}
