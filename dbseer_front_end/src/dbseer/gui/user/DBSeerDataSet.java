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
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import dbseer.comp.UserInputValidator;
import dbseer.gui.DBSeerConstants;
import dbseer.gui.DBSeerExceptionHandler;
import dbseer.gui.DBSeerGUI;
import dbseer.gui.xml.XStreamHelper;
import dbseer.stat.StatisticalPackageRunner;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.io.*;
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

	protected static int idToAssign = 0;

	@XStreamOmitField
	protected boolean dataSetLoaded = false;
	@XStreamOmitField
	protected boolean modelVariableLoaded = false;
	@XStreamOmitField
	protected String uniqueVariableName = "";
	@XStreamOmitField
	protected String uniqueModelVariableName = "";

	protected String name = "";

	protected String transactionFilePath = "";
	protected String statementFilePath = "";
	protected String queryFilePath = "";
	protected String monitoringDataPath = "";
	protected String transCountPath = "";
	protected String averageLatencyPath = "";
	protected String percentileLatencyPath = "";
	protected String headerPath = "";
	protected String pageInfoPath = "";
	protected String statementStatPath = "";

	protected Boolean useEntireDataSet = true;
	@XStreamOmitField
	protected Boolean live;

	protected int startIndex = 0;
	protected int endIndex = 0;

	protected int numTransactionTypes = 0;

	@XStreamImplicit
	protected java.util.List<String> transactionTypeNames = new ArrayList<String>();
	@XStreamImplicit
	protected java.util.Set<Integer> validTransactions = new HashSet<Integer>();

	@XStreamOmitField
	protected String transactionFilePathBackup = "";
	@XStreamOmitField
	protected String statementFilePathBackup = "";
	@XStreamOmitField
	protected String queryFilePathBackup = "";
	@XStreamOmitField
	protected String nameBackup = "";
	@XStreamOmitField
	protected String monitoringDataPathBackup = "";
	@XStreamOmitField
	protected String transCountPathBackup = "";
	@XStreamOmitField
	protected String averageLatencyPathBackup = "";
	@XStreamOmitField
	protected String percentileLatencyPathBackup = "";
	@XStreamOmitField
	protected String headerPathBackup = "";
	@XStreamOmitField
	protected String pageInfoPathBackup = "";
	@XStreamOmitField
	protected String statementStatPathBackup = "";
	@XStreamOmitField
	protected Boolean useEntireDataSetBackup = true;

	@XStreamOmitField
	protected int numTransactionTypesBackup = 0;
	@XStreamOmitField
	protected int startIndexBackup = 0;
	@XStreamOmitField
	protected int endIndexBackup = 0;

	@XStreamOmitField
	protected java.util.List<String> transactionTypeNamesBackup = new ArrayList<String>();
	@XStreamOmitField
	protected java.util.Set<Integer> validTransactionsBackup = new HashSet<Integer>();

	@XStreamOmitField
	protected java.util.List<DBSeerTransactionSampleList> transactionSampleLists = new ArrayList<DBSeerTransactionSampleList>();

	@XStreamOmitField
	protected java.util.List<String> statementsOffsetFiles = new ArrayList<String>();

//	protected String IOConfiguration = "[]";
//	protected String lockConfiguration = "[]";

	@XStreamOmitField
	protected JTable table;

	@XStreamOmitField
	protected DBSeerDataSetTableModel tableModel;

	public DBSeerDataSet()
	{
		live = new Boolean(false);
		Boolean[] trueFalse = {Boolean.TRUE, Boolean.FALSE};
		JComboBox trueFalseBox = new JComboBox(trueFalse);
		final DefaultCellEditor dce = new DefaultCellEditor(trueFalseBox);

		uniqueVariableName = "dataset_" + UUID.randomUUID().toString().replace('-', '_');
		uniqueModelVariableName = "mv_" + UUID.randomUUID().toString().replace('-', '_');
		name = "Unnamed dataset";
		tableModel = new DBSeerDataSetTableModel(null, new String[]{"Name", "Value"}, true);
		tableModel.addTableModelListener(this);
		table = new JTable(tableModel){
			@Override
			public TableCellEditor getCellEditor(int row, int col)
			{
				if ((row == DBSeerDataSet.TYPE_USE_ENTIRE_DATASET || row > TYPE_NUM_TRANSACTION_TYPE + numTransactionTypes) &&
						col == 1)
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
			validTransactions.add(i+1);
		}

		for (int i = 0; i < numTransactionTypes; ++i)
		{
			if (validTransactions.contains(i+1))
			{
				tableModel.addRow(new Object[]{"Use Transaction Type " + (i + 1), Boolean.TRUE});
			}
			else
			{
				tableModel.addRow(new Object[]{"Use Transaction Type " + (i + 1), Boolean.FALSE});
			}
		}

		this.updateTable();
		dataSetLoaded = false;

		tableModel.setUseEntireDataSet(this.useEntireDataSet.booleanValue());
	}

	public DBSeerDataSet(boolean isLive)
	{
		live = new Boolean(isLive);
		Boolean[] trueFalse = {Boolean.TRUE, Boolean.FALSE};
		JComboBox trueFalseBox = new JComboBox(trueFalse);
		final DefaultCellEditor dce = new DefaultCellEditor(trueFalseBox);

		uniqueVariableName = "dataset_" + UUID.randomUUID().toString().replace('-', '_');
		uniqueModelVariableName = "mv_" + UUID.randomUUID().toString().replace('-', '_');
		if (isLive)
		{
			name = "Live Data";
		}
		tableModel = new DBSeerDataSetTableModel(null, new String[]{"Name", "Value"}, !isLive);
		tableModel.addTableModelListener(this);
		table = new JTable(tableModel){
			@Override
			public TableCellEditor getCellEditor(int row, int col)
			{
				if ((row == DBSeerDataSet.TYPE_USE_ENTIRE_DATASET || row > TYPE_NUM_TRANSACTION_TYPE + numTransactionTypes) &&
						col == 1)
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

		this.updateLiveDataSet();

		for (int i = 0; i < numTransactionTypes; ++i)
		{
			tableModel.addRow(new Object[]{"Name of Transaction Type " + (i+1), "Type " + (i+1)});
			validTransactions.add(i+1);
		}

		for (int i = 0; i < numTransactionTypes; ++i)
		{
			if (validTransactions.contains(i+1))
			{
				tableModel.addRow(new Object[]{"Use Transaction Type " + (i + 1), Boolean.TRUE});
			}
			else
			{
				tableModel.addRow(new Object[]{"Use Transaction Type " + (i + 1), Boolean.FALSE});
			}
		}

		this.updateTable();
		dataSetLoaded = false;

		tableModel.setUseEntireDataSet(this.useEntireDataSet.booleanValue());
	}

	public void updateLiveDataSet()
	{
		// only do this if the dataset is live dataset.
		if (live)
		{
			String livePath = DBSeerGUI.userSettings.getDBSeerRootPath() + File.separator + DBSeerConstants.LIVE_DATASET_PATH;
			this.averageLatencyPath = livePath + File.separator + "avg_latency";
			this.transCountPath = livePath + File.separator + "trans_count";
			this.transactionFilePath = livePath + File.separator + "allLogs-t.txt";
			this.statementFilePath = livePath + File.separator + "allLogs-s.txt";
			this.queryFilePath = livePath + File.separator + "allLogs-q.txt";
			this.headerPath = livePath + File.separator + "dataset_header.m";
			this.monitoringDataPath = livePath + File.separator + "monitor";

			// check valid transactions.
			validTransactions.clear();
			File transTypeFile = new File(livePath + File.separator + "trans_type");

			// use trans_type if exists
			int numTrans = 0;
			if (transTypeFile.exists())
			{
				try
				{
					BufferedReader br = new BufferedReader(new FileReader(transTypeFile));
					String line;
					while ((line = br.readLine()) != null)
					{
						if (line.isEmpty())
						{
							continue;
						}
						else
						{
							String[] inds = line.split(",");
							for (String ind : inds)
							{
								int indexNum = Integer.parseInt(ind);
								validTransactions.add(indexNum);
								if (indexNum > numTrans)
								{
									numTrans = indexNum;
								}
							}
							break;
						}
					}
					this.numTransactionTypes = numTrans;
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
			// otherwise, count prctile_latency_* files
			else
			{
				File liveDir = new File(livePath);
				FileFilter filter = new WildcardFileFilter("prctile_latency_*");
				File[] files = liveDir.listFiles(filter);
				if (files != null)
				{
					this.numTransactionTypes = files.length;
					for (int i = 0; i < numTransactionTypes; ++i)
					{
						validTransactions.add(i);
					}
				}
			}

			if (this.numTransactionTypes == 0)
			{
//				DBSeerExceptionHandler.handleException(new Exception("Unable to figure out the number of transactions for the live dataset."));
			}

			this.updateTable();
		}
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

		this.validTransactionsBackup.clear();
		for (Integer valid : this.validTransactions)
		{
			this.validTransactionsBackup.add(valid);
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

		this.validTransactions.clear();
		for (Integer valid : this.validTransactionsBackup)
		{
			this.validTransactions.add(valid);
		}
	}

	protected void useDefaultIfNull()
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

		if (this.live == null) this.live = new Boolean(false);
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
		if (this.validTransactions == null) this.validTransactions = new HashSet<Integer>();
		if (this.validTransactionsBackup == null) this.validTransactionsBackup = new HashSet<Integer>();
		if (this.transactionSampleLists == null) this.transactionSampleLists = new ArrayList<DBSeerTransactionSampleList>();
		if (this.statementsOffsetFiles == null) this.statementsOffsetFiles = new ArrayList<String>();
	}

	protected Object readResolve()
	{
		useDefaultIfNull();
		Boolean[] trueFalse = {Boolean.TRUE, Boolean.FALSE};
		JComboBox trueFalseBox = new JComboBox(trueFalse);
		final DefaultCellEditor dce = new DefaultCellEditor(trueFalseBox);

		uniqueVariableName = "dataset_" + UUID.randomUUID().toString().replace('-', '_');
		tableModel = new DBSeerDataSetTableModel(null, new String[]{"Name", "Value"}, true);
		tableModel.addTableModelListener(this);
		table = new JTable(tableModel){
			@Override
			public TableCellEditor getCellEditor(int row, int col)
			{
				if ((row == DBSeerDataSet.TYPE_USE_ENTIRE_DATASET || row > TYPE_NUM_TRANSACTION_TYPE + numTransactionTypes) &&
						col == 1)
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
		if (live)
		{
			updateLiveDataSet();
		}
		for (int i = 0; i < numTransactionTypes; ++i)
		{
			if (validTransactions.contains(i+1))
			{
				tableModel.addRow(new Object[]{"Use Transaction Type " + (i + 1), Boolean.TRUE});
			}
			else
			{
				tableModel.addRow(new Object[]{"Use Transaction Type " + (i + 1), Boolean.FALSE});
			}
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

		if (dataSetLoaded == false || live)
		{
			StatisticalPackageRunner runner = DBSeerGUI.runner;
			String dbseerPath = DBSeerGUI.userSettings.getDBSeerRootPath();

			try
			{
				runner.eval("rmpath " + dbseerPath + ";");
				runner.eval("rmpath " + dbseerPath + "/common_mat;");
				runner.eval("rmpath " + dbseerPath + "/predict_mat;");
				runner.eval("rmpath " + dbseerPath + "/predict_data;");
				runner.eval("rmpath " + dbseerPath + "/predict_mat/prediction_center;");

				runner.eval("addpath " + dbseerPath + ";");
				runner.eval("addpath " + dbseerPath + "/common_mat;");
				runner.eval("addpath " + dbseerPath + "/predict_mat;");
				runner.eval("addpath " + dbseerPath + "/predict_data;");
				runner.eval("addpath " + dbseerPath + "/predict_mat/prediction_center;");

				runner.eval(this.uniqueVariableName + " = DataSet;");
				runner.eval(this.uniqueVariableName + ".header_path = '" + this.headerPath + "';");
				runner.eval(this.uniqueVariableName + ".monitor_path = '" + this.monitoringDataPath + "';");
				runner.eval(this.uniqueVariableName + ".avg_latency_path = '" + this.averageLatencyPath + "';");
				runner.eval(this.uniqueVariableName + ".percentile_latency_path = '" +
						this.percentileLatencyPath + "';");
				runner.eval(this.uniqueVariableName + ".trans_count_path = '" + this.transCountPath + "';");
				runner.eval(this.uniqueVariableName + ".statement_stat_path = '" + this.statementStatPath + "';");
				//runner.eval(this.uniqueVariableName + ".page_info_path = '" + this.pageInfoPath + "';");
				runner.eval(this.uniqueVariableName + ".startIdx = " + this.startIndex + ";");
				runner.eval(this.uniqueVariableName + ".endIdx = " + this.endIndex + ";");
				if (this.useEntireDataSet.booleanValue())
				{
					runner.eval(this.uniqueVariableName + ".use_entire = true;");
				}
				else
				{
					runner.eval(this.uniqueVariableName + ".use_entire = false;");
				}
				String tranType = "[";
				for (int i = 0; i < transactionTypeNames.size(); ++i)
				{
					if (validTransactions.contains(i+1))
					{
						tranType += (i+1) + " ";
					}
				}
				tranType += "]";
				runner.eval(this.uniqueVariableName + ".tranTypes = " + tranType);
				runner.eval(this.uniqueVariableName + ".loadStatistics;");
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
		if (!dataSetLoaded || live)
		{
			loadDataset();
		}

		if (!modelVariableLoaded)
		{
			StatisticalPackageRunner runner = DBSeerGUI.runner;
			String dbseerPath = DBSeerGUI.userSettings.getDBSeerRootPath();

			try
			{
				runner.eval("rmpath " + dbseerPath + ";");
				runner.eval("rmpath " + dbseerPath + "/common_mat;");
				runner.eval("rmpath " + dbseerPath + "/predict_mat;");
				runner.eval("rmpath " + dbseerPath + "/predict_data;");
				runner.eval("rmpath " + dbseerPath + "/predict_mat/prediction_center;");

				runner.eval("addpath " + dbseerPath + ";");
				runner.eval("addpath " + dbseerPath + "/common_mat;");
				runner.eval("addpath " + dbseerPath + "/predict_mat;");
				runner.eval("addpath " + dbseerPath + "/predict_data;");
				runner.eval("addpath " + dbseerPath + "/predict_mat/prediction_center;");

				runner.eval("[mvGrouped " + uniqueModelVariableName + "] = load_mv(" +
						uniqueVariableName + ".header," +
						uniqueVariableName + ".monitor," +
						uniqueVariableName + ".averageLatency," +
						uniqueVariableName + ".percentileLatency," +
						uniqueVariableName + ".transactionCount," +
						uniqueVariableName + ".diffedMonitor," +
						uniqueVariableName + ".statementStat," +
						uniqueVariableName + ".tranTypes);");
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
//			if (i > TYPE_NUM_TRANSACTION_TYPE && i < )
			if (i > TYPE_NUM_TRANSACTION_TYPE && i < TYPE_NUM_TRANSACTION_TYPE + this.numTransactionTypes)
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
		validTransactions.clear();

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
			if (i > TYPE_NUM_TRANSACTION_TYPE && i <= TYPE_NUM_TRANSACTION_TYPE + this.numTransactionTypes)
			{
//				transactionTypeNames.add(i-TYPE_NUM_TRANSACTION_TYPE-1, (String)tableModel.getValueAt(i, 1));
				transactionTypeNames.add((String) tableModel.getValueAt(i, 1));
			}
			if (i > TYPE_NUM_TRANSACTION_TYPE + numTransactionTypes)
			{
				int idx = i - TYPE_NUM_TRANSACTION_TYPE - numTransactionTypes;
				if (idx <= numTransactionTypes && idx >= 1)
				{
					if ( ((Boolean)tableModel.getValueAt(i, 1)).booleanValue() )
					{
						validTransactions.add(idx);
					}
					else
					{
						validTransactions.remove(idx);
					}
				}
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
			if (i > TYPE_NUM_TRANSACTION_TYPE + numTransactionTypes)
			{
				if (i - TYPE_NUM_TRANSACTION_TYPE - numTransactionTypes <= numTransactionTypes)
				{
					if (this.validTransactions.contains(i - TYPE_NUM_TRANSACTION_TYPE - numTransactionTypes))
					{
						tableModel.setValueAt(true, i, 1);
					}
					else
					{
						tableModel.setValueAt(false, i, 1);
					}
				}
			}
		}
		tableModel.fireTableDataChanged();
	}

	public synchronized String getName()
	{
		return name;
	}

	public synchronized void setName(String name)
	{
//		setFromTable();
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
//		setFromTable();
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
//		setFromTable();
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
//		setFromTable();
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
//		setFromTable();
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
//		setFromTable();
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
//		setFromTable();
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
//		setFromTable();
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
//		setFromTable();
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
//		setFromTable();
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
//		setFromTable();
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
//		setFromTable();
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
//		setFromTable();
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
//		setFromTable();
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
//		setFromTable();
		this.numTransactionTypes = numTransactionTypes;
		updateTable();
		tableModel.fireTableDataChanged();
		this.dataSetLoaded = false;
	}

	public synchronized List<String> getTransactionTypeNames()
	{
		ArrayList<String> validTransactionNames = new ArrayList<String>();
		for (int i = 0; i < transactionTypeNames.size(); ++i)
		{
			if (validTransactions.contains(i+1))
			{
				validTransactionNames.add(transactionTypeNames.get(i));
			}
		}
		return validTransactionNames;
	}

	public synchronized List<String> getAllTransactionTypeNames()
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
//		if (tableModelEvent.getFirstRow() == DBSeerDataSet.TYPE_NUM_TRANSACTION_TYPE &&
//				tableModelEvent.getColumn() == 1)
//		{
//			int numTransactionType = (Integer.parseInt((String)table.getValueAt(DBSeerDataSet.TYPE_NUM_TRANSACTION_TYPE, 1)));
//			int currentRowCount = tableModel.getRowCount();
//
//			ArrayList<String> previousTypeNames = new ArrayList<String>();
//			for (int i = DBSeerDataSet.TYPE_NUM_TRANSACTION_TYPE + 1; i < DBSeerDataSet.TYPE_NUM_TRANSACTION_TYPE + 1 + numTransactionTypes; ++i) //tableModel.getRowCount(); ++i)
//			{
//				previousTypeNames.add((String)table.getValueAt(i, 1));
//			}
//
////			for (int i = currentRowCount-1; i > DBSeerDataSet.TYPE_NUM_TRANSACTION_TYPE; --i)
//			for (int i = DBSeerDataSet.TYPE_NUM_TRANSACTION_TYPE + numTransactionTypes; i > DBSeerDataSet.TYPE_NUM_TRANSACTION_TYPE; --i)
//			{
//				tableModel.removeRow(i);
//			}
//
//			for (int i = 0; i < numTransactionType; ++i)
//			{
//				String name = (i < previousTypeNames.size()) ? previousTypeNames.get(i) : "Type " + (i+1);
//				tableModel.addRow(new Object[]{"Name of Transaction Type " + (i+1), name});
//			}
//		}
	}

	public void setReinitialize()
	{
		this.dataSetLoaded = false;
		this.modelVariableLoaded = false;
	}

	public Boolean getLive()
	{
		return live;
	}

	public void setTransactionTypeName(int i, String name)
	{
		transactionTypeNames.set(i,  name);
		updateTable();
	}

	public void clearTransactionTypes()
	{
		transactionTypeNames.clear();
		validTransactions.clear();
		numTransactionTypes = 0;
		updateTable();
	}

	public void addTransactionType(String name)
	{
		transactionTypeNames.add(name);
		validTransactions.add(transactionTypeNames.size());
		++numTransactionTypes;
		updateTable();
	}

	public void addTransactionRows()
	{
		for (int i = 0; i < numTransactionTypes; ++i)
		{
			tableModel.addRow(new Object[]{"Name of Transaction Type " + (i+1), "Type " + (i+1)});
		}

		for (int i = 0; i < numTransactionTypes; ++i)
		{
			if (validTransactions.contains(i+1))
			{
				tableModel.addRow(new Object[]{"Use Transaction Type " + (i + 1), Boolean.TRUE});
			}
			else
			{
				tableModel.addRow(new Object[]{"Use Transaction Type " + (i + 1), Boolean.FALSE});
			}
		}
	}

	public void enableTransaction(int i)
	{
		validTransactions.add(i+1);
	}

	public void disableTransaction(int i)
	{
		validTransactions.remove(i+1);
	}

	public boolean isTransactionEnabled(int i)
	{
		return validTransactions.contains(i+1);
	}
}
