package dbseer.gui.panel;

import dbseer.gui.DBSeerFileLoadDialog;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.text.NumberFormat;

/**
 * Created by dyoon on 2014. 6. 2..
 */
public class DBSeerConfigurationPanel extends JPanel
{
	private DBSeerLoginPanel loginPanel;
	private DBSeerConfigListPanel configListPanel;
	private DBSeerProfileListPanel profileListPanel;

	public DBSeerConfigurationPanel()
	{
		initializeGUI();
	}

	private void initializeGUI()
	{
		this.setLayout(new MigLayout("fill"));

		loginPanel = new DBSeerLoginPanel();
		loginPanel.setBorder(BorderFactory.createTitledBorder("Middleware Login"));
		configListPanel = new DBSeerConfigListPanel();
		configListPanel.setBorder(BorderFactory.createTitledBorder("Available Configs"));
		profileListPanel = new DBSeerProfileListPanel();
		profileListPanel.setBorder(BorderFactory.createTitledBorder("Available Data Profiles"));

		this.add(loginPanel, "dock west");
		this.add(configListPanel, "grow");
		this.add(profileListPanel, "grow");
	}
}
