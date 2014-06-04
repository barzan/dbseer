package dbseer.gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;

/**
 * Created by dyoon on 2014. 5. 24..
 */
public class DBSeerDataProfile
{
	private static final String[] tableHeaders = {"Name of profile", "Monitoring Data", "Transaction Count",
			"Average Latency", "Percentile Latency", "Header", "Number of transaction types",
			"Start Index", "End Index", "Max Throughput Index", "I/O Configuration", "Lock Configuration"
		};

	private static final int TYPE_NAME = 0;
	private static final int TYPE_MONITORING_DATA = 1;
	private static final int TYPE_TRANSACTION_COUNT = 2;
	private static final int TYPE_AVERAGE_LATENCY = 3;
	private static final int TYPE_PERCENTILE_LATENCY = 4;
	private static final int TYPE_HEADER = 5;
	private static final int TYPE_NUM_TRANSACTION_TYPE = 6;
	private static final int TYPE_START_INDEX = 7;
	private static final int TYPE_END_INDEX = 8;
	private static final int TYPE_MAX_THROUGHPUT_INDEX = 9;
	private static final int TYPE_IO_CONFIG = 10;
	private static final int TYPE_LOCK_CONFIG =11;

	private static int idToAssign = 0;

	private boolean profileChanged = true;

	private int id = 0;
	private String name = "";

	private String monitoringDataPath = "";
	private String transCountPath = "";
	private String averageLatencyPath = "";
	private String percentileLatencyPath = "";
	private String headerPath = "";

	private int numTransactionTypes = 0;
	private int startIndex = 0;
	private int endIndex = 0;
	private int maxThroughputIndex = 0;
	private String IOConfiguration = "";
	private String lockConfiguration = "";

	private JTable table;
	private DefaultTableModel tableModel;

	public DBSeerDataProfile()
	{
		id = getIdToAssign();
		name = "Unnamed profile " + DBSeerGUI.profiles.getSize();
		tableModel = new DBSeerConfigurationTableModel(null, new String[]{"Name", "Value"});
		table = new JTable(tableModel);
		table.setFillsViewportHeight(true);
		table.getColumnModel().getColumn(0).setMaxWidth(400);
		table.getColumnModel().getColumn(0).setPreferredWidth(300);
		table.getColumnModel().getColumn(1).setPreferredWidth(800);

		for (String header : tableHeaders)
		{
			tableModel.addRow(new Object[]{header, ""});
		}
		this.updateTable();
	}

	private static synchronized int getIdToAssign()
	{
		return idToAssign++;
	}

	public String toString()
	{
		return name;
	}

	public JTable getTable()
	{
		return table;
	}

	public void setFromTable()
	{
		TableCellEditor editor = table.getCellEditor();
		if ( editor != null ) editor.stopCellEditing(); // stop editing

		for (int i = 0; i < tableModel.getRowCount(); ++i)
		{
			for (int j = 0; j < tableHeaders.length; ++j)
			{
				if (tableModel.getValueAt(i,0).equals(tableHeaders[j]))
				{
					switch (j)
					{
						case TYPE_NAME:
							this.name = (String)tableModel.getValueAt(i, 1);
							break;
						case TYPE_AVERAGE_LATENCY:
							this.averageLatencyPath = (String)tableModel.getValueAt(i, 1);
							break;
						case TYPE_END_INDEX:
							this.endIndex = ((Integer)tableModel.getValueAt(i, 1)).intValue();
							break;
						case TYPE_HEADER:
							this.headerPath = (String)tableModel.getValueAt(i, 1);
							break;
						case TYPE_IO_CONFIG:
							this.IOConfiguration = (String)tableModel.getValueAt(i, 1);
							break;
						case TYPE_LOCK_CONFIG:
							this.IOConfiguration = (String)tableModel.getValueAt(i, 1);
							break;
						case TYPE_MAX_THROUGHPUT_INDEX:
							this.maxThroughputIndex = ((Integer)tableModel.getValueAt(i, 1)).intValue();
							break;
						case TYPE_MONITORING_DATA:
							this.monitoringDataPath = (String)tableModel.getValueAt(i, 1);
							break;
						case TYPE_NUM_TRANSACTION_TYPE:
							this.numTransactionTypes = ((Integer)tableModel.getValueAt(i, 1)).intValue();
							break;
						case TYPE_PERCENTILE_LATENCY:
							this.percentileLatencyPath = (String)tableModel.getValueAt(i, 1);
							break;
						case TYPE_START_INDEX:
							this.startIndex = ((Integer)tableModel.getValueAt(i, 1)).intValue();
							break;
						case TYPE_TRANSACTION_COUNT:
							this.transCountPath = (String)tableModel.getValueAt(i, 1);
							break;
						default:
							break;
					}
					break;
				}
			}
		}
	}

	private void updateTable()
	{
		for (int i = 0; i < tableModel.getRowCount(); ++i)
		{
			for (int j = 0; j < tableHeaders.length; ++j)
			{
				if (tableModel.getValueAt(i,0).equals(tableHeaders[j]))
				{
					switch (j)
					{
						case TYPE_NAME:
							tableModel.setValueAt(this.name, i, 1);
							break;
						case TYPE_AVERAGE_LATENCY:
							tableModel.setValueAt(this.averageLatencyPath, i, 1);
							break;
						case TYPE_END_INDEX:
							tableModel.setValueAt(this.endIndex, i, 1);
							break;
						case TYPE_HEADER:
							tableModel.setValueAt(this.headerPath, i, 1);
							break;
						case TYPE_IO_CONFIG:
							tableModel.setValueAt(this.IOConfiguration, i, 1);
							break;
						case TYPE_LOCK_CONFIG:
							tableModel.setValueAt(this.lockConfiguration, i, 1);
							break;
						case TYPE_MAX_THROUGHPUT_INDEX:
							tableModel.setValueAt(this.maxThroughputIndex, i, 1);
							break;
						case TYPE_MONITORING_DATA:
							tableModel.setValueAt(this.monitoringDataPath, i, 1);
							break;
						case TYPE_NUM_TRANSACTION_TYPE:
							tableModel.setValueAt(this.numTransactionTypes, i, 1);
							break;
						case TYPE_PERCENTILE_LATENCY:
							tableModel.setValueAt(this.percentileLatencyPath, i, 1);
							break;
						case TYPE_START_INDEX:
							tableModel.setValueAt(this.startIndex, i, 1);
							break;
						case TYPE_TRANSACTION_COUNT:
							tableModel.setValueAt(this.transCountPath, i, 1);
							break;
						default:
							break;
					}
					break;
				}
			}
		}
	}

	public synchronized String getName()
	{
		return name;
	}

	public synchronized void setName(String name)
	{
		this.name = name;
		updateTable();
		tableModel.fireTableDataChanged();
		this.profileChanged = true;
	}

	public synchronized int getNumTransactionTypes()
	{
		return numTransactionTypes;
	}

	public synchronized void setNumTransactionTypes(int numTransactionTypes)
	{
		this.numTransactionTypes = numTransactionTypes;
		updateTable();
		tableModel.fireTableDataChanged();
		this.profileChanged = true;
	}

	public synchronized int getStartIndex()
	{
		return startIndex;
	}

	public synchronized void setStartIndex(int startIndex)
	{
		this.startIndex = startIndex;
		updateTable();
		tableModel.fireTableDataChanged();
		this.profileChanged = true;
	}

	public synchronized int getEndIndex()
	{
		return endIndex;
	}

	public synchronized void setEndIndex(int endIndex)
	{
		this.endIndex = endIndex;
		updateTable();
		tableModel.fireTableDataChanged();
		this.profileChanged = true;
	}

	public synchronized int getMaxThroughputIndex()
	{
		return maxThroughputIndex;
	}

	public synchronized void setMaxThroughputIndex(int maxThroughputIndex)
	{
		this.maxThroughputIndex = maxThroughputIndex;
		updateTable();
		tableModel.fireTableDataChanged();
		this.profileChanged = true;
	}

	public synchronized String getIOConfiguration()
	{
		return IOConfiguration;
	}

	public synchronized void setIOConfiguration(String IOConfiguration)
	{
		this.IOConfiguration = IOConfiguration;
		updateTable();
		tableModel.fireTableDataChanged();
		this.profileChanged = true;
	}

	public synchronized String getLockConfiguration()
	{
		return lockConfiguration;
	}

	public synchronized void setLockConfiguration(String lockConfiguration)
	{
		this.lockConfiguration = lockConfiguration;
		updateTable();
		tableModel.fireTableDataChanged();
		this.profileChanged = true;
	}


	public synchronized String getMonitoringDataPath()
	{
		return monitoringDataPath;
	}

	public synchronized void setMonitoringDataPath(String monitoringDataPath)
	{
		this.monitoringDataPath = monitoringDataPath;
		updateTable();
		tableModel.fireTableDataChanged();
		this.profileChanged = true;
	}

	public synchronized String getTransCountPath()
	{
		return transCountPath;
	}

	public synchronized void setTransCountPath(String transCountPath)
	{
		this.transCountPath = transCountPath;
		updateTable();
		tableModel.fireTableDataChanged();
		this.profileChanged = true;
	}

	public synchronized String getAverageLatencyPath()
	{
		return averageLatencyPath;
	}

	public synchronized void setAverageLatencyPath(String averageLatencyPath)
	{
		this.averageLatencyPath = averageLatencyPath;
		updateTable();
		tableModel.fireTableDataChanged();
		this.profileChanged = true;
	}

	public synchronized String getPercentileLatencyPath()
	{
		return percentileLatencyPath;
	}

	public synchronized void setPercentileLatencyPath(String percentileLatencyPath)
	{
		this.percentileLatencyPath = percentileLatencyPath;
		updateTable();
		tableModel.fireTableDataChanged();
		this.profileChanged = true;
	}

	public synchronized String getHeaderPath()
	{
		return headerPath;
	}

	public synchronized void setHeaderPath(String headerPath)
	{
		this.headerPath = headerPath;
		updateTable();
		tableModel.fireTableDataChanged();
		this.profileChanged = true;
	}

	public synchronized boolean isProfileChanged()
	{
		return profileChanged;
	}

	public synchronized void setProfileChanged(boolean profileChanged)
	{
		this.profileChanged = profileChanged;
	}

	public int getId()
	{
		return id;
	}
}
