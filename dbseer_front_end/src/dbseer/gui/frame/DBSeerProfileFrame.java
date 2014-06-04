package dbseer.gui.frame;

import dbseer.gui.DBSeerDataProfile;
import dbseer.gui.DBSeerGUI;
import dbseer.gui.actions.AddProfileAction;
import dbseer.gui.actions.OpenDirectoryAction;
import dbseer.gui.panel.DBSeerProfileListPanel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by dyoon on 2014. 6. 3..
 */
public class DBSeerProfileFrame extends JFrame implements ActionListener
{
	boolean isEditMode;
	private DBSeerDataProfile profile;
	private JScrollPane scrollPane;
	private JButton openDirectoryButton;
	private JButton addProfileButton;
	private JButton editProfileButton;
	private JButton cancelButton;
	private JList list;

	public DBSeerProfileFrame(String title, DBSeerDataProfile profile, JList list)
	{
		this.setTitle(title);
		this.profile = profile;
		this.list = list;
		isEditMode = false;
		initializeGUI();
	}

	public DBSeerProfileFrame(String title, DBSeerDataProfile profile, JList list, boolean isEditMode)
	{
		this.setTitle(title);
		this.profile = profile;
		this.list = list;
		this.isEditMode = isEditMode;
		initializeGUI();
	}

	private void initializeGUI()
	{
		this.setLayout(new MigLayout("fill", "[fill,grow]"));
		scrollPane = new JScrollPane(profile.getTable());
		profile.getTable().setFillsViewportHeight(true);
		scrollPane.setPreferredSize(new Dimension(1000,400));

		openDirectoryButton = new JButton(new OpenDirectoryAction(profile));
		addProfileButton = new JButton(new AddProfileAction(profile, this, list));
		editProfileButton = new JButton("Edit Profile");
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
					profile.setFromTable();
					frame.dispose();
				}
			});

		}

	}
}
