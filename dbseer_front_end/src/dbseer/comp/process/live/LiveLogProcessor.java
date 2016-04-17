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

import dbseer.comp.process.system.dstat.DstatSystemLogProcessor;
import dbseer.comp.process.transaction.TransactionLogProcessor;
import dbseer.comp.process.transaction.TransactionLogWriter;
import dbseer.comp.process.system.SystemLogProcessor;
import dbseer.comp.process.transaction.mysql.MySQLTransactionLogProcessor;
import dbseer.gui.DBSeerConstants;
import dbseer.gui.DBSeerGUI;
import dbseer.gui.DBSeerSettings;
import dbseer.middleware.constant.MiddlewareConstants;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListener;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Dong Young Yoon on 1/4/16.
 */
public class LiveLogProcessor
{
	private String dir;
	private String serverStr;
	private String[] servers;
	private List<SystemLogProcessor> sysLogProcessors;
//	private TransactionLogProcessor transactionLogProcessor;
	private TransactionLogWriter transactionLogWriter;
	private LiveTransactionLogWriter liveTransactionLogWriter;
	private LiveMonitor liveMonitor;

	private ExecutorService liveLogExecutor;

	private volatile long sysStartTime;
	private volatile long txStartTime;

	private boolean isStarted;

//	private Tailer sysLogTailer;
//	private Tailer dbLogTailer;
//
//	private File sysLogFile;
//	private File dbLogFile;

	public LiveLogProcessor(String dir, String serverStr)
	{
		this.dir = dir;
		this.serverStr = serverStr;
		this.isStarted = false;
	}

	public void start() throws Exception
	{
		liveLogExecutor = Executors.newCachedThreadPool();
		sysStartTime = 0;
		txStartTime = 0;

		// check live directory
		File liveDir = new File(dir);
		if (!liveDir.exists())
		{
			// create directories if it does not exist.
			liveDir.mkdirs();
		}

		// start sys log processors
		servers = serverStr.split(MiddlewareConstants.SERVER_STRING_DELIMITER);
		for (String server : servers)
		{
			FileUtils.forceMkdir(new File(dir + File.separator + server));
			FileUtils.cleanDirectory(new File(dir + File.separator + server));
			File sysFile = new File(dir + File.separator + "sys.log." + server);
			SystemLogProcessor sysLogProcessor;
			if (DBSeerGUI.osType == DBSeerConstants.OS_LINUX)
			{
				sysLogProcessor = new DstatSystemLogProcessor(dir + File.separator + server, this);
			}
			else
			{
				sysLogProcessor = new DstatSystemLogProcessor(dir + File.separator + server, this);
			}
			sysLogProcessor.initialize();
			LiveLogTailer sysLogTailerListener = new LiveLogTailer(sysLogProcessor);
			LogTailer sysLogTailer = new LogTailer(sysFile, sysLogTailerListener, 250, 0, false);

			liveLogExecutor.submit(sysLogTailer);
		}

		// start tx log processors
		TransactionLogProcessor txLogProcessor;
		if (DBSeerGUI.databaseType == DBSeerConstants.DB_MYSQL)
		{
			txLogProcessor = new MySQLTransactionLogProcessor(DBSeerGUI.settings.mysqlLogDelimiter, DBSeerGUI.settings.mysqlQueryDelimiter);
		}
		else
		{
			txLogProcessor = new MySQLTransactionLogProcessor( DBSeerGUI.settings.mysqlLogDelimiter, DBSeerGUI.settings.mysqlQueryDelimiter);
		}
		File txLogFile = new File(dir + File.separator + "tx.log");
		LiveLogTailer txLogTailerListener = new LiveLogTailer(txLogProcessor);
		LogTailer txLogTailer = new LogTailer(txLogFile, txLogTailerListener, 250, 0, false);

		transactionLogWriter = new TransactionLogWriter(dir, servers, DBSeerGUI.liveMonitorInfo, this);
		transactionLogWriter.initialize();

		liveTransactionLogWriter = new LiveTransactionLogWriter(txLogProcessor, transactionLogWriter);
		liveMonitor = new LiveMonitor();

		liveLogExecutor.submit(txLogTailer);
		liveLogExecutor.submit(liveTransactionLogWriter);
		liveLogExecutor.submit(liveMonitor);
		this.isStarted = true;
	}

	public void stop() throws Exception
	{
		if (liveLogExecutor != null)
		{
			liveLogExecutor.shutdownNow();
		}
	}

	public void reset() throws Exception
	{
		// reinitialize TransactionLogWriter
		transactionLogWriter = new TransactionLogWriter(dir, servers, DBSeerGUI.liveMonitorInfo, this);
		transactionLogWriter.initialize();
	}

	public boolean isTxWritingStarted()
	{
		return transactionLogWriter.isWritingStarted();
	}

	public long getSysStartTime()
	{
		return sysStartTime;
	}

	public void setSysStartTime(long sysStartTime)
	{
		this.sysStartTime = sysStartTime;
	}

	public long getTxStartTime()
	{
		return txStartTime;
	}

	public String[] getServers()
	{
		return servers;
	}

	public void setTxStartTime(long txStartTime)
	{
		this.txStartTime = txStartTime;
	}

	public boolean isStarted()
	{
		return isStarted;
	}

	public void setStarted(boolean started)
	{
		isStarted = started;
	}
}
