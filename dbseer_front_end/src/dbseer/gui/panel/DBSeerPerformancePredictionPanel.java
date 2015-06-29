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

import dbseer.comp.PredictionCenter;
import dbseer.gui.DBSeerConstants;
import dbseer.gui.DBSeerGUI;
import dbseer.gui.frame.DBSeerPredictionFrame;
import dbseer.gui.model.SharedComboBoxModel;
import dbseer.gui.user.DBSeerConfiguration;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by dyoon on 5/8/15.
 */
public class DBSeerPerformancePredictionPanel extends JPanel implements ActionListener
{
	private DBSeerWhatIfAnalysisPanel whatIfAnalysisPanel;
	private DBSeerBottleneckAnalysisPanel bottleneckAnalysisPanel;
	private DBSeerBlameAnalysisPanel blameAnalysisPanel;
	private DBSeerThrottleAnalysisPanel throttlingAnalysisPanel;

	private JComboBox trainConfigComboBox;

	private JPanel tabbedPanePanel;
	private JTabbedPane tabbedPane;

	private JButton performAnalysisButton;
	private JButton viewAnalysisChartButton;
	private JButton cancelAnalysisButton;

	private JTextArea answerTextArea;
	private int lastChartType;
	private PredictionCenter predictionCenter;
	private boolean isAnalysisDone = false;

	private SwingWorker<Void, Void> currentWorker;

	public DBSeerPerformancePredictionPanel()
	{
		this.setLayout(new MigLayout("flowy, ins 0","[][grow]","[][fill,grow]"));
		initialize();
	}

	private void initialize()
	{
		trainConfigComboBox = new JComboBox(new SharedComboBoxModel(DBSeerGUI.configs));
		trainConfigComboBox.setBorder(BorderFactory.createTitledBorder("Choose a config"));
		trainConfigComboBox.addActionListener(this);

		tabbedPanePanel = new JPanel();
		tabbedPanePanel.setLayout(new MigLayout("fill"));
		tabbedPane = new JTabbedPane();

		whatIfAnalysisPanel = new DBSeerWhatIfAnalysisPanel(this);
		bottleneckAnalysisPanel = new DBSeerBottleneckAnalysisPanel();
		blameAnalysisPanel = new DBSeerBlameAnalysisPanel();
		throttlingAnalysisPanel = new DBSeerThrottleAnalysisPanel();

		performAnalysisButton = new JButton("Run Analysis");
		performAnalysisButton.addActionListener(this);
		viewAnalysisChartButton = new JButton("View Analysis Chart");
		viewAnalysisChartButton.addActionListener(this);
		viewAnalysisChartButton.setEnabled(false);
		cancelAnalysisButton = new JButton("Cancel Analysis");
		cancelAnalysisButton.addActionListener(this);
		cancelAnalysisButton.setEnabled(false);

		answerTextArea = new JTextArea();
		answerTextArea.setEditable(false);
		answerTextArea.setLineWrap(true);
		answerTextArea.setBorder(BorderFactory.createTitledBorder("Analysis Result"));

		JScrollPane answerScrollPane = new JScrollPane(answerTextArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		answerScrollPane.setViewportView(answerTextArea);
		answerScrollPane.setAutoscrolls(false);

		tabbedPane.addTab("What-if Analysis", whatIfAnalysisPanel);
		tabbedPane.addTab("Bottleneck Analysis", bottleneckAnalysisPanel);
		tabbedPane.addTab("Blame Analysis", blameAnalysisPanel);
		tabbedPane.addTab("Throttle Analysis", throttlingAnalysisPanel);

		tabbedPanePanel.add(tabbedPane, "grow");
		this.add(trainConfigComboBox, "growx, split 4");
		this.add(performAnalysisButton, "growx");
		this.add(viewAnalysisChartButton, "growx");
		this.add(cancelAnalysisButton, "growx, wrap");
		this.add(tabbedPanePanel, "growx");
		this.add(answerScrollPane, "grow");
	}

	public DBSeerConfiguration getTrainConfig()
	{
		return (DBSeerConfiguration)trainConfigComboBox.getSelectedItem();
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		// perform analysis
		if (e.getSource() == performAnalysisButton)
		{
			if (trainConfigComboBox.getSelectedItem() == null)
			{
				JOptionPane.showMessageDialog(null, "Please select a config first.", "Warning",
						JOptionPane.WARNING_MESSAGE);
				return;
			}
			final DBSeerConfiguration trainConfig = (DBSeerConfiguration) trainConfigComboBox.getSelectedItem();
			if (trainConfig.getDatasetCount() == 0)
			{
				JOptionPane.showMessageDialog(null, "The selected train config does not include a dataset.", "Warning",
						JOptionPane.WARNING_MESSAGE);
				return;
			}
			if (tabbedPane.getSelectedIndex() == DBSeerConstants.ANALYSIS_THROTTLING)
			{
				if (throttlingAnalysisPanel.getTargetLatency() == 0)
				{
					JOptionPane.showMessageDialog(null, "Latency must be greater than zero.", "Warning",
							JOptionPane.WARNING_MESSAGE);
					return;
				}
			}


			isAnalysisDone = false;
			answerTextArea.setText("");
			cancelAnalysisButton.setEnabled(true);

			if (tabbedPane.getSelectedIndex() == DBSeerConstants.ANALYSIS_WHATIF)
			{
				int idx = 0;
				String predictionString = "";
				for (String target : DBSeerWhatIfAnalysisPanel.predictionTargets)
				{
					if (target.equalsIgnoreCase(whatIfAnalysisPanel.getPredictionTarget()))
					{
						predictionString = DBSeerWhatIfAnalysisPanel.actualPredictions[idx];
						break;
					}
					++idx;
				}


				final PredictionCenter center = new PredictionCenter(DBSeerGUI.runner,
						predictionString, DBSeerGUI.userSettings.getDBSeerRootPath(), true);

				center.setPredictionDescription("What-if Analysis");
				final int targetIndex = whatIfAnalysisPanel.getSelectedPredictionTargetIndex();
				DBSeerGUI.status.setText("Running What-if Analysis...");
				final SwingWorker<Void, Void> worker = performAnalysis(center, DBSeerConstants.ANALYSIS_WHATIF, DBSeerConstants.CHART_XYLINE);

				SwingWorker<Void, Void> printWorker = new SwingWorker<Void, Void>()
				{
					@Override
					protected Void doInBackground() throws Exception
					{
						while (!worker.isDone())
						{
							Thread.sleep(250);
						}
						return null;
					}

					@Override
					protected void done()
					{
						if (!worker.isCancelled())
						{
							printWhatIfAnalysisResult(center, trainConfig, targetIndex);
							DBSeerGUI.status.setText("What-if Analysis Completed.");
						}
						cancelAnalysisButton.setEnabled(false);
					}
				};
				printWorker.execute();
			}
			else if (tabbedPane.getSelectedIndex() == DBSeerConstants.ANALYSIS_BOTTLENECK)
			{
				int chartType = DBSeerConstants.CHART_XYLINE;
				final int selectedIndex = bottleneckAnalysisPanel.getSelectedQuestion();
				final PredictionCenter center = new PredictionCenter(DBSeerGUI.runner,
						DBSeerBottleneckAnalysisPanel.actualFunctions[selectedIndex],
						DBSeerGUI.userSettings.getDBSeerRootPath(), true);

				center.setPredictionDescription("Bottleneck Analysis");
				DBSeerGUI.status.setText("Running Bottleneck Analysis...");
				if (selectedIndex == DBSeerBottleneckAnalysisPanel.BOTTLENECK_RESOURCE)
				{
					chartType = DBSeerConstants.CHART_BAR;
				}
				final SwingWorker<Void, Void> worker = performAnalysis(center, DBSeerConstants.ANALYSIS_BOTTLENECK, chartType);
				SwingWorker<Void, Void> printWorker = new SwingWorker<Void, Void>()
				{
					@Override
					protected Void doInBackground() throws Exception
					{
						while (!worker.isDone())
						{
							Thread.sleep(250);
						}
						return null;
					}

					@Override
					protected void done()
					{
						if (!worker.isCancelled())
						{
							printBottleneckAnalysisResult(center, trainConfig, selectedIndex);
							DBSeerGUI.status.setText("Bottleneck Analysis Completed.");
						}
						cancelAnalysisButton.setEnabled(false);
					}
				};
				printWorker.execute();
			}
			else if (tabbedPane.getSelectedIndex() == DBSeerConstants.ANALYSIS_BLAME)
			{
				final int selectedIndex = blameAnalysisPanel.getSelectedIndex();
				final PredictionCenter center = new PredictionCenter(DBSeerGUI.runner,
						blameAnalysisPanel.getAnalysisFunction(), DBSeerGUI.userSettings.getDBSeerRootPath(), true);

				center.setPredictionDescription("Blame Analysis");
				DBSeerGUI.status.setText("Running Blame Analysis...");
				final SwingWorker<Void, Void> worker = performAnalysis(center, DBSeerConstants.ANALYSIS_BLAME, DBSeerConstants.CHART_XYLINE);
				SwingWorker<Void, Void> printWorker = new SwingWorker<Void, Void>()
				{
					@Override
					protected Void doInBackground() throws Exception
					{
						while (!worker.isDone())
						{
							Thread.sleep(250);
						}
						return null;
					}

					@Override
					protected void done()
					{
						if (!worker.isCancelled())
						{
							printBlameAnalysisResult(center, trainConfig, selectedIndex);
							DBSeerGUI.status.setText("Blame Analysis Completed.");
						}
						cancelAnalysisButton.setEnabled(false);
					}
				};
				printWorker.execute();
			}
			else if (tabbedPane.getSelectedIndex() == DBSeerConstants.ANALYSIS_THROTTLING)
			{
				final PredictionCenter center = new PredictionCenter(DBSeerGUI.runner,
						"ThrottlingAnalysis", DBSeerGUI.userSettings.getDBSeerRootPath(), true);
				final int throttleType = throttlingAnalysisPanel.getThrottleType();

				center.setPredictionDescription("Throttle Analysis");
				center.setThrottleLatencyType(throttlingAnalysisPanel.getLatencyType());
				if (throttleType == DBSeerThrottleAnalysisPanel.THROTTLE_INDIVIDUAL)
				{
					center.setThrottleIndividualTransactions(true);
					center.setThrottlePenalty(throttlingAnalysisPanel.getPenaltyMatrix());
				}
				else if (throttleType == DBSeerThrottleAnalysisPanel.THROTTLE_OVERALL)
				{
					center.setThrottleIndividualTransactions(false);
				}
				DBSeerGUI.status.setText("Running Throttle Analysis...");

				final SwingWorker<Void, Void> worker = performAnalysis(center, DBSeerConstants.ANALYSIS_THROTTLING, DBSeerConstants.CHART_XYLINE);
				SwingWorker<Void, Void> printWorker = new SwingWorker<Void, Void>()
				{
					@Override
					protected Void doInBackground() throws Exception
					{
						while (!worker.isDone())
						{
							Thread.sleep(250);
						}
						return null;
					}

					@Override
					protected void done()
					{
						if (!worker.isCancelled())
						{
							printThrottlingAnalysisResult(center, trainConfig, throttleType);
							DBSeerGUI.status.setText("Throttle Analysis Completed.");
						}
						cancelAnalysisButton.setEnabled(false);
					}
				};
				printWorker.execute();
			}
		}
		else if (e.getSource() == viewAnalysisChartButton)
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					if (predictionCenter != null)
					{
						DBSeerGUI.status.setText("");
						DBSeerPredictionFrame predictionFrame = new DBSeerPredictionFrame(predictionCenter, lastChartType);
						predictionFrame.pack();
						predictionFrame.setVisible(true);
					}
				}
			});
		}
		else if (e.getSource() == trainConfigComboBox)
		{
			DBSeerConfiguration trainConfig = (DBSeerConfiguration)trainConfigComboBox.getSelectedItem();
			if (trainConfig != null)
			{
				whatIfAnalysisPanel.updateTransactionType(trainConfig);
				throttlingAnalysisPanel.updateTransactionType(trainConfig);
				throttlingAnalysisPanel.updatePenaltyPanel(trainConfig);
			}
		}
		else if (e.getSource() == cancelAnalysisButton)
		{
			if (currentWorker != null)
			{
				DBSeerGUI.status.setText("Cancelling Analysis Process. This may take a few minutes.");
				final JButton performAnalysis = this.performAnalysisButton;
				final JComboBox trainConfigComboBox = this.trainConfigComboBox;
				final DBSeerConfiguration trainConfig = (DBSeerConfiguration) trainConfigComboBox.getSelectedItem();

				currentWorker.cancel(true);
				trainConfig.setReinitialize();

				SwingUtilities.invokeLater(new Runnable()
				{
					@Override
					public void run()
					{
						trainConfigComboBox.setEnabled(false);
						performAnalysis.setEnabled(false);
						DBSeerGUI.mainFrame.getMainTabbedPane().setEnabled(false);
					}
				});

				SwingWorker<Void, Void> cancelWorker = new SwingWorker<Void, Void>()
				{
					@Override
					protected Void doInBackground() throws Exception
					{
						DBSeerGUI.isProxyRenewing = true;
						DBSeerGUI.resetStatRunner();
						DBSeerGUI.isProxyRenewing = false;
						return null;
					}

					@Override
					protected void done()
					{
						performAnalysis.setEnabled(true);
						trainConfigComboBox.setEnabled(true);
						DBSeerGUI.mainFrame.getMainTabbedPane().setEnabled(true);
						DBSeerGUI.status.setText("Analysis has been cancelled successfully.");
					}
				};
				cancelWorker.execute();
			}
		}
	}

	private void printBlameAnalysisResult(PredictionCenter center, DBSeerConfiguration config, int targetIndex)
	{
		switch (targetIndex)
		{
			case DBSeerBlameAnalysisPanel.TARGET_CPU:
			{
				center.printBlameAnalysisCPUResult(answerTextArea, config);
				break;
			}
			case DBSeerBlameAnalysisPanel.TARGET_IO:
			{
				center.printBlameAnalysisIOResult(answerTextArea, config);
				break;
			}
			case DBSeerBlameAnalysisPanel.TARGET_LOCK:
			{
				center.printBlameAnalysisLockResult(answerTextArea, config);
				break;
			}
			default:
				break;
		}
	}

	private void printBottleneckAnalysisResult(PredictionCenter center, DBSeerConfiguration config, int targetIndex)
	{
		switch (targetIndex)
		{
			case DBSeerBottleneckAnalysisPanel.BOTTLENECK_MAX_THROUGHPUT:
			{
				center.printBottleneckAnalysisMaxThroughputResult(answerTextArea, config);
				break;
			}
			case DBSeerBottleneckAnalysisPanel.BOTTLENECK_RESOURCE:
			{
				center.printBottleneckAnalysisResourceResult(answerTextArea, config);
				break;
			}
			default:
				break;
		}
	}

	private void printWhatIfAnalysisResult(PredictionCenter center, DBSeerConfiguration config, int targetIndex)
	{
		switch(targetIndex)
		{
			case DBSeerWhatIfAnalysisPanel.TARGET_LATENCY:
			{
				center.printWhatIfAnalysisLatencyResult(answerTextArea, config);
				break;
			}
			case DBSeerWhatIfAnalysisPanel.TARGET_IO:
			{
				center.printWhatIfAnalysisIOResult(answerTextArea, config);
				break;
			}
			case DBSeerWhatIfAnalysisPanel.TARGET_CPU:
			{
				center.printWhatIfAnalysisCPUResult(answerTextArea, config);
				break;
			}
			case DBSeerWhatIfAnalysisPanel.TARGET_FLUSH_RATE:
			{
				center.printWhatIfAnalysisFlushRateResult(answerTextArea, config);
				break;
			}
			default:
				break;
		}
	}

	private void printThrottlingAnalysisResult(PredictionCenter center, DBSeerConfiguration config, int throttleType)
	{
		if (throttleType == DBSeerThrottleAnalysisPanel.THROTTLE_OVERALL)
		{
			center.printThrottlingAnalysisOverallResult(answerTextArea, config);
		}
		else if (throttleType == DBSeerThrottleAnalysisPanel.THROTTLE_INDIVIDUAL)
		{
			center.printThrottlingAnalysisIndividualResult(answerTextArea, config);
		}
	}

	private SwingWorker<Void, Void> performAnalysis(PredictionCenter center, final int analyisType, int chartType)
	{
		trainConfigComboBox.setEnabled(false);
		performAnalysisButton.setEnabled(false);
		viewAnalysisChartButton.setEnabled(false);

		final int cType = chartType;
		final PredictionCenter pc = center;
		final DBSeerConfiguration trainConfig = (DBSeerConfiguration) trainConfigComboBox.getSelectedItem();

		SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>()
		{
			boolean isRun = false;

			@Override
			protected Void doInBackground() throws Exception
			{
				trainConfig.initialize();
				pc.setTestMode(DBSeerConstants.TEST_MODE_MIXTURE_TPS);
				pc.setTrainConfig((DBSeerConfiguration) trainConfigComboBox.getSelectedItem());

				double workloadRatio = 1.0;
				if (tabbedPane.getSelectedIndex() == DBSeerConstants.ANALYSIS_WHATIF)
				{
					workloadRatio = whatIfAnalysisPanel.getWorkloadRatio() / 100.0;
				}
				else if (tabbedPane.getSelectedIndex() == DBSeerConstants.ANALYSIS_THROTTLING)
				{
					pc.setThrottleTargetLatency(throttlingAnalysisPanel.getTargetLatency());
					pc.setThrottleTargetTransactionIndex(throttlingAnalysisPanel.getTransactionTypeIndex());
				}

				pc.setTestWorkloadRatio(workloadRatio);
				pc.setTestManualMinTPS(trainConfig.getMinTPS() * workloadRatio);
				pc.setTestManualMaxTPS(trainConfig.getMaxTPS() * workloadRatio);
				pc.setTestMixture(trainConfig.getTransactionMixString());

				if (tabbedPane.getSelectedIndex() == DBSeerConstants.ANALYSIS_WHATIF &&
						whatIfAnalysisPanel.getMixtureType() == DBSeerWhatIfAnalysisPanel.MIXTURE_DIFFERENT)
				{
					int targetTransaction = whatIfAnalysisPanel.getTransactionType();
					int ratioOption = whatIfAnalysisPanel.getMixtureRatio();

					String mixtureString = "[";
					double[] originalMix = trainConfig.getTransactionMix();
					int index = 0;
					for (double m : originalMix)
					{
						// Only include target transaction
						if (ratioOption == whatIfAnalysisPanel.RATIO_ONLY)
						{
							if (index != targetTransaction)
							{
								mixtureString += "0";
							}
							else
							{
								mixtureString += m;
							}
						}
						else
						{
							if (index == targetTransaction)
							{
								mixtureString += (m * DBSeerWhatIfAnalysisPanel.ratios[ratioOption]);
							}
							else
							{
								mixtureString += m;
							}
						}
						++index;
						mixtureString += " ";
					}
					mixtureString += "]";
					pc.setTestMixture(mixtureString);
				}
				pc.setIoConfiguration(trainConfig.getIoConfiguration());
				pc.setLockConfiguration(trainConfig.getLockConfiguration());

				if (pc.initialize())
				{
					isRun = pc.run();
				}
				return null;
			}

			@Override
			protected void done()
			{
				if (isRun)
				{
					predictionCenter = pc;
					lastChartType = cType;
					viewAnalysisChartButton.setEnabled(true);
				}
				isAnalysisDone = true;
				performAnalysisButton.setEnabled(true);
				trainConfigComboBox.setEnabled(true);
			}
		};

		currentWorker = worker;
		worker.execute();
		return worker;
	}
}
