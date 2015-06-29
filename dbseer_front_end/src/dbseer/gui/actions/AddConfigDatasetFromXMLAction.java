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
import dbseer.gui.dialog.DBSeerFileLoadDialog;
import dbseer.gui.user.DBSeerUserSettings;
import dbseer.gui.xml.XStreamHelper;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;

/**
 * Created by dyoon on 2014. 6. 13..
 */
public class AddConfigDatasetFromXMLAction extends AbstractAction
{
	private DBSeerFileLoadDialog dialog;
	public AddConfigDatasetFromXMLAction()
	{
		super("Add Config/Datasets From XML");
		dialog = new DBSeerFileLoadDialog();
	}

	@Override
	public void actionPerformed(ActionEvent actionEvent)
	{
		dialog.createFileDialog("Select XML settings file to add", DBSeerFileLoadDialog.FILE_ONLY);
		dialog.showDialog();

		if (dialog.getFile() != null)
		{
			File xmlToAdd = dialog.getFile();
			XStreamHelper xmlHelper = new XStreamHelper();
			DBSeerUserSettings settings = (DBSeerUserSettings)xmlHelper.fromXML(xmlToAdd.getAbsolutePath());

			DefaultListModel datasetToAdd = settings.getDatasets();
			DefaultListModel configToAdd = settings.getConfigs();

			for (int i = 0; i < datasetToAdd.getSize(); ++i)
			{
				DBSeerGUI.datasets.addElement(datasetToAdd.getElementAt(i));
			}

			for (int i = 0; i < configToAdd.getSize(); ++i)
			{
				DBSeerGUI.configs.addElement(configToAdd.getElementAt(i));
			}
		}
	}
}
