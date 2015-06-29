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

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dyoon on 2014. 6. 12..
 */
@XStreamAlias("dbseer")
public class DBSeerUserSettings
{
	@XStreamAlias("path")
	private String DBSeerRootPath = "";

	private String lastMiddlewareIP = "";

	private int lastMiddlewarePort = 0;

	private String lastMiddlewareID = "";

	@XStreamImplicit
	private List<DBSeerDataSet> datasets = new ArrayList<DBSeerDataSet>();

	@XStreamImplicit
	private List<DBSeerConfiguration> configs = new ArrayList<DBSeerConfiguration>();

	public DBSeerUserSettings()
	{
	}

	public void addDatasets(DefaultListModel datasetList)
	{
		if (datasets == null)
		{
			datasets = new ArrayList<DBSeerDataSet>();
		}

		for (int i = 0; i < datasetList.getSize(); ++i)
		{
			datasets.add((DBSeerDataSet) datasetList.get(i));
		}
	}

	public void addConfigs(DefaultListModel configList)
	{
		if (configs == null)
		{
			configs = new ArrayList<DBSeerConfiguration>();
		}

		for (int i= 0; i < configList.getSize(); ++i)
		{
			configs.add((DBSeerConfiguration) configList.get(i));
		}
	}

	public void setDatasets(DefaultListModel datasetList)
	{
		if (datasets == null)
		{
			datasets = new ArrayList<DBSeerDataSet>();
		}
		else
		{
			datasets.clear();
		}

		for (int i = 0; i < datasetList.getSize(); ++i)
		{
			datasets.add((DBSeerDataSet) datasetList.get(i));
		}
	}

	public void setConfigs(DefaultListModel configList)
	{
		if (configs == null)
		{
			configs = new ArrayList<DBSeerConfiguration>();
		}
		else
		{
			configs.clear();
		}

		for (int i= 0; i <  configList.getSize(); ++i)
		{
			configs.add((DBSeerConfiguration) configList.get(i));
		}
	}

	public DefaultListModel getDatasets()
	{
		DefaultListModel datasetList = new DefaultListModel();
		for (DBSeerDataSet dataset : datasets)
		{
			datasetList.addElement(dataset);
		}
		return datasetList;
	}

	public DefaultListModel getConfigs()
	{
		DefaultListModel configList = new DefaultListModel();
		for (DBSeerConfiguration config : configs)
		{
			configList.addElement(config);
		}
		return configList;
	}

	public String getDBSeerRootPath()
	{
		return DBSeerRootPath;
	}

	public void setDBSeerRootPath(String DBSeerRootPath)
	{
		this.DBSeerRootPath = DBSeerRootPath;
	}

	private Object readResolve()
	{
		if (configs == null)
		{
			configs = new ArrayList<DBSeerConfiguration>();
		}
		if (datasets == null)
		{
			datasets = new ArrayList<DBSeerDataSet>();
		}
		return this;
	}

	public String getLastMiddlewareIP()
	{
		return lastMiddlewareIP;
	}

	public void setLastMiddlewareIP(String lastMiddlewareIP)
	{
		this.lastMiddlewareIP = lastMiddlewareIP;
	}

	public int getLastMiddlewarePort()
	{
		return lastMiddlewarePort;
	}

	public void setLastMiddlewarePort(int lastMiddlewarePort)
	{
		this.lastMiddlewarePort = lastMiddlewarePort;
	}

	public String getLastMiddlewareID()
	{
		return lastMiddlewareID;
	}

	public void setLastMiddlewareID(String lastMiddlewareID)
	{
		this.lastMiddlewareID = lastMiddlewareID;
	}
}
