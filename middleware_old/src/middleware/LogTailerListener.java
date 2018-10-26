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

import org.apache.commons.io.input.TailerListenerAdapter;

import java.util.concurrent.BlockingQueue;

/**
 * Created by dyoon on 9/24/15.
 */
public class LogTailerListener extends TailerListenerAdapter
{
	private int type;
	private BlockingQueue<IncrementalLog> queue;
	private boolean resumed;
	private long offset;
	private boolean run = true;

	public LogTailerListener(int type, BlockingQueue<IncrementalLog> queue, boolean resumed)
	{
		this.type = type;
		this.queue = queue;
		this.resumed = resumed;
		this.offset = 0;
	}

	public void handle(String line, long offset)
	{
		// if it is resumed then no need to send dstat headers.
		if (resumed)
		{
			if (line.contains("Dstat") || line.contains("Author") || line.contains("Host")
					|| line.contains("Cmdline") || line.contains("epoch") || line.isEmpty())
			{
				return;
			}
		}
		IncrementalLog log = new IncrementalLog(type, (line + System.lineSeparator()).getBytes(), offset);

		while (!queue.offer(log))
		{
			try
			{
				Thread.sleep(100);
			}
			catch (InterruptedException e)
			{
				if (this.run)
				{
					e.printStackTrace();
				}
			}
			if (!this.run) break;
		}
		this.offset = offset;
	}

	public void stop()
	{
		this.run = false;
	}
}
