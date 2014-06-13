package dbseer.gui.actions;

import dbseer.gui.DBSeerGUI;
import dbseer.gui.xml.XStreamHelper;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.FileNotFoundException;

/**
 * Created by dyoon on 2014. 6. 12..
 */
public class SaveSettingsAction extends AbstractAction
{
	public SaveSettingsAction()
	{
		super("Save Setting");
	}

	@Override
	public void actionPerformed(ActionEvent actionEvent)
	{
		DBSeerGUI.userSettings.setConfigs(DBSeerGUI.configs);
		DBSeerGUI.userSettings.setDatasets(DBSeerGUI.datasets);

		XStreamHelper xmlHelper = new XStreamHelper();
		try
		{
			xmlHelper.toXML(DBSeerGUI.userSettings, DBSeerGUI.settingsPath);
		}
		catch (FileNotFoundException e)
		{
			JOptionPane.showMessageDialog(null, e.toString(), "Error while saving settings.", JOptionPane.ERROR_MESSAGE);
		}

		JOptionPane.showMessageDialog(null, "Setting successfully saved.");
	}
}
