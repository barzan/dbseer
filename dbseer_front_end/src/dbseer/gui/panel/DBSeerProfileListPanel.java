package dbseer.gui.panel;

import dbseer.gui.DBSeerConfiguration;
import dbseer.gui.DBSeerDataProfile;
import dbseer.gui.DBSeerGUI;

import dbseer.gui.frame.DBSeerProfileFrame;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by dyoon on 2014. 6. 3..
 */
public class DBSeerProfileListPanel extends JPanel implements ActionListener
{
	private JList list;
	private JButton addButton;
	private JButton editButton;
	private JButton removeButton;

	public DBSeerProfileListPanel()
	{
		initializeGUI();
	}

	private void initializeGUI()
	{
		this.setLayout(new MigLayout("", "[align center, grow]", "[fill, grow] [align center]"));

		JScrollPane scrollPane = new JScrollPane();
		list = new JList(DBSeerGUI.profiles);
		list.setVisibleRowCount(15);

		scrollPane.setViewportView(list);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setPreferredSize(new Dimension(300,300));

		addButton = new JButton("Add");
		editButton = new JButton("Edit");
		removeButton = new JButton("Remove");

		addButton.addActionListener(this);
		editButton.addActionListener(this);
		removeButton.addActionListener(this);

		this.add(scrollPane, "wrap, growx");
		this.add(addButton, "split 3");
		this.add(editButton);
		this.add(removeButton);
	}

	@Override
	public void actionPerformed(ActionEvent actionEvent)
	{
		if (actionEvent.getSource() == this.addButton)
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					DBSeerDataProfile newProfile = new DBSeerDataProfile();
					DBSeerProfileFrame profileFrame = new DBSeerProfileFrame("Add data profile", newProfile, list);
					profileFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
					profileFrame.pack();
					profileFrame.setVisible(true);
				}
			});
		}
		else if (actionEvent.getSource() == this.editButton)
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					DBSeerDataProfile profile = (DBSeerDataProfile)list.getSelectedValue();
					if ( profile != null )
					{
						DBSeerProfileFrame profileFrame = new DBSeerProfileFrame("Edit data profile", profile, list, true);
						profileFrame.pack();
						profileFrame.setVisible(true);
					}
				}
			});
		}
		else if (actionEvent.getSource() == this.removeButton)
		{
			Object[] profiles = list.getSelectedValues();
			if (profiles.length == 0)
			{
				return;
			}

			int confirm = JOptionPane.showConfirmDialog(null,
					"This will also remove configurations that contain profiles being removed! " +
							"Do you want to proceed?",
					"Warning",
					JOptionPane.YES_NO_OPTION);

			if (confirm == JOptionPane.YES_OPTION)
			{
				for (Object profileObj : profiles)
				{
					DBSeerDataProfile profile = (DBSeerDataProfile) profileObj;

					// delete configurations containing the profile being removed.
					for (int i = 0; i < DBSeerGUI.configs.getSize(); ++i)
					{
						DBSeerConfiguration config = (DBSeerConfiguration) DBSeerGUI.configs.getElementAt(i);
						for (int j = 0; j < config.getProfileList().getSize(); ++j)
						{
							DBSeerDataProfile profileToDelete = config.getProfile(j);
							if (profileToDelete.equals(profile))
							{
								DBSeerGUI.configs.removeElement(config);
								--i;
								break;
							}
						}
					}

					DBSeerGUI.profiles.removeElement(profile);
				}
			}
		}
	}
}
