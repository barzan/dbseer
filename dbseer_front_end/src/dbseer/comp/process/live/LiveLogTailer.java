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

import dbseer.comp.process.base.LogProcessor;
import dbseer.gui.DBSeerExceptionHandler;
import org.apache.commons.io.input.TailerListenerAdapter;

/**
 * Created by Dong Young Yoon on 1/4/16.
 */
public class LiveLogTailer extends TailerListenerAdapter
{
	private LogProcessor processor;

	public LiveLogTailer(LogProcessor processor)
	{
		this.processor = processor;
	}

	@Override
	public void handle(String line)
	{
		try
		{
			processor.handle(line);
		}
		catch (Exception e)
		{
			DBSeerExceptionHandler.handleException(e);
		}
	}

	public void handle(String line, long offset)
	{
		try
		{
			processor.handle(line);
		}
		catch (Exception e)
		{
			DBSeerExceptionHandler.handleException(e);
		}
	}
}
