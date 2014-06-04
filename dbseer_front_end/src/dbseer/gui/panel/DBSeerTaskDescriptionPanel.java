package dbseer.gui.panel;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by dyoon on 2014. 5. 18..
 */
public class DBSeerTaskDescriptionPanel extends JPanel
{
	private final JLabel labelTaskName = new JLabel();
	private final JTextField fieldTaskName = new JTextField();
	private final JLabel labelWorkloadName = new JLabel();
	private final JTextField fieldWorkloadName = new JTextField();
	private final JComboBox taskList = new JComboBox();

	private String[] availableTasks = {"FlushRatePrediction"};

	public DBSeerTaskDescriptionPanel()
	{
		initializeGUI();
	}

	private void initializeGUI()
	{
		this.setLayout(new MigLayout());
		GridBagConstraints constraint = new GridBagConstraints();

		labelTaskName.setText("Task Name");
		labelTaskName.setHorizontalAlignment(JLabel.LEFT);
		this.add(labelTaskName, "cell 0 0");

		for (String task : availableTasks)
		{
			taskList.addItem(task);
		}
		this.add(taskList, "cell 1 0");

		labelWorkloadName.setText("Workload Name");
		labelWorkloadName.setHorizontalAlignment(JLabel.LEFT);
		this.add(labelWorkloadName, "cell 0 1");

		fieldWorkloadName.setColumns(15);
		this.add(fieldWorkloadName, "cell 1 1");
	}

	public String GetTaskName()
	{
		return fieldTaskName.getText();
	}

	public String GetWorkloadName()
	{
		return fieldWorkloadName.getText();
	}
}
