package dbseer.gui.frame;

import dbseer.gui.DBSeerGUI;
import dbseer.gui.panel.*;
import dbseer.gui.actions.*;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
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
	private DBSeerPredictionControlPanel predictionPanel;
	private JPanel statusPanel;

	public DBSeerMainFrame()
	{
		initializeGUI();
	}

	private void initializeGUI()
	{
		this.setTitle("DBSeer GUI Frontend");
		System.setProperty("apple.laf.useScreenMenuBar", "true"); // for mac os
		System.setProperty("com.apple.mrj.application.apple.menu.about.name", "DBSeer"); // for mac os
		layout = new MigLayout("ins 5 5 5 5", "[grow,fill]", "[][grow,fill]push[fill]");

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
		predictionPanel = new DBSeerPredictionControlPanel();

		statusPanel = new JPanel();
		statusPanel.setLayout(new MigLayout("fill, insets 0 0 0 0", "[grow 25][grow 75]"));

		DBSeerGUI.status.setHorizontalAlignment(JLabel.LEFT);
		DBSeerGUI.status.setHorizontalTextPosition(JLabel.LEFT);
		DBSeerGUI.status.setBorder(BorderFactory.createLoweredBevelBorder());
		DBSeerGUI.status.setPreferredSize(new Dimension(500, 20));

		DBSeerGUI.middlewareStatus.setHorizontalAlignment(JLabel.LEFT);
		DBSeerGUI.middlewareStatus.setHorizontalTextPosition(JLabel.LEFT);
		DBSeerGUI.middlewareStatus.setBorder(BorderFactory.createLoweredBevelBorder());
		DBSeerGUI.middlewareStatus.setPreferredSize(new Dimension(500, 20));
		DBSeerGUI.middlewareStatus.setText("Middleware: Not Connected");

		statusPanel.add(DBSeerGUI.middlewareStatus, "growx");
		statusPanel.add(DBSeerGUI.status, "growx");

		mainTabbedPane.addTab("Settings", null, configPanel, "Set-up configurations for DBSeer");
		mainTabbedPane.addTab("Plot/Graph", null, plotControlPanel, "Displays plot/graphs from DB statistics");
		mainTabbedPane.addTab("Prediction", null, predictionPanel, "Predicts various metrics");

		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		this.setLayout(layout);
		this.add(pathChoosePanel, "cell 0 0");
		//this.add(taskDescPanel, "cell 0 1");
		this.add(mainTabbedPane, "cell 0 1, grow");
		//this.add(configPane, "cell 0 2, grow");
		//this.add(plotControlPanel, "cell 1 0");

		this.add(statusPanel, "dock south, grow");

		// disable frame resizing
		this.setResizable(false);

	}

}
