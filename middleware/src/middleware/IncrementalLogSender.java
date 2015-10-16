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

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by dyoon on 9/16/15.
 */
public class IncrementalLogSender implements Runnable
{
	private BlockingQueue<IncrementalLog> queue;
	private DataOutputStream output;
	private ServerSocket serverSocket;
	private Socket sock;

	private int interval = 1; // in seconds
	private boolean terminate = false;
	private SharedData data;

	public IncrementalLogSender(int port, BlockingQueue<IncrementalLog> queue, SharedData data)
	{
		this.queue = queue;
		this.data = data;
		try
		{
			serverSocket = new ServerSocket();
			serverSocket.setReuseAddress(true);
			serverSocket.bind(new InetSocketAddress(port));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void run()
	{
		// wait for the connection from the GUI.
		try
		{
			sock = serverSocket.accept();
			output = new DataOutputStream(new BufferedOutputStream(sock.getOutputStream()));
		}
		catch (SocketException e)
		{
			if (!this.terminate)
			{
				e.printStackTrace();
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		while (true)
		{
			long qOffset = 0, sOffset = 0, tOffset = 0, sysOffset = 0;
			// when there are logs to process.
			if (queue.size() > 0)
			{
				// drain logs to a separate list.
				ArrayList<IncrementalLog> logsToProcess = new ArrayList<IncrementalLog>();
				queue.drainTo(logsToProcess);

				ByteArrayOutputStream queryLog = new ByteArrayOutputStream();
				ByteArrayOutputStream stmtLog = new ByteArrayOutputStream();
				ByteArrayOutputStream trxLog = new ByteArrayOutputStream();
				ByteArrayOutputStream sysLog = new ByteArrayOutputStream();


				// write each log to output stream for compression.
				for (IncrementalLog log : logsToProcess)
				{
					try
					{
						if (log.getType() == IncrementalLog.TYPE_QUERY)
						{
							queryLog.write(log.getBuf());
							qOffset = log.getOffset();
						}
						else if (log.getType() == IncrementalLog.TYPE_STATEMENT)
						{
							stmtLog.write(log.getBuf());
							sOffset =log.getOffset();
						}
						else if (log.getType() == IncrementalLog.TYPE_TRANSACTION)
						{
							trxLog.write(log.getBuf());
							tOffset =log.getOffset();
						}
						else if (log.getType() == IncrementalLog.TYPE_SYSLOG)
						{
							sysLog.write(log.getBuf());
							sysOffset =log.getOffset();
						}
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
				}

				// if there are logs to send, we compress them and send to the client.
				try
				{
					if (queryLog.size() > 0)
					{
						ByteArrayOutputStream compressed = new ByteArrayOutputStream();
						ZipOutputStream zip = new ZipOutputStream(compressed);
						zip.putNextEntry(new ZipEntry("compressed"));
						zip.write(queryLog.toByteArray());
						zip.close();

						if (sendCompressedLog(IncrementalLog.TYPE_QUERY, compressed))
						{
							data.qStartOffset = qOffset;
						}
					}
					if (stmtLog.size() > 0)
					{
						ByteArrayOutputStream compressed = new ByteArrayOutputStream();
						ZipOutputStream zip = new ZipOutputStream(compressed);
						zip.putNextEntry(new ZipEntry("compressed"));
						zip.write(stmtLog.toByteArray());
						zip.close();

						if (sendCompressedLog(IncrementalLog.TYPE_STATEMENT, compressed))
						{
							data.sStartOffset = sOffset;
						}
					}
					if (trxLog.size() > 0)
					{
						ByteArrayOutputStream compressed = new ByteArrayOutputStream();
						ZipOutputStream zip = new ZipOutputStream(compressed);
						zip.putNextEntry(new ZipEntry("compressed"));
						zip.write(trxLog.toByteArray());
						zip.close();

						if (sendCompressedLog(IncrementalLog.TYPE_TRANSACTION, compressed))
						{
							data.tStartOffset = tOffset;
						}
					}
					if (sysLog.size() > 0)
					{
						ByteArrayOutputStream compressed = new ByteArrayOutputStream();
						ZipOutputStream zip = new ZipOutputStream(compressed);
						zip.putNextEntry(new ZipEntry("compressed"));
						zip.write(sysLog.toByteArray());
						zip.close();

						if (sendCompressedLog(IncrementalLog.TYPE_SYSLOG, compressed))
						{
							data.sysStartOffset = sysOffset;
						}
					}
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}

			// sleep before processing next batch of logs.
			int sleepTime = 0;
			try
			{
				while (sleepTime < interval * 1000)
				{
					Thread.sleep(250);
					sleepTime += 250;
					if (terminate)
					{
						break;
					}
				}
			}
			catch (InterruptedException e)
			{
				if (!terminate)
				{
					e.printStackTrace();
				}
			}
			if (terminate)
			{
				break;
			}
		}
	}

	private synchronized boolean sendCompressedLog(int type, ByteArrayOutputStream stream)
	{
		if (terminate)
		{
			return false;
		}
		byte[] compressedBuf = stream.toByteArray();
		try
		{
			output.writeInt(type);
			output.writeInt(compressedBuf.length);
			output.write(compressedBuf, 0, compressedBuf.length);
			output.flush();
		}
		catch (SocketException e)
		{
			// assume the GUI disconnected.
			this.setTerminate(true);
			return false;
//			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public void closeSocket()
	{
		try
		{
			if (!serverSocket.isClosed())
			{
				serverSocket.close();
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public synchronized void setTerminate(boolean terminate)
	{
		this.terminate = terminate;
		try
		{
			if (!serverSocket.isClosed())
			{
				if (sock != null)
				{
					sock.getOutputStream().close();
				}
				serverSocket.close();
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
