package dbseer.gui;

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

	private MigLayout layout;
	private DBSeerPathChoosePanel pathChoosePanel;
	private DBSeerTaskDescriptionPanel taskDescPanel;
	private DBSeerPlotControlPanel plotControlPanel;

	public DBSeerMainFrame()
	{
		initializeGUI();
	}

	private void initializeGUI()
	{
		this.setTitle("DBSeer GUI Frontend");
		System.setProperty("apple.laf.useScreenMenuBar", "true"); // for mac os
		System.setProperty("com.apple.mrj.application.apple.menu.about.name", "DBSeer"); // for mac os
		layout = new MigLayout();

		// create Menubar.
		menuBar = new JMenuBar();

		// and file menu.
		menu = new JMenu("File");

		// Open directory.
		menuItem = new JMenuItem(new OpenDirectoryAction());
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.ALT_MASK));
		menu.add(menuItem);

		menu.addSeparator();
		// and menu item.
		// set header
		menuItem = new JMenuItem(new SetHeaderAction());
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, ActionEvent.ALT_MASK));
		menu.add(menuItem);

		// set monitoring data.
		menuItem = new JMenuItem(new SetMonitoringDataAction());
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, ActionEvent.ALT_MASK));
		menu.add(menuItem);

		// set transaction count
		menuItem = new JMenuItem(new SetTransactionCountAction());
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, ActionEvent.ALT_MASK));
		menu.add(menuItem);

		// set average latency
		menuItem = new JMenuItem(new SetAverageLatencyAction());
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.ALT_MASK));
		menu.add(menuItem);

		// set percentile latency
		menuItem = new JMenuItem(new SetPercentileLatencyAction());
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.ALT_MASK));
		menu.add(menuItem);

		menuBar.add(menu);
		this.setJMenuBar(menuBar);

		pathChoosePanel = new DBSeerPathChoosePanel();
		taskDescPanel = new DBSeerTaskDescriptionPanel();
		plotControlPanel = new DBSeerPlotControlPanel();
		configPane = new JScrollPane(DBSeerGUI.config.getTable());
		configPane.setBorder(BorderFactory.createTitledBorder("Configurations"));
		DBSeerGUI.status.setHorizontalAlignment(JLabel.LEFT);
		DBSeerGUI.status.setHorizontalTextPosition(JLabel.LEFT);
		DBSeerGUI.status.setPreferredSize(new Dimension(1000,16));
		DBSeerGUI.status.setBorder(BorderFactory.createLoweredBevelBorder());

		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		this.setLayout(layout);
		this.add(pathChoosePanel, "cell 0 0");
		//this.add(taskDescPanel, "cell 0 1");
		this.add(plotControlPanel, "cell 0 1");
		this.add(configPane, "cell 0 2, grow");
		//this.add(plotControlPanel, "cell 1 0");
		this.add(DBSeerGUI.status, "dock south");
	}
}
