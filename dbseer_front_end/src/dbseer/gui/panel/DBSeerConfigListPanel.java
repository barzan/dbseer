package dbseer.gui.panel;

import dbseer.gui.DBSeerConfiguration;
import dbseer.gui.DBSeerGUI;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by dyoon on 2014. 6. 3..
 */
public class DBSeerConfigListPanel extends JPanel implements ActionListener
{
	private JList list;
	private JButton addButton;
	private JButton editButton;
	private JButton removeButton;
	private DefaultListModel listModel;

	public DBSeerConfigListPanel()
	{
		initializeGUI();
	}

	private void initializeGUI()
	{
		this.setLayout(new MigLayout("", "[align center, grow]", "[fill,grow] [align center]"));
		listModel = new DefaultListModel();

		for (DBSeerConfiguration config : DBSeerGUI.configs)
		{
			listModel.addElement(config);
		}

		list = new JList(listModel);
		list.setVisibleRowCount(15);
		list.setPreferredSize(new Dimension(300,300));
		list.setMaximumSize(new Dimension(1000,1000));

		addButton = new JButton("Add");
		editButton = new JButton("Edit");
		removeButton = new JButton("Remove");

		this.add(list, "wrap, growx");
		this.add(addButton, "split 3");
		this.add(editButton);
		this.add(removeButton);
	}

	@Override
	public void actionPerformed(ActionEvent actionEvent)
	{
		if (actionEvent.getSource() == addButton)
		{

		}
	}
}
