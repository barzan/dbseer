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

package dbseer.middleware;

import com.esotericsoftware.minlog.Log;
import dbseer.gui.panel.DBSeerMiddlewarePanel;
import dbseer.middleware.client.MiddlewareClient;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by Dong Young Yoon on 1/3/16.
 */
public class MiddlewareClientRunner
{
	private ExecutorService clientExecutor;

	private String host;
	private String id;
	private String password;
	private int port;
	private String dir;
	private DBSeerMiddlewarePanel panel;
	private MiddlewareClient client;

	public MiddlewareClientRunner(String id, String password, String host, int port, String dir, DBSeerMiddlewarePanel panel)
	{
		this.id = id;
		this.password = password;
		this.host = host;
		this.port = port;
		this.dir = dir;
		this.panel = panel;
	}

	public MiddlewareClient getClient()
	{
		return client;
	}

	public void run() throws Exception
	{
		client = new MiddlewareClient(host, id, password, port, dir);
		client.addObserver(panel);
		client.setLogLevel(Log.LEVEL_NONE);

		clientExecutor = Executors.newSingleThreadExecutor();
		Future clientFuture = clientExecutor.submit(client);
	}

	public void stopNow() throws Exception
	{
		if (clientExecutor != null)
		{
			clientExecutor.shutdownNow();
		}
	}

	public void stop() throws Exception
	{
		if (client != null)
		{
			client.stopMonitoring();
			client.disconnect();
		}
	}
}
