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
		super("Save Current Settings");
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
			JOptionPane.showMessageDialog(DBSeerGUI.mainFrame, e.getMessage(), "Error while saving settings.", JOptionPane.ERROR_MESSAGE);
		}

		JOptionPane.showMessageDialog(DBSeerGUI.mainFrame, "Settings successfully saved.");
	}

	public static void action()
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
			JOptionPane.showMessageDialog(DBSeerGUI.mainFrame, e.getMessage(), "Error while saving settings.", JOptionPane.ERROR_MESSAGE);
		}
	}
}
