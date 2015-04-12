package dbseer.gui.user;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import dbseer.comp.UserInputValidator;
import dbseer.gui.DBSeerGUI;
import matlabcontrol.MatlabInvocationException;
import matlabcontrol.MatlabProxy;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Created by dyoon on 2014. 6. 2..
 */
@XStreamAlias("config")
public class DBSeerConfiguration
{
	// Grouping Type Constants
	public static final int GROUP_NONE = 0;
	public static final int GROUP_RANGE = 1;
	public static final int GROUP_REL_DIFF = 2;
	public static final int GROUP_NUM_CLUSTER = 3;

	// Grouping Target Constants
	//public static final int GROUP_TARGET_NONE = 0;
	public static final int GROUP_TARGET_INDIVIDUAL_TRANS_COUNT = 0;
	public static final int GROUP_TARGET_TPS = 1;

	// Table Header Constants
	private static final int TYPE_NAME = 0;
	private static final int TYPE_TRANSACTION_TYPE = 1;
	private static final int TYPE_IO_CONFIGURATION = 2;
	private static final int TYPE_LOCK_CONFIGURATION = 3;
//	private static final int TYPE_NUM_CLUSTERS = 4;
//	private static final int TYPE_WHICH_TRANSACTION = 5;
//	private static final int TYPE_MIN_FREQUENECY = 6;
//	private static final int TYPE_MIN_TPS = 7;
//	private static final int TYPE_MAX_TPS = 8;
//	private static final int TYPE_ALLOWED_RELATIVE_DIFF = 9;

	private static final String[] tableHeaders = {"Name of configuration", "Transaction types", "IO Configuration",
			"Lock Configuration"
	};
	//, "IO configuration",
//			"Lock configuration", "# clusters for a group", "Which transaction type to group",
//			"Minimum frequency for a group", "Minimum TPS for a group", "Maximum TPS for a group",
//			"Allowed relative diff"};

//	private static final String[] groupingTypes = {"None", "Group by range", "Group by relative diff",
//			"Group by clustering"};
//
//	private static final String[] groupingTargets = {"Individual transactions", "TPS"};

	@XStreamOmitField
	private JTable table;

	@XStreamOmitField
	private DefaultTableModel tableModel;

//	@XStreamOmitField
//	private JComboBox groupTypeComboBox;
//
//	@XStreamOmitField
//	private JComboBox groupTargetComboBox;
//
//	@XStreamOmitField
//	private JTextArea groupsTextArea;

	@XStreamOmitField
	private String uniqueVariableName = "";

	private String name = ""; // table
	private String ioConfiguration = "[]"; // table
	private String lockConfiguration = "[]"; // table
	private String transactionTypes = "[]"; // table
//	private String groupingRange = "[]"; // text area
//	private String whichTransTypeToGroup ="[]"; // table

	@XStreamOmitField
	private boolean isInitialized = false;

//	private int groupingType = GROUP_NONE; // combo box
//	private int groupingTarget = GROUP_TARGET_INDIVIDUAL_TRANS_COUNT; // combo box
//	private int numClusters = 0; // table

//	private double minFrequency = 0; // table
//	private double minTPS = 0; // table
//	private double maxTPS = 0; // table
//	private double allowedRelDiff = 0; // table

	// Configuration consists of multiple datasets.
	@XStreamOmitField
	private DefaultListModel datasetList;

	@XStreamImplicit
	private List<DBSeerDataSet> datasets;

	public DBSeerConfiguration()
	{
		datasetList = new DefaultListModel();
		datasets = new ArrayList<DBSeerDataSet>();
		uniqueVariableName = "config_" + UUID.randomUUID().toString().replace('-', '_');
		name = "Unnamed config";
		tableModel = new DBSeerConfigurationTableModel(null, new String[]{"Name", "Value"});
		isInitialized = false;

		table = new JTable(tableModel);
		table.setFillsViewportHeight(true);
		table.getColumnModel().getColumn(0).setMaxWidth(600);
		table.getColumnModel().getColumn(0).setPreferredWidth(500);
		table.getColumnModel().getColumn(1).setPreferredWidth(800);
		table.setRowHeight(20);

//		groupTypeComboBox = new JComboBox(groupingTypes);
//		groupTargetComboBox = new JComboBox(groupingTargets);
//		groupsTextArea = new JTextArea();

		for (String header : tableHeaders)
		{
			tableModel.addRow(new Object[]{header, ""});
		}
		this.updateTable();
	}

	private Object readResolve()
	{
		if (datasets == null)
		{
			datasets = new ArrayList<DBSeerDataSet>();
		}
		datasetList = new DefaultListModel();
		for (DBSeerDataSet dataset : datasets)
		{
			datasetList.addElement(dataset);
		}
		uniqueVariableName = "config_" + UUID.randomUUID().toString().replace('-', '_');
		tableModel = new DBSeerConfigurationTableModel(null, new String[]{"Name", "Value"});
		isInitialized = false;

		table = new JTable(tableModel);
		table.setFillsViewportHeight(true);
		table.getColumnModel().getColumn(0).setMaxWidth(600);
		table.getColumnModel().getColumn(0).setPreferredWidth(500);
		table.getColumnModel().getColumn(1).setPreferredWidth(800);
		table.setRowHeight(20);

//		groupTypeComboBox = new JComboBox(groupingTypes);
//		groupTargetComboBox = new JComboBox(groupingTargets);
//		groupsTextArea = new JTextArea();

		for (String header : tableHeaders)
		{
			tableModel.addRow(new Object[]{header, ""});
		}
		this.updateTable();

		return this;
	}

	public boolean initialize()
	{
		if (uniqueVariableName == "")
		{
			uniqueVariableName = "config_" + UUID.randomUUID().toString().replace('-', '_');
		}

//		if (!isInitialized)
		{
			MatlabProxy proxy = DBSeerGUI.proxy;

			try
			{
				proxy.eval(this.uniqueVariableName + " = PredictionConfig;");
				proxy.eval(this.uniqueVariableName + ".cleanDataset;");
				if (datasetList.getSize() == 0)
				{
					JOptionPane.showMessageDialog(null, "Please add datasets to the train config.", "Warning",
							JOptionPane.WARNING_MESSAGE);
					return false;
				}
				for (int i = 0; i < datasetList.getSize(); ++i)
				{
					DBSeerDataSet profile = (DBSeerDataSet) datasetList.getElementAt(i);
					profile.loadDataset();
					proxy.eval(this.uniqueVariableName + ".addDataset(" + profile.getUniqueVariableName() + ");");
				}

//				setGroupingStrategy();

				proxy.eval(this.uniqueVariableName + ".setTransactionType(" + this.transactionTypes + ");");
//				proxy.eval(this.uniqueVariableName + ".setIOConfiguration(" + this.ioConfiguration + ");");
//				proxy.eval(this.uniqueVariableName + ".setLockConfiguration(" + this.lockConfiguration + ");");
				proxy.eval(this.uniqueVariableName + ".initialize;");
			}
			catch (MatlabInvocationException e)
			{
				e.printStackTrace();
			}

//			isInitialized = true;
		}
		return true;
	}

	public String mapTransactionTypes(DBSeerDataSet testDataset)
	{
		// align transactions with the test dataset. (with the first dataset in the config)
		DBSeerDataSet trainDataset = this.getDataset(0);

		ArrayList<Integer> currentTransactionTypes = new ArrayList<Integer>();
		ArrayList<Integer> newTransactionTypes = new ArrayList<Integer>();

		String transactionTypeString = this.transactionTypes.trim();
		String newTransactionType = "[";
		String[] tokens = transactionTypeString.trim().split("[\\[\\]\\s]+");

		for (String token : tokens)
		{
			if (!token.isEmpty())
			{
				currentTransactionTypes.add(Integer.parseInt(token));
			}
		}

		List<String> testTransactionTypeNames = testDataset.getTransactionTypeNames();

		for (Integer i : currentTransactionTypes)
		{
			int idx = i.intValue();
			String transactionName = trainDataset.getTransactionTypeNames().get(idx-1);

			int matchIndex = testTransactionTypeNames.indexOf(transactionName);

			if (matchIndex >= 0)
			{
				newTransactionTypes.add(matchIndex+1);
			}
			else
			{
				JOptionPane.showMessageDialog(null, "Transaction types between the test dataset and datasets in the train config must match.\n" +
						"'" + transactionName + "' is not found in the test dataset.", "Error",
						JOptionPane.ERROR_MESSAGE);
				return null;
			}
		}

		for (Integer i : newTransactionTypes)
		{
			int idx = i.intValue();
			newTransactionType = newTransactionType + idx + " ";
		}
		newTransactionType = newTransactionType.trim() + "]";

		return newTransactionType;
	}

//	private void setGroupingStrategy()
//	{
//		// no grouping.
//		if (this.groupingType == GROUP_NONE)
//		{
//			return;
//		}
//
//		MatlabProxy proxy = DBSeerGUI.proxy;
//
//		try
//		{
//		 	if (this.groupingType == GROUP_RANGE)
//		    {
//			    proxy.eval(this.uniqueVariableName + "_groups = " + this.groupingRange + ";");
//			    proxy.eval(this.uniqueVariableName + "_groupingStrategy = " +
//					    "struct('groups', " + this.uniqueVariableName + "_groups);");
//			    proxy.eval(this.uniqueVariableName + ".setGroupingStrategy(" +
//					    this.uniqueVariableName + "_groupingStrategy);");
//		    }
//			else
//		    {
//			    String groupStructString = "struct('minFreq', " + this.minFrequency + ", " +
//					    "'minTPS', " + this.minTPS + ", " +
//					    "'maxTPS', " + this.maxTPS + ", " +
//					    "'groupByTPSinsteadOfIndivCounts', " + (this.groupingTarget == GROUP_TARGET_TPS ? "true" : "false") + ", ";
//
//
//			    if (this.groupingType == GROUP_REL_DIFF)
//			    {
//				    groupStructString += "'allowedRelativeDiff', " + this.allowedRelDiff + ", ";
//			    }
//			    else if (this.groupingType == GROUP_NUM_CLUSTER)
//			    {
//				    groupStructString += "'nClusters', " + this.numClusters + ", ";
//			    }
//
//			    if (this.groupingTarget == GROUP_TARGET_INDIVIDUAL_TRANS_COUNT)
//			    {
//				    groupStructString += "'byWhichTranTypes', " + this.whichTransTypeToGroup + ")";
//			    }
//
//			    proxy.eval(this.uniqueVariableName + "_groupParams = " + groupStructString + ";");
//			    proxy.eval(this.uniqueVariableName + "_groupingStrategy = " +
//					    "struct('groupParams', " + this.uniqueVariableName + "_groupParams);");
//			    proxy.eval(this.uniqueVariableName + ".setGroupingStrategy(" +
//					    this.uniqueVariableName + "_groupingStrategy);");
//		    }
//		}
//		catch (MatlabInvocationException e)
//		{
//			e.printStackTrace();
//		}
//	}

	public String toString()
	{
		return name;
	}

	public JTable getTable()
	{
		return table;
	}

	public DefaultListModel getDatasetList()
	{
		return datasetList;
	}

	public void setDatasetList(DefaultListModel datasetList)
	{
		this.datasetList = datasetList;
		datasets.clear();

		for (int i = 0; i < datasetList.getSize(); ++i)
		{
			datasets.add((DBSeerDataSet) this.datasetList.get(i));
		}
	}

	public boolean validateTable()
	{
		for (int i = 0; i < tableModel.getRowCount(); ++i)
		{
			for (int j = 0; j < tableHeaders.length; ++j)
			{
				if (tableModel.getValueAt(i,0).equals(tableHeaders[j]))
				{
					switch (j)
					{
						case TYPE_TRANSACTION_TYPE:
						{
							if (!UserInputValidator.validateSingleRowMatrix((String)tableModel.getValueAt(i, 1)))
							{
								JOptionPane.showMessageDialog(null, "Please enter transaction types correctly.\nIt has to be in the form of a MATLAB single row matrix. e.g. [1 2 3 4 5]",
										"Warning", JOptionPane.WARNING_MESSAGE);
								return false;
							}
							break;
						}
						case TYPE_IO_CONFIGURATION:
						{
							if (!UserInputValidator.validateSingleRowMatrix((String)tableModel.getValueAt(i, 1)))
							{
								JOptionPane.showMessageDialog(null, "Please enter IO configuration correctly.\nIt has to be in the form of a MATLAB single row matrix. e.g. [1000000 1500 10]",
										"Warning", JOptionPane.WARNING_MESSAGE);
								return false;
							}
							break;
						}
						case TYPE_LOCK_CONFIGURATION:
						{
							if (!UserInputValidator.validateSingleRowMatrix((String)tableModel.getValueAt(i, 1)))
							{
								JOptionPane.showMessageDialog(null, "Please enter Lock configuration correctly.\nIt has to be in the form of a MATLAB single row matrix. e.g. [0.08 0.0001 2 0.8]",
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
		}
		return true;
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
						case TYPE_TRANSACTION_TYPE:
							this.transactionTypes = (String)tableModel.getValueAt(i, 1);
							break;
						case TYPE_IO_CONFIGURATION:
							this.ioConfiguration = (String)tableModel.getValueAt(i, 1);
							break;
						case TYPE_LOCK_CONFIGURATION:
							this.lockConfiguration = (String)tableModel.getValueAt(i, 1);
							break;
//						case TYPE_NUM_CLUSTERS:
//							this.numClusters = Integer.parseInt((String) tableModel.getValueAt(i, 1));
//							break;
//						case TYPE_WHICH_TRANSACTION:
//							this.whichTransTypeToGroup = (String)tableModel.getValueAt(i, 1);
//							break;
//						case TYPE_MIN_FREQUENECY:
//							this.minFrequency = Double.parseDouble((String) tableModel.getValueAt(i, 1));
//							break;
//						case TYPE_MIN_TPS:
//							this.minTPS = Double.parseDouble((String) tableModel.getValueAt(i, 1));
//							break;
//						case TYPE_MAX_TPS:
//							this.maxTPS = Double.parseDouble((String) tableModel.getValueAt(i, 1));
//							break;
//						case TYPE_ALLOWED_RELATIVE_DIFF:
//							this.allowedRelDiff = Double.parseDouble((String) tableModel.getValueAt(i, 1));
//							break;
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
						case TYPE_TRANSACTION_TYPE:
							tableModel.setValueAt(this.transactionTypes, i, 1);
							break;
						case TYPE_IO_CONFIGURATION:
							tableModel.setValueAt(this.ioConfiguration, i, 1);
							break;
						case TYPE_LOCK_CONFIGURATION:
							tableModel.setValueAt(this.lockConfiguration, i, 1);
							break;
//						case TYPE_NUM_CLUSTERS:
//							tableModel.setValueAt(String.valueOf(this.numClusters), i, 1);
//							break;
//						case TYPE_WHICH_TRANSACTION:
//							tableModel.setValueAt(this.whichTransTypeToGroup, i, 1);
//							break;
//						case TYPE_MIN_FREQUENECY:
//							tableModel.setValueAt(String.valueOf(this.minFrequency), i, 1);
//							break;
//						case TYPE_MIN_TPS:
//							tableModel.setValueAt(String.valueOf(this.minTPS), i, 1);
//							break;
//						case TYPE_MAX_TPS:
//							tableModel.setValueAt(String.valueOf(this.maxTPS), i, 1);
//							break;
//						case TYPE_ALLOWED_RELATIVE_DIFF:
//							tableModel.setValueAt(String.valueOf(this.allowedRelDiff), i, 1);
//							break;
						default:
							break;
					}
					break;
				}
			}
		}
	}

	public void addDataset(DBSeerDataSet dataset)
	{
		datasetList.addElement(dataset);
		datasets.add(dataset);
		isInitialized = false;
	}

	public void removeDataset(DBSeerDataSet dataset)
	{
		datasetList.removeElement(dataset);
		datasets.remove(dataset);
		isInitialized = false;
	}

	public int getDatasetCount()
	{
		return datasetList.getSize();
	}

	public DBSeerDataSet getDataset(int i)
	{
		return (DBSeerDataSet) datasetList.getElementAt(i);
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getIoConfiguration()
	{
		return ioConfiguration;
	}

	public void setIoConfiguration(String ioConfiguration)
	{
		this.ioConfiguration = ioConfiguration;
		isInitialized = false;
	}

	public String getLockConfiguration()
	{
		return lockConfiguration;
	}

	public void setLockConfiguration(String lockConfiguration)
	{
		this.lockConfiguration = lockConfiguration;
		isInitialized = false;
	}
//
//	public String getGroupingRange()
//	{
//		return groupingRange;
//	}
//
//	public void setGroupingRange(String groupingRange)
//	{
//		this.groupingRange = groupingRange;
//		isInitialized = false;
//	}
//
//	public int getGroupingType()
//	{
//		return groupingType;
//	}
//
//	public void setGroupingType(int groupingType)
//	{
//		this.groupingType = groupingType;
//		isInitialized = false;
//	}
//
//	public int getGroupingTarget()
//	{
//		return groupingTarget;
//	}
//
//	public void setGroupingTarget(int groupingTarget)
//	{
//		this.groupingTarget = groupingTarget;
//		isInitialized = false;
//	}
//
//	public double getMinFrequency()
//	{
//		return minFrequency;
//	}
//
//	public void setMinFrequency(double minFrequency)
//	{
//		this.minFrequency = minFrequency;
//		isInitialized = false;
//	}
//
//	public double getMinTPS()
//	{
//		return minTPS;
//	}
//
//	public void setMinTPS(double minTPS)
//	{
//		this.minTPS = minTPS;
//		isInitialized = false;
//	}
//
//	public double getMaxTPS()
//	{
//		return maxTPS;
//	}
//
//	public void setMaxTPS(double maxTPS)
//	{
//		this.maxTPS = maxTPS;
//		isInitialized = false;
//	}
//
//	public int getNumClusters()
//	{
//		return numClusters;
//	}
//
//	public void setNumClusters(int numClusters)
//	{
//		this.numClusters = numClusters;
//		isInitialized = false;
//	}
//
//	public double getAllowedRelDiff()
//	{
//		return allowedRelDiff;
//	}
//
//	public void setAllowedRelDiff(double allowedRelDiff)
//	{
//		this.allowedRelDiff = allowedRelDiff;
//		isInitialized = false;
//	}
//
//	public String getWhichTransTypeToGroup()
//	{
//		return whichTransTypeToGroup;
//	}
//
//	public void setWhichTransTypeToGroup(String whichTransTypeToGroup)
//	{
//		this.whichTransTypeToGroup = whichTransTypeToGroup;
//		isInitialized = false;
//	}

	public String getTransactionTypes()
	{
		return transactionTypes;
	}

	public void setTransactionTypes(String transactionTypes)
	{
		this.transactionTypes = transactionTypes;
		isInitialized = false;
	}

//	public JComboBox getGroupTypeComboBox()
//	{
//		return groupTypeComboBox;
//	}
//
//	public JComboBox getGroupTargetComboBox()
//	{
//		return groupTargetComboBox;
//	}
//
//	public JTextArea getGroupsTextArea()
//	{
//		return groupsTextArea;
//	}

	public String getUniqueVariableName()
	{
		return uniqueVariableName;
	}
}
