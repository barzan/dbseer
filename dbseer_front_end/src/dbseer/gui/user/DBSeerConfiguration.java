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
import dbseer.comp.MatlabFunctions;
import dbseer.comp.UserInputValidator;
import dbseer.gui.DBSeerExceptionHandler;
import dbseer.gui.DBSeerGUI;
import dbseer.stat.StatisticalPackageRunner;
import matlabcontrol.MatlabInvocationException;
import matlabcontrol.MatlabProxy;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import java.util.ArrayList;
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
//	private static final int TYPE_TRANSACTION_TYPE = 1;
	private static final int TYPE_IO_MAX_LOG_CAPACITY = 1;
	private static final int TYPE_IO_MAX_FLUSH_RATE = 2;
	private static final int TYPE_IO_SCALE_FACTOR = 3;

	private static final int TYPE_LOCK_BEGIN_COST = 4;
	private static final int TYPE_LOCK_INTERLOCK_INTERVAL = 5;
	private static final int TYPE_LOCK_DOMAIN_MULTIPLIER = 6;
	private static final int TYPE_LOCK_COST_MULTIPLIER = 7;

	private static final String[] tableHeaders = {"Name of configuration", "IO: Max Log Capacity",
			"IO: Max Flush Rate (pages/sec)", "IO: Scale Factor", "Lock: Begin Cost", "Lock: Inter-lock Interval",
			"Lock: Domain Multiplier", "Lock: Cost Multiplier"
	};

	@XStreamOmitField
	private JTable table;

	@XStreamOmitField
	private DefaultTableModel tableModel;

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

	private int numTransactionType = 0;

	private double ioMaxLogCapacity = 0;
	private double ioMaxFlushRate = 0;
	private double ioScaleFactor = 0;

	private double lockBeginCost = 0;
	private double lockInterLockInterval = 0;
	private double lockDomainMultiplier = 0;
	private double lockCostMultiplier = 0;

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
			StatisticalPackageRunner runner = DBSeerGUI.runner;

			String dbseerPath = DBSeerGUI.userSettings.getDBSeerRootPath();

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

			runner.eval(this.uniqueVariableName + " = PredictionConfig;");
			runner.eval(this.uniqueVariableName + ".cleanDataset;");
			if (datasetList.getSize() == 0)
			{
				JOptionPane.showMessageDialog(null, "Please add datasets to the train config.", "Warning",
						JOptionPane.WARNING_MESSAGE);
				return false;
			}
			DBSeerDataSet firstProfile = (DBSeerDataSet) datasetList.getElementAt(0);
			numTransactionType = firstProfile.getNumTransactionTypes();
			for (int i = 0; i < datasetList.getSize(); ++i)
			{
				DBSeerDataSet profile = (DBSeerDataSet) datasetList.getElementAt(i);
				profile.loadModelVariable();
				runner.eval(this.uniqueVariableName + ".addDataset(" + profile.getUniqueVariableName() + ");");
			}

			String transactionType = "[";
			for (int i = 1; i <= numTransactionType; ++i)
			{
				transactionType += i + " ";
			}
			transactionType += "]";

			runner.eval(this.uniqueVariableName + ".setTransactionType(" + transactionType + ");");
			runner.eval(this.uniqueVariableName + ".initialize;");
		}
		return true;
	}

	public String mapTransactionTypes(DBSeerDataSet testDataset)
	{
		// align transactions with the test dataset. (with the first dataset in the config)
		DBSeerDataSet trainDataset = this.getDataset(0);

		ArrayList<Integer> currentTransactionTypes = new ArrayList<Integer>();
		ArrayList<Integer> newTransactionTypes = new ArrayList<Integer>();

//		String transactionTypeString = this.transactionTypes.trim();
		String newTransactionType = "[";
//		String[] tokens = transactionTypeString.trim().split("[\\[\\]\\s]+");

//		for (String token : tokens)
//		{
//			if (!token.isEmpty())
//			{
//				currentTransactionTypes.add(Integer.parseInt(token));
//			}
//		}

		List<String> testTransactionTypeNames = testDataset.getTransactionTypeNames();

		for (int i = 0; i < numTransactionType; ++i)
		{
			String transactionName = trainDataset.getTransactionTypeNames().get(i);

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
						case TYPE_IO_MAX_LOG_CAPACITY:
						{
							if (!UserInputValidator.validateNumber((String)tableModel.getValueAt(i, 1)))
							{
								JOptionPane.showMessageDialog(null, "Please enter 'IO: Max Log Capacity' correctly.\nIt has to be a positive number.",
										"Warning", JOptionPane.WARNING_MESSAGE);
								return false;
							}
							break;
						}
						case TYPE_IO_MAX_FLUSH_RATE:
						{
							if (!UserInputValidator.validateNumber((String)tableModel.getValueAt(i, 1)))
							{
								JOptionPane.showMessageDialog(null, "Please enter 'IO: Max Flush Rate' correctly.\nIt has to be a positive number.",
										"Warning", JOptionPane.WARNING_MESSAGE);
								return false;
							}
							break;
						}
						case TYPE_IO_SCALE_FACTOR:
						{
							if (!UserInputValidator.validateNumber((String)tableModel.getValueAt(i, 1)))
							{
								JOptionPane.showMessageDialog(null, "Please enter 'IO: Scale Factor' correctly.\nIt has to be a positive number.",
										"Warning", JOptionPane.WARNING_MESSAGE);
								return false;
							}
							break;
						}
						case TYPE_LOCK_BEGIN_COST:
						{
							if (!UserInputValidator.validateNumber((String)tableModel.getValueAt(i, 1)))
							{
								JOptionPane.showMessageDialog(null, "Please enter 'Lock: Begin Cost' correctly.\nIt has to be a positive number.",
										"Warning", JOptionPane.WARNING_MESSAGE);
								return false;
							}
							break;
						}
						case TYPE_LOCK_INTERLOCK_INTERVAL:
						{
							if (!UserInputValidator.validateNumber((String)tableModel.getValueAt(i, 1)))
							{
								JOptionPane.showMessageDialog(null, "Please enter 'Lock: Inter-lock Interval' correctly.\nIt has to be a positive number.",
										"Warning", JOptionPane.WARNING_MESSAGE);
								return false;
							}
							break;
						}
						case TYPE_LOCK_DOMAIN_MULTIPLIER:
						{
							if (!UserInputValidator.validateNumber((String)tableModel.getValueAt(i, 1)))
							{
								JOptionPane.showMessageDialog(null, "Please enter 'Lock: Domain Multiplier' correctly.\nIt has to be a positive number.",
										"Warning", JOptionPane.WARNING_MESSAGE);
								return false;
							}
							break;
						}
						case TYPE_LOCK_COST_MULTIPLIER:
						{
							if (!UserInputValidator.validateNumber((String)tableModel.getValueAt(i, 1)))
							{
								JOptionPane.showMessageDialog(null, "Please enter 'Lock: Cost Multiplier' correctly.\nIt has to be a positive number.",
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
//						case TYPE_TRANSACTION_TYPE:
//							this.transactionTypes = (String)tableModel.getValueAt(i, 1);
//							break;
						case TYPE_IO_MAX_LOG_CAPACITY:
							this.ioMaxLogCapacity = Double.parseDouble((String)tableModel.getValueAt(i, 1));
							break;
						case TYPE_IO_MAX_FLUSH_RATE:
							this.ioMaxFlushRate = Double.parseDouble((String)tableModel.getValueAt(i, 1));
							break;
						case TYPE_IO_SCALE_FACTOR:
							this.ioScaleFactor = Double.parseDouble((String)tableModel.getValueAt(i, 1));
							break;
						case TYPE_LOCK_BEGIN_COST:
							this.lockBeginCost = Double.parseDouble((String)tableModel.getValueAt(i, 1));
							break;
						case TYPE_LOCK_INTERLOCK_INTERVAL:
							this.lockInterLockInterval = Double.parseDouble((String)tableModel.getValueAt(i, 1));
							break;
						case TYPE_LOCK_DOMAIN_MULTIPLIER:
							this.lockDomainMultiplier = Double.parseDouble((String)tableModel.getValueAt(i, 1));
							break;
						case TYPE_LOCK_COST_MULTIPLIER:
							this.lockCostMultiplier = Double.parseDouble((String)tableModel.getValueAt(i, 1));
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
						case TYPE_IO_MAX_LOG_CAPACITY:
							tableModel.setValueAt(String.valueOf(this.ioMaxLogCapacity), i, 1);
							break;
						case TYPE_IO_MAX_FLUSH_RATE:
							tableModel.setValueAt(String.valueOf(this.ioMaxFlushRate), i, 1);
							break;
						case TYPE_IO_SCALE_FACTOR:
							tableModel.setValueAt(String.valueOf(this.ioScaleFactor), i, 1);
							break;
						case TYPE_LOCK_BEGIN_COST:
							tableModel.setValueAt(String.valueOf(this.lockBeginCost), i, 1);
							break;
						case TYPE_LOCK_INTERLOCK_INTERVAL:
							tableModel.setValueAt(String.valueOf(this.lockInterLockInterval), i, 1);
							break;
						case TYPE_LOCK_DOMAIN_MULTIPLIER:
							tableModel.setValueAt(String.valueOf(this.lockDomainMultiplier), i, 1);
							break;
						case TYPE_LOCK_COST_MULTIPLIER:
							tableModel.setValueAt(String.valueOf(this.lockCostMultiplier), i, 1);
							break;
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

	public DBSeerDataSet getDataset()
	{
		return (DBSeerDataSet) datasetList.getElementAt(0);
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
		String ioConfString = "[" + this.ioMaxLogCapacity + " " + this.ioMaxFlushRate + " " + this.ioScaleFactor + "]";
		return ioConfString;
	}
//
//	public void setIoConfiguration(String ioConfiguration)
//	{
//		this.ioConfiguration = ioConfiguration;
//		isInitialized = false;
//	}
//
	public String getLockConfiguration()
	{
		String lockConfString = "[" + this.lockBeginCost + " " + this.lockInterLockInterval + " " + this.lockDomainMultiplier + " " + this.lockCostMultiplier + "]";
		return lockConfString;
	}
//
//	public void setLockConfiguration(String lockConfiguration)
//	{
//		this.lockConfiguration = lockConfiguration;
//		isInitialized = false;
//	}
//
//	public String getTransactionTypes()
//	{
//		return transactionTypes;
//	}
//
//	public void setTransactionTypes(String transactionTypes)
//	{
//		this.transactionTypes = transactionTypes;
//		isInitialized = false;
//	}

	public String getUniqueVariableName()
	{
		return uniqueVariableName;
	}

	public double getMinTPS()
	{
		return MatlabFunctions.getMinTPS((DBSeerDataSet)datasetList.getElementAt(0));
	}

	public double getMaxTPS()
	{
		return MatlabFunctions.getMaxTPS((DBSeerDataSet) datasetList.getElementAt(0));
	}

	public double[] getTransactionMix()
	{
		return MatlabFunctions.getTotalTransactionMix((DBSeerDataSet) datasetList.getElementAt(0));
	}

	public String getTransactionMixString()
	{
		double[] mix = MatlabFunctions.getTotalTransactionMix((DBSeerDataSet) datasetList.getElementAt(0));
		String str = "[";
		for (double m : mix)
		{
			str += m;
			str += " ";
		}
		str += "]";
		return str;
	}

	public void setReinitialize()
	{
		this.isInitialized = false;
		for (DBSeerDataSet dataset : datasets)
		{
			dataset.setReinitialize();
		}
	}
}
