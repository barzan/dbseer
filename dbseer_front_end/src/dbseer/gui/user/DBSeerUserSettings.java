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

	@XStreamImplicit
	private List<DBSeerDataSet> datasets = new ArrayList<DBSeerDataSet>();

	@XStreamImplicit
	private List<DBSeerConfiguration> configs = new ArrayList<DBSeerConfiguration>();

	public DBSeerUserSettings()
	{
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
}
