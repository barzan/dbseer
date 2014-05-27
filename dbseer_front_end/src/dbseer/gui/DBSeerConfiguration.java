package dbseer.gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/**
 * Created by dyoon on 2014. 5. 24..
 */
public class DBSeerConfiguration
{
	private final String[] tableHeaders = {"DBSeer Root", "Monitoring Data", "Transaction Count",
			"Average Latency", "Percentile Latency", "Header"};



	private boolean configChanged = true;
	private String rootPath = "";
	private String monitoringDataPath = "";
	private String transCountPath = "";
	private String averageLatencyPath = "";
	private String percentileLatencyPath = "";
	private String headerPath = "";

	private JTable table;
	private DefaultTableModel tableModel;

	public DBSeerConfiguration()
	{
		tableModel = new DBSeerConfigurationTableModel(null, new String[]{"Name", "Value"});
		table = new JTable(tableModel);
		table.setFillsViewportHeight(true);
		table.getColumnModel().getColumn(0).setMaxWidth(280);
		table.getColumnModel().getColumn(0).setPreferredWidth(120);
		table.getColumnModel().getColumn(1).setPreferredWidth(300);

		for (String header : tableHeaders)
		{
			tableModel.addRow(new Object[]{header, ""});
		}
	}

	public JTable getTable()
	{
		return table;
	}

	private void updateTable()
	{
		for (int i = 0; i < tableModel.getRowCount(); ++i)
		{
			if (tableModel.getValueAt(i,0).equals("Monitoring Data"))
			{
				tableModel.setValueAt(this.monitoringDataPath, i, 1);
			}
			else if (tableModel.getValueAt(i,0).equals("DBSeer Root"))
			{
				tableModel.setValueAt(this.rootPath, i, 1);
			}
			else if (tableModel.getValueAt(i,0).equals("Transaction Count"))
			{
				tableModel.setValueAt(this.transCountPath, i, 1);
			}
			else if (tableModel.getValueAt(i,0).equals("Average Latency"))
			{
				tableModel.setValueAt(this.averageLatencyPath, i, 1);
			}
			else if (tableModel.getValueAt(i,0).equals("Percentile Latency"))
			{
				tableModel.setValueAt(this.percentileLatencyPath, i, 1);
			}
			else if (tableModel.getValueAt(i,0).equals("Header"))
			{
				tableModel.setValueAt(this.headerPath, i, 1);
			}
		}
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
		this.configChanged = true;
	}

	public synchronized String getRootPath()
	{
		return rootPath;
	}

	public synchronized void setRootPath(String rootPath)
	{
		this.rootPath = rootPath;
		updateTable();
		tableModel.fireTableDataChanged();
		this.configChanged = true;
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
		this.configChanged = true;
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
		this.configChanged = true;
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
		this.configChanged = true;
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
		this.configChanged = true;
	}

	public synchronized boolean isConfigChanged()
	{
		return configChanged;
	}

	public synchronized void setConfigChanged(boolean configChanged)
	{
		this.configChanged = configChanged;
	}
}
