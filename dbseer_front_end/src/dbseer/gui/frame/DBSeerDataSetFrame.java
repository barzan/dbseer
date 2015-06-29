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

package dbseer.gui.frame;

import dbseer.gui.panel.DBSeerDatasetListPanel;
import dbseer.gui.user.DBSeerDataSet;
import dbseer.gui.actions.AddDataSetAction;
import dbseer.gui.actions.OpenDirectoryAction;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by dyoon on 2014. 6. 3..
 */
public class DBSeerDataSetFrame extends JFrame implements ActionListener
{
	boolean isEditMode;
	private DBSeerDataSet profile;
	private DBSeerDataSet profileBackup;
	private JScrollPane scrollPane;
	private JButton openDirectoryButton;
	private JButton addProfileButton;
	private JButton editProfileButton;
	private JButton cancelButton;
	private JList list;

	private DBSeerDatasetListPanel panel;

	public DBSeerDataSetFrame(String title, DBSeerDataSet profile, JList list)
	{
		this.setTitle(title);
		this.profile = profile;
		this.list = list;
		isEditMode = false;
		initializeGUI();
	}

	public DBSeerDataSetFrame(String title, DBSeerDataSet profile, JList list, boolean isEditMode, DBSeerDatasetListPanel panel)
	{
		this.setTitle(title);
		this.profile = profile;
		this.list = list;
		this.isEditMode = isEditMode;
		this.panel = panel;
		initializeGUI();
	}

	private void initializeGUI()
	{
		this.profile.backup();
		this.profile.updateTable();
		this.setLayout(new MigLayout("fill", "[fill,grow]"));
		scrollPane = new JScrollPane(profile.getTable());
		profile.getTable().setFillsViewportHeight(true);
		profile.getTable().setFont(new Font("Verdana", Font.PLAIN, 14));
		scrollPane.setPreferredSize(new Dimension(1000,400));

		openDirectoryButton = new JButton(new OpenDirectoryAction(profile));
		addProfileButton = new JButton(new AddDataSetAction(profile, this, list, panel));
		editProfileButton = new JButton("Save");
		editProfileButton.addActionListener(this);
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(this);

		this.add(scrollPane, "wrap");
		this.add(openDirectoryButton, "split 3");
		if (isEditMode) this.add(editProfileButton);
		else this.add(addProfileButton);
		this.add(cancelButton);
	}

	@Override
	public void actionPerformed(ActionEvent actionEvent)
	{
		if (actionEvent.getSource() == cancelButton)
		{
			this.profile.restore();
			this.profile.updateTable();
			this.dispose();
		}
		else if (actionEvent.getSource() == editProfileButton)
		{
			final JFrame frame = this;
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					if (profile.validateTable())
					{
						profile.setFromTable();
						frame.dispose();
					}
				}
			});

		}

	}
}
