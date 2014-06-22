package dbseer.gui.panel;

import dbseer.gui.DBSeerConstants;
import dbseer.gui.comp.PredictionCenter;
import dbseer.gui.panel.prediction.*;
import dbseer.gui.user.DBSeerConfiguration;
import dbseer.gui.DBSeerGUI;
import dbseer.gui.actions.CheckPredictionBoxAction;
import dbseer.gui.frame.DBSeerPredictionFrame;
import dbseer.gui.model.SharedComboBoxModel;
import dbseer.gui.user.DBSeerDataSet;
import matlabcontrol.MatlabInvocationException;
import matlabcontrol.MatlabProxy;
import net.miginfocom.swing.MigLayout;

import javax.smartcardio.Card;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
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

	private JButton predictionButton;

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
	private TransactionCountsWaitTimeToLatencyPanel transactionCountsWaitTimeToLatencyPanel;
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
	 	this.setLayout(new MigLayout("", "[][]"));

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
		testMixtureLabel = new JLabel("Test Mixture:");
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
		groupingRangeLabel = new JLabel("Group ranges");

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
		mixtureTPSSetupPanel.add(testMixtureTextField, "grow, wrap");
		mixtureTPSSetupPanel.add(testMinTPSLabel, "split 2");
		mixtureTPSSetupPanel.add(testMinTPSTextField, "grow, wrap");
		mixtureTPSSetupPanel.add(testMaxTPSLabel, "split 2");
		mixtureTPSSetupPanel.add(testMaxTPSTextField, "grow, wrap");

		predictionSetupPanel = new JPanel(new CardLayout());
		predictionSetupPanel.setBorder(BorderFactory.createTitledBorder("Prediction Setup"));
		predictionSetupPanel.setPreferredSize(new Dimension(400, 200));
		predictionSetupPanel.add(new EmptyPredictionPanel(), "No Prediction");

		addPredictionPanels();

		((CardLayout)predictionSetupPanel.getLayout()).show(predictionSetupPanel,
				(String) predictionComboBox.getSelectedItem());

		controlPanel.add(predictionSetupPanel, "spanx 2, grow, wrap");
		controlPanel.add(predictionButton, "");

//		this.add(testPanel, "cell 0 1, aligny top, grow");
		this.add(controlPanel, "cell 0 0 1 2, aligny top");
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
		transactionCountsWaitTimeToLatencyPanel = new TransactionCountsWaitTimeToLatencyPanel();
		blownTransactionCountsToCpuPanel = new BlownTransactionCountsToCpuPanel();
		blownTransactionCountsToIOPanel = new BlownTransactionCountsToIOPanel();
		linearPredictionPanel = new LinearPredictionPanel();
		physicalReadPredictionPanel = new PhysicalReadPredictionPanel();

		predictionSetupPanel.add(flushRatePredictionByTPSPanel, "FlushRatePredictionByTPS");
		predictionSetupPanel.add(flushRatePredictionByCountsPanel, "FlushRatePredictionByCounts");
		predictionSetupPanel.add(maxThroughputPredictionPanel, "MaxThroughputPrediction");
		predictionSetupPanel.add(lockPredictionPanel, "LockPrediction");
		predictionSetupPanel.add(transactionCountsToCpuByTPSPanel, "TransactionCountsToCpuByTPS");
		predictionSetupPanel.add(transactionCountsToCpuByCountsPanel, "TransactionCountsToCpuByCounts");
		predictionSetupPanel.add(transactionCountsToIOPanel, "TransactionCountsToIO");
		predictionSetupPanel.add(transactionCountsToLatencyPanel, "TransactionCountsToLatency");
		predictionSetupPanel.add(transactionCountsWaitTimeToLatencyPanel, "TransactionCountsWaitTimeToLatency");
		predictionSetupPanel.add(blownTransactionCountsToCpuPanel, "BlownTransactionCountsToCpu");
		predictionSetupPanel.add(blownTransactionCountsToIOPanel, "BlownTransactionCountsToIO");
		predictionSetupPanel.add(linearPredictionPanel, "LinearPrediction");
		predictionSetupPanel.add(physicalReadPredictionPanel, "PhysicalReadPrediction");
	}

	private void setPredictionSpecificValues(PredictionCenter center)
	{
		if (((String)predictionComboBox.getSelectedItem()).compareTo("FlushRatePredictionByTPS") == 0)
		{
			center.setIoConfiguration(flushRatePredictionByTPSPanel.getIOConf());
		}
		else if (((String)predictionComboBox.getSelectedItem()).compareTo("FlushRatePredictionByCounts") == 0)
		{
			center.setIoConfiguration(flushRatePredictionByCountsPanel.getIOConf());
			center.setTransactionTypeToPlot(flushRatePredictionByCountsPanel.getWhichTransactiontoPlot());
		}
		else if (((String)predictionComboBox.getSelectedItem()).compareTo("MaxThroughputPrediction") == 0)
		{
			center.setIoConfiguration(maxThroughputPredictionPanel.getIOConf());
			center.setLockConfiguration(maxThroughputPredictionPanel.getLockConf());
		}
		else if (((String)predictionComboBox.getSelectedItem()).compareTo("LockPrediction") == 0)
		{
			center.setLockConfiguration(lockPredictionPanel.getLockConf());
			center.setLearnLock(lockPredictionPanel.getLearnLock());
			center.setLockType(lockPredictionPanel.getLockType());
		}
		else if (((String)predictionComboBox.getSelectedItem()).compareTo("TransactionCountsToCpuByCounts") == 0)
		{
			center.setTransactionTypeToPlot(transactionCountsToCpuByCountsPanel.getWhichTransactiontoPlot());
		}
		else if (((String)predictionComboBox.getSelectedItem()).compareTo("TransactionCountsWaitTimeToLatency") == 0)
		{
			center.setTransactionTypeToPlot(transactionCountsWaitTimeToLatencyPanel.getWhichTransactiontoPlot());
		}
		else if (((String)predictionComboBox.getSelectedItem()).compareTo("LinearPrediction") == 0)
		{
			center.setTransactionTypeToPlot(linearPredictionPanel.getWhichTransactiontoPlot());
		}
	}

	@Override
	public void actionPerformed(ActionEvent actionEvent)
	{
		if (actionEvent.getSource() == predictionButton)
		{
			final DBSeerConfiguration trainConfig = (DBSeerConfiguration)trainConfigComboBox.getSelectedItem();

			if (trainConfig == null)
			{
				return;
			}

			predictionButton.setEnabled(false);
			DBSeerGUI.status.setText("Performing prediction...");

			final PredictionCenter center = new PredictionCenter(DBSeerGUI.proxy,
					(String)predictionComboBox.getSelectedItem(),
					DBSeerGUI.userSettings.getDBSeerRootPath());

			// setup configuration variables for prediction center
			center.setTestMode(predictionTestModeComboBox.getSelectedIndex());
			if (testDatasetComboBox.getSelectedItem() != null)
			{
				center.setTestDataset((DBSeerDataSet) testDatasetComboBox.getSelectedItem());
			}
			center.setTrainConfig((DBSeerConfiguration) trainConfigComboBox.getSelectedItem());
			center.setGroupingTarget(groupingTargetBox.getSelectedIndex());
			center.setGroupingType(groupingTypeBox.getSelectedIndex());
			center.setGroupRange(groupingRangeTextArea.getText());
			String minFreqString = minFrequencyTextField.getText();
			center.setTestMinFrequency(!minFreqString.isEmpty() ? Double.parseDouble(minFreqString) : 0.0);
			String minTPS = minTPSTextField.getText();
			center.setTestMinTPS(!minTPS.isEmpty() ? Double.parseDouble(minTPS) : 0.0);
			String maxTPS = maxTPSTextField.getText();
			center.setTestMaxTPS(!maxTPS.isEmpty() ? Double.parseDouble(maxTPS) : 0.0);
			String allowedRelDiff = allowedRelativeDiffTextField.getText();
			center.setAllowedRelativeDiff(!allowedRelDiff.isEmpty() ? Double.parseDouble(allowedRelDiff) : 0.0);
			String numCluster = numClusterTextField.getText();
			center.setNumClusters(!numCluster.isEmpty() ? Integer.parseInt(numCluster) : 0);
			center.setTransactionTypesToGroup(transactionTypeToGroupTextField.getText());

			String testManualMinTPS = testMinTPSTextField.getText();
			center.setTestManualMinTPS(!testManualMinTPS.isEmpty() ? Double.parseDouble(testManualMinTPS) : 0.0);
			String testManualMaxTPS = testMaxTPSTextField.getText();
			center.setTestManualMaxTPS(!testManualMaxTPS.isEmpty() ? Double.parseDouble(testManualMaxTPS) : 0.0);
			String testMixture = testMixtureTextField.getText();
			center.setTestMixture(testMixture);

			// Prediction-specific options
			setPredictionSpecificValues(center);

			SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>()
			{
				@Override
				protected Void doInBackground() throws Exception
				{
					center.initialize();

					return null;
				}

				@Override
				protected void done()
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
			};

			worker.execute();
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

			if (((String) predictionComboBox.getSelectedItem()).compareTo("PhysicalReadPrediction") == 0)
			{
				predictionTestModeComboBox.setSelectedIndex(DBSeerConstants.TEST_MODE_DATASET);
				predictionTestModeComboBox.setEnabled(false);
			}
			else
			{
				predictionTestModeComboBox.setEnabled(true);
			}
		}
		else if (actionEvent.getSource() == trainConfigComboBox)
		{
			String ioConf = ((DBSeerConfiguration)trainConfigComboBox.getSelectedItem()).getIoConfiguration();
			String lockConf = ((DBSeerConfiguration)trainConfigComboBox.getSelectedItem()).getLockConfiguration();

			flushRatePredictionByTPSPanel.setIOConf(ioConf);
			flushRatePredictionByCountsPanel.setIOConf(ioConf);
			maxThroughputPredictionPanel.setIOConf(ioConf);
			maxThroughputPredictionPanel.setLockConf(lockConf);
			lockPredictionPanel.setLockConf(lockConf);
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
					groupingTypeBox.setEnabled(true);

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
