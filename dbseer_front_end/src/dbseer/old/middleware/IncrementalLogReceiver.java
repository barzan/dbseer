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

import dbseer.gui.DBSeerConstants;
import dbseer.gui.DBSeerGUI;

import java.io.*;
import java.net.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by dyoon on 9/16/15.
 */
public class IncrementalLogReceiver implements Runnable
{
	// same types from the middleware.
	public static final int TYPE_STATEMENT = 1;
	public static final int TYPE_QUERY = 2;
	public static final int TYPE_TRANSACTION = 3;
	public static final int TYPE_SYSLOG = 4;

	private Socket sock;
	private String ip;
	private int port;

	private DataInputStream input;

	private BufferedWriter bw;

	private String datasetPath = "";

	private File datasetDir;
	private File queryFile;
	private File statementFile;
	private File transactionFile;
	private File syslogFile;

	private BufferedWriter queryWriter;
	private BufferedWriter statementWriter;
	private BufferedWriter transactionWriter;
	private BufferedWriter syslogWriter;

	private boolean terminate = false;

	// port is 34444 for now.
	public IncrementalLogReceiver(String ip, String path)
	{
		this.ip = ip;
		this.port = 34444;
		this.datasetPath = path;

		this.datasetDir = new File(this.datasetPath);
	}

	@Override
	public void run()
	{
		try
		{
			// create the dataset directory if it does not exist.
			if (!this.datasetDir.exists())
			{
				this.datasetDir.mkdirs();
			}

			queryFile = new File(this.datasetDir + File.separator + "allLogs-q.txt");
			statementFile = new File(this.datasetDir + File.separator + "allLogs-s.txt");
			transactionFile = new File(this.datasetDir + File.separator + "allLogs-t.txt");
			syslogFile = new File(this.datasetDir + File.separator + "log_exp_1.csv");

			queryWriter = new BufferedWriter(new FileWriter(queryFile, true));
			statementWriter = new BufferedWriter(new FileWriter(statementFile, true));
			transactionWriter = new BufferedWriter(new FileWriter(transactionFile, true));
			syslogWriter = new BufferedWriter(new FileWriter(syslogFile, true));

//			sock = new Socket();
//			sock.connect(new InetSocketAddress(ip, port));
//			input = new DataInputStream(sock.getInputStream());
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		InetSocketAddress address = new InetSocketAddress(ip, port);

		int retry = 0;

		while (retry < 3)
		{
			try
			{
				++retry;
				sock = new Socket();
				sock.connect(address, 2000);
				input = new DataInputStream(sock.getInputStream());
			}
			catch (SocketTimeoutException e)
			{
				try
				{
					Thread.sleep(100);
				}
				catch (InterruptedException e1)
				{
					e1.printStackTrace();
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			if (sock.isConnected())
			{
				break;
			}
		}

		try
		{
			while (true)
			{
				int type = input.readInt();
				int len = input.readInt();
				byte[] log = new byte[len];
				int read = 0;
				while (read < len)
				{
					read += input.read(log, read, len - read);
				}
				// need to decompress..
				ByteArrayInputStream bis = new ByteArrayInputStream(log);
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				ZipInputStream zip = new ZipInputStream(bis);

				byte[] buf = new byte[8192];
				int count;
				ZipEntry ze = zip.getNextEntry();
				while ((count = zip.read(buf, 0, 8192)) != -1)
				{
					bos.write(buf, 0, count);
				}
				zip.close();

				if (type == TYPE_QUERY)
				{
					queryWriter.write(bos.toString());
					DBSeerGUI.queryLogQueue.put(bos.toString());
					queryWriter.flush();
				}
				else if (type == TYPE_STATEMENT)
				{
					statementWriter.write(bos.toString());
					DBSeerGUI.stmtLogQueue.put(bos.toString());
					statementWriter.flush();
				}
				else if (type == TYPE_TRANSACTION)
				{
					transactionWriter.write(bos.toString());
					DBSeerGUI.trxLogQueue.put(bos.toString());
					transactionWriter.flush();
				}
				else if (type == TYPE_SYSLOG)
				{
					syslogWriter.write(bos.toString());
					DBSeerGUI.sysLogQueue.put(bos.toString());
					syslogWriter.flush();
				}
				if (terminate)
				{
					break;
				}
			}
		}
		catch (SocketException e)
		{
			try
			{
				queryWriter.close();
				statementWriter.close();
				transactionWriter.close();
				syslogWriter.close();
			}
			catch (IOException e1)
			{
				e1.printStackTrace();
			}

		}
		catch (EOFException e)
		{
			try
			{
				queryWriter.close();
				statementWriter.close();
				transactionWriter.close();
				syslogWriter.close();
			}
			catch (IOException e1)
			{
				e1.printStackTrace();
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}

	public void setTerminate(boolean terminate)
	{
		this.terminate = terminate;
	}
}
