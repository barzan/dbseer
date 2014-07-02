package dbseer.gui.panel;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;

/**
 * Created by dyoon on 2014. 6. 2..
 */
public class DBSeerConfigurationPanel extends JPanel
{
	private DBSeerMiddlewarePanel loginPanel;
	private DBSeerConfigListPanel configListPanel;
	private DBSeerDatasetListPanel profileListPanel;

	public DBSeerConfigurationPanel()
	{
		initializeGUI();
	}

	private void initializeGUI()
	{
		this.setLayout(new MigLayout("fill"));

		loginPanel = new DBSeerMiddlewarePanel();
		loginPanel.setBorder(BorderFactory.createTitledBorder("Middleware"));
		configListPanel = new DBSeerConfigListPanel();
		configListPanel.setBorder(BorderFactory.createTitledBorder("Available Configs"));
		profileListPanel = new DBSeerDatasetListPanel();
		profileListPanel.setBorder(BorderFactory.createTitledBorder("Available Datasets"));

		this.add(loginPanel, "dock west");
		this.add(configListPanel, "grow");
		this.add(profileListPanel, "grow");
	}
}
