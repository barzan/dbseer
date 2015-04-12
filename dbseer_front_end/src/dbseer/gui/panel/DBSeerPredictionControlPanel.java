package dbseer.gui.panel;

import dbseer.gui.DBSeerConstants;
import dbseer.comp.PredictionCenter;
import dbseer.comp.UserInputValidator;
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
public class DBSeerPredictionControlPanel extends JPanel implements ActionListener
{
	public static Set<String> predictionSet = new HashSet<String>();

	private JComboBox predictionComboBox;
	private JComboBox trainConfigComboBox;
	private JComboBox predictionTestModeComboBox;
	private JComboBox testDatasetComboBox;
	private JComboBox groupingTypeBox;
	private JComboBox groupingTargetBox;

	private JTextField testMixtureTextField;
	private JTextField testMinTPSTextField;
	private JTextField testMaxTPSTextField;
	private JTextField minFrequencyTextField;
	private JTextField minTPSTextField;
	private JTextField maxTPSTextField;
	private JTextField allowedRelativeDiffTextField;
	private JTextField numClusterTextField;
	private JTextField transactionTypeToGroupTextField;
	private JTextArea groupingRangeTextArea;

	private JLabel testMixtureLabel;
	private JLabel testMinTPSLabel;
	private JLabel testMaxTPSLabel;
	private JLabel minFrequencyLabel;
	private JLabel minTPSLabel;
	private JLabel maxTPSLabel;
	private JLabel allowedRelativeDiffLabel;
	private JLabel numClusterLabel;
	private JLabel transactionTypesToGroupLabel;
	private JLabel groupingRangeLabel;
	private JLabel elapsedTimeLabel;

	private JButton predictionButton;
	private JButton showButton;

	private JPanel controlPanel;
	private JPanel testPanel;
	private JPanel predictionSetupPanel;
	private JPanel datasetSetupPanel;
	private JPanel mixtureTPSSetupPanel;
	private JPanel groupingSetupPanel;

	private JScrollPane groupingRangeScrollPane;

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

	public DBSeerPredictionControlPanel()
	{
		initializeGUI();
	}

	private void initializeGUI()
	{
	 	this.setLayout(new MigLayout("fill", "[][]"));

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
		showButton = new JButton("Show Result");
		showButton.setEnabled(false);
		showButton.addActionListener(this);
		elapsedTimeLabel = new JLabel("");

		controlPanel = new JPanel();
		controlPanel.setLayout(new MigLayout("fill, ins 5 5 5 5"));
		controlPanel.add(predictionComboBox, "cell 0 0, growx");
		controlPanel.add(trainConfigComboBox, "cell 0 1, growx");
		controlPanel.setBorder(BorderFactory.createTitledBorder("Choose a prediction"));

		groupingSetupPanel = new JPanel();
		groupingSetupPanel.setLayout(new MigLayout("fill, ins 5 5 5 5"));
		groupingSetupPanel.setBorder(BorderFactory.createTitledBorder("Grouping options"));

		datasetSetupPanel = new JPanel();
		datasetSetupPanel.setLayout(new MigLayout("fill, ins 5 5 5 5"));
		datasetSetupPanel.setBorder(BorderFactory.createTitledBorder("Choose a test dataset"));
		datasetSetupPanel.setPreferredSize(new Dimension(600,300));

		mixtureTPSSetupPanel = new JPanel();
		mixtureTPSSetupPanel.setLayout(new MigLayout("fill, ins 5 5 5 5"));
		mixtureTPSSetupPanel.setBorder(BorderFactory.createTitledBorder("Specify test TPS + mixture"));
		mixtureTPSSetupPanel.setPreferredSize(new Dimension(300,150));

		predictionTestModeComboBox = new JComboBox(DBSeerGUI.availablePredictionTestModes);
		predictionTestModeComboBox.setBorder(BorderFactory.createTitledBorder("Prediction Test Target"));
		predictionTestModeComboBox.addActionListener(this);

		testDatasetComboBox = new JComboBox(new SharedComboBoxModel(DBSeerGUI.datasets));
		testDatasetComboBox.setBorder(BorderFactory.createTitledBorder("Test Dataset"));

		testMixtureTextField = new JTextField();
		testMixtureLabel = new JLabel("Test Mixture (e.g. [0.4 0.4 0.1 0.05 0.05]):");
		testMinTPSTextField = new JTextField();
		testMinTPSLabel = new JLabel("Test Minimum TPS:");
		testMaxTPSTextField = new JTextField();
		testMaxTPSLabel = new JLabel("Test Maximum TPS:");

		controlPanel.add(predictionTestModeComboBox, "growx, wrap");

		minFrequencyLabel = new JLabel("Min Freq:");
		minTPSLabel = new JLabel("Min TPS:");
		maxTPSLabel = new JLabel("Max TPS:");
		allowedRelativeDiffLabel = new JLabel("Allowed relative diff:");
		numClusterLabel = new JLabel("Number of clusters:");
		transactionTypesToGroupLabel = new JLabel("Transactions to group:");
		groupingRangeLabel = new JLabel("Group ranges (e.g. [100 200; 300 400; 500 600 ... ]):");

		minFrequencyTextField = new JTextField();
		minTPSTextField = new JTextField();
		maxTPSTextField = new JTextField();
		allowedRelativeDiffTextField = new JTextField();
		numClusterTextField = new JTextField();
		transactionTypeToGroupTextField = new JTextField();
		groupingRangeTextArea = new JTextArea();
		groupingRangeScrollPane = new JScrollPane(groupingRangeTextArea);
		groupingRangeScrollPane.setPreferredSize(new Dimension(60,60));

		groupingSetupPanel.add(groupingTypeBox, "cell 0 0, growx");
		groupingSetupPanel.add(groupingTargetBox, "cell 1 0, growx, wrap");
		groupingSetupPanel.add(minFrequencyLabel, "split 2");
		groupingSetupPanel.add(minFrequencyTextField, "grow");
		groupingSetupPanel.add(allowedRelativeDiffLabel, "split 2");
		groupingSetupPanel.add(allowedRelativeDiffTextField, "grow, wrap");
		groupingSetupPanel.add(minTPSLabel, "split 2");
		groupingSetupPanel.add(minTPSTextField, "grow");
		groupingSetupPanel.add(numClusterLabel, "split 2");
		groupingSetupPanel.add(numClusterTextField, "grow, wrap");
		groupingSetupPanel.add(maxTPSLabel, "split 2");
		groupingSetupPanel.add(maxTPSTextField, "grow");
		groupingSetupPanel.add(transactionTypesToGroupLabel, "split 2");
		groupingSetupPanel.add(transactionTypeToGroupTextField, "grow, wrap");
		groupingSetupPanel.add(groupingRangeLabel, "wrap");
		groupingSetupPanel.add(groupingRangeScrollPane, "spanx 2, grow");

		datasetSetupPanel.add(testDatasetComboBox, "cell 0 0, growx");
		datasetSetupPanel.add(groupingSetupPanel, "cell 0 1, growx");

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

//		this.add(testPanel, "cell 0 1, aligny top, grow");
		this.add(controlPanel, "cell 0 0 1 2, grow, aligny top");
		this.add(datasetSetupPanel, "cell 1 0, grow");
		this.add(mixtureTPSSetupPanel, "cell 1 1, grow");

		setFormAvailability();
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

//		predictionSetupPanel.add(flushRatePredictionByTPSPanel, "FlushRatePredictionByTPS");
//		predictionSetupPanel.add(flushRatePredictionByCountsPanel, "FlushRatePredictionByCounts");
//		predictionSetupPanel.add(maxThroughputPredictionPanel, "MaxThroughputPrediction");
//		predictionSetupPanel.add(lockPredictionPanel, "LockPrediction");
//		predictionSetupPanel.add(transactionCountsToCpuByTPSPanel, "TransactionCountsToCpuByTPS");
//		predictionSetupPanel.add(transactionCountsToCpuByCountsPanel, "TransactionCountsToCpuByCounts");
//		predictionSetupPanel.add(transactionCountsToIOPanel, "TransactionCountsToIO");
//		predictionSetupPanel.add(transactionCountsToLatencyPanel, "TransactionCountsToLatency");
//		predictionSetupPanel.add(transactionCountsWaitTimeToLatencyPanel, "TransactionCountsWaitTimeToLatency");
//		predictionSetupPanel.add(blownTransactionCountsToCpuPanel, "BlownTransactionCountsToCpu");
//		predictionSetupPanel.add(blownTransactionCountsToIOPanel, "BlownTransactionCountsToIO");
//		predictionSetupPanel.add(linearPredictionPanel, "LinearPrediction");
//		predictionSetupPanel.add(physicalReadPredictionPanel, "PhysicalReadPrediction");
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
		if (!UserInputValidator.validateSingleRowMatrix(testMixtureTextField.getText().trim(), "Test Mixture", testMixtureTextField.isEnabled()) ||
				!UserInputValidator.validateSingleRowMatrix(transactionTypeToGroupTextField.getText().trim(), "Transactions to group", transactionTypeToGroupTextField.isEnabled()) ||
				!UserInputValidator.validateNumber(minFrequencyTextField.getText().trim(), "Min Freq", minFrequencyTextField.isEnabled()) ||
				!UserInputValidator.validateNumber(minTPSTextField.getText().trim(), "Min TPS", minTPSTextField.isEnabled()) ||
				!UserInputValidator.validateNumber(maxTPSTextField.getText().trim(), "Max TPS", maxTPSTextField.isEnabled()) ||
				!UserInputValidator.validateNumber(testMinTPSTextField.getText().trim(), "Test Minimum TPS", testMinTPSTextField.isEnabled()) ||
				!UserInputValidator.validateNumber(testMaxTPSTextField.getText().trim(), "Test Maximum TPS", testMaxTPSTextField.isEnabled()) ||
				!UserInputValidator.validateNumber(allowedRelativeDiffTextField.getText().trim(), "Allowed Relative diff", allowedRelativeDiffTextField.isEnabled()) ||
				!UserInputValidator.validateNumber(numClusterTextField.getText().trim(), "Number of clusters", numClusterTextField.isEnabled()) ||
				!UserInputValidator.validateMatlabMatrix(groupingRangeTextArea.getText().trim(), "Grouping Range", groupingRangeTextArea.isEnabled())
				)
		{
			return false;
		}
		if (testMixtureTextField.isEnabled())
		{
			if (!UserInputValidator.matchMatrixDimension(((DBSeerConfiguration) trainConfigComboBox.getSelectedItem()).getTransactionTypes(),
					testMixtureTextField.getText().trim()))
			{
				JOptionPane.showMessageDialog(null, "Test Mixture must have same dimension as the transaction type of the train config.", "Warning",
						JOptionPane.WARNING_MESSAGE);
				return false;
			}
		}
		if (minTPSTextField.isEnabled() || maxTPSTextField.isEnabled())
		{
			int minTPS = Integer.parseInt(minTPSTextField.getText().trim());
			int maxTPS = Integer.parseInt(maxTPSTextField.getText().trim());

			if (minTPS < 0 || maxTPS < 0)
			{
				JOptionPane.showMessageDialog(null, "Min/Max TPS must be greater than zero.", "Warning",
						JOptionPane.WARNING_MESSAGE);
				return false;
			}
			if (minTPS >= maxTPS)
			{
				JOptionPane.showMessageDialog(null, "Max TPS must be greater than Min TPS.", "Warning",
						JOptionPane.WARNING_MESSAGE);
				return false;
			}
		}
		if (testMinTPSTextField.isEnabled() || testMaxTPSTextField.isEnabled())
		{
			int minTPS = Integer.parseInt(testMinTPSTextField.getText().trim());
			int maxTPS = Integer.parseInt(testMaxTPSTextField.getText().trim());

			if (minTPS < 0 || maxTPS < 0)
			{
				JOptionPane.showMessageDialog(null, "Test Minimum/Maximum TPS must be greater than zero.", "Warning",
						JOptionPane.WARNING_MESSAGE);
				return false;
			}
			if (minTPS >= maxTPS)
			{
				JOptionPane.showMessageDialog(null, "Test Maximum TPS must be greater than Test Minimum TPS.", "Warning",
						JOptionPane.WARNING_MESSAGE);
				return false;
			}
		}
		return true;
	}

	@Override
	public void actionPerformed(ActionEvent actionEvent)
	{
		final PredictionCenter center = new PredictionCenter(DBSeerGUI.proxy,
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

			center.setTestMode(predictionTestModeComboBox.getSelectedIndex());
			if (testDatasetComboBox.getSelectedItem() != null)
			{
				center.setTestDataset((DBSeerDataSet) testDatasetComboBox.getSelectedItem());
			}
			center.setTrainConfig((DBSeerConfiguration) trainConfigComboBox.getSelectedItem());
			center.setGroupingTarget(groupingTargetBox.getSelectedIndex());
			center.setGroupingType(groupingTypeBox.getSelectedIndex());
			center.setGroupRange(groupingRangeTextArea.getText());
			String minFreqString = minFrequencyTextField.getText().trim();
			center.setTestMinFrequency(!minFreqString.isEmpty() ? Double.parseDouble(minFreqString) : 0.0);
			String minTPS = minTPSTextField.getText().trim();
			center.setTestMinTPS(!minTPS.isEmpty() ? Double.parseDouble(minTPS) : 0.0);
			String maxTPS = maxTPSTextField.getText().trim();
			center.setTestMaxTPS(!maxTPS.isEmpty() ? Double.parseDouble(maxTPS) : 0.0);
			String allowedRelDiff = allowedRelativeDiffTextField.getText().trim();
			center.setAllowedRelativeDiff(!allowedRelDiff.isEmpty() ? Double.parseDouble(allowedRelDiff) : 0.0);
			String numCluster = numClusterTextField.getText().trim();
			center.setNumClusters(!numCluster.isEmpty() ? Integer.parseInt(numCluster) : 0);
			center.setTransactionTypesToGroup(transactionTypeToGroupTextField.getText());

			String testManualMinTPS = testMinTPSTextField.getText().trim();
			center.setTestManualMinTPS(!testManualMinTPS.isEmpty() ? Double.parseDouble(testManualMinTPS) : 0.0);
			String testManualMaxTPS = testMaxTPSTextField.getText().trim();
			center.setTestManualMaxTPS(!testManualMaxTPS.isEmpty() ? Double.parseDouble(testManualMaxTPS) : 0.0);
			String testMixture = testMixtureTextField.getText().trim();
			center.setTestMixture(testMixture);

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

			if (trainConfig == null)
			{
				JOptionPane.showMessageDialog(null, "Please select a train config.", "Warning",
						JOptionPane.WARNING_MESSAGE);
				return;
			}

			if (((String)predictionTestModeComboBox.getSelectedItem()).equalsIgnoreCase("Dataset") &&
					testDatasetComboBox.getSelectedItem() == null)
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
//						SwingUtilities.invokeLater(new Runnable()
//						{
//							@Override
//							public void run()
//							{
//								DBSeerPredictionFrame predictionFrame = new DBSeerPredictionFrame(center);
//								predictionFrame.pack();
//								predictionFrame.setVisible(true);
//								predictionButton.setEnabled(true);
//								predictionButton.requestFocus();
//								DBSeerGUI.status.setText("");
//							}
//						});
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
		else if (actionEvent.getSource() == predictionTestModeComboBox ||
				actionEvent.getSource() == groupingTargetBox ||
				actionEvent.getSource() == groupingTypeBox)
		{
			setFormAvailability();
		}
		else if (actionEvent.getSource() == predictionComboBox)
		{
			CardLayout layout = (CardLayout)predictionSetupPanel.getLayout();
			layout.show(predictionSetupPanel, (String)predictionComboBox.getSelectedItem());

			if (((String) predictionComboBox.getSelectedItem()).compareTo("Physical Disk Read") == 0)
			{
				predictionTestModeComboBox.setSelectedIndex(DBSeerConstants.TEST_MODE_DATASET);
				predictionTestModeComboBox.setEnabled(false);
			}
			else
			{
				predictionTestModeComboBox.setEnabled(true);
			}

			if (((String) predictionComboBox.getSelectedItem()).compareTo("Latency (99% Quantile)") == 0 ||
					((String) predictionComboBox.getSelectedItem()).compareTo("Latency (Median)") == 0 ||
					((String) predictionComboBox.getSelectedItem()).compareTo("Latency (With Lock Waits, 99% Quantile)") == 0 ||
					((String) predictionComboBox.getSelectedItem()).compareTo("Latency (With Lock Waits, Median)") == 0)
			{
				groupingTypeBox.setSelectedIndex(DBSeerConstants.GROUP_NONE);
				groupingTypeBox.setEnabled(false);
			}
			else
			{
				groupingTypeBox.setEnabled(true);
			}
		}
		else if (actionEvent.getSource() == trainConfigComboBox)
		{
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
		}
	}

	// change setEnabled for components depending on user selected combobox values.
	private void setFormAvailability()
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				if (predictionTestModeComboBox.getSelectedIndex() == DBSeerConstants.TEST_MODE_DATASET)
				{
					for (Component comp : datasetSetupPanel.getComponents())
					{
						comp.setEnabled(true);
					}
					for (Component comp : mixtureTPSSetupPanel.getComponents())
					{
						comp.setEnabled(false);
					}

					// Disable all grouping first.
					for (Component comp : groupingSetupPanel.getComponents())
					{
						comp.setEnabled(false);
					}
					groupingRangeTextArea.setEditable(false);
					groupingRangeTextArea.setEnabled(false);
//					groupingTypeBox.setEnabled(true);
					if (((String) predictionComboBox.getSelectedItem()).compareTo("Latency (99% Quantile)") == 0 ||
							((String) predictionComboBox.getSelectedItem()).compareTo("Latency (Median)") == 0 ||
							((String) predictionComboBox.getSelectedItem()).compareTo("Latency (With Lock Waits, 99% Quantile)") == 0 ||
							((String) predictionComboBox.getSelectedItem()).compareTo("Latency (With Lock Waits, Median)") == 0)
					{
						groupingTypeBox.setEnabled(false);
					}
					else
					{
						groupingTypeBox.setEnabled(true);
					}

					if (groupingTypeBox.getSelectedIndex() == DBSeerConstants.GROUP_NONE)
					{
						// nothing is enabled.
						return;
					}

					minTPSTextField.setEnabled(true);
					minTPSLabel.setEnabled(true);
					minFrequencyLabel.setEnabled(true);
					minFrequencyTextField.setEnabled(true);
					maxTPSLabel.setEnabled(true);
					maxTPSTextField.setEnabled(true);

					if (groupingTypeBox.getSelectedIndex() == DBSeerConstants.GROUP_RANGE)
					{
						groupingTargetBox.setEnabled(true);
						groupingRangeLabel.setEnabled(true);
						groupingRangeTextArea.setEnabled(true);
						groupingRangeTextArea.setEditable(true);
						groupingRangeScrollPane.setEnabled(true);
						return;
					}
					else if (groupingTypeBox.getSelectedIndex() == DBSeerConstants.GROUP_REL_DIFF)
					{
						groupingTargetBox.setEnabled(true);
						allowedRelativeDiffTextField.setEnabled(true);
						allowedRelativeDiffLabel.setEnabled(true);
					}
					else if (groupingTypeBox.getSelectedIndex() == DBSeerConstants.GROUP_NUM_CLUSTER)
					{
						groupingTargetBox.setEnabled(true);
						numClusterTextField.setEnabled(true);
						numClusterLabel.setEnabled(true);
					}

					if (groupingTargetBox.getSelectedIndex() == DBSeerConstants.GROUP_TARGET_INDIVIDUAL_TRANS_COUNT)
					{
						transactionTypesToGroupLabel.setEnabled(true);
						transactionTypeToGroupTextField.setEnabled(true);
					}
					else if (groupingTargetBox.getSelectedIndex() == DBSeerConstants.GROUP_TARGET_TPS)
					{
						transactionTypesToGroupLabel.setEnabled(false);
						transactionTypeToGroupTextField.setEnabled(false);
					}
				}
				else if (predictionTestModeComboBox.getSelectedIndex() == DBSeerConstants.TEST_MODE_MIXTURE_TPS)
				{
					datasetSetupPanel.setEnabled(false);
					for (Component comp : datasetSetupPanel.getComponents())
					{
						comp.setEnabled(false);
					}
					for (Component comp : groupingSetupPanel.getComponents())
					{
						comp.setEnabled(false);
					}
					mixtureTPSSetupPanel.setEnabled(true);
					for (Component comp : mixtureTPSSetupPanel.getComponents())
					{
						comp.setEnabled(true);
					}
				}
			}
		});

	}
}
