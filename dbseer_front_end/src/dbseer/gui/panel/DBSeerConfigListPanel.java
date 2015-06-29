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

import dbseer.gui.user.DBSeerConfiguration;
import dbseer.gui.DBSeerGUI;
import dbseer.gui.frame.DBSeerConfigFrame;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Created by dyoon on 2014. 6. 3..
 */
public class DBSeerConfigListPanel extends JPanel implements ActionListener, MouseListener
{
	private JList list;

	private JButton addButton;
	private JButton editButton;
	private JButton removeButton;
	private JScrollPane scrollPane;

	public DBSeerConfigListPanel()
	{
		initializeGUI();
	}

	private void initializeGUI()
	{
		this.setLayout(new MigLayout("ins 0", "[align center, grow]", "[fill,grow] [align center]"));

		list = new JList(DBSeerGUI.configs);
		list.setVisibleRowCount(8);
		scrollPane = new JScrollPane();
		scrollPane.setViewportView(list);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setPreferredSize(new Dimension(100, 100));

		addButton = new JButton("Add");
		editButton = new JButton("Edit");
		removeButton = new JButton("Remove");

		addButton.addActionListener(this);
		editButton.addActionListener(this);
		removeButton.addActionListener(this);
		list.addMouseListener(this);

		if (DBSeerGUI.configs.size() == 0)
		{
			editButton.setEnabled(false);
		}

		this.add(scrollPane, "wrap, growx");
		this.add(addButton, "split 3");
		this.add(editButton);
		this.add(removeButton);
	}

	@Override
	public void actionPerformed(ActionEvent actionEvent)
	{
		final DBSeerConfigListPanel currentPanel = this;
		if (actionEvent.getSource() == addButton)
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					DBSeerConfiguration newConfig = new DBSeerConfiguration();
					DBSeerConfigFrame configFrame = new DBSeerConfigFrame("Add configuration", newConfig, list, false, currentPanel);
					configFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
					configFrame.pack();
					configFrame.setVisible(true);
				}
			});
		}
		else if (actionEvent.getSource() == editButton)
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					DBSeerConfiguration config = (DBSeerConfiguration)list.getSelectedValue();
					if (config != null)
					{
						DBSeerConfigFrame configFrame = new DBSeerConfigFrame("Edit configuration", config, list, true, currentPanel);
						configFrame.pack();
						configFrame.setVisible(true);
					}
				}
			});
		}
		else if (actionEvent.getSource() == removeButton)
		{
			Object[] configs = list.getSelectedValues();

			if (configs.length == 0)
			{
				return;
			}

			int confirm = JOptionPane.showConfirmDialog(null,
					"Do you really want to remove selected configurations?",
					"Warning",
					JOptionPane.YES_NO_OPTION);

			if (confirm == JOptionPane.YES_OPTION)
			{
				for (Object configObj : configs)
				{
					DBSeerConfiguration config = (DBSeerConfiguration)configObj;
					DBSeerGUI.configs.removeElement(config);
				}
			}

			if (DBSeerGUI.configs.size() == 0)
			{
				editButton.setEnabled(false);
			}

		}
	}

	public JButton getEditButton()
	{
		return editButton;
	}

	public JButton getAddButton()
	{
		return addButton;
	}

	@Override
	public void mouseClicked(MouseEvent mouseEvent)
	{
		final DBSeerConfigListPanel currentPanel = this;
		if (mouseEvent.getClickCount() == 2)
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					DBSeerConfiguration config = (DBSeerConfiguration)list.getSelectedValue();
					if (config != null)
					{
						DBSeerConfigFrame configFrame = new DBSeerConfigFrame("Edit configuration", config, list, true, currentPanel);
						configFrame.pack();
						configFrame.setVisible(true);
					}
				}
			});
		}
	}

	@Override
	public void mousePressed(MouseEvent mouseEvent)
	{

	}

	@Override
	public void mouseReleased(MouseEvent mouseEvent)
	{

	}

	@Override
	public void mouseEntered(MouseEvent mouseEvent)
	{

	}

	@Override
	public void mouseExited(MouseEvent mouseEvent)
	{

	}
}
