package dbseer.gui.user;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import dbseer.comp.MatlabFunctions;
import dbseer.comp.UserInputValidator;
import dbseer.gui.DBSeerExceptionHandler;
import dbseer.gui.DBSeerGUI;
import dbseer.gui.xml.XStreamHelper;
import matlabcontrol.MatlabInvocationException;
import matlabcontrol.MatlabProxy;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.text.TableView;
import java.awt.*;
import java.io.File;
import java.util.*;
import java.util.List;

/**
 * Created by dyoon on 2014. 5. 24..
 */
@XStreamAlias("dataset")
public class DBSeerDataSet implements TableModelListener
{
	public static final String[] tableHeaders = {"Name of Dataset", "Transaction File", "Statement File", "Query File", "Monitoring Data File", "Transaction Count File",
			"Average Latency File", "Percentile Latency File", "Header File",// "Statement Stat", // "Number of transaction types",
			"Use Entire Dataset", "Use Partial Dataset: Start Index", "Use Partial Dataset: End Index", "Number of Transaction Types"}; //, "Max Throughput Index"}; //"I/O Configuration", "Lock Configuration"
		//};

	public static final int TYPE_NAME = 0;
	public static final int TYPE_TRANSACTION_FILE = 1;
	public static final int TYPE_STATEMENT_FILE = 2;
	public static final int TYPE_QUERY_FILE = 3;
	public static final int TYPE_MONITORING_DATA = 4;
	public static final int TYPE_TRANSACTION_COUNT = 5;
	public static final int TYPE_AVERAGE_LATENCY = 6;
	public static final int TYPE_PERCENTILE_LATENCY = 7;
	public static final int TYPE_HEADER = 8;
//	public static final int TYPE_NUM_TRANSACTION_TYPE = 6;
//	public static final int TYPE_STATEMENT_STAT = 6;
	public static final int TYPE_USE_ENTIRE_DATASET = 9;
	public static final int TYPE_START_INDEX = 10;
	public static final int TYPE_END_INDEX = 11;
	public static final int TYPE_NUM_TRANSACTION_TYPE = 12;
//	public static final int TYPE_MAX_THROUGHPUT_INDEX = 9;
//	public static final int TYPE_IO_CONFIG = 10;
//	public static final int TYPE_LOCK_CONFIG =11;

	private static int idToAssign = 0;

	@XStreamOmitField
	private boolean dataSetLoaded = false;

	@XStreamOmitField
	private boolean modelVariableLoaded = false;

	@XStreamOmitField
	private String uniqueVariableName = "";
	@XStreamOmitField
	private String uniqueModelVariableName = "";

	private String name = "";

	private String transactionFilePath = "";
	private String statementFilePath = "";
	private String queryFilePath = "";
	private String monitoringDataPath = "";
	private String transCountPath = "";
	private String averageLatencyPath = "";
	private String percentileLatencyPath = "";
	private String headerPath = "";
	private String pageInfoPath = "";
	private String statementStatPath = "";

	private Boolean useEntireDataSet = true;

	private int startIndex = 0;
	private int endIndex = 0;

	private int numTransactionTypes = 0;

	@XStreamImplicit
	private java.util.List<String> transactionTypeNames = new ArrayList<String>();

	@XStreamOmitField
	private String transactionFilePathBackup = "";
	@XStreamOmitField
	private String statementFilePathBackup = "";
	@XStreamOmitField
	private String queryFilePathBackup = "";
	@XStreamOmitField
	private String nameBackup = "";
	@XStreamOmitField
	private String monitoringDataPathBackup = "";
	@XStreamOmitField
	private String transCountPathBackup = "";
	@XStreamOmitField
	private String averageLatencyPathBackup = "";
	@XStreamOmitField
	private String percentileLatencyPathBackup = "";
	@XStreamOmitField
	private String headerPathBackup = "";
	@XStreamOmitField
	private String pageInfoPathBackup = "";
	@XStreamOmitField
	private String statementStatPathBackup = "";
	@XStreamOmitField
	private Boolean useEntireDataSetBackup = true;

	@XStreamOmitField
	private int numTransactionTypesBackup = 0;
	@XStreamOmitField
	private int startIndexBackup = 0;
	@XStreamOmitField
	private int endIndexBackup = 0;

	@XStreamOmitField
	private java.util.List<String> transactionTypeNamesBackup = new ArrayList<String>();

	@XStreamOmitField
	private java.util.List<DBSeerTransactionSampleList> transactionSampleLists = new ArrayList<DBSeerTransactionSampleList>();

	@XStreamOmitField
	private java.util.List<String> statementsOffsetFiles = new ArrayList<String>();

//	private String IOConfiguration = "[]";
//	private String lockConfiguration = "[]";

	@XStreamOmitField
	private JTable table;

	@XStreamOmitField
	private DBSeerDataSetTableModel tableModel;

	public DBSeerDataSet()
	{
		Boolean[] trueFalse = {Boolean.TRUE, Boolean.FALSE};
		JComboBox trueFalseBox = new JComboBox(trueFalse);
		final DefaultCellEditor dce = new DefaultCellEditor(trueFalseBox);

		uniqueVariableName = "dataset_" + UUID.randomUUID().toString().replace('-', '_');
		uniqueModelVariableName = "mv_" + UUID.randomUUID().toString().replace('-', '_');
		name = "Unnamed dataset";
		tableModel = new DBSeerDataSetTableModel(null, new String[]{"Name", "Value"});
		tableModel.addTableModelListener(this);
		table = new JTable(tableModel){
			@Override
			public TableCellEditor getCellEditor(int row, int col)
			{
				if (row == DBSeerDataSet.TYPE_USE_ENTIRE_DATASET && col == 1)
					return dce;
				return super.getCellEditor(row, col);
			}
		};
		DefaultTableCellRenderer customRenderder = new DefaultTableCellRenderer(){
			@Override
			public Component getTableCellRendererComponent(JTable jTable, Object o, boolean b, boolean b2, int row, int col)
			{
				Component cell = super.getTableCellRendererComponent(jTable, o, b, b2, row, col);
				if (row == DBSeerDataSet.TYPE_START_INDEX || row == DBSeerDataSet.TYPE_END_INDEX)
				{
					if (((Boolean)table.getValueAt(DBSeerDataSet.TYPE_USE_ENTIRE_DATASET, 1)).booleanValue() == true)
					{
						cell.setForeground(Color.LIGHT_GRAY);
					}
					else
					{
						cell.setForeground(Color.BLACK);
					}
				}
				else if (row == DBSeerDataSet.TYPE_AVERAGE_LATENCY ||
						row == DBSeerDataSet.TYPE_HEADER ||
						row == DBSeerDataSet.TYPE_MONITORING_DATA ||
						row == DBSeerDataSet.TYPE_PERCENTILE_LATENCY ||
						row == DBSeerDataSet.TYPE_TRANSACTION_COUNT ||
						row == DBSeerDataSet.TYPE_NUM_TRANSACTION_TYPE ||
						row == DBSeerDataSet.TYPE_STATEMENT_FILE ||
						row == DBSeerDataSet.TYPE_QUERY_FILE ||
						row == DBSeerDataSet.TYPE_TRANSACTION_FILE)
				{
					cell.setForeground(Color.LIGHT_GRAY);
				}
				else
				{
					cell.setForeground(Color.BLACK);
				}
				return cell;
			}
		};
		table.getColumnModel().getColumn(0).setCellRenderer(customRenderder);
		table.getColumnModel().getColumn(1).setCellRenderer(customRenderder);

		table.setFillsViewportHeight(true);
		table.getColumnModel().getColumn(0).setMaxWidth(400);
		table.getColumnModel().getColumn(0).setPreferredWidth(300);
		table.getColumnModel().getColumn(1).setPreferredWidth(600);
		table.setRowHeight(20);

		for (String header : tableHeaders)
		{
			if (header.equalsIgnoreCase("Use Entire DataSet"))
				tableModel.addRow(new Object[]{header, Boolean.TRUE});
			else
				tableModel.addRow(new Object[]{header, ""});
		}

		for (int i = 0; i < numTransactionTypes; ++i)
		{
			tableModel.addRow(new Object[]{"Name of Transaction Type " + (i+1), "Type " + (i+1)});
		}

		this.updateTable();
		dataSetLoaded = false;

		tableModel.setUseEntireDataSet(this.useEntireDataSet.booleanValue());
	}

	public void backup()
	{
		this.nameBackup = name;
		this.transactionFilePathBackup = transactionFilePath;
		this.statementFilePathBackup = statementFilePath;
		this.queryFilePathBackup = queryFilePath;
		this.monitoringDataPathBackup = monitoringDataPath;
		this.transCountPathBackup = transCountPath;
		this.averageLatencyPathBackup = averageLatencyPath;
		this.percentileLatencyPathBackup = percentileLatencyPath;
		this.headerPathBackup = headerPath;
		this.pageInfoPathBackup = pageInfoPath;
		this.statementStatPathBackup = statementStatPath;

		this.useEntireDataSetBackup = useEntireDataSet;
		this.startIndexBackup = startIndex;
		this.endIndexBackup = endIndex;
		this.numTransactionTypesBackup = numTransactionTypes;

		this.transactionTypeNamesBackup.clear();
		for (String name : this.transactionTypeNames)
		{
			this.transactionTypeNamesBackup.add(name);
		}
	}

	public void restore()
	{
		this.name = nameBackup;
		this.transactionFilePath = transactionFilePathBackup;
		this.statementFilePath = statementFilePathBackup;
		this.queryFilePath = queryFilePathBackup;
		this.monitoringDataPath = monitoringDataPathBackup;
		this.transCountPath = transCountPathBackup;
		this.averageLatencyPath = averageLatencyPathBackup;
		this.percentileLatencyPath = percentileLatencyPathBackup;
		this.headerPath = headerPathBackup;
		this.pageInfoPath = pageInfoPathBackup;
		this.statementStatPath = statementStatPathBackup;

		this.useEntireDataSet = useEntireDataSetBackup;
		this.startIndex = startIndexBackup;
		this.endIndex = endIndexBackup;
		this.numTransactionTypes = numTransactionTypesBackup;

		this.transactionTypeNames.clear();
		for (String name : this.transactionTypeNamesBackup)
		{
			this.transactionTypeNames.add(name);
		}
	}

	private void useDefaultIfNull()
	{
		if (this.name == null) this.name = "";
		if (this.transactionFilePath == null) this.transactionFilePath = "";
		if (this.statementFilePath == null) this.statementFilePath = "";
		if (this.queryFilePath == null) this.queryFilePath = "";
		if (this.monitoringDataPath == null) this.monitoringDataPath = "";
		if (this.transCountPath == null) this.transCountPath = "";
		if (this.averageLatencyPath == null) this.averageLatencyPath = "";
		if (this.percentileLatencyPath == null) this.percentileLatencyPath = "";
		if (this.headerPath == null) this.headerPath = "";
		if (this.pageInfoPath == null) this.pageInfoPath = "";
		if (this.statementStatPath == null) this.statementStatPath = "";

		if (this.useEntireDataSet == null) this.useEntireDataSet = true;
		if (this.transactionTypeNames == null) this.transactionTypeNames = new ArrayList<String>();
		if (this.transactionTypeNamesBackup == null) this.transactionTypeNamesBackup = new ArrayList<String>();
		for (int i = 0; i < this.transactionTypeNames.size(); ++i)
		{
			if (this.transactionTypeNames.get(i) == null)
			{
				this.transactionTypeNames.set(i, "");
			}
		}
		if (this.transactionSampleLists == null) this.transactionSampleLists = new ArrayList<DBSeerTransactionSampleList>();
		if (this.statementsOffsetFiles == null) this.statementsOffsetFiles = new ArrayList<String>();
	}

	private Object readResolve()
	{
		useDefaultIfNull();
		Boolean[] trueFalse = {Boolean.TRUE, Boolean.FALSE};
		JComboBox trueFalseBox = new JComboBox(trueFalse);
		final DefaultCellEditor dce = new DefaultCellEditor(trueFalseBox);

		uniqueVariableName = "dataset_" + UUID.randomUUID().toString().replace('-', '_');
		tableModel = new DBSeerDataSetTableModel(null, new String[]{"Name", "Value"});
		tableModel.addTableModelListener(this);
		table = new JTable(tableModel){
			@Override
			public TableCellEditor getCellEditor(int row, int col)
			{
				if (row == DBSeerDataSet.TYPE_USE_ENTIRE_DATASET && col == 1)
					return dce;
				return super.getCellEditor(row, col);
			}
		};
		DefaultTableCellRenderer customRenderer = new DefaultTableCellRenderer(){
			@Override
			public Component getTableCellRendererComponent(JTable jTable, Object o, boolean b, boolean b2, int row, int col)
			{
				Component cell = super.getTableCellRendererComponent(jTable, o, b, b2, row, col);
				if (row == DBSeerDataSet.TYPE_START_INDEX || row == DBSeerDataSet.TYPE_END_INDEX)
				{
					if (((Boolean)table.getValueAt(DBSeerDataSet.TYPE_USE_ENTIRE_DATASET, 1)).booleanValue() == true)
					{
						cell.setForeground(Color.LIGHT_GRAY);
					}
					else
					{
						cell.setForeground(Color.BLACK);
					}
				}
				else if (row == DBSeerDataSet.TYPE_AVERAGE_LATENCY ||
						row == DBSeerDataSet.TYPE_HEADER ||
						row == DBSeerDataSet.TYPE_MONITORING_DATA ||
						row == DBSeerDataSet.TYPE_PERCENTILE_LATENCY ||
						row == DBSeerDataSet.TYPE_TRANSACTION_COUNT ||
						row == DBSeerDataSet.TYPE_NUM_TRANSACTION_TYPE ||
						row == DBSeerDataSet.TYPE_QUERY_FILE ||
						row == DBSeerDataSet.TYPE_STATEMENT_FILE ||
						row == DBSeerDataSet.TYPE_TRANSACTION_FILE)
				{
					cell.setForeground(Color.LIGHT_GRAY);
				}
				else
				{
					cell.setForeground(Color.BLACK);
				}
				return cell;
			}
		};
		table.getColumnModel().getColumn(0).setCellRenderer(customRenderer);
		table.getColumnModel().getColumn(1).setCellRenderer(customRenderer);

		table.setFillsViewportHeight(true);
		table.getColumnModel().getColumn(0).setMaxWidth(400);
		table.getColumnModel().getColumn(0).setPreferredWidth(300);
		table.getColumnModel().getColumn(1).setPreferredWidth(600);
		table.setRowHeight(20);

		for (String header : tableHeaders)
		{
			if (header.equalsIgnoreCase("Use Entire DataSet"))
				tableModel.addRow(new Object[]{header, Boolean.TRUE});
			else
				tableModel.addRow(new Object[]{header, ""});
		}
		for (int i = 0; i < numTransactionTypes; ++i)
		{
			tableModel.addRow(new Object[]{"Name of Transaction Type " + (i+1), "Type " + (i+1)});
		}
		this.updateTable();
		dataSetLoaded = false;
		tableModel.setUseEntireDataSet(this.useEntireDataSet.booleanValue());

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

		XStreamHelper xmlHelper = new XStreamHelper();
		statementsOffsetFiles.clear();
		transactionSampleLists.clear();

		for (int i = 0; i < this.numTransactionTypes; ++i)
		{
			String datasetPath = new File(this.averageLatencyPath).getParent();
			String samplePath = datasetPath + File.separator + "transaction_" + (i+1) + ".xml";
			String offsetPath = datasetPath + File.separator + "transaction_" + (i+1) + ".stmt";

			statementsOffsetFiles.add(offsetPath);

			if (new File(samplePath).exists())
			{
				DBSeerTransactionSampleList sampleList = (DBSeerTransactionSampleList) xmlHelper.fromXML(samplePath);
				transactionSampleLists.add(sampleList);
			}
			else
			{
				DBSeerTransactionSampleList sampleList = new DBSeerTransactionSampleList();
				transactionSampleLists.add(sampleList);
			}
		}

		if (dataSetLoaded == false)
		{
			MatlabProxy proxy = DBSeerGUI.proxy;
			String dbseerPath = DBSeerGUI.userSettings.getDBSeerRootPath();

			try
			{
				proxy.eval("rmpath " + dbseerPath + ";");
				proxy.eval("rmpath " + dbseerPath + "/common_mat;");
				proxy.eval("rmpath " + dbseerPath + "/predict_mat;");
				proxy.eval("rmpath " + dbseerPath + "/predict_data;");
				proxy.eval("rmpath " + dbseerPath + "/predict_mat/prediction_center;");

				proxy.eval("addpath " + dbseerPath + ";");
				proxy.eval("addpath " + dbseerPath + "/common_mat;");
				proxy.eval("addpath " + dbseerPath + "/predict_mat;");
				proxy.eval("addpath " + dbseerPath + "/predict_data;");
				proxy.eval("addpath " + dbseerPath + "/predict_mat/prediction_center;");

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
				if (this.useEntireDataSet.booleanValue())
				{
					proxy.eval(this.uniqueVariableName + ".use_entire = true;");
				}
				else
				{
					proxy.eval(this.uniqueVariableName + ".use_entire = false;");
				}
				proxy.eval(this.uniqueVariableName + ".loadStatistics;");


			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

			dataSetLoaded = true;
		}
	}

	public void loadModelVariable()
	{
		// set the unique name for mv first.
		if (uniqueModelVariableName == "")
		{
			uniqueModelVariableName = "mv_" + UUID.randomUUID().toString().replace('-', '_');
		}

		// load dataset if it already has not been done.
		if (!dataSetLoaded)
		{
			loadDataset();
		}

		if (!modelVariableLoaded)
		{
			MatlabProxy proxy = DBSeerGUI.proxy;
			String dbseerPath = DBSeerGUI.userSettings.getDBSeerRootPath();

			try
			{
				proxy.eval("rmpath " + dbseerPath + ";");
				proxy.eval("rmpath " + dbseerPath + "/common_mat;");
				proxy.eval("rmpath " + dbseerPath + "/predict_mat;");
				proxy.eval("rmpath " + dbseerPath + "/predict_data;");
				proxy.eval("rmpath " + dbseerPath + "/predict_mat/prediction_center;");

				proxy.eval("addpath " + dbseerPath + ";");
				proxy.eval("addpath " + dbseerPath + "/common_mat;");
				proxy.eval("addpath " + dbseerPath + "/predict_mat;");
				proxy.eval("addpath " + dbseerPath + "/predict_data;");
				proxy.eval("addpath " + dbseerPath + "/predict_mat/prediction_center;");

				proxy.eval("[mvGrouped " + uniqueModelVariableName + "] = load_mv(" +
						uniqueVariableName + ".header," +
						uniqueVariableName + ".monitor," +
						uniqueVariableName + ".averageLatency," +
						uniqueVariableName + ".percentileLatency," +
						uniqueVariableName + ".transactionCount," +
						uniqueVariableName + ".diffedMonitor," +
						uniqueVariableName + ".statementStat);");
			}
			catch (Exception e)
			{
				DBSeerExceptionHandler.handleException(e);
			}
		}
	}

	public String getUniqueModelVariableName()
	{
		return uniqueModelVariableName;
	}

	public List<String> getStatementOffsetFileList()
	{
		return statementsOffsetFiles;
	}

	public boolean validateTable()
	{
		boolean useEntire = false;
		for (int i = 0; i < tableModel.getRowCount(); ++i)
		{
			for (int j = 0; j < tableHeaders.length; ++j)
			{
				if (tableModel.getValueAt(i, 0).equals(tableHeaders[j]))
				{
					switch (j)
					{
						case TYPE_USE_ENTIRE_DATASET:
							useEntire = ((Boolean) tableModel.getValueAt(i, 1)).booleanValue();
							break;
					}
				}
			}
		}

		HashSet<String> checkDuplicates = new HashSet<String>();

		for (int i = 0; i < tableModel.getRowCount(); ++i)
		{
			for (int j = 0; j < tableHeaders.length; ++j)
			{
				if (tableModel.getValueAt(i,0).equals(tableHeaders[j]))
				{
					switch (j)
					{
						case TYPE_END_INDEX:
						{
							if (!useEntire && !UserInputValidator.validateNumber((String) tableModel.getValueAt(i, 1)))
							{
								JOptionPane.showMessageDialog(null, "Please enter end index correctly.\nIt has to be an integer.",
										"Warning", JOptionPane.WARNING_MESSAGE);
								return false;
							}
							break;
						}
						case TYPE_START_INDEX:
						{
							if (!useEntire && !UserInputValidator.validateNumber((String) tableModel.getValueAt(i, 1)))
							{
								JOptionPane.showMessageDialog(null, "Please enter start index correctly.\nIt has to be an integer.",
										"Warning", JOptionPane.WARNING_MESSAGE);
								return false;
							}
							break;
						}
						default:
							break;
					}
					break;
				}
			}
			if (i > TYPE_NUM_TRANSACTION_TYPE)
			{
				if (!checkDuplicates.add((String)tableModel.getValueAt(i, 1)) && tableModel.getValueAt(i, 1) != "")
				{
					JOptionPane.showMessageDialog(null, "Please enter transaction type names correctly.\nEach name has to be unique.",
							"Warning", JOptionPane.WARNING_MESSAGE);
					return false;
				}
			}
		}
		return true;
	}

	public void setFromTable()
	{
		TableCellEditor editor = table.getCellEditor();
		if ( editor != null ) editor.stopCellEditing(); // stop editing
		transactionTypeNames.clear();

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
						case TYPE_TRANSACTION_FILE:
							this.transactionFilePath = (String)tableModel.getValueAt(i, 1);
							break;
						case TYPE_STATEMENT_FILE:
							this.statementFilePath = (String)tableModel.getValueAt(i, 1);
							break;
						case TYPE_QUERY_FILE:
							this.queryFilePath = (String)tableModel.getValueAt(i, 1);
							break;
						case TYPE_AVERAGE_LATENCY:
							this.averageLatencyPath = (String)tableModel.getValueAt(i, 1);
							break;
						case TYPE_END_INDEX:
							if (UserInputValidator.validateNumber((String)tableModel.getValueAt(i, 1)))
								this.endIndex = Integer.parseInt((String)tableModel.getValueAt(i, 1));
							break;
						case TYPE_HEADER:
							this.headerPath = (String)tableModel.getValueAt(i, 1);
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
//						case TYPE_STATEMENT_STAT:
//							this.statementStatPath = (String)tableModel.getValueAt(i, 1);
//							break;
						case TYPE_USE_ENTIRE_DATASET:
							this.useEntireDataSet = (Boolean)tableModel.getValueAt(i, 1);
							break;
						case TYPE_START_INDEX:
							if (UserInputValidator.validateNumber((String)tableModel.getValueAt(i, 1)))
								this.startIndex = Integer.parseInt((String) tableModel.getValueAt(i, 1));
							break;
						case TYPE_TRANSACTION_COUNT:
							this.transCountPath = (String)tableModel.getValueAt(i, 1);
							break;
						case TYPE_NUM_TRANSACTION_TYPE:
							if (UserInputValidator.validateNumber((String)tableModel.getValueAt(i, 1)))
								this.numTransactionTypes = Integer.parseInt((String) tableModel.getValueAt(i, 1));
							break;
						default:
							break;
					}
					break;
				}
			}
			if (i > TYPE_NUM_TRANSACTION_TYPE)
			{
//				transactionTypeNames.add(i-TYPE_NUM_TRANSACTION_TYPE-1, (String)tableModel.getValueAt(i, 1));
				transactionTypeNames.add((String) tableModel.getValueAt(i, 1));
			}
		}
		dataSetLoaded = false;
	}

	public void updateTable()
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
							tableModel.setValueAt(this.name, i, 1);
							break;
						case TYPE_TRANSACTION_FILE:
							tableModel.setValueAt(this.transactionFilePath, i, 1);
							break;
						case TYPE_STATEMENT_FILE:
							tableModel.setValueAt(this.statementFilePath, i, 1);
							break;
						case TYPE_QUERY_FILE:
							tableModel.setValueAt(this.queryFilePath, i, 1);
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
//						case TYPE_STATEMENT_STAT:
//							tableModel.setValueAt(this.statementStatPath, i, 1);
//							break;
						case TYPE_USE_ENTIRE_DATASET:
							tableModel.setValueAt(this.useEntireDataSet.booleanValue(), i, 1);
							break;
						case TYPE_START_INDEX:
							tableModel.setValueAt(String.valueOf(this.startIndex), i, 1);
							break;
						case TYPE_TRANSACTION_COUNT:
							tableModel.setValueAt(this.transCountPath, i, 1);
							break;
						case TYPE_NUM_TRANSACTION_TYPE:
							tableModel.setValueAt(String.valueOf(this.numTransactionTypes), i, 1);
							break;
						default:
							break;
					}
					break;
				}
			}

			if (i > TYPE_NUM_TRANSACTION_TYPE)
			{
				if (i - TYPE_NUM_TRANSACTION_TYPE <= transactionTypeNames.size())
				{
					tableModel.setValueAt(this.transactionTypeNames.get(i - TYPE_NUM_TRANSACTION_TYPE - 1), i, 1);
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
		setFromTable();
		this.name = name;
		updateTable();
		tableModel.fireTableDataChanged();
		this.dataSetLoaded = false;
	}

	public synchronized String getTransactionFilePath()
	{
		return transactionFilePath;
	}

	public synchronized void setTransactionFilePath(String transactionFilePath)
	{
		setFromTable();
		this.transactionFilePath = transactionFilePath;
		updateTable();
		tableModel.fireTableDataChanged();
		this.dataSetLoaded = false;
	}

	public synchronized String getStatementFilePath()
	{
		return statementFilePath;
	}

	public synchronized void setStatementFilePath(String statementFilePath)
	{
		setFromTable();
		this.statementFilePath = statementFilePath;
		updateTable();
		tableModel.fireTableDataChanged();
		this.dataSetLoaded = false;
	}

	public synchronized String getQueryFilePath()
	{
		return queryFilePath;
	}

	public synchronized void setQueryFilePath(String queryFilePath)
	{
		setFromTable();
		this.queryFilePath = queryFilePath;
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
		setFromTable();
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
		setFromTable();
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
		setFromTable();
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
		setFromTable();
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
		setFromTable();
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
		setFromTable();
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
		setFromTable();
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
		setFromTable();
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
		setFromTable();
		this.statementStatPath = statementStatPath;
		updateTable();
		tableModel.fireTableDataChanged();
		this.dataSetLoaded = false;
	}

	public synchronized Boolean getUseEntireDataSet()
	{
		return useEntireDataSet;
	}

	public synchronized void setUseEntireDataSet(Boolean useEntireDataSet)
	{
		setFromTable();
		this.useEntireDataSet = useEntireDataSet;
		updateTable();
		tableModel.fireTableDataChanged();
		this.dataSetLoaded = false;
	}

	public synchronized int getNumTransactionTypes()
	{
		return numTransactionTypes;
	}

	public synchronized void setNumTransactionTypes(int numTransactionTypes)
	{
		setFromTable();
		this.numTransactionTypes = numTransactionTypes;
		updateTable();
		tableModel.fireTableDataChanged();
		this.dataSetLoaded = false;
	}

	public synchronized List<String> getTransactionTypeNames()
	{
		return transactionTypeNames;
	}

	public synchronized List<DBSeerTransactionSampleList> getTransactionSampleLists()
	{
		return transactionSampleLists;
	}

	@Override
	public void tableChanged(TableModelEvent tableModelEvent)
	{
		if (tableModelEvent.getFirstRow() == DBSeerDataSet.TYPE_USE_ENTIRE_DATASET &&
				tableModelEvent.getColumn() == 1)
		{
			if (((Boolean)table.getValueAt(DBSeerDataSet.TYPE_USE_ENTIRE_DATASET, 1)).booleanValue() == true)
			{
				tableModel.setUseEntireDataSet(true);
			}
			else
			{
				tableModel.setUseEntireDataSet(false);
			}
		}
		if (tableModelEvent.getFirstRow() == DBSeerDataSet.TYPE_NUM_TRANSACTION_TYPE &&
				tableModelEvent.getColumn() == 1)
		{
			int numTransactionType = (Integer.parseInt((String)table.getValueAt(DBSeerDataSet.TYPE_NUM_TRANSACTION_TYPE, 1)));
			int currentRowCount = tableModel.getRowCount();

			ArrayList<String> previousTypeNames = new ArrayList<String>();
			for (int i = DBSeerDataSet.TYPE_NUM_TRANSACTION_TYPE + 1; i < tableModel.getRowCount(); ++i)
			{
				previousTypeNames.add((String)table.getValueAt(i, 1));
			}

			for (int i = currentRowCount-1; i > DBSeerDataSet.TYPE_NUM_TRANSACTION_TYPE; --i)
			{
				tableModel.removeRow(i);
			}

			for (int i = 0; i < numTransactionType; ++i)
			{
				String name = (i < previousTypeNames.size()) ? previousTypeNames.get(i) : "Type " + (i+1);
				tableModel.addRow(new Object[]{"Name of Transaction Type " + (i+1), name});
			}
		}
	}

	public void setReinitialize()
	{
		this.dataSetLoaded = false;
		this.modelVariableLoaded = false;
	}
}
