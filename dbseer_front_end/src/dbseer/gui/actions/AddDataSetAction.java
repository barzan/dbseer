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

import dbseer.gui.panel.DBSeerDatasetListPanel;
import dbseer.gui.user.DBSeerDataSet;
import dbseer.gui.DBSeerGUI;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Created by dyoon on 2014. 6. 3..
 */
public class AddDataSetAction extends AbstractAction
{
	private DBSeerDataSet profile;
	private JFrame frame;
	private JList list; // JList to update
	private DBSeerDatasetListPanel panel;

	public AddDataSetAction(DBSeerDataSet profile, JFrame frame, JList list, DBSeerDatasetListPanel panel)
	{
		super("Add Dataset");
		this.profile = profile;
		this.frame = frame;
		this.list = list;
		this.panel = panel;
	}

	@Override
	public void actionPerformed(ActionEvent actionEvent)
	{
//		SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>()
//		{
//			@Override
//			protected Void doInBackground() throws Exception
//			{
//				profile.setFromTable();
//				DBSeerGUI.datasets.addElement(profile);
//				list.setListData(DBSeerGUI.datasets.toArray());
//				list.invalidate();
//				return null;
//			}
//
//			@Override
//			protected void done()
//			{
//				frame.dispose();
//			}
//		};
//
//		worker.execute();

		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				if (profile.validateTable())
				{
					profile.setFromTable();
					DBSeerGUI.datasets.addElement(profile);
					frame.dispose();
				}
			}
		});

		if (DBSeerGUI.datasets.size() != 0)
		{
			panel.getEditButton().setEnabled(true);
		}
	}
}
