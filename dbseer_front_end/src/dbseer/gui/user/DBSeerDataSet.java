package dbseer.gui.user;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import dbseer.gui.DBSeerGUI;
import matlabcontrol.MatlabInvocationException;
import matlabcontrol.MatlabProxy;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import java.util.UUID;

/**
 * Created by dyoon on 2014. 5. 24..
 */
@XStreamAlias("dataset")
public class DBSeerDataSet implements TableModelListener
{
	private static final String[] tableHeaders = {"Name of dataset", "Monitoring Data", "Transaction Count",
			"Average Latency", "Percentile Latency", "Header", "Statement Stat", // "Number of transaction types",
			"Start Index", "End Index"}; //, "Max Throughput Index"}; //"I/O Configuration", "Lock Configuration"
		//};

	private static final int TYPE_NAME = 0;
	private static final int TYPE_MONITORING_DATA = 1;
	private static final int TYPE_TRANSACTION_COUNT = 2;
	private static final int TYPE_AVERAGE_LATENCY = 3;
	private static final int TYPE_PERCENTILE_LATENCY = 4;
	private static final int TYPE_HEADER = 5;
//	private static final int TYPE_NUM_TRANSACTION_TYPE = 6;
	private static final int TYPE_STATEMENT_STAT = 6;
	private static final int TYPE_START_INDEX = 7;
	private static final int TYPE_END_INDEX = 8;
	private static final int TYPE_PAGE_INFO = 9;
//	private static final int TYPE_MAX_THROUGHPUT_INDEX = 9;
//	private static final int TYPE_IO_CONFIG = 10;
//	private static final int TYPE_LOCK_CONFIG =11;

	private static int idToAssign = 0;

	@XStreamOmitField
	private boolean dataSetLoaded = false;

	@XStreamOmitField
	private String uniqueVariableName = "";

	private String name = "";

	private String monitoringDataPath = "";
	private String transCountPath = "";
	private String averageLatencyPath = "";
	private String percentileLatencyPath = "";
	private String headerPath = "";
	private String pageInfoPath = "";
	private String statementStatPath = "";

	private int startIndex = 0;
	private int endIndex = 0;

//	private String IOConfiguration = "[]";
//	private String lockConfiguration = "[]";

	@XStreamOmitField
	private JTable table;

	@XStreamOmitField
	private DefaultTableModel tableModel;

	public DBSeerDataSet()
	{
		uniqueVariableName = "dataset_" + UUID.randomUUID().toString().replace('-', '_');
		name = "Unnamed dataset";
		tableModel = new DBSeerConfigurationTableModel(null, new String[]{"Name", "Value"});
		tableModel.addTableModelListener(this);
		table = new JTable(tableModel);
		table.setFillsViewportHeight(true);
		table.getColumnModel().getColumn(0).setMaxWidth(400);
		table.getColumnModel().getColumn(0).setPreferredWidth(300);
		table.getColumnModel().getColumn(1).setPreferredWidth(600);
		table.setRowHeight(20);

		for (String header : tableHeaders)
		{
			tableModel.addRow(new Object[]{header, ""});
		}
		this.updateTable();
		dataSetLoaded = false;
	}

	private Object readResolve()
	{
		uniqueVariableName = "dataset_" + UUID.randomUUID().toString().replace('-', '_');
		tableModel = new DBSeerConfigurationTableModel(null, new String[]{"Name", "Value"});
		tableModel.addTableModelListener(this);
		table = new JTable(tableModel);
		table.setFillsViewportHeight(true);
		table.getColumnModel().getColumn(0).setMaxWidth(400);
		table.getColumnModel().getColumn(0).setPreferredWidth(300);
		table.getColumnModel().getColumn(1).setPreferredWidth(600);
		table.setRowHeight(20);

		for (String header : tableHeaders)
		{
			tableModel.addRow(new Object[]{header, ""});
		}
		this.updateTable();
		dataSetLoaded = false;

		return this;
	}

	public String toString()
	{
		return name;
	}

	public JTable getTable()
	{
		return table;
	}

	public void loadDataset()
	{
		if (uniqueVariableName == "")
		{
			uniqueVariableName = "dataset_" + UUID.randomUUID().toString().replace('-', '_');
		}

		if (dataSetLoaded == false)
		{
			MatlabProxy proxy = DBSeerGUI.proxy;

			try
			{
				proxy.eval(this.uniqueVariableName + " = DataSet;");
				proxy.eval(this.uniqueVariableName + ".header_path = '" + this.headerPath + "';");
				proxy.eval(this.uniqueVariableName + ".monitor_path = '" + this.monitoringDataPath + "';");
				proxy.eval(this.uniqueVariableName + ".avg_latency_path = '" + this.averageLatencyPath + "';");
				proxy.eval(this.uniqueVariableName + ".percentile_latency_path = '" +
						this.percentileLatencyPath + "';");
				proxy.eval(this.uniqueVariableName + ".trans_count_path = '" + this.transCountPath + "';");
				proxy.eval(this.uniqueVariableName + ".statement_stat_path = '" + this.statementStatPath + "';");
				//proxy.eval(this.uniqueVariableName + ".page_info_path = '" + this.pageInfoPath + "';");
				proxy.eval(this.uniqueVariableName + ".startIdx = " + this.startIndex + ";");
				proxy.eval(this.uniqueVariableName + ".endIdx = " + this.endIndex + ";");
				proxy.eval(this.uniqueVariableName + ".loadStatistics;");
			}
			catch (MatlabInvocationException e)
			{
				e.printStackTrace();
			}

			dataSetLoaded = true;
		}
	}

	public void setFromTable()
	{
		TableCellEditor editor = table.getCellEditor();
//		if ( editor != null ) editor.stopCellEditing(); // stop editing

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
							if (tableModel.getValueAt(i, 1) != "")
								this.endIndex = Integer.parseInt((String)tableModel.getValueAt(i, 1));
							break;
						case TYPE_HEADER:
							this.headerPath = (String)tableModel.getValueAt(i, 1);
							break;
						case TYPE_PAGE_INFO:
							this.pageInfoPath = (String)tableModel.getValueAt(i, 1);
							break;
//						case TYPE_IO_CONFIG:
//							this.IOConfiguration = (String)tableModel.getValueAt(i, 1);
//							break;
//						case TYPE_LOCK_CONFIG:
//							this.IOConfiguration = (String)tableModel.getValueAt(i, 1);
//							break;
//						case TYPE_MAX_THROUGHPUT_INDEX:
//							this.maxThroughputIndex = Integer.parseInt((String) tableModel.getValueAt(i, 1));
//							break;
						case TYPE_MONITORING_DATA:
							this.monitoringDataPath = (String)tableModel.getValueAt(i, 1);
							break;
//						case TYPE_NUM_TRANSACTION_TYPE:
//							this.numTransactionTypes = Integer.parseInt((String)tableModel.getValueAt(i, 1));
//							break;
						case TYPE_PERCENTILE_LATENCY:
							this.percentileLatencyPath = (String)tableModel.getValueAt(i, 1);
							break;
						case TYPE_STATEMENT_STAT:
							this.statementStatPath = (String)tableModel.getValueAt(i, 1);
							break;
						case TYPE_START_INDEX:
							if (tableModel.getValueAt(i, 1) != "")
								this.startIndex = Integer.parseInt((String) tableModel.getValueAt(i, 1));
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
							tableModel.setValueAt(String.valueOf(this.endIndex), i, 1);
							break;
						case TYPE_HEADER:
							tableModel.setValueAt(this.headerPath, i, 1);
							break;
						case TYPE_PAGE_INFO:
							tableModel.setValueAt(this.pageInfoPath, i, 1);
							break;
//						case TYPE_IO_CONFIG:
//							tableModel.setValueAt(this.IOConfiguration, i, 1);
//							break;
//						case TYPE_LOCK_CONFIG:
//							tableModel.setValueAt(this.lockConfiguration, i, 1);
//							break;
//						case TYPE_MAX_THROUGHPUT_INDEX:
//							tableModel.setValueAt(String.valueOf(this.maxThroughputIndex), i, 1);
//							break;
						case TYPE_MONITORING_DATA:
							tableModel.setValueAt(this.monitoringDataPath, i, 1);
							break;
//						case TYPE_NUM_TRANSACTION_TYPE:
//							tableModel.setValueAt(String.valueOf(this.numTransactionTypes), i, 1);
//							break;
						case TYPE_PERCENTILE_LATENCY:
							tableModel.setValueAt(this.percentileLatencyPath, i, 1);
							break;
						case TYPE_STATEMENT_STAT:
							tableModel.setValueAt(this.statementStatPath, i, 1);
							break;
						case TYPE_START_INDEX:
							tableModel.setValueAt(String.valueOf(this.startIndex), i, 1);
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
		this.dataSetLoaded = false;
	}

//	public synchronized int getNumTransactionTypes()
//	{
//		return numTransactionTypes;
//	}
//
//	public synchronized void setNumTransactionTypes(int numTransactionTypes)
//	{
//		this.numTransactionTypes = numTransactionTypes;
//		updateTable();
//		tableModel.fireTableDataChanged();
//		this.dataSetLoaded = false;
//	}

	public synchronized int getStartIndex()
	{
		return startIndex;
	}

	public synchronized void setStartIndex(int startIndex)
	{
		this.startIndex = startIndex;
		updateTable();
		tableModel.fireTableDataChanged();
		this.dataSetLoaded = false;
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
		this.dataSetLoaded = false;
	}

//	public synchronized int getMaxThroughputIndex()
//	{
//		return maxThroughputIndex;
//	}
//
//	public synchronized void setMaxThroughputIndex(int maxThroughputIndex)
//	{
//		this.maxThroughputIndex = maxThroughputIndex;
//		updateTable();
//		tableModel.fireTableDataChanged();
//		this.dataSetLoaded = false;
//	}
//
//	public synchronized String getIOConfiguration()
//	{
//		return IOConfiguration;
//	}
//
//	public synchronized void setIOConfiguration(String IOConfiguration)
//	{
//		this.IOConfiguration = IOConfiguration;
//		updateTable();
//		tableModel.fireTableDataChanged();
//		this.dataSetLoaded = false;
//	}

//	public synchronized String getLockConfiguration()
//	{
//		return lockConfiguration;
//	}
//
//	public synchronized void setLockConfiguration(String lockConfiguration)
//	{
//		this.lockConfiguration = lockConfiguration;
//		updateTable();
//		tableModel.fireTableDataChanged();
//		this.dataSetLoaded = false;
//	}
	
	public synchronized String getMonitoringDataPath()
	{
		return monitoringDataPath;
	}

	public synchronized void setMonitoringDataPath(String monitoringDataPath)
	{
		this.monitoringDataPath = monitoringDataPath;
		updateTable();
		tableModel.fireTableDataChanged();
		this.dataSetLoaded = false;
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
		this.dataSetLoaded = false;
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
		this.dataSetLoaded = false;
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
		this.dataSetLoaded = false;
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
		this.dataSetLoaded = false;
	}

	public synchronized boolean isDataSetLoaded()
	{
		return dataSetLoaded;
	}

	public synchronized void setDataSetLoaded(boolean dataSetLoaded)
	{
		this.dataSetLoaded = dataSetLoaded;
	}

	public synchronized String getUniqueVariableName()
	{
		return uniqueVariableName;
	}

	public synchronized String getPageInfoPath()
	{
		return pageInfoPath;
	}

	public synchronized void setPageInfoPath(String pageInfoPath)
	{
		this.pageInfoPath = pageInfoPath;
		updateTable();
		tableModel.fireTableDataChanged();
		this.dataSetLoaded = false;
	}

	public synchronized String getStatementStatPath()
	{
		return statementStatPath;
	}

	public synchronized void setStatementStatPath(String statementStatPath)
	{
		this.statementStatPath = statementStatPath;
		updateTable();
		tableModel.fireTableDataChanged();
		this.dataSetLoaded = false;
	}

	@Override
	public void tableChanged(TableModelEvent tableModelEvent)
	{
	}
}
