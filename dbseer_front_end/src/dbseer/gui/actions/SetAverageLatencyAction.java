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

import dbseer.gui.dialog.DBSeerFileLoadDialog;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Created by dyoon on 2014. 5. 25..
 */
public class SetAverageLatencyAction extends AbstractAction
{
	private DBSeerFileLoadDialog loadDialog;

	public SetAverageLatencyAction()
	{
		super("Set Average Latency Data");

		loadDialog = new DBSeerFileLoadDialog();
	}

	@Override
	public void actionPerformed(ActionEvent actionEvent)
	{
		loadDialog.createFileDialog("Select Average Latency Data", DBSeerFileLoadDialog.FILE_ONLY);
		loadDialog.showDialog();

		if (loadDialog.getFile() != null)
		{
			//DBSeerGUI.config.setAverageLatencyPath(loadDialog.getFile().getAbsolutePath());
		}
	}
}
