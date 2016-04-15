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

package dbseer.gui.panel;

import dbseer.gui.DBSeerConstants;
import dbseer.comp.PredictionCenter;
import dbseer.comp.UserInputValidator;
import dbseer.gui.events.InformationChartMouseListener;
import dbseer.gui.panel.prediction.*;
import dbseer.gui.user.DBSeerConfiguration;
import dbseer.gui.DBSeerGUI;
import dbseer.gui.frame.DBSeerPredictionFrame;
import dbseer.gui.model.SharedComboBoxModel;
import dbseer.gui.user.DBSeerDataSet;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by dyoon on 2014. 6. 3..
 */
public class DBSeerPredictionConsolePanel extends JPanel implements ActionListener
{
	public static Set<String> predictionSet = new HashSet<String>();

	private JComboBox predictionComboBox;
	private JComboBox trainConfigComboBox;
	private JComboBox testDatasetComboBox;
	private JComboBox groupingTypeBox;
	private JComboBox groupingTargetBox;

	private JTextField testMixtureTextField;
	private JTextField testMinTPSTextField;
	private JTextField testMaxTPSTextField;

	private JLabel testMixtureLabel;
	private JLabel testMinTPSLabel;
	private JLabel testMaxTPSLabel;
	private JLabel elapsedTimeLabel;

	private JButton predictionButton;
	private JButton showButton;

	private JPanel controlPanel;
	private JPanel predictionSetupPanel;
	private JPanel datasetSetupPanel;
	private JPanel mixtureTPSSetupPanel;
	private JPanel groupingSetupPanel;

	private InformationChartMouseListener informationChartMouseListener;
	private DBSeerPredictionInformationPanel infoPanel;
	private DBSeerPredictionWithTPSMixturePanel tpsMixturePanel;
	private DBSeerPredictionWithTestDatasetPanel testDatasetPanel;

	private JTabbedPane setupPane;

	// Individual prediction panels
	private FlushRatePredictionByTPSPanel flushRatePredictionByTPSPanel;
	private FlushRatePredictionByCountsPanel flushRatePredictionByCountsPanel;
	private MaxThroughputPredictionPanel maxThroughputPredictionPanel;
	private LockPredictionPanel lockPredictionPanel;
	private TransactionCountsToCpuByTPSPanel transactionCountsToCpuByTPSPanel;
	private TransactionCountsToCpuByCountsPanel transactionCountsToCpuByCountsPanel;
	private TransactionCountsToIOPanel transactionCountsToIOPanel;
	private TransactionCountsToLatencyPanel transactionCountsToLatencyPanel;
	private TransactionCountsToLatencyPanel transactionCountsToLatencyPanel99;
	private TransactionCountsToLatencyPanel transactionCountsToLatencyPanelMedian;
	private TransactionCountsWaitTimeToLatencyPanel transactionCountsWaitTimeToLatencyPanel;
	private TransactionCountsWaitTimeToLatencyPanel transactionCountsWaitTimeToLatencyPanel99;
	private TransactionCountsWaitTimeToLatencyPanel transactionCountsWaitTimeToLatencyPanelMedian;
	private BlownTransactionCountsToCpuPanel blownTransactionCountsToCpuPanel;
	private BlownTransactionCountsToIOPanel blownTransactionCountsToIOPanel;
	private LinearPredictionPanel linearPredictionPanel;
	private PhysicalReadPredictionPanel physicalReadPredictionPanel;

	public DBSeerPredictionConsolePanel()
	{
		initializeGUI();
	}

	private void initializeGUI()
	{
	 	this.setLayout(new MigLayout("fill, ins 0", "[][fill]", "[fill][fill]"));

		predictionComboBox = new JComboBox(new DefaultComboBoxModel(DBSeerGUI.availablePredictions));
		trainConfigComboBox = new JComboBox(new SharedComboBoxModel(DBSeerGUI.configs));
		predictionComboBox.addActionListener(this);

		predictionComboBox.setBorder(BorderFactory.createTitledBorder("Predictions"));
		trainConfigComboBox.setBorder(BorderFactory.createTitledBorder("Train Config"));
		trainConfigComboBox.addActionListener(this);

		groupingTypeBox = new JComboBox(DBSeerConstants.GROUP_TYPES);
		groupingTargetBox = new JComboBox(DBSeerConstants.GROUP_TARGETS);
		groupingTypeBox.addActionListener(this);
		groupingTargetBox.addActionListener(this);

		predictionButton = new JButton("Predict");
		predictionButton.addActionListener(this);
		predictionButton.setEnabled(false);
		showButton = new JButton("Show Result");
		showButton.setEnabled(false);
		showButton.addActionListener(this);
		elapsedTimeLabel = new JLabel("");

		controlPanel = new JPanel();
		controlPanel.setLayout(new MigLayout("fill, ins 0"));
		controlPanel.add(predictionComboBox, "growx, wrap");
		controlPanel.add(trainConfigComboBox, "growx, wrap");
		controlPanel.setBorder(BorderFactory.createTitledBorder("Choose a prediction"));

		groupingSetupPanel = new JPanel();
		groupingSetupPanel.setLayout(new MigLayout("fill, ins 5 5 5 5"));
		groupingSetupPanel.setBorder(BorderFactory.createTitledBorder("Grouping options"));

		datasetSetupPanel = new JPanel();
		datasetSetupPanel.setLayout(new MigLayout("fill, ins 5 5 5 5"));
		datasetSetupPanel.setBorder(BorderFactory.createTitledBorder("Choose a test dataset"));
//		datasetSetupPanel.setPreferredSize(new Dimension(600, 300));

		mixtureTPSSetupPanel = new JPanel();
		mixtureTPSSetupPanel.setLayout(new MigLayout("fill, ins 5 5 5 5"));
		mixtureTPSSetupPanel.setBorder(BorderFactory.createTitledBorder("Specify test TPS + mixture"));
		mixtureTPSSetupPanel.setPreferredSize(new Dimension(300, 150));

//		predictionTestModeComboBox = new JComboBox(DBSeerGUI.availablePredictionTestModes);
//		predictionTestModeComboBox.setBorder(BorderFactory.createTitledBorder("Prediction Test Target"));
//		predictionTestModeComboBox.addActionListener(this);

		testDatasetComboBox = new JComboBox(new SharedComboBoxModel(DBSeerGUI.datasets));
		testDatasetComboBox.setBorder(BorderFactory.createTitledBorder("Test Dataset"));

		testMixtureTextField = new JTextField();
		testMixtureLabel = new JLabel("Test Mixture (e.g. [0.4 0.4 0.1 0.05 0.05]):");
		testMinTPSTextField = new JTextField();
		testMinTPSLabel = new JLabel("Test Minimum TPS:");
		testMaxTPSTextField = new JTextField();
		testMaxTPSLabel = new JLabel("Test Maximum TPS:");

//		controlPanel.add(predictionTestModeComboBox, "growx, wrap");
		mixtureTPSSetupPanel.add(testMixtureLabel, "split 2");
		mixtureTPSSetupPanel.add(testMixtureTextField, "growx, wrap");
		mixtureTPSSetupPanel.add(testMinTPSLabel, "split 2");
		mixtureTPSSetupPanel.add(testMinTPSTextField, "growx, wrap");
		mixtureTPSSetupPanel.add(testMaxTPSLabel, "split 2");
		mixtureTPSSetupPanel.add(testMaxTPSTextField, "growx, wrap");

		predictionSetupPanel = new JPanel(new CardLayout());
		predictionSetupPanel.setBorder(BorderFactory.createTitledBorder("Additional Configuration"));
		predictionSetupPanel.setPreferredSize(new Dimension(400, 200));
		predictionSetupPanel.add(new EmptyPredictionPanel(), "No Prediction");

		addPredictionPanels();

		((CardLayout)predictionSetupPanel.getLayout()).show(predictionSetupPanel,
				(String) predictionComboBox.getSelectedItem());

		controlPanel.add(predictionSetupPanel, "spanx 2, grow, wrap");
		controlPanel.add(predictionButton, "split 2, growx");
		controlPanel.add(showButton, "growx");
		controlPanel.add(elapsedTimeLabel);

		setupPane = new JTabbedPane(JTabbedPane.BOTTOM);

		tpsMixturePanel = new DBSeerPredictionWithTPSMixturePanel();
		testDatasetPanel = new DBSeerPredictionWithTestDatasetPanel();
		informationChartMouseListener = new InformationChartMouseListener(tpsMixturePanel);
		infoPanel = new DBSeerPredictionInformationPanel(informationChartMouseListener);

		JScrollPane tpsMixtureScrollPane = new JScrollPane();
		tpsMixtureScrollPane.setViewportView(tpsMixturePanel);
		JScrollPane testDatasetScrollPane = new JScrollPane();
		testDatasetScrollPane.setViewportView(testDatasetPanel);

		setupPane.addTab("Predict with Hypothetical TPS/Mixture", tpsMixtureScrollPane);
		setupPane.addTab("Predict and Compare with Test Dataset", testDatasetScrollPane);
		setupPane.setPreferredSize(new Dimension(600, 240));
//		setupPane.setMinimumSize(new Dimension(1280, 280));

		infoPanel.setPreferredSize(new Dimension(640, 200));
//		infoPanel.setMinimumSize(new Dimension(640, 300));
		infoPanel.initialize();

		this.add(controlPanel, "cell 0 0, aligny top");
		this.add(infoPanel, "cell 1 0, grow, wrap");
		this.add(setupPane, "cell 0 1, grow, span");
	}

	private void addPredictionPanels()
	{
		flushRatePredictionByTPSPanel = new FlushRatePredictionByTPSPanel();
		flushRatePredictionByCountsPanel = new FlushRatePredictionByCountsPanel();
		maxThroughputPredictionPanel = new MaxThroughputPredictionPanel();
		lockPredictionPanel = new LockPredictionPanel();
		transactionCountsToCpuByTPSPanel = new TransactionCountsToCpuByTPSPanel();
		transactionCountsToCpuByCountsPanel = new TransactionCountsToCpuByCountsPanel();
		transactionCountsToIOPanel = new TransactionCountsToIOPanel();
		transactionCountsToLatencyPanel = new TransactionCountsToLatencyPanel();
		transactionCountsToLatencyPanel99 = new TransactionCountsToLatencyPanel();
		transactionCountsToLatencyPanelMedian = new TransactionCountsToLatencyPanel();
		transactionCountsWaitTimeToLatencyPanel = new TransactionCountsWaitTimeToLatencyPanel();
		transactionCountsWaitTimeToLatencyPanel99 = new TransactionCountsWaitTimeToLatencyPanel();
		transactionCountsWaitTimeToLatencyPanelMedian = new TransactionCountsWaitTimeToLatencyPanel();
		blownTransactionCountsToCpuPanel = new BlownTransactionCountsToCpuPanel();
		blownTransactionCountsToIOPanel = new BlownTransactionCountsToIOPanel();
		linearPredictionPanel = new LinearPredictionPanel();
		physicalReadPredictionPanel = new PhysicalReadPredictionPanel();

		predictionSetupPanel.add(flushRatePredictionByTPSPanel, "Disk Flush Rate by TPS");
		predictionSetupPanel.add(flushRatePredictionByCountsPanel, "Disk Flush Rate by Individual Transactions");
		predictionSetupPanel.add(maxThroughputPredictionPanel, "Max Throughput");
		predictionSetupPanel.add(lockPredictionPanel, "Lock");
		predictionSetupPanel.add(transactionCountsToCpuByTPSPanel, "CPU by TPS");
		predictionSetupPanel.add(transactionCountsToCpuByCountsPanel, "CPU by Individual Transactions");
		predictionSetupPanel.add(transactionCountsToIOPanel, "IO by TPS");
		predictionSetupPanel.add(transactionCountsToLatencyPanel, "Latency");
		predictionSetupPanel.add(transactionCountsToLatencyPanel99, "Latency (99% Quantile)");
		predictionSetupPanel.add(transactionCountsToLatencyPanelMedian, "Latency (Median)");
		predictionSetupPanel.add(transactionCountsWaitTimeToLatencyPanel, "Latency (With Lock Waits)");
		predictionSetupPanel.add(transactionCountsWaitTimeToLatencyPanel99, "Latency (With Lock Waits, 99% Quantile)");
		predictionSetupPanel.add(transactionCountsWaitTimeToLatencyPanelMedian, "Latency (With Lock Waits, Median)");
		predictionSetupPanel.add(blownTransactionCountsToCpuPanel, "CPU by Blown Transaction Counts");
		predictionSetupPanel.add(blownTransactionCountsToIOPanel, "IO by Blown Transaction Counts");
		predictionSetupPanel.add(linearPredictionPanel, "Log Writes");
		predictionSetupPanel.add(physicalReadPredictionPanel, "Physical Disk Read");
	}

	private boolean setPredictionSpecificValues(PredictionCenter center)
	{
		if (((String)predictionComboBox.getSelectedItem()).compareTo("Disk Flush Rate by TPS") == 0)
		{
			if (!UserInputValidator.validateSingleRowMatrix(flushRatePredictionByTPSPanel.getIOConf()))
			{
				JOptionPane.showMessageDialog(null, "Please enter IO configuration correctly.", "Warning",
						JOptionPane.WARNING_MESSAGE);
				return false;
			}
			center.setIoConfiguration(flushRatePredictionByTPSPanel.getIOConf());
		}
		else if (((String)predictionComboBox.getSelectedItem()).compareTo("Disk Flush Rate by Individual Transactions") == 0)
		{
			if (!UserInputValidator.validateSingleRowMatrix(flushRatePredictionByCountsPanel.getIOConf()))
			{
				JOptionPane.showMessageDialog(null, "Please enter IO configuration correctly.", "Warning",
						JOptionPane.WARNING_MESSAGE);
				return false;
			}
			center.setIoConfiguration(flushRatePredictionByCountsPanel.getIOConf());
			if (!UserInputValidator.validateSingleRowMatrix(flushRatePredictionByCountsPanel.getWhichTransactiontoPlot()) &&
					!UserInputValidator.validateNumber(flushRatePredictionByCountsPanel.getWhichTransactiontoPlot()))
			{
				JOptionPane.showMessageDialog(null, "Please enter transaction type to plot correctly.", "Warning",
						JOptionPane.WARNING_MESSAGE);
				return false;
			}
			center.setTransactionTypeToPlot(flushRatePredictionByCountsPanel.getWhichTransactiontoPlot());
		}
		else if (((String)predictionComboBox.getSelectedItem()).compareTo("Max Throughput") == 0)
		{
			if (!UserInputValidator.validateSingleRowMatrix(maxThroughputPredictionPanel.getIOConf()))
			{
				JOptionPane.showMessageDialog(null, "Please enter IO configuration correctly.", "Warning",
						JOptionPane.WARNING_MESSAGE);
				return false;
			}
			center.setIoConfiguration(maxThroughputPredictionPanel.getIOConf());
			if (!UserInputValidator.validateSingleRowMatrix(maxThroughputPredictionPanel.getLockConf()))
			{
				JOptionPane.showMessageDialog(null, "Please enter lock configuration correctly.", "Warning",
						JOptionPane.WARNING_MESSAGE);
				return false;
			}
			center.setLockConfiguration(maxThroughputPredictionPanel.getLockConf());
		}
		else if (((String)predictionComboBox.getSelectedItem()).compareTo("Lock") == 0)
		{
			if (!UserInputValidator.validateSingleRowMatrix(lockPredictionPanel.getLockConf()))
			{
				JOptionPane.showMessageDialog(null, "Please enter lock configuration correctly.", "Warning",
						JOptionPane.WARNING_MESSAGE);
				return false;
			}
			center.setLockConfiguration(lockPredictionPanel.getLockConf());
			center.setLockType(lockPredictionPanel.getLockType());
			center.setLearnLock(lockPredictionPanel.getLearnLock());
		}
		else if (((String)predictionComboBox.getSelectedItem()).compareTo("CPU by Individual Transactions") == 0)
		{
			if (!UserInputValidator.validateSingleRowMatrix(transactionCountsToCpuByCountsPanel.getWhichTransactiontoPlot()) &&
					!UserInputValidator.validateNumber(transactionCountsToCpuByCountsPanel.getWhichTransactiontoPlot()))
			{
				JOptionPane.showMessageDialog(null, "Please enter transaction type to plot correctly.", "Warning",
						JOptionPane.WARNING_MESSAGE);
				return false;
			}
			center.setTransactionTypeToPlot(transactionCountsToCpuByCountsPanel.getWhichTransactiontoPlot());
		}
		else if (((String)predictionComboBox.getSelectedItem()).compareTo("Latency (With Lock Waits)") == 0)
		{
			if (!UserInputValidator.validateSingleRowMatrix(transactionCountsWaitTimeToLatencyPanel.getWhichTransactiontoPlot()) &&
					!UserInputValidator.validateNumber(transactionCountsWaitTimeToLatencyPanel.getWhichTransactiontoPlot()))
			{
				JOptionPane.showMessageDialog(null, "Please enter transaction type to plot correctly.", "Warning",
						JOptionPane.WARNING_MESSAGE);
				return false;
			}
			center.setTransactionTypeToPlot(transactionCountsWaitTimeToLatencyPanel.getWhichTransactiontoPlot());
		}
		else if (((String)predictionComboBox.getSelectedItem()).compareTo("Latency (With Lock Waits, 99% Quantile)") == 0)
		{
			if (!UserInputValidator.validateSingleRowMatrix(transactionCountsWaitTimeToLatencyPanel99.getWhichTransactiontoPlot()) &&
					!UserInputValidator.validateNumber(transactionCountsWaitTimeToLatencyPanel99.getWhichTransactiontoPlot()))
			{
				JOptionPane.showMessageDialog(null, "Please enter transaction type to plot correctly.", "Warning",
						JOptionPane.WARNING_MESSAGE);
				return false;
			}
			center.setTransactionTypeToPlot(transactionCountsWaitTimeToLatencyPanel99.getWhichTransactiontoPlot());
		}
		else if (((String)predictionComboBox.getSelectedItem()).compareTo("Latency (With Lock Waits, Median)") == 0)
		{
			if (!UserInputValidator.validateSingleRowMatrix(transactionCountsWaitTimeToLatencyPanelMedian.getWhichTransactiontoPlot()) &&
					!UserInputValidator.validateNumber(transactionCountsWaitTimeToLatencyPanelMedian.getWhichTransactiontoPlot()))
			{
				JOptionPane.showMessageDialog(null, "Please enter transaction type to plot correctly.", "Warning",
						JOptionPane.WARNING_MESSAGE);
				return false;
			}
			center.setTransactionTypeToPlot(transactionCountsWaitTimeToLatencyPanelMedian.getWhichTransactiontoPlot());
		}
		else if (((String)predictionComboBox.getSelectedItem()).compareTo("Log Writes") == 0)
		{
			if (!UserInputValidator.validateSingleRowMatrix(linearPredictionPanel.getWhichTransactiontoPlot()) &&
					!UserInputValidator.validateNumber(linearPredictionPanel.getWhichTransactiontoPlot()))
			{
				JOptionPane.showMessageDialog(null, "Please enter transaction type to plot correctly.", "Warning",
						JOptionPane.WARNING_MESSAGE);
				return false;
			}
			center.setTransactionTypeToPlot(linearPredictionPanel.getWhichTransactiontoPlot());
		}
		return true;
	}

	private boolean validateUserInput()
	{
		if (setupPane.getSelectedIndex() == DBSeerConstants.TEST_MODE_DATASET)
		{
			if (!testDatasetPanel.checkValidMinMaxTPS())
			{
					JOptionPane.showMessageDialog(null, "Maximum TPS must be greater than minimum TPS.", "Warning",
							JOptionPane.WARNING_MESSAGE);
					return false;
			}
		}
		else if (setupPane.getSelectedIndex() == DBSeerConstants.TEST_MODE_MIXTURE_TPS)
		{
			int minTPS = tpsMixturePanel.getMinTPS();
			int maxTPS = tpsMixturePanel.getMaxTPS();

			if (minTPS > maxTPS)
			{
				JOptionPane.showMessageDialog(null, "Hypothetical maximum TPS must be greater than minimum TPS.", "Warning",
						JOptionPane.WARNING_MESSAGE);
				return false;
			}
			if (!tpsMixturePanel.checkTransactionMix())
			{
				JOptionPane.showMessageDialog(null, "Transaction mix should have at least one transaction type " +
								"with its weight greater than zero.", "Warning",
						JOptionPane.WARNING_MESSAGE);
				return false;
			}
		}
		return true;
	}

	@Override
	public void actionPerformed(ActionEvent actionEvent)
	{
		final PredictionCenter center = new PredictionCenter(DBSeerGUI.runner,
				(String)predictionComboBox.getSelectedItem(),
				DBSeerGUI.userSettings.getDBSeerRootPath());

		// setup configuration variables for prediction center
		if (actionEvent.getSource() == predictionButton ||
				actionEvent.getSource() == showButton)
		{
			if (actionEvent.getSource() == predictionButton)
			{
				if (!validateUserInput())
				{
					return;
				}
			}

			center.setTestMode(setupPane.getSelectedIndex());
			center.setTrainConfig((DBSeerConfiguration) trainConfigComboBox.getSelectedItem());

			if (setupPane.getSelectedIndex() == DBSeerConstants.TEST_MODE_MIXTURE_TPS)
			{
				center.setTestManualMaxTPS(tpsMixturePanel.getMaxTPS());
				center.setTestManualMinTPS(tpsMixturePanel.getMinTPS());
				center.setTestMixture(tpsMixturePanel.getTransactionMix());
			}
			else
			{
				center.setTestDataset(testDatasetPanel.getTestDataset());
				center.setGroupingTarget(testDatasetPanel.getGroupingTarget());
				center.setGroupingType(testDatasetPanel.getGroupingType());
				center.setTestMinFrequency(testDatasetPanel.getMinSizeForGroup());
				center.setTestMinTPS(testDatasetPanel.getMinTPS());
				center.setTestMaxTPS(testDatasetPanel.getMaxTPS());
				center.setAllowedRelativeDiff(testDatasetPanel.getAllowedRelativeDifference());
				center.setNumClusters(testDatasetPanel.getNumGroups());
				center.setGroupRange(testDatasetPanel.getGroupRanges());
				center.setTransactionTypesToGroup(testDatasetPanel.getTransactionTypesToGroup());
			}

			// Prediction-specific options
			if (!setPredictionSpecificValues(center))
			{
				return;
			}
		}

		if (actionEvent.getSource() == predictionButton)
		{
			elapsedTimeLabel.setText("");
			final DBSeerConfiguration trainConfig = (DBSeerConfiguration)trainConfigComboBox.getSelectedItem();
			DBSeerDataSet dataset = trainConfig.getDataset();

			if (trainConfig == null)
			{
				JOptionPane.showMessageDialog(null, "Please select a train config.", "Warning",
						JOptionPane.WARNING_MESSAGE);
				return;
			}

			dataset.loadDataset(false);

			if (dataset.getTransactionTypeNames().size() == 0)
			{
				JOptionPane.showMessageDialog(null, "The dataset needs to have at least one transaction type enabled.", "Warning",
						JOptionPane.WARNING_MESSAGE);
				return;
			}


			if (setupPane.getSelectedIndex() == DBSeerConstants.TEST_MODE_DATASET &&
					testDatasetPanel.getTestDataset() == null)
			{
				JOptionPane.showMessageDialog(null, "Please select a test dataset.", "Warning",
						JOptionPane.WARNING_MESSAGE);
				return;
			}

			predictionButton.setEnabled(false);
			showButton.setEnabled(false);
			DBSeerGUI.status.setText("Performing Prediction...");

			SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>()
			{
				boolean isInitialized = false;
				boolean isRun = false;
				long startTime, endTime;
				double timeElapsed = 0;

				@Override
				protected Void doInBackground() throws Exception
				{
					isInitialized = center.initialize();

					if (isInitialized)
					{
						startTime = System.nanoTime();
						isRun = center.run();
						endTime = System.nanoTime();
						timeElapsed = (double)(endTime - startTime) / (1000 * 1000 * 1000);
					}
					return null;
				}

				@Override
				protected void done()
				{
					if (isRun)
					{
						DBSeerGUI.status.setText("Prediction Complete.");
						elapsedTimeLabel.setText(String.format("Elapsed Time: %.2fs", timeElapsed));
						predictionButton.setEnabled(true);
						showButton.setEnabled(true);
					}
					else
					{
						elapsedTimeLabel.setText("");
						predictionButton.setEnabled(true);
						predictionButton.requestFocus();
						showButton.setEnabled(false);
						DBSeerGUI.status.setText("");
					}
				}
			};

			worker.execute();
		}
		else if (actionEvent.getSource() == showButton)
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					DBSeerPredictionFrame predictionFrame = new DBSeerPredictionFrame(center);
					predictionFrame.pack();
					predictionFrame.setVisible(true);
					predictionButton.setEnabled(true);
					predictionButton.requestFocus();
					DBSeerGUI.status.setText("");
				}
			});
		}
		else if (actionEvent.getSource() == predictionComboBox)
		{
			CardLayout layout = (CardLayout)predictionSetupPanel.getLayout();
			layout.show(predictionSetupPanel, (String)predictionComboBox.getSelectedItem());

			if (((String) predictionComboBox.getSelectedItem()).compareTo("Physical Disk Read") == 0)
			{
				setupPane.setEnabledAt(DBSeerConstants.TEST_MODE_MIXTURE_TPS, false);
				setupPane.setSelectedIndex(DBSeerConstants.TEST_MODE_DATASET);
			}
			else
			{
				setupPane.setEnabledAt(DBSeerConstants.TEST_MODE_MIXTURE_TPS, true);
			}

			if (((String) predictionComboBox.getSelectedItem()).compareTo("Latency (99% Quantile)") == 0 ||
					((String) predictionComboBox.getSelectedItem()).compareTo("Latency (Median)") == 0 ||
					((String) predictionComboBox.getSelectedItem()).compareTo("Latency (With Lock Waits, 99% Quantile)") == 0 ||
					((String) predictionComboBox.getSelectedItem()).compareTo("Latency (With Lock Waits, Median)") == 0)
			{
				if (testDatasetPanel.getGroupingTypeBox() != null)
				{
					testDatasetPanel.getGroupingTypeBox().setSelectedIndex(DBSeerConstants.GROUP_NONE);
					testDatasetPanel.getGroupingTypeBox().setEnabled(false);
				}
//				groupingTypeBox.setEnabled(false);
			}
			else
			{
				if (testDatasetPanel.getGroupingTypeBox() != null)
				{
					testDatasetPanel.getGroupingTypeBox().setEnabled(true);
				}
//				groupingTypeBox.setEnabled(true);
			}
		}
		else if (actionEvent.getSource() == trainConfigComboBox)
		{
			DBSeerGUI.status.setText("Loading dataset from the train config...");
			predictionButton.setEnabled(false);
			DBSeerConfiguration config = (DBSeerConfiguration)trainConfigComboBox.getSelectedItem();
			if (config != null)
			{
				String ioConf = ((DBSeerConfiguration) trainConfigComboBox.getSelectedItem()).getIoConfiguration();
				String lockConf = ((DBSeerConfiguration) trainConfigComboBox.getSelectedItem()).getLockConfiguration();

				flushRatePredictionByTPSPanel.setIOConf(ioConf);
				flushRatePredictionByCountsPanel.setIOConf(ioConf);
				maxThroughputPredictionPanel.setIOConf(ioConf);
				maxThroughputPredictionPanel.setLockConf(lockConf);
				lockPredictionPanel.setLockConf(lockConf);
			}
			final DBSeerDataSet dataset = config.getDataset(0);
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					infoPanel.setDataset(dataset);
					tpsMixturePanel.setDataset(dataset);
					testDatasetPanel.setDataset(dataset);
					DBSeerGUI.repaintMainFrame();
					DBSeerGUI.status.setText("");
					predictionButton.setEnabled(true);
				}
			});
		}
	}
}
