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

package dbseer.comp.process.system;

import dbseer.comp.process.base.LogProcessor;
import dbseer.comp.process.live.LiveLogProcessor;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

/**
 * Created by Dong Young Yoon on 1/3/16.
 */
public abstract class SystemLogProcessor implements LogProcessor
{
	protected LiveLogProcessor liveLogProcessor;
	protected boolean isInitialized;
	protected PrintWriter sysWriter;
	protected PrintWriter headerWriter;
	protected String dir;
	protected String name;

	private SystemLogProcessor()
	{

	}

	public SystemLogProcessor(String dir)
	{
		this.isInitialized = false;
		this.dir = dir;
	}

	public SystemLogProcessor(String dir, LiveLogProcessor liveLogProcessor)
	{
		this.isInitialized = false;
		this.dir = dir;
		this.liveLogProcessor = liveLogProcessor;
	}

	public void initialize() throws Exception
	{
		if (!isInitialized)
		{
			File logDir = new File(this.dir);
			if (!logDir.exists())
			{
				logDir.mkdirs();
			}

			File sysFile = new File(this.dir + File.separator + "monitor");
			File headerFile = new File(this.dir + File.separator + "dataset_header.m");
			sysFile.getParentFile().mkdirs();
			headerFile.getParentFile().mkdirs();
			this.name = sysFile.getParentFile().getName();
			this.sysWriter = new PrintWriter(new FileWriter(sysFile, false));
			this.headerWriter = new PrintWriter(new FileWriter(headerFile, false));
			this.isInitialized = true;
		}
	}
}
