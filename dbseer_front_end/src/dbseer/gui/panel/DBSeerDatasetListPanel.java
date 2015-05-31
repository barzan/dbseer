package dbseer.gui.panel;

import dbseer.gui.user.DBSeerConfiguration;
import dbseer.gui.user.DBSeerDataSet;
import dbseer.gui.DBSeerGUI;

import dbseer.gui.frame.DBSeerDataSetFrame;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * Created by dyoon on 2014. 6. 3..
 */
public class DBSeerDatasetListPanel extends JPanel implements ActionListener, MouseListener
{
	private JList list;
	private JButton addButton;

	private JButton editButton;
	private JButton removeButton;

	public DBSeerDatasetListPanel()
	{
		initializeGUI();
	}

	private void initializeGUI()
	{
		this.setLayout(new MigLayout("ins 0", "[align center, grow]", "[fill, grow] [align center]"));

		JScrollPane scrollPane = new JScrollPane();
		list = new JList(DBSeerGUI.datasets);
		list.setVisibleRowCount(8);

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

		if (DBSeerGUI.datasets.size() == 0)
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
		final DBSeerDatasetListPanel currentPanel = this;
		if (actionEvent.getSource() == this.addButton)
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					DBSeerDataSet newProfile = new DBSeerDataSet();
					DBSeerDataSetFrame profileFrame = new DBSeerDataSetFrame("Add dataset", newProfile, list, false, currentPanel);
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
					DBSeerDataSet profile = (DBSeerDataSet)list.getSelectedValue();
					if ( profile != null )
					{
						DBSeerDataSetFrame profileFrame = new DBSeerDataSetFrame("Edit dataset", profile, list, true, currentPanel);
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
					"This will also remove configurations that contain datasets being removed! " +
							"Do you want to proceed?",
					"Warning",
					JOptionPane.YES_NO_OPTION);

			if (confirm == JOptionPane.YES_OPTION)
			{
				for (Object profileObj : profiles)
				{
					DBSeerDataSet profile = (DBSeerDataSet) profileObj;

					// delete configurations containing the profile being removed.
					for (int i = 0; i < DBSeerGUI.configs.getSize(); ++i)
					{
						DBSeerConfiguration config = (DBSeerConfiguration) DBSeerGUI.configs.getElementAt(i);
						for (int j = 0; j < config.getDatasetList().getSize(); ++j)
						{
							DBSeerDataSet profileToDelete = config.getDataset(j);
							if (profileToDelete.equals(profile))
							{
								DBSeerGUI.configs.removeElement(config);
								--i;
								break;
							}
						}
					}

					DBSeerGUI.datasets.removeElement(profile);
				}
			}

			if (DBSeerGUI.datasets.size() == 0)
			{
				editButton.setEnabled(false);
			}
		}
	}

	public JButton getEditButton()
	{
		return editButton;
	}

	@Override
	public void mouseClicked(MouseEvent mouseEvent)
	{
		final DBSeerDatasetListPanel currentPanel = this;
		if (mouseEvent.getClickCount() == 2)
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					DBSeerDataSet profile = (DBSeerDataSet)list.getSelectedValue();
					if ( profile != null )
					{
						DBSeerDataSetFrame profileFrame = new DBSeerDataSetFrame("Edit dataset", profile, list, true, currentPanel);
						profileFrame.pack();
						profileFrame.setVisible(true);
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
