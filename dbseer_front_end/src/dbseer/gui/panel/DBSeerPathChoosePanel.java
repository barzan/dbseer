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

import dbseer.gui.DBSeerExceptionHandler;
import dbseer.gui.dialog.DBSeerFileLoadDialog;
import dbseer.gui.DBSeerGUI;
import dbseer.gui.xml.XStreamHelper;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;

/**
 * Created by dyoon on 2014. 5. 18..
 */
public class DBSeerPathChoosePanel extends JPanel implements ActionListener
{
	private JButton openButton;
	private DBSeerFileLoadDialog fileLoadDialog;
	private JLabel pathToDBSeerLabel;

	public DBSeerPathChoosePanel()
	{
		super(new MigLayout());

		fileLoadDialog = new DBSeerFileLoadDialog();

		openButton = new JButton("Change Root Path");
		pathToDBSeerLabel = new JLabel();
		pathToDBSeerLabel.setText("Current DBSeer Root Path: " + DBSeerGUI.userSettings.getDBSeerRootPath());
		pathToDBSeerLabel.setPreferredSize(new Dimension(500, 10));
		openButton.addActionListener(this);

		add(openButton);
		add(pathToDBSeerLabel, "wrap");

	}

	@Override
	public void actionPerformed(ActionEvent actionEvent)
	{
		if (actionEvent.getSource() == openButton)
		{
			fileLoadDialog.createFileDialog("Select DBSeer Root Directory", DBSeerFileLoadDialog.DIRECTORY_ONLY);
			fileLoadDialog.showDialog();
			if (fileLoadDialog.getFile() != null)
			{
				String rootPath = fileLoadDialog.getFile().getAbsolutePath();
				pathToDBSeerLabel.setText("Current DBSeer Root Directory: " + rootPath);
				DBSeerGUI.userSettings.setDBSeerRootPath(rootPath);
				DBSeerGUI.liveDataset.updateLiveDataSet();

				XStreamHelper xmlHelper = new XStreamHelper();
				try
				{
					xmlHelper.toXML(DBSeerGUI.userSettings, DBSeerGUI.settingsPath);
				}
				catch (FileNotFoundException e)
				{
					DBSeerExceptionHandler.handleException(e, "Failed to save the root path configuration.");
				}
			}
		}
	}
}
