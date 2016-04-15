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

package dbseer.gui.user;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

/**
 * Created by dyoon on 14. 11. 26..
 */

public class DBSeerTransactionSampleList
{
	private ArrayList<DBSeerTransactionSample> samples;
	private String samplePath;

	public DBSeerTransactionSampleList()
	{
		samples = new ArrayList<DBSeerTransactionSample>();
	}

	public DBSeerTransactionSampleList(String samplePath)
	{
		this.samplePath = samplePath;
		this.samples = new ArrayList<DBSeerTransactionSample>();
	}

	private Object readResolve()
	{
		if (samples == null)
		{
			samples = new ArrayList<DBSeerTransactionSample>();
		}
		return this;
	}

	public void readSamples()
	{
		File sampleFile = new File(samplePath);
		if (sampleFile.isFile())
		{
			try
			{
				RandomAccessFile file = new RandomAccessFile(sampleFile, "r");
				String stmt = "";
				String line = file.readLine();

				while (line != null)
				{
					if (line.equals("---"))
					{
						DBSeerTransactionSample sample = new DBSeerTransactionSample(0, stmt);
						samples.add(sample);
						stmt = "";
					}
					else
					{
						stmt = stmt + line + "\n";
					}
					line = file.readLine();
				}
			}
			catch (FileNotFoundException e)
			{
				e.printStackTrace();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	public ArrayList<DBSeerTransactionSample> getSamples()
	{
		return samples;
	}
}
