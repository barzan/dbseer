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

package dbseer.gui.frame;

import dbseer.comp.process.live.LiveMonitorInfo;
import dbseer.gui.DBSeerGUI;
import dbseer.gui.panel.*;
import dbseer.gui.actions.*;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * Created by dyoon on 2014. 5. 18..
 */
public class DBSeerMainFrame extends JFrame
{
	private JMenuBar menuBar;
	private JMenu menu;
	private JMenuItem menuItem;
	private JScrollPane configPane;
	private JTabbedPane mainTabbedPane;

	private MigLayout layout;
	private DBSeerPathChoosePanel pathChoosePanel;
	private DBSeerTaskDescriptionPanel taskDescPanel;
	private DBSeerPlotControlPanel plotControlPanel;
	private DBSeerConfigurationPanel configPanel;
	private DBSeerPredictionConsolePanel predictionConsolePanel;
	private DBSeerPerformancePredictionPanel performancePredictionPanel;
	private JPanel statusPanel;

	public DBSeerMainFrame(String title)
	{
		this.setTitle(title);
		initializeGUI();
	}

	private void initializeGUI()
	{
		System.setProperty("apple.laf.useScreenMenuBar", "true"); // for mac os
		System.setProperty("com.apple.mrj.application.apple.menu.about.name", "DBSeer"); // for mac os
//		layout = new MigLayout("fill, ins 5 5 5 5", "[grow]", "[grow][grow,shrink 100]push[shrink 0]");
		layout = new MigLayout("fill, ins 0", "[grow]", "[grow,shrink 100]push[grow,shrink 0]");

		// create Menubar.
		menuBar = new JMenuBar();

		// and file menu.
		menu = new JMenu("File");

		// Save current setting
		menuItem = new JMenuItem(new SaveSettingsAction());
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		menu.add(menuItem);

		// Add dataset configuration from XML.
		menuItem = new JMenuItem(new AddConfigDatasetFromXMLAction());
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		menu.add(menuItem);

		// separator.
		menu.addSeparator();

		// Process dataset.
		menuItem = new JMenuItem(new ProcessDatasetDirectoryAction());
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		menu.add(menuItem);

		// Process dataset without DBSCAN.
		menuItem = new JMenuItem(new ProcessDatasetDirectoryWithoutDBSCANAction());
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		menu.add(menuItem);

		menu.addSeparator();

		menuItem = new JMenuItem(new EditCausalModelAction());
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		menu.add(menuItem);

		menu.addSeparator();

		menuItem = new JMenuItem(new QuitAction());
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		menu.add(menuItem);

//
//		// and menu item.
//		// set header
//		menuItem = new JMenuItem(new SetHeaderAction());
//		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, ActionEvent.ALT_MASK));
//		menu.add(menuItem);
//
//		// set monitoring data.
//		menuItem = new JMenuItem(new SetMonitoringDataAction());
//		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, ActionEvent.ALT_MASK));
//		menu.add(menuItem);
//
//		// set transaction count
//		menuItem = new JMenuItem(new SetTransactionCountAction());
//		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, ActionEvent.ALT_MASK));
//		menu.add(menuItem);
//
//		// set average latency
//		menuItem = new JMenuItem(new SetAverageLatencyAction());
//		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.ALT_MASK));
//		menu.add(menuItem);
//
//		// set percentile latency
//		menuItem = new JMenuItem(new SetPercentileLatencyAction());
//		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.ALT_MASK));
//		menu.add(menuItem);

		menuBar.add(menu);
		this.setJMenuBar(menuBar);

		// tabbed pane
		mainTabbedPane = new JTabbedPane();

		pathChoosePanel = new DBSeerPathChoosePanel();
		taskDescPanel = new DBSeerTaskDescriptionPanel();
		plotControlPanel = new DBSeerPlotControlPanel();
		configPanel = new DBSeerConfigurationPanel();
		predictionConsolePanel = new DBSeerPredictionConsolePanel();
		performancePredictionPanel = new DBSeerPerformancePredictionPanel();

		statusPanel = new JPanel();
		statusPanel.setLayout(new MigLayout("fill, insets 0 0 0 0", "[grow 25][grow 75]"));

		DBSeerGUI.status.setHorizontalAlignment(JLabel.LEFT);
		DBSeerGUI.status.setHorizontalTextPosition(JLabel.LEFT);
		DBSeerGUI.status.setBorder(BorderFactory.createLoweredBevelBorder());
		DBSeerGUI.status.setPreferredSize(new Dimension(500, 20));

		DBSeerGUI.explainStatus.setHorizontalAlignment(JLabel.LEFT);
		DBSeerGUI.explainStatus.setHorizontalTextPosition(JLabel.LEFT);
		DBSeerGUI.explainStatus.setBorder(BorderFactory.createLoweredBevelBorder());
		DBSeerGUI.explainStatus.setPreferredSize(new Dimension(500, 20));

		DBSeerGUI.middlewareStatus.setHorizontalAlignment(JLabel.LEFT);
		DBSeerGUI.middlewareStatus.setHorizontalTextPosition(JLabel.LEFT);
		DBSeerGUI.middlewareStatus.setBorder(BorderFactory.createLoweredBevelBorder());
		DBSeerGUI.middlewareStatus.setPreferredSize(new Dimension(500, 20));
		DBSeerGUI.middlewareStatus.setText("Middleware: Not Connected");

		statusPanel.add(DBSeerGUI.middlewareStatus, "growx");
		statusPanel.add(DBSeerGUI.status, "growx");

		mainTabbedPane.addTab("Settings", null, configPanel, "Set-up configurations for DBSeer");
		mainTabbedPane.addTab("Live Monitoring", null, DBSeerGUI.liveMonitorPanel, "Live monitoring from the middleware.");
		mainTabbedPane.addTab("DBSherlock (Performance Analysis)", null, plotControlPanel, "Displays plot/graphs from DB statistics");
		mainTabbedPane.addTab("Performance Prediction", null, performancePredictionPanel, "Perform performance/resource predictions");

		// disabled for now.
//		mainTabbedPane.addTab("Performance Prediction (For Advanced Users)", null, predictionConsolePanel, "Predicts various metrics");

		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		this.setLayout(layout);
//		this.add(pathChoosePanel, "grow, wrap");
		//this.add(taskDescPanel, "cell 0 1");
//		this.add(mainTabbedPane, "grow, wrap");
		//this.add(configPane, "cell 0 2, grow");
		//this.add(plotControlPanel, "cell 1 0");

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new MigLayout("fill"));
		mainPanel.add(pathChoosePanel, "dock north, wrap");
		mainPanel.add(mainTabbedPane, "grow");
//		JScrollPane mainScrollPane = new JScrollPane();
//		mainScrollPane.setViewportView(mainPanel);
//		mainScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
//		mainScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

		this.add(mainPanel, "grow, wrap");
		this.add(statusPanel, "dock south, push");
		this.setResizable(true);
		this.setMinimumSize(new Dimension(1200, 800));
	}

	public JTabbedPane getMainTabbedPane()
	{
		return mainTabbedPane;
	}

	public void resetLiveMonitoring()
	{
		final DBSeerMainFrame frame = this;
		DBSeerGUI.liveMonitorInfo = new LiveMonitorInfo();
		DBSeerGUI.liveMonitorPanel = new DBSeerLiveMonitorPanel();
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				mainTabbedPane.removeTabAt(1);
				mainTabbedPane.insertTab("Live Monitoring", null, DBSeerGUI.liveMonitorPanel, "Live monitoring from the middleware.", 1);
				DBSeerGUI.liveMonitorPanel.revalidate();
				DBSeerGUI.liveMonitorPanel.repaint();
				frame.repaint();
			}
		});
	}

}
