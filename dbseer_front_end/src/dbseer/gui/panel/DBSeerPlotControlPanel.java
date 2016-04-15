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

import dbseer.gui.frame.DBSeerPlotCustomFrame;
import dbseer.gui.frame.DBSeerPlotPresetFrame;
import dbseer.gui.model.SharedComboBoxModel;
import dbseer.gui.user.DBSeerDataSet;
import dbseer.gui.DBSeerGUI;

import dbseer.stat.StatisticalPackageRunner;
import matlabcontrol.MatlabProxy;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * Created by dyoon on 2014. 5. 25..
 */
public class DBSeerPlotControlPanel extends JPanel implements ActionListener
{
	public static Set<String> chartsToDraw = new HashSet<String>();
	public static Map<String, String> axisMap = new HashMap<String, String>();

	private JPanel buttonPanel;
	private JButton plotButton;
	private JComboBox profileComboBox;
	private JTabbedPane plotTabbedPane;
	private DBSeerPlotPresetPanel plotPresetPanel;
	private DBSeerPlotCustomPanel plotCustomPanel;

	public DBSeerPlotControlPanel()
	{
		initializeAxisMap();
		initializeGUI();
	}

	private void initializeAxisMap()
	{
		axisMap.put("Time", "time");
		axisMap.put("DBMS: Average Latency", "averageTransLatency");
		axisMap.put("DBMS: Total Transaction Count", "clientTotalSubmittedTrans");
		axisMap.put("OS: Average CPU (usr)", "AvgCpuUser");
		axisMap.put("OS: Average CPU (sys)", "AvgCpuSys");
		axisMap.put("OS: Average CPU (idle)", "AvgCpuIdle");
		axisMap.put("OS: Average CPU (wait)", "AvgCpuWai");
		axisMap.put("OS: Average CPU (hiq)", "AvgCpuHiq");
		axisMap.put("OS: Average CPU (siq)", "AvgCpuSiq");
		axisMap.put("OS: Asynchronous IO", "osAsynchronousIO");
		axisMap.put("OS: # of Context Switches", "osNumberOfContextSwitches");
		axisMap.put("OS: # of Disk Sector Reads", "osNumberOfSectorReads");
		axisMap.put("OS: # of Disk Sector Writes", "osNumberOfSectorWrites");
		axisMap.put("OS: # of Reads Issued", "osNumberOfReadsIssued");
		axisMap.put("OS: # of Writes Completed", "osNumberOfWritesCompleted");
		axisMap.put("OS: # of Swap In Since Last Boot", "osNumberOfSwapInSinceLastBoot");
		axisMap.put("OS: # of Swap Out Since Last Boot", "osNumberOfSwapOutSinceLastBoot");
		axisMap.put("OS: # of Processes Created", "osNumberOfProcessesCreated");
		axisMap.put("OS: # of Processes Currently Running", "osNumberOfProcessesCurrentlyRunning");
		axisMap.put("OS: Disk Utilization", "osDiskUtilization");
		axisMap.put("OS: Free Swap", "osFreeSwapSpace");
		axisMap.put("OS: Used Swap", "osUsedSwapSpace");
		axisMap.put("OS: # of Allocated Pages", "osNumberOfAllocatedPage");
		axisMap.put("OS: # of Free Pages", "osNumberOfFreePages");
		axisMap.put("OS: # of Major Page Faults", "osNumberOfMajorPageFaults");
		axisMap.put("OS: # of Minor Page Faults", "osNumberOfMinorPageFaults");
		axisMap.put("OS: Network Send (KB)", "osNetworkSendKB");
		axisMap.put("OS: Network Recv (KB)", "osNetworkRecvKB");
		axisMap.put("DBMS: Changed Rows", "dbmsChangedRows");
		axisMap.put("DBMS: Cumulated Changed Rows", "dbmsCumChangedRows");
		axisMap.put("DBMS: Cumulated Flushed Pages", "dbmsCumFlushedPages");
		axisMap.put("DBMS: Flushed Pages", "dbmsFlushedPages");
		axisMap.put("DBMS: Current Dirty Pages", "dbmsCurrentDirtyPages");
		axisMap.put("DBMS: Dirty Pages", "dbmsDirtyPages");
		axisMap.put("DBMS: Data Pages", "dbmsDataPages");
		axisMap.put("DBMS: Free Pages", "dbmsFreePages");
		axisMap.put("DBMS: Total Pages", "dbmsTotalPages");
		axisMap.put("DBMS: Threads Running", "dbmsThreadsRunning");
		axisMap.put("DBMS: Total Writes (MB)", "dbmsTotalWritesMB");
		axisMap.put("DBMS: Log Writes (MB)", "dbmsLogWritesMB");
		axisMap.put("DBMS: # of Physical Log Writes", "dbmsNumberOfPhysicalLogWrites");
		axisMap.put("DBMS: # of Data Reads", "dbmsNumberOfDataReads");
		axisMap.put("DBMS: # of Data Writes", "dbmsNumberOfDataWrites");
		axisMap.put("DBMS: # of Log Write Requests", "dbmsNumberOfLogWriteRequests");
		axisMap.put("DBMS: # of Fsync Log Writes", "dbmsNumberOfFysncLogWrites");
		axisMap.put("DBMS: # of Pending Log Writes", "dbmsNumberOfPendingLogWrites");
		axisMap.put("DBMS: # of Pending Log Fsyncs", "dbmsNumberOfPendingLogFsyncs");
		axisMap.put("DBMS: # of Next Row Read Requests", "dbmsNumberOfNextRowReadRequests");
		axisMap.put("DBMS: # of Row Insert Requests", "dbmsNumberOfRowInsertRequests");
		axisMap.put("DBMS: # of First Entry Read Requests", "dbmsNumberOfFirstEntryReadRequests");
		axisMap.put("DBMS: # of Key Based Read Requests" ,"dbmsNumberOfKeyBasedReadRequests");
		axisMap.put("DBMS: # of Next Key Based Read Requests", "dbmsNumberOfNextKeyBasedReadRequests");
		axisMap.put("DBMS: # of Previous Key Based Read Requests", "dbmsNumberOfPrevKeyBasedReadRequests");
		axisMap.put("DBMS: # of Row Read Requests", "dbmsNumberOfRowReadRequests");
		axisMap.put("DBMS: Page Writes (MB)", "dbmsPageWritesMB");
		axisMap.put("DBMS: Double Page Writes (MB)", "dbmsDoublePageWritesMB");
		axisMap.put("DBMS: Double Writes Operation", "dbmsDoubleWritesOperations");
		axisMap.put("DBMS: # of Pending Writes", "dbmsNumberOfPendingWrites");
		axisMap.put("DBMS: # of Pending Reads", "dbmsNumberOfPendingReads");
		axisMap.put("DBMS: Buffer Pool Writes", "dbmsBufferPoolWrites");
		axisMap.put("DBMS: Random Read Aheads", "dbmsRandomReadAheads");
		axisMap.put("DBMS: Sequential Read Aheads", "dbmsSequentialReadAheads");
		axisMap.put("DBMS: # of Logical Read Requests", "dbmsNumberOfLogicalReadRequests");
		axisMap.put("DBMS: # of Logical Reads From Disk", "dbmsNumberOfLogicalReadsFromDisk");
		axisMap.put("DBMS: # of Waits For Flush", "dbmsNumberOfWaitsForFlush");
		axisMap.put("DBMS: Committed Commands", "dbmsCommittedCommands");
		axisMap.put("DBMS: Rolled-back Commands", "dbmsRolledbackCommands");
		axisMap.put("DBMS: Rollback Handler", "dbmsRollbackHandler");
		axisMap.put("DBMS: Current Lock Waits", "dbmsCurrentLockWaits");
		axisMap.put("DBMS: Lock Waits", "dbmsLockWaits");
		axisMap.put("DBMS: Lock Wait Time", "dbmsLockWaitTime");
		axisMap.put("DBMS: Read Requests", "dbmsReadRequests");
		axisMap.put("DBMS: Reads", "dbmsReads");
		axisMap.put("DBMS: Physical Reads (MB)", "dbmsPhysicalReadsMB");
	}

	private void initializeGUI()
	{
		this.setLayout(new MigLayout("fill, ins 0"));

		plotTabbedPane = new JTabbedPane(JTabbedPane.TOP);
		plotPresetPanel = new DBSeerPlotPresetPanel();
		plotCustomPanel = new DBSeerPlotCustomPanel();

		plotTabbedPane.addTab("Default", null, plotPresetPanel, "Plot/Graph Presets");
		plotTabbedPane.addTab("Custom", null, plotCustomPanel, "Custom Presets");

		buttonPanel = new JPanel();
		buttonPanel.setLayout(new MigLayout("ins 0"));
//		buttonPanel.setPreferredSize(new Dimension(300, 200));

		plotButton = new JButton();
		plotButton.setText("Load & Plot");
		plotButton.addActionListener(this);

		profileComboBox = new JComboBox(new SharedComboBoxModel(DBSeerGUI.datasets));
		profileComboBox.setBorder(BorderFactory.createTitledBorder("Choose a dataset"));
		profileComboBox.setPreferredSize(new Dimension(250,100));

		buttonPanel.add(profileComboBox, "dock north");
		buttonPanel.add(plotButton);
		this.add(buttonPanel, "dock west");
		this.add(plotTabbedPane, "grow");
	}

	@Override
	public void actionPerformed(ActionEvent actionEvent)
	{
		final DBSeerDataSet profile = (DBSeerDataSet)profileComboBox.getSelectedItem();
		if (actionEvent.getSource() == plotButton)
		{
			final String[] charts = DBSeerPlotControlPanel.chartsToDraw.toArray(
					new String[DBSeerPlotControlPanel.chartsToDraw.size()]);

			if (profile == null)
			{
				JOptionPane.showMessageDialog(null, "Please select a dataset.", "Warning", JOptionPane.WARNING_MESSAGE);
				return;
			}

			if (profile.getLive() && !DBSeerGUI.isLiveDataReady)
			{
				JOptionPane.showMessageDialog(null, "DBSeer must be monitoring in order to draw plots from its live dataset.",
						"Warning", JOptionPane.WARNING_MESSAGE);
				return;
			}

			if (plotTabbedPane.getSelectedIndex() == 0 && charts.length == 0)
			{
				JOptionPane.showMessageDialog(null, "Please select one or more default plots to draw.", "Warning", JOptionPane.WARNING_MESSAGE);
				return;
			}

			DBSeerGUI.currentDataset = profile;

			plotButton.setEnabled(false);
			DBSeerGUI.status.setText("Plotting...");

			SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>()
			{
				protected boolean isLoadSuccess = true;
				@Override
				protected Void doInBackground() throws Exception
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

						runner.eval("plotter = Plotter;");
						if (!profile.loadDataset(profile.isCurrent()))
						{
							isLoadSuccess = false;
							JOptionPane.showMessageDialog(DBSeerGUI.mainFrame, "Dataset has some data missing or is not ready yet.", "Message", JOptionPane.INFORMATION_MESSAGE);
						}
						if (isLoadSuccess)
						{
							runner.eval("[mvGrouped mvUngrouped] = load_mv2(" +
									profile.getUniqueVariableName() + ".header," +
									profile.getUniqueVariableName() + ".monitor," +
									profile.getUniqueVariableName() + ".averageLatency," +
									profile.getUniqueVariableName() + ".percentileLatency," +
									profile.getUniqueVariableName() + ".transactionCount," +
									profile.getUniqueVariableName() + ".diffedMonitor," +
									profile.getUniqueVariableName() + ".statementStat," +
									profile.getUniqueVariableName() + ".tranTypes);");
							runner.eval("plotter.mv = mvUngrouped;");
						}
					}
					catch (Exception e)
					{
						JOptionPane.showMessageDialog(DBSeerGUI.mainFrame, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
						isLoadSuccess = false;
						e.printStackTrace();
					}
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
							if (isLoadSuccess)
							{
								// if preset.
								if (plotTabbedPane.getSelectedIndex() == 0)
								{
									DBSeerPlotPresetFrame plotFrame = new DBSeerPlotPresetFrame(charts, profile);
									if (plotFrame.isInitSuccess())
									{
										plotFrame.pack();
										plotFrame.setVisible(true);
										plotFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
									}
								}
								// if custom.
								if (plotTabbedPane.getSelectedIndex() == 1)
								{
									DBSeerPlotCustomFrame plotFrame = new DBSeerPlotCustomFrame(plotCustomPanel.getXAxis(),
											plotCustomPanel.getYAxis(), profile);
									plotFrame.setPreferredSize(new Dimension(1024, 768));
									plotFrame.pack();
									plotFrame.setVisible(true);
									plotFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
								}
							}
							plotButton.setEnabled(true);
							plotButton.requestFocus();
							DBSeerGUI.status.setText("");
						}
					});
				}
			};

			worker.execute();
		}
	}
}
