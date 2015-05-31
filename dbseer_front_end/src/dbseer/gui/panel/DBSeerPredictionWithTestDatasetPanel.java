package dbseer.gui.panel;

import dbseer.comp.MatlabFunctions;
import dbseer.gui.DBSeerConstants;
import dbseer.gui.DBSeerGUI;
import dbseer.gui.model.SharedComboBoxModel;
import dbseer.gui.user.DBSeerDataSet;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

/**
 * Created by dyoon on 5/5/15.
 */
public class DBSeerPredictionWithTestDatasetPanel extends JPanel
{
	private DBSeerDataSet trainDataset = null;

	private JLabel selectLabel = new JLabel("Please select a train config.");

	private JComboBox testDatasetComboBox;
	private JComboBox groupingTypeBox;
	private JComboBox groupingTargetBox;

	private JPanel groupingOptionsPanel;
	private JPanel transactionTypesToGroupPanel;

	private JLabel minSizeForGroupLabel;
	private JSlider minSizeForGroupSlider;
	private JTextField minSizeForGroupField;

	private JLabel allowedRelativeDifferenceLabel;
	private JSlider allowedRelativeDifferenceSlider;
	private JTextField allowedRelativeDifferenceField;

	private JLabel minTPSLabel;
	private JSlider minTPSSlider;
	private JTextField minTPSField;

	private JLabel maxTPSLabel;
	private JSlider maxTPSSlider;
	private JTextField maxTPSField;

	private JLabel numGroupLabel;
	private JSlider numGroupSlider;
	private JTextField numGroupField;

	private JLabel groupingRangesLabel;
	private JTextArea groupingRangesTextArea;
	private JScrollPane groupingRangesScrollPane;

	private ArrayList<JCheckBox> transactionTypesCheckBoxes = new ArrayList<JCheckBox>();

	private boolean firstInitialization = false;

	public DBSeerPredictionWithTestDatasetPanel()
	{
		this.setLayout(new MigLayout("align 50% 50%"));
		this.add(selectLabel);
	}

	public void setDataset(DBSeerDataSet dataset)
	{
		this.trainDataset = dataset;
		if (!firstInitialization)
		{
			this.remove(selectLabel);
			this.setLayout(new MigLayout("fill, ins 0"));
			initialize();
			firstInitialization = true;
		}
	}

	private void initialize()
	{
		final DBSeerPredictionWithTestDatasetPanel datasetPanel = this;
		// combobox to select test dataset.
		testDatasetComboBox = new JComboBox(new SharedComboBoxModel(DBSeerGUI.datasets));
		testDatasetComboBox.setBorder(BorderFactory.createTitledBorder("Test Dataset"));
		testDatasetComboBox.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent actionEvent)
			{
				if (testDatasetComboBox.getSelectedItem() == null)
				{
					return;
				}
				final DBSeerDataSet dataset = (DBSeerDataSet)testDatasetComboBox.getSelectedItem();
				DBSeerGUI.status.setText("Loading Test Dataset...");

				SwingUtilities.invokeLater(new Runnable()
				{
					@Override
					public void run()
					{
						dataset.loadModelVariable();
						int numRows = MatlabFunctions.getTotalRows(dataset);
						int minTPS = (int)MatlabFunctions.getMinTPS(dataset);
						int maxTPS = (int)MatlabFunctions.getMaxTPS(dataset);

						minSizeForGroupLabel.setText(String.format("Minimum Size of Group (1-%d):", numRows));
						minSizeForGroupSlider.setMaximum(numRows);

						minTPSLabel.setText(String.format("Minimum TPS (%d-%d):", minTPS, maxTPS));
						minTPSSlider.setMinimum(minTPS);
						minTPSSlider.setMaximum(maxTPS);
						minTPSSlider.setValue(minTPS);
						minTPSField.setText(String.format("%d", minTPS));

						maxTPSLabel.setText(String.format("Maximum TPS (%d-%d):", minTPS, maxTPS));
						maxTPSSlider.setMinimum(minTPS);
						maxTPSSlider.setMaximum(maxTPS);
						maxTPSSlider.setValue(maxTPS);
						maxTPSField.setText(String.format("%d", maxTPS));

						// set transaction type checkboxes
						for (JCheckBox checkBox : transactionTypesCheckBoxes)
						{
							transactionTypesToGroupPanel.remove(checkBox);
						}
						transactionTypesCheckBoxes.clear();

						for (String transactionType : dataset.getTransactionTypeNames())
						{
							JCheckBox transactionTypeCheckBox = new JCheckBox(transactionType);
							transactionTypeCheckBox.setSelected(true);
							transactionTypesToGroupPanel.add(transactionTypeCheckBox, "wrap");
							transactionTypesCheckBoxes.add(transactionTypeCheckBox);
						}

						datasetPanel.revalidate();
						DBSeerGUI.status.setText("");
					}
				});
			}
		});

		groupingTypeBox = new JComboBox(DBSeerConstants.GROUP_TYPES);
		groupingTypeBox.setBorder(BorderFactory.createTitledBorder("Grouping Type"));
		groupingTypeBox.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent actionEvent)
			{
				if (groupingTypeBox.getSelectedIndex() == DBSeerConstants.GROUP_NONE)
				{
					for (Component component : groupingOptionsPanel.getComponents())
					{
						component.setEnabled(false);
					}
					for (Component component : transactionTypesToGroupPanel.getComponents())
					{
						component.setEnabled(false);
					}
					groupingTargetBox.setEnabled(false);
				}
				else
				{
					groupingTargetBox.setEnabled(true);

					minTPSLabel.setEnabled(true);
					minTPSSlider.setEnabled(true);
					minTPSField.setEnabled(true);
					maxTPSLabel.setEnabled(true);
					maxTPSSlider.setEnabled(true);
					maxTPSField.setEnabled(true);
					minSizeForGroupLabel.setEnabled(true);
					minSizeForGroupSlider.setEnabled(true);
					minSizeForGroupField.setEnabled(true);

					groupingRangesLabel.setEnabled(false);
					groupingRangesTextArea.setEnabled(false);
					groupingRangesTextArea.setEditable(false);
					groupingRangesScrollPane.setEnabled(false);

					allowedRelativeDifferenceLabel.setEnabled(false);
					allowedRelativeDifferenceSlider.setEnabled(false);
					allowedRelativeDifferenceField.setEnabled(false);

					numGroupLabel.setEnabled(false);
					numGroupSlider.setEnabled(false);
					numGroupField.setEnabled(false);

					if (groupingTargetBox.getSelectedIndex() == DBSeerConstants.GROUP_TARGET_INDIVIDUAL_TRANS_COUNT)
					{
						for (Component component : transactionTypesToGroupPanel.getComponents())
						{
							component.setEnabled(true);
						}
					}
				}

				if (groupingTypeBox.getSelectedIndex() == DBSeerConstants.GROUP_RANGE)
				{
					groupingRangesLabel.setEnabled(true);
					groupingRangesTextArea.setEnabled(true);
					groupingRangesTextArea.setEditable(true);
					groupingRangesScrollPane.setEnabled(true);
				}
				else if(groupingTypeBox.getSelectedIndex() == DBSeerConstants.GROUP_REL_DIFF)
				{
					allowedRelativeDifferenceLabel.setEnabled(true);
					allowedRelativeDifferenceSlider.setEnabled(true);
					allowedRelativeDifferenceField.setEnabled(true);
				}
				else if(groupingTypeBox.getSelectedIndex() == DBSeerConstants.GROUP_NUM_CLUSTER)
				{
					numGroupLabel.setEnabled(true);
					numGroupSlider.setEnabled(true);
					numGroupField.setEnabled(true);
				}
			}
		});

		groupingTargetBox = new JComboBox(DBSeerConstants.GROUP_TARGETS);
		groupingTargetBox.setBorder(BorderFactory.createTitledBorder("Grouping Target"));
		groupingTargetBox.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent actionEvent)
			{
				if (groupingTargetBox.getSelectedIndex() == DBSeerConstants.GROUP_TARGET_INDIVIDUAL_TRANS_COUNT)
				{
					for (Component component : transactionTypesToGroupPanel.getComponents())
					{
						component.setEnabled(true);
					}
				}
				else if (groupingTargetBox.getSelectedIndex() == DBSeerConstants.GROUP_TARGET_TPS)
				{
					for (Component component : transactionTypesToGroupPanel.getComponents())
					{
						component.setEnabled(false);
					}
				}
			}
		});

		groupingOptionsPanel = new JPanel();
		groupingOptionsPanel.setLayout(new MigLayout("fill"));
		groupingOptionsPanel.setBorder(BorderFactory.createTitledBorder("Grouping Options"));

		transactionTypesToGroupPanel = new JPanel();
		transactionTypesToGroupPanel.setLayout(new MigLayout("fill"));
		transactionTypesToGroupPanel.setBorder(BorderFactory.createTitledBorder("Transaction Types for Grouping"));
		transactionTypesToGroupPanel.setMinimumSize(new Dimension(240, 10));

		minSizeForGroupLabel = new JLabel("Minimum Size of Group:");
		minSizeForGroupSlider = new JSlider(1, 1000, 1);
		minSizeForGroupField = new JTextField(4);
		minSizeForGroupField.setText("1");

		minSizeForGroupField.addKeyListener(new KeyAdapter()
		{
			@Override
			public void keyReleased(KeyEvent keyEvent)
			{
				String newValue = minSizeForGroupField.getText();
				minSizeForGroupSlider.setValue(1);
				if (!newValue.matches("\\d+(\\.\\d*)?"))
				{
					return;
				}
				double value = Double.parseDouble(newValue);
				minSizeForGroupSlider.setValue((int) value);
			}
		});
		minSizeForGroupSlider.addChangeListener(new ChangeListener()
		{
			@Override
			public void stateChanged(ChangeEvent changeEvent)
			{
				minSizeForGroupField.setText(String.format("%d", minSizeForGroupSlider.getValue()));
			}
		});

		allowedRelativeDifferenceLabel = new JLabel("Allowed Relative Difference:");
		allowedRelativeDifferenceSlider = new JSlider(0, 1000, 0);
		allowedRelativeDifferenceField = new JTextField(4);
		allowedRelativeDifferenceField.setText("0.000");

		allowedRelativeDifferenceField.addKeyListener(new KeyAdapter()
		{
			@Override
			public void keyReleased(KeyEvent keyEvent)
			{
				int prevValue = allowedRelativeDifferenceSlider.getValue();
				String newValue = allowedRelativeDifferenceField.getText();
				if (!newValue.matches("\\d+(\\.\\d*)?"))
				{
					allowedRelativeDifferenceSlider.setValue(0);
					return;
				}
				double value = Double.parseDouble(newValue);
				if ((int) (1000 * value) != prevValue)
				{
					allowedRelativeDifferenceSlider.setValue((int) (1000 * value));
				}
			}
		});
		allowedRelativeDifferenceSlider.addChangeListener(new ChangeListener()
		{
			@Override
			public void stateChanged(ChangeEvent changeEvent)
			{
				allowedRelativeDifferenceField.setText(String.format("%.3f", (double) allowedRelativeDifferenceSlider.getValue() / 1000.0));
			}
		});

		minTPSLabel = new JLabel("Minimum TPS:");
		minTPSSlider = new JSlider(0, 10000, 0);
		minTPSField = new JTextField(4);

		minTPSSlider.addChangeListener(new ChangeListener()
		{
			@Override
			public void stateChanged(ChangeEvent changeEvent)
			{
				minTPSField.setText(String.format("%d", minTPSSlider.getValue()));
			}
		});
		minTPSField.addKeyListener(new KeyAdapter()
		{
			@Override
			public void keyReleased(KeyEvent keyEvent)
			{
				String newValue = minTPSField.getText();
				minTPSSlider.setValue(0);
				if (!newValue.matches("\\d+(\\.\\d*)?"))
				{
					return;
				}
				double value = Double.parseDouble(newValue);
				minTPSSlider.setValue((int) value);
			}
		});

		maxTPSLabel = new JLabel("Maximum TPS:");
		maxTPSSlider = new JSlider(0, 10000, 0);
		maxTPSField = new JTextField(4);

		maxTPSSlider.addChangeListener(new ChangeListener()
		{
			@Override
			public void stateChanged(ChangeEvent changeEvent)
			{
				maxTPSField.setText(String.format("%d", maxTPSSlider.getValue()));
			}
		});
		maxTPSField.addKeyListener(new KeyAdapter()
		{
			@Override
			public void keyReleased(KeyEvent keyEvent)
			{
				String newValue = maxTPSField.getText();
				maxTPSSlider.setValue(0);
				if (!newValue.matches("\\d+(\\.\\d*)?"))
				{
					return;
				}
				double value = Double.parseDouble(newValue);
				maxTPSSlider.setValue((int) value);
			}
		});

		numGroupLabel = new JLabel("Number of Group/Clusters (1-100):");
		numGroupSlider = new JSlider(1, 100, 1);
		numGroupField = new JTextField(4);
		numGroupField.setText("1");

		numGroupSlider.addChangeListener(new ChangeListener()
		{
			@Override
			public void stateChanged(ChangeEvent changeEvent)
			{
				numGroupField.setText(String.format("%d", numGroupSlider.getValue()));
			}
		});
		numGroupField.addKeyListener(new KeyAdapter()
		{
			@Override
			public void keyReleased(KeyEvent keyEvent)
			{
				String newValue = numGroupField.getText();
				numGroupSlider.setValue(1);
				if (!newValue.matches("\\d+(\\.\\d*)?"))
				{
					return;
				}
				double value = Double.parseDouble(newValue);
				numGroupSlider.setValue((int) value);
			}
		});

		groupingRangesLabel = new JLabel("Manual Grouping Ranges (e.g. [100 200; 300 400; 500 600; ... ]):");
		groupingRangesTextArea = new JTextArea();
		groupingRangesScrollPane = new JScrollPane(groupingRangesTextArea);
		groupingRangesScrollPane.setPreferredSize(new Dimension(640,120));

		groupingOptionsPanel.add(minSizeForGroupLabel);
		groupingOptionsPanel.add(minSizeForGroupSlider);
		groupingOptionsPanel.add(minSizeForGroupField);
		groupingOptionsPanel.add(allowedRelativeDifferenceLabel);
		groupingOptionsPanel.add(allowedRelativeDifferenceSlider);
		groupingOptionsPanel.add(allowedRelativeDifferenceField, "wrap");
		groupingOptionsPanel.add(minTPSLabel);
		groupingOptionsPanel.add(minTPSSlider);
		groupingOptionsPanel.add(minTPSField);
		groupingOptionsPanel.add(maxTPSLabel);
		groupingOptionsPanel.add(maxTPSSlider);
		groupingOptionsPanel.add(maxTPSField, "wrap");
		groupingOptionsPanel.add(numGroupLabel);
		groupingOptionsPanel.add(numGroupSlider);
		groupingOptionsPanel.add(numGroupField, "wrap");
		groupingOptionsPanel.add(groupingRangesLabel, "spanx, wrap");
		groupingOptionsPanel.add(groupingRangesScrollPane, "spanx");

		this.add(testDatasetComboBox, "split 3");
		this.add(groupingTypeBox);
		this.add(groupingTargetBox, "wrap");
		this.add(groupingOptionsPanel);
		this.add(transactionTypesToGroupPanel, "grow");

		// default setup.
		for (Component component : groupingOptionsPanel.getComponents())
		{
			component.setEnabled(false);
		}
		for (Component component : transactionTypesToGroupPanel.getComponents())
		{
			component.setEnabled(false);
		}
		groupingTargetBox.setEnabled(false);
	}

	public DBSeerDataSet getTestDataset()
	{
		if (testDatasetComboBox.getSelectedItem() != null)
			return (DBSeerDataSet)testDatasetComboBox.getSelectedItem();
		else
			return null;
	}

	public int getGroupingType()
	{
		return groupingTypeBox.getSelectedIndex();
	}

	public int getGroupingTarget()
	{
		return groupingTargetBox.getSelectedIndex();
	}

	public int getMinSizeForGroup()
	{
		return minSizeForGroupSlider.getValue();
	}

	public int getMinTPS()
	{
		return minTPSSlider.getValue();
	}

	public int getMaxTPS()
	{
		return maxTPSSlider.getValue();
	}

	public double getAllowedRelativeDifference()
	{
		return (double)allowedRelativeDifferenceSlider.getValue() / 1000.0;
	}

	public int getNumGroups()
	{
		return numGroupSlider.getValue();
	}

	public String getGroupRanges()
	{
		return groupingRangesTextArea.getText();
	}

	public String getTransactionTypesToGroup()
	{
		String group = "[";
		int idx = 1;
		for (JCheckBox box : transactionTypesCheckBoxes)
		{
			if (box.isSelected())
			{
				group += idx;
				group += " ";
			}
			++idx;
		}
		group += "]";
		return group;
	}

	public JComboBox getGroupingTypeBox()
	{
		return groupingTypeBox;
	}

	public boolean checkValidMinMaxTPS()
	{
		if (minTPSSlider.isEnabled() || maxTPSSlider.isEnabled())
		{
			if (minTPSSlider.getValue() > maxTPSSlider.getValue())
			{
				return false;
			}
		}
		return true;
	}
}
