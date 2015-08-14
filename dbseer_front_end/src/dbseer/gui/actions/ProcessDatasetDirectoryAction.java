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

import dbseer.comp.DataCenter;
import dbseer.gui.DBSeerGUI;
import dbseer.gui.dialog.ProgressDialog;
import dbseer.gui.dialog.DBSeerFileLoadDialog;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by dyoon on 2014. 7. 22..
 */
public class ProcessDatasetDirectoryAction extends AbstractAction
{
	private DBSeerFileLoadDialog loadDialog;

	public ProcessDatasetDirectoryAction()
	{
		super("Process Dataset");

		loadDialog = new DBSeerFileLoadDialog();
	}

	@Override
	public void actionPerformed(ActionEvent actionEvent)
	{
		loadDialog.createFileDialog("Open Directory for Processing", DBSeerFileLoadDialog.DIRECTORY_ONLY);
		loadDialog.showDialog();

		if (loadDialog.getFile() != null)
		{
			final String directory = loadDialog.getFile().getAbsolutePath();

			DBSeerGUI.status.setText("Processing Dataset...");
//			final ProgressDialog dialog = new ProgressDialog(null, "Processing dataset");
//			dialog.setLocationRelativeTo(null);
			SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>()
			{
				@Override
				protected Void doInBackground() throws Exception
				{
					DataCenter dc = new DataCenter(directory, true);
					if (!dc.parseLogs())
					{
						System.out.println("Parsing log failure");
					}
					//dc.performDBSCAN();
					dc.processDataset();
					return null;
				}

				@Override
				protected void done()
				{
					DBSeerGUI.status.setText("");
					JOptionPane.showMessageDialog(null, "Dataset has been processed.");
				}
			};

			worker.execute();
		}
	}
}
