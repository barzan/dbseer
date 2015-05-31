package dbseer.gui.frame;

import dbseer.gui.panel.DBSeerConfigListPanel;
import dbseer.gui.user.DBSeerConfiguration;
import dbseer.gui.user.DBSeerDataSet;
import dbseer.gui.DBSeerGUI;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by dyoon on 2014. 6. 7..
 */
public class DBSeerConfigFrame extends JFrame implements ActionListener
{
	private DBSeerConfiguration config;
	private JList list;
	private boolean isEditMode = false;

	private JScrollPane tableScrollPane;
	private JButton addConfigButton;
	private JButton editConfigButton;
	private JButton cancelButton;

	private JPanel groupComboBoxPanel;
	private JComboBox groupingTypeBox;
	private JComboBox groupingTargetBox;

	private JScrollPane textScrollPane;
	private JTextArea groupsTextArea;

	private JPanel profileListPanel;
	private JScrollPane availableProfileListPane;
	private JScrollPane selectedProfileListPane;
	private JList availableProfileList;
	private JList selectedProfileList;
	private JButton addProfileToConfigButton;
	private JButton removeProfileFromConfigButton;

	private DefaultListModel originalProfileList;
	private DefaultListModel availableProfiles;
	private DBSeerConfigListPanel panel;

	public DBSeerConfigFrame(String title, DBSeerConfiguration config, JList list, boolean isEditMode, DBSeerConfigListPanel panel)
	{
		this.setTitle(title);
		this.config = config;
		this.list = list;
		this.isEditMode = isEditMode;
		this.panel = panel;

		DefaultListModel original = config.getDatasetList();
		availableProfiles = new DefaultListModel();
		for (int i = 0; i < DBSeerGUI.datasets.getSize(); ++i)
		{
			if (!original.contains(DBSeerGUI.datasets.getElementAt(i)))
			{
				availableProfiles.addElement(DBSeerGUI.datasets.getElementAt(i));
			}
		}

		if (isEditMode)
		{
			// let's backup selected datasets for the config in edit mode.
			// it will be used to restore the datasets when editing has been cancelled by user.
			originalProfileList = new DefaultListModel();
			for (int i = 0; i < original.getSize(); ++i)
			{
				originalProfileList.addElement(original.getElementAt(i));
			}
		}

		initializeGUI();
	}

	private void initializeGUI()
	{
		this.setLayout(new MigLayout("fill"));

		tableScrollPane = new JScrollPane(config.getTable());
		config.getTable().setFillsViewportHeight(true);
		config.getTable().setFont(new Font("Verdana", Font.PLAIN, 14));
		tableScrollPane.setPreferredSize(new Dimension(600, 300));

//		groupingTypeBox = config.getGroupTypeComboBox();
//		groupingTypeBox.setBorder(BorderFactory.createTitledBorder("Grouping Type"));
//		groupingTargetBox = config.getGroupTargetComboBox();
//		groupingTargetBox.setBorder(BorderFactory.createTitledBorder("Grouping Target"));
//		groupsTextArea = config.getGroupsTextArea();

//		if (groupingTypeBox.getSelectedIndex() != DBSeerConfiguration.GROUP_RANGE)
//		{
//			groupsTextArea.setEnabled(false);
//			groupsTextArea.setText("Disabled");
//		}

//		textScrollPane = new JScrollPane(groupsTextArea);
//		textScrollPane.setPreferredSize(new Dimension(300, 100));
//		textScrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
//				"Specify groups manually here"));

//		availableProfileList = new JList(DBSeerGUI.datasets);
		availableProfileList = new JList(availableProfiles);
		availableProfileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		selectedProfileList = new JList(config.getDatasetList());

		availableProfileListPane = new JScrollPane(availableProfileList);
		availableProfileListPane.setPreferredSize(new Dimension(200, 300));
		availableProfileListPane.setBorder(BorderFactory.createTitledBorder("Available datasets"));

		selectedProfileListPane = new JScrollPane(selectedProfileList);
		selectedProfileListPane.setPreferredSize(new Dimension(200, 300));
		selectedProfileListPane.setBorder(BorderFactory.createTitledBorder("Dataset for this train config"));

		addProfileToConfigButton = new JButton(">>");
		removeProfileFromConfigButton = new JButton("<<");
		if (config.getDatasetCount() > 0)
		{
			addProfileToConfigButton.setEnabled(false);
		}
		else
		{
			removeProfileFromConfigButton.setEnabled(false);
		}

		this.add(tableScrollPane, "cell 0 0 2 2, grow");

//		groupComboBoxPanel = new JPanel(new MigLayout("fill" , "[fill][fill]"));
//		groupComboBoxPanel.add(groupingTypeBox, "grow");
//		groupComboBoxPanel.add(groupingTargetBox, "grow");

		profileListPanel = new JPanel(new MigLayout("fill", "", "[grow][grow][grow]"));
		profileListPanel.add(availableProfileListPane, "dock west, grow");
		profileListPanel.add(selectedProfileListPane, "dock east, grow");
		profileListPanel.add(addProfileToConfigButton, "cell 0 0, bottom, growx");
		profileListPanel.add(removeProfileFromConfigButton, "cell 0 1, top, growx");

		addConfigButton = new JButton("Add Config");
		editConfigButton = new JButton("Save");
		cancelButton = new JButton("Cancel");

//		this.add(groupComboBoxPanel, "cell 2 0");
		this.add(profileListPanel, "cell 2 0, grow");
//		this.add(textScrollPane, "cell 0 3 2 1, grow");

		if (isEditMode)
		{
			this.add(editConfigButton, "cell 2 3, grow, split 2");
//			groupingTypeBox.setSelectedIndex(config.getGroupingType());
//			groupingTargetBox.setSelectedIndex(config.getGroupingTarget());
		}
		else
		{
			this.add(addConfigButton, "cell 2 3, grow, split 2");
		}
		this.add(cancelButton, "grow");

		// add action listeners to components in this frame.
//		groupingTypeBox.addActionListener(this);
//		groupingTargetBox.addActionListener(this);
		addConfigButton.addActionListener(this);
		editConfigButton.addActionListener(this);
		cancelButton.addActionListener(this);
		addProfileToConfigButton.addActionListener(this);
		removeProfileFromConfigButton.addActionListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent actionEvent)
	{
		final JFrame frame = this;

		if (actionEvent.getSource() == groupingTypeBox)
		{
			if (groupingTypeBox.getSelectedIndex() == DBSeerConfiguration.GROUP_RANGE)
			{
				groupsTextArea.setEnabled(true);
				if (groupsTextArea.getText().compareTo("Disabled") == 0)
				{
					groupsTextArea.setText("");
				}
			}
			else
			{
				groupsTextArea.setEnabled(false);
			}
		}
		else if (actionEvent.getSource() == addConfigButton)
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					if (config.validateTable())
					{
						config.setFromTable();
						DBSeerGUI.configs.addElement(config);
						frame.dispose();
					}
				}
			});

			if (DBSeerGUI.configs.size() != 0)
			{
				panel.getEditButton().setEnabled(true);
			}
		}
		else if (actionEvent.getSource() == editConfigButton)
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					if (config.validateTable())
					{
						config.setFromTable();
						frame.dispose();
					}
				}
			});
		}
		else if (actionEvent.getSource() == cancelButton)
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					if (isEditMode)
					{
						// rollback profile
						config.setDatasetList(originalProfileList);
						frame.dispose();
					}
					else {
						frame.dispose();
					}
				}
			});
		}
		else if (actionEvent.getSource() == addProfileToConfigButton)
		{
			Object[] profileObjsToAdd = availableProfileList.getSelectedValues();
			for (Object profileObj : profileObjsToAdd)
			{
				DBSeerDataSet profile = (DBSeerDataSet)profileObj;
				config.addDataset(profile);
				availableProfiles.removeElement(profile);
			}
			if (config.getDatasetCount() > 0)
			{
				addProfileToConfigButton.setEnabled(false);
			}
		}
		else if (actionEvent.getSource() == removeProfileFromConfigButton)
		{
			Object[] profileObjsToRemove = selectedProfileList.getSelectedValues();
			for (Object profileObj : profileObjsToRemove)
			{
				DBSeerDataSet profile = (DBSeerDataSet)profileObj;
				config.removeDataset(profile);
				availableProfiles.addElement(profile);
			}
			if (config.getDatasetCount() == 0)
			{
				addProfileToConfigButton.setEnabled(true);
			}
		}
	}
}
