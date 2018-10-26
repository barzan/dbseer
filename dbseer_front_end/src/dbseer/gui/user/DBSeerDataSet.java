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
import com.thoughtworks.xstream.converters.basic.BigDecimalConverter;
import dbseer.comp.UserInputValidator;
import dbseer.gui.DBSeerConstants;
import dbseer.gui.DBSeerExceptionHandler;
import dbseer.gui.DBSeerGUI;
import dbseer.gui.xml.XStreamHelper;
import dbseer.stat.StatisticalPackageRunner;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.io.input.ReversedLinesFileReader;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.io.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.List;

/**
 * Created by dyoon on 2014. 5. 24..
 */
@XStreamAlias("dataset")
public class DBSeerDataSet implements TableModelListener
{
	public static final String[] tableHeaders = {"Name of Dataset", "Path",// "Statement File", "Query File", "Monitoring Data File", "Transaction Count File",
			//"Average Latency File", "Percentile Latency File", "Header File",// "Statement Stat", // "Number of transaction types",
			"Use Entire Dataset", "Use Partial Dataset: Start Index", "Use Partial Dataset: End Index", "Number of Transaction Types"}; //, "Max Throughput Index"}; //"I/O Configuration", "Lock Configuration"
		//};

	public static final int TYPE_NAME = 0;
	public static final int TYPE_PATH = 1;
	public static final int TYPE_USE_ENTIRE_DATASET = 2;
	public static final int TYPE_START_INDEX = 3;
	public static final int TYPE_END_INDEX = 4;
	public static final int TYPE_NUM_TRANSACTION_TYPE = 5;

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
	protected String path = "";

	protected String transactionFilePath = "";
	protected String statementFilePath = "";
	protected String queryFilePath = "";

	protected Boolean useEntireDataSet = true;
	@XStreamOmitField
	protected Boolean live;
	@XStreamOmitField
	protected boolean isCurrent;

	protected int startIndex = 0;
	protected int endIndex = 0;

	protected int numTransactionTypes = 0;

	@XStreamOmitField
	protected ArrayList<DBSeerDataSetPath> datasetPathList = new ArrayList<DBSeerDataSetPath>();

	protected java.util.List<DBSeerTransactionType> transactionTypes = new ArrayList<DBSeerTransactionType>();

	@XStreamOmitField
	protected java.util.List<DBSeerTransactionType> transactionTypesBackup = new ArrayList<DBSeerTransactionType>();
//	protected java.util.List<String> transactionTypeNames = new ArrayList<String>();
//	@XStreamImplicit
//	protected java.util.Set<Integer> validTransactions = new HashSet<Integer>();

	@XStreamOmitField
	protected String pathBackup = "";
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
				else if (row == DBSeerDataSet.TYPE_NUM_TRANSACTION_TYPE)
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

		this.useEntireDataSet = true;
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
			DBSeerTransactionType type = new DBSeerTransactionType("Type " + (i+1), true);
			transactionTypes.add(type);
		}

		for (int i = 0; i < transactionTypes.size(); ++i)
		{
			DBSeerTransactionType txType = transactionTypes.get(i);
			if (txType.isEnabled())
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
		isCurrent = false;

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
//				else if (row == DBSeerDataSet.TYPE_AVERAGE_LATENCY ||
//						row == DBSeerDataSet.TYPE_HEADER ||
//						row == DBSeerDataSet.TYPE_MONITORING_DATA ||
//						row == DBSeerDataSet.TYPE_PERCENTILE_LATENCY ||
//						row == DBSeerDataSet.TYPE_TRANSACTION_COUNT ||
//						row == DBSeerDataSet.TYPE_NUM_TRANSACTION_TYPE ||
//						row == DBSeerDataSet.TYPE_STATEMENT_FILE ||
//						row == DBSeerDataSet.TYPE_QUERY_FILE ||
//						row == DBSeerDataSet.TYPE_TRANSACTION_FILE)
				else if (row == DBSeerDataSet.TYPE_NUM_TRANSACTION_TYPE)
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

		this.useEntireDataSet = true;
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
			DBSeerTransactionType type = new DBSeerTransactionType("Type " + (i+1), true);
			transactionTypes.add(type);
		}

		for (int i = 0; i < transactionTypes.size(); ++i)
		{
			DBSeerTransactionType txType = transactionTypes.get(i);
			if (txType.isEnabled())
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
//		if (live)
//		{
//			String livePath = DBSeerGUI.userSettings.getDBSeerRootPath() + File.separator + DBSeerConstants.LIVE_DATASET_PATH;
//			this.averageLatencyPath = livePath + File.separator + "avg_latency";
//			this.transCountPath = livePath + File.separator + "trans_count";
//			this.transactionFilePath = livePath + File.separator + "allLogs-t.txt";
//			this.statementFilePath = livePath + File.separator + "allLogs-s.txt";
//			this.queryFilePath = livePath + File.separator + "allLogs-q.txt";
//			this.headerPath = livePath + File.separator + "dataset_header.m";
//			this.monitoringDataPath = livePath + File.separator + "monitor";
//
//			// check valid transactions.
//			validTransactions.clear();
//			File transTypeFile = new File(livePath + File.separator + "trans_type");
//
//			// use trans_type if exists
//			int numTrans = 0;
//			if (transTypeFile.exists())
//			{
//				try
//				{
//					BufferedReader br = new BufferedReader(new FileReader(transTypeFile));
//					String line;
//					while ((line = br.readLine()) != null)
//					{
//						if (line.isEmpty())
//						{
//							continue;
//						}
//						else
//						{
//							String[] inds = line.split(",");
//							for (String ind : inds)
//							{
//								int indexNum = Integer.parseInt(ind);
//								validTransactions.add(indexNum);
//								if (indexNum > numTrans)
//								{
//									numTrans = indexNum;
//								}
//							}
//							break;
//						}
//					}
//					this.numTransactionTypes = numTrans;
//				}
//				catch (FileNotFoundException e)
//				{
//					e.printStackTrace();
//				}
//				catch (IOException e)
//				{
//					e.printStackTrace();
//				}
//			}
//			// otherwise, count prctile_latency_* files
//			else
//			{
//				File liveDir = new File(livePath);
//				FileFilter filter = new WildcardFileFilter("prctile_latency_*");
//				File[] files = liveDir.listFiles(filter);
//				if (files != null)
//				{
//					this.numTransactionTypes = files.length;
//					for (int i = 0; i < numTransactionTypes; ++i)
//					{
//						validTransactions.add(i);
//					}
//				}
//			}
//
//			if (this.numTransactionTypes == 0)
//			{
////				DBSeerExceptionHandler.handleException(new Exception("Unable to figure out the number of transactions for the live dataset."));
//			}
//
//			this.updateTable();
//		}
	}

	public void backup()
	{
		this.nameBackup = name;
		this.pathBackup = path;

		this.useEntireDataSetBackup = useEntireDataSet;
		this.startIndexBackup = startIndex;
		this.endIndexBackup = endIndex;
		this.numTransactionTypesBackup = numTransactionTypes;

		if (this.transactionTypeNamesBackup == null)
		{
			this.transactionTypeNamesBackup = new ArrayList<>();
		}
		this.transactionTypeNamesBackup.clear();
		this.transactionTypesBackup.clear();
		for (DBSeerTransactionType type : this.transactionTypes)
		{
			this.transactionTypesBackup.add(type);
		}
	}

	public void restore()
	{
		this.name = nameBackup;
		this.path = pathBackup;

		this.useEntireDataSet = useEntireDataSetBackup;
		this.startIndex = startIndexBackup;
		this.endIndex = endIndexBackup;
		this.numTransactionTypes = numTransactionTypesBackup;

		this.transactionTypes.clear();
		for (DBSeerTransactionType type : this.transactionTypesBackup)
		{
			this.transactionTypes.add(type);
		}
	}

	protected void useDefaultIfNull()
	{
		if (this.name == null) this.name = "";
		if (this.path == null) this.path = "";
		if (this.live == null) this.live = new Boolean(false);
		if (this.useEntireDataSet == null) this.useEntireDataSet = true;
		if (this.transactionTypes == null) this.transactionTypes = new ArrayList<DBSeerTransactionType>();
		if (this.transactionTypesBackup == null) this.transactionTypesBackup = new ArrayList<DBSeerTransactionType>();
		if (this.validTransactionsBackup == null) this.validTransactionsBackup = new HashSet<Integer>();
		if (this.transactionSampleLists == null) this.transactionSampleLists = new ArrayList<DBSeerTransactionSampleList>();
		if (this.statementsOffsetFiles == null) this.statementsOffsetFiles = new ArrayList<String>();
		if (this.datasetPathList == null) this.datasetPathList = new ArrayList<DBSeerDataSetPath>();
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
				else if (row == DBSeerDataSet.TYPE_NUM_TRANSACTION_TYPE)
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

		for (int i = 0; i < transactionTypes.size(); ++i)
		{
			DBSeerTransactionType txType = transactionTypes.get(i);
			tableModel.addRow(new Object[]{"Name of Transaction Type " + (i+1), txType.getName()});
		}

		if (live)
		{
			updateLiveDataSet();
		}
		for (int i = 0; i < transactionTypes.size(); ++i)
		{
			DBSeerTransactionType txType = transactionTypes.get(i);
			if (txType.isEnabled())
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

	public boolean loadDatasetPath()
	{
		datasetPathList.clear();

		// read current directory first.
		DBSeerDataSetPath newPath = getDBSeerDatasetPath(this.path);
		if (newPath == null)
		{
			return false;
		}
		if (!newPath.hasEmptyPath())
		{
			datasetPathList.add(newPath);
		}

		// read sub-directories
		File topDirectory = new File(this.path);
		final String[] subDirectories = topDirectory.list(new FilenameFilter()
		{
			@Override
			public boolean accept(File file, String s)
			{
				return new File(file, s).isDirectory();
			}
		});

		for (String subDir : subDirectories)
		{
			newPath = getDBSeerDatasetPath(this.path + File.separator + subDir);
			if (!newPath.hasEmptyPath())
			{
				datasetPathList.add(newPath);
			}
		}

		if (datasetPathList.size() == 0)
		{
			return false;
		}
		return true;
	}

	private DBSeerDataSetPath getDBSeerDatasetPath(String dir)
	{
		File datasetDir = new File(dir);
		File[] files = datasetDir.listFiles();

		DBSeerDataSetPath newPath = new DBSeerDataSetPath();
		newPath.setRoot(dir);
		newPath.setName(datasetDir.getName());

		boolean allFlag = false;

		if (files == null)
		{
			return null;
		}

		for (File file : files)
		{
			if (file.isDirectory())
			{
				continue;
			}

			String fileLower = file.getName().toLowerCase();

			if (fileLower.contains("monitor"))
			{
				newPath.setMonitor(file.getAbsolutePath());
			}
			else if (fileLower.contains("header"))
			{
				newPath.setHeader(file.getAbsolutePath());
			}
			else if (fileLower.contains("avg_latency"))
			{
				newPath.setAvgLatency(file.getAbsolutePath());
			}
			else if (fileLower.contains("trans_count"))
			{
				newPath.setTxCount(file.getAbsolutePath());
			}
			else if (fileLower.contains("prctile_latencies.mat"))
			{
				newPath.setPrcLatency(file.getAbsolutePath());
			}
			else if (fileLower.contains("tx.log") || fileLower.contains("sys.log"))
			{
				allFlag = true;
			}
		}

		// assign default names.
		if (!allFlag)
		{
			if (newPath.getMonitor().isEmpty())
			{
				newPath.setMonitor(dir + File.separator + "monitor");
			}
			if (newPath.getHeader().isEmpty())
			{
				newPath.setHeader(dir + File.separator + "dataset_header.m");
			}
			if (newPath.getAvgLatency().isEmpty())
			{
				newPath.setAvgLatency(dir + File.separator + "avg_latency");
			}
			if (newPath.getTxCount().isEmpty())
			{
				newPath.setTxCount(dir + File.separator + "trans_count");
			}
		}

		return newPath;
	}

	public synchronized boolean loadDataset(boolean isFirstTime)
	{
//		if (dataSetLoaded == false || this.numTransactionTypes == 0)
		{
			if (!loadDatasetPath())
			{
				return false;
			}
			this.numTransactionTypes = this.datasetPathList.get(0).getNumTransactionType();
			if (this.numTransactionTypes == 0)
			{
				return false;
			}
		}

		if (uniqueVariableName == "")
		{
			uniqueVariableName = "dataset_" + UUID.randomUUID().toString().replace('-', '_');
		}

		XStreamHelper xmlHelper = new XStreamHelper();
		statementsOffsetFiles.clear();
		transactionSampleLists.clear();

		for (int i = 0; i < this.numTransactionTypes; ++i)
		{
			String datasetPath = this.datasetPathList.get(0).getRoot();
			String samplePath = datasetPath + File.separator + "tx_sample_" + (i+1);
			String offsetPath = datasetPath + File.separator + "transaction_" + (i+1) + ".stmt";

			statementsOffsetFiles.add(offsetPath);

			if (new File(samplePath).exists())
			{
				DBSeerTransactionSampleList sampleList = new DBSeerTransactionSampleList(samplePath);
				sampleList.readSamples();
				transactionSampleLists.add(sampleList);
			}
			else
			{
				DBSeerTransactionSampleList sampleList = new DBSeerTransactionSampleList();
				transactionSampleLists.add(sampleList);
			}
		}

//		if (dataSetLoaded == false || live)
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
				runner.eval(this.uniqueVariableName + ".datasets = {};");

				int datasetCount = 1;
				for (DBSeerDataSetPath datasetPath : datasetPathList)
				{
					String datasetPathName = "dataset_" + (datasetCount++);
					runner.eval(datasetPathName + " = DataSetPath;");
					runner.eval(datasetPathName + ".name = '" + datasetPath.getName() + "';");
					runner.eval(datasetPathName + ".path = '" + datasetPath.getRoot() + "';");
					runner.eval(datasetPathName + ".header_path = '" + datasetPath.getHeader() + "';");
					runner.eval(datasetPathName + ".avg_latency_path = '" + datasetPath.getAvgLatency() + "';");
					runner.eval(datasetPathName + ".monitor_path = '" + datasetPath.getMonitor() + "';");
					runner.eval(datasetPathName + ".trans_count_path = '" + datasetPath.getTxCount() + "';");
					runner.eval(datasetPathName + ".percentile_latency_path = '" + datasetPath.getPrcLatency() + "';");

					runner.eval(this.uniqueVariableName + ".datasets{end+1} = " + datasetPathName + ";");
				}

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

				if (isFirstTime || this.transactionTypes.isEmpty())
				{
					this.transactionTypes.clear();
					for (int i = 0; i < this.numTransactionTypes; ++i)
					{
						DBSeerTransactionType txType = new DBSeerTransactionType("Type " + (i+1), true);
						this.transactionTypes.add(txType);
					}
//					this.addTransactionRows();
					updateTable();
				}
				for (int i = 0; i < transactionTypes.size(); ++i)
				{
					DBSeerTransactionType txType = transactionTypes.get(i);
					if (txType.isEnabled())
					{
						tranType += (i + 1) + " ";
					}
				}
				tranType += "]";
				runner.eval(this.uniqueVariableName + ".tranTypes = " + tranType);
				runner.eval(this.uniqueVariableName + ".loadStatistics;");
			}
			catch (Exception e)
			{
				DBSeerExceptionHandler.handleException(e);
				return false;
			}

			dataSetLoaded = true;
		}
		updateTable();
		return true;
	}

	public synchronized boolean loadDataset(boolean isFirstTime, long startIdx, long endIdx)
	{
		if (!loadDatasetPath())
		{
			return false;
		}
		this.numTransactionTypes = this.datasetPathList.get(0).getNumTransactionType();
		if (this.numTransactionTypes == 0)
		{
			return false;
		}

		if (uniqueVariableName == "")
		{
			uniqueVariableName = "dataset_" + UUID.randomUUID().toString().replace('-', '_');
		}


		XStreamHelper xmlHelper = new XStreamHelper();
		statementsOffsetFiles.clear();
		transactionSampleLists.clear();

		for (int i = 0; i < this.numTransactionTypes; ++i)
		{
			String datasetPath = this.datasetPathList.get(0).getRoot();
			String samplePath = datasetPath + File.separator + "tx_sample_" + (i+1);
			String offsetPath = datasetPath + File.separator + "transaction_" + (i+1) + ".stmt";

			statementsOffsetFiles.add(offsetPath);

			if (new File(samplePath).exists())
			{
				DBSeerTransactionSampleList sampleList = new DBSeerTransactionSampleList(samplePath);
				sampleList.readSamples();
				transactionSampleLists.add(sampleList);
			}
			else
			{
				DBSeerTransactionSampleList sampleList = new DBSeerTransactionSampleList();
				transactionSampleLists.add(sampleList);
			}
		}

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
			runner.eval(this.uniqueVariableName + ".datasets = {};");

			int datasetCount = 1;
			for (DBSeerDataSetPath datasetPath : datasetPathList)
			{
				String datasetPathName = "dataset_" + (datasetCount++);
				runner.eval(datasetPathName + " = DataSetPath;");
				runner.eval(datasetPathName + ".name = '" + datasetPath.getName() + "';");
				runner.eval(datasetPathName + ".path = '" + datasetPath.getRoot() + "';");
				runner.eval(datasetPathName + ".header_path = '" + datasetPath.getHeader() + "';");
				runner.eval(datasetPathName + ".avg_latency_path = '" + datasetPath.getAvgLatency() + "';");
				runner.eval(datasetPathName + ".monitor_path = '" + datasetPath.getMonitor() + "';");
				runner.eval(datasetPathName + ".trans_count_path = '" + datasetPath.getTxCount() + "';");
				runner.eval(datasetPathName + ".percentile_latency_path = '" + datasetPath.getPrcLatency() + "';");

				runner.eval(this.uniqueVariableName + ".datasets{end+1} = " + datasetPathName + ";");
			}

			runner.eval(this.uniqueVariableName + ".startIdx = " + startIdx + ";");
			runner.eval(this.uniqueVariableName + ".endIdx = " + endIdx + ";");
			runner.eval(this.uniqueVariableName + ".use_entire = false;");
			String tranType = "[";

			if (isFirstTime || this.transactionTypes.isEmpty())
			{
				this.transactionTypes.clear();
				for (int i = 0; i < this.numTransactionTypes; ++i)
				{
					DBSeerTransactionType txType = new DBSeerTransactionType("Type " + (i+1), true);
					this.transactionTypes.add(txType);
				}
//				this.addTransactionRows();
				updateTable();
			}
			for (int i = 0; i < transactionTypes.size(); ++i)
			{
				DBSeerTransactionType txType = transactionTypes.get(i);
				if (txType.isEnabled())
				{
					tranType += (i + 1) + " ";
				}
			}
			tranType += "]";
			runner.eval(this.uniqueVariableName + ".tranTypes = " + tranType);
			runner.eval(this.uniqueVariableName + ".loadStatistics;");
		}
		catch (Exception e)
		{
			DBSeerExceptionHandler.handleException(e);
			return false;
		}

		dataSetLoaded = true;
		return true;
	}

	public synchronized boolean loadModelVariable()
	{
		// set the unique name for mv first.
		if (uniqueModelVariableName == "")
		{
			uniqueModelVariableName = "mv_" + UUID.randomUUID().toString().replace('-', '_');
		}


		if (!loadDataset(false))
		{
			return false;
		}
		// load dataset if it already has not been done.
//		if (!dataSetLoaded || live)
//		{
//			if (!loadDataset(false))
//			{
//				return false;
//			}
//		}

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

				runner.eval("[mvGrouped " + uniqueModelVariableName + "] = load_mv2(" +
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
				return false;
			}
		}
		return true;
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
		transactionTypes.clear();

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
						case TYPE_PATH:
							this.path = (String)tableModel.getValueAt(i, 1);
							break;
						case TYPE_END_INDEX:
							if (UserInputValidator.validateNumber(String.valueOf(tableModel.getValueAt(i, 1))))
								this.endIndex = (Integer)tableModel.getValueAt(i, 1);
							break;
						case TYPE_USE_ENTIRE_DATASET:
							this.useEntireDataSet = (Boolean)tableModel.getValueAt(i, 1);
							break;
						case TYPE_START_INDEX:
							if (UserInputValidator.validateNumber(String.valueOf(tableModel.getValueAt(i, 1))));
								this.startIndex = (Integer)tableModel.getValueAt(i, 1);
							break;
						case TYPE_NUM_TRANSACTION_TYPE:
							if (UserInputValidator.validateNumber((String.valueOf(tableModel.getValueAt(i, 1)))));
								this.numTransactionTypes = (Integer)tableModel.getValueAt(i, 1);
							break;
						default:
							break;
					}
					break;
				}
			}
			if (i > TYPE_NUM_TRANSACTION_TYPE && i <= TYPE_NUM_TRANSACTION_TYPE + this.numTransactionTypes)
			{
				DBSeerTransactionType txType = new DBSeerTransactionType((String) tableModel.getValueAt(i, 1), true);
				transactionTypes.add(txType);
			}
			if (i > TYPE_NUM_TRANSACTION_TYPE + numTransactionTypes)
			{
				int idx = i - TYPE_NUM_TRANSACTION_TYPE - numTransactionTypes;
				if (idx <= numTransactionTypes && idx >= 1)
				{
					if ( ((Boolean)tableModel.getValueAt(i, 1)).booleanValue() )
					{
						transactionTypes.get(idx-1).setEnabled(true);
					}
					else
					{
						transactionTypes.get(idx-1).setEnabled(false);
					}
				}
			}
		}
		dataSetLoaded = false;
	}

	public void updateTable()
	{
		// remove all rows.
		tableModel.setRowCount(0);

		for (int i=0;i<tableHeaders.length;++i)
		{
			switch(i)
			{
				case TYPE_NAME:
					tableModel.addRow(new Object[]{tableHeaders[i], this.name});
					break;
				case TYPE_PATH:
					tableModel.addRow(new Object[]{tableHeaders[i], this.path});
					break;
				case TYPE_USE_ENTIRE_DATASET:
					tableModel.addRow(new Object[]{tableHeaders[i], this.useEntireDataSet});
					break;
				case TYPE_START_INDEX:
					tableModel.addRow(new Object[]{tableHeaders[i], this.startIndex});
					break;
				case TYPE_END_INDEX:
					tableModel.addRow(new Object[]{tableHeaders[i], this.endIndex});
					break;
				case TYPE_NUM_TRANSACTION_TYPE:
					tableModel.addRow(new Object[]{tableHeaders[i], this.transactionTypes.size()});
					break;
				default:
					break;
			}
		}
		int count = 1;
		for (DBSeerTransactionType txType : transactionTypes)
		{
			tableModel.addRow(new Object[]{"Name of Transaction Type " + (count++), txType.getName()});
		}
		count = 1;
		for (DBSeerTransactionType txType : transactionTypes)
		{
			tableModel.addRow(new Object[]{"Use Transaction Type " + (count++), (Boolean)txType.isEnabled()});
		}
	}

	public void updateTableOld()
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
						case TYPE_PATH:
							tableModel.setValueAt(this.path, i, 1);
							break;
						case TYPE_END_INDEX:
							tableModel.setValueAt(String.valueOf(this.endIndex), i, 1);
							break;
						case TYPE_USE_ENTIRE_DATASET:
							tableModel.setValueAt(this.useEntireDataSet.booleanValue(), i, 1);
							break;
						case TYPE_START_INDEX:
							tableModel.setValueAt(String.valueOf(this.startIndex), i, 1);
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
				if (i - TYPE_NUM_TRANSACTION_TYPE <= transactionTypes.size())
				{
					DBSeerTransactionType txType = transactionTypes.get(i - TYPE_NUM_TRANSACTION_TYPE - 1);
					tableModel.setValueAt(txType.getName(), i, 1);
				}
			}
			if (i > TYPE_NUM_TRANSACTION_TYPE + numTransactionTypes)
			{
				if (i - TYPE_NUM_TRANSACTION_TYPE - numTransactionTypes <= numTransactionTypes)
				{
					DBSeerTransactionType txType = transactionTypes.get(i - TYPE_NUM_TRANSACTION_TYPE - numTransactionTypes - 1);
					if (txType.isEnabled())
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

	public String getPath()
	{
		return path;
	}

	public void setPath(String path)
	{
		this.path = path;
		updateTable();
		tableModel.fireTableDataChanged();
		this.dataSetLoaded = false;
	}
	public synchronized String getTransactionFilePath()
	{
		if (transactionFilePath.isEmpty())
		{
			return this.path + File.separator + "allLogs-t.txt";
		}
		else
		{
			return transactionFilePath;
		}
	}

	public synchronized void setTransactionFilePath(String transactionFilePath)
	{
		this.transactionFilePath = transactionFilePath;
		updateTable();
		tableModel.fireTableDataChanged();
		this.dataSetLoaded = false;
	}

	public synchronized String getStatementFilePath()
	{
		if (statementFilePath.isEmpty())
		{
			return this.path + File.separator + "allLogs-s.txt";
		}
		else
		{
			return statementFilePath;
		}
	}

	public synchronized void setStatementFilePath(String statementFilePath)
	{
		this.statementFilePath = statementFilePath;
		updateTable();
		tableModel.fireTableDataChanged();
		this.dataSetLoaded = false;
	}

	public synchronized String getQueryFilePath()
	{
		if (queryFilePath.isEmpty())
		{
			return this.path + File.separator + "allLogs-q.txt";
		}
		else
		{
			return queryFilePath;
		}
	}

	public synchronized void setQueryFilePath(String queryFilePath)
	{
		this.queryFilePath = queryFilePath;
		updateTable();
		tableModel.fireTableDataChanged();
		this.dataSetLoaded = false;
	}

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
		for (DBSeerTransactionType type : transactionTypes)
		{
			if (type.isEnabled())
			{
				validTransactionNames.add(type.getName());
			}
		}
		return validTransactionNames;
	}

	public synchronized List<DBSeerTransactionType> getTransactionTypes()
	{
		return this.transactionTypes;
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
		transactionTypes.get(i).setName(name);
		updateTable();
	}

	public void clearTransactionTypes()
	{
		transactionTypes.clear();
		numTransactionTypes = 0;
		updateTable();
	}

	public void addTransactionType(String name)
	{
		DBSeerTransactionType newType = new DBSeerTransactionType(name, true);
		transactionTypes.add(newType);
		++numTransactionTypes;
		updateTable();
	}

	public void addTransactionRows()
	{
		for (int i = 0; i < transactionTypes.size(); ++i)
		{
			DBSeerTransactionType txType = transactionTypes.get(i);
			tableModel.addRow(new Object[]{"Name of Transaction Type " + (i+1), txType.getName()});
		}

		for (int i = 0; i < transactionTypes.size(); ++i)
		{
			DBSeerTransactionType txType = transactionTypes.get(i);
			if (txType.isEnabled())
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
		transactionTypes.get(i).setEnabled(true);
	}

	public void disableTransaction(int i)
	{
		transactionTypes.get(i).setEnabled(false);
	}

	public boolean isCurrent()
	{
		return isCurrent;
	}

	public void setCurrent(boolean current)
	{
		isCurrent = current;
	}

	public long getStartTime()
	{
		if (!loadDatasetPath())
		{
			return -1;
		}

		if (datasetPathList.isEmpty())
		{
			return -1;
		}

		DBSeerDataSetPath path = datasetPathList.get(0);

		String latencyPath = path.getAvgLatency();

		File latencyFile = new File(latencyPath);

		try
		{
			BufferedReader firstLineReader = new BufferedReader(new FileReader(latencyFile));
			String firstLine = firstLineReader.readLine();

			return this.getTimestamp(firstLine);
		}
		catch (FileNotFoundException e)
		{
			return -1;
		}
		catch (IOException e)
		{
			return -1;
		}
	}

	public long getEndTime()
	{
		if (!loadDatasetPath())
		{
			return -1;
		}

		if (datasetPathList.isEmpty())
		{
			return -1;
		}

		DBSeerDataSetPath path = datasetPathList.get(0);
		String latencyPath = path.getAvgLatency();
		File latencyFile = new File(latencyPath);

		try
		{
			ReversedLinesFileReader lastLineReader = new ReversedLinesFileReader(latencyFile);
			String lastLine = lastLineReader.readLine();

			return this.getTimestamp(lastLine);
		}
		catch (FileNotFoundException e)
		{
			return -1;
		}
		catch (IOException e)
		{
			return -1;
		}

	}

	public long getTimestamp(String line)
	{
		String[] data = line.trim().split("\\s+");
		BigDecimal bd = new BigDecimal(data[0]);
		return bd.longValueExact();
	}
}
