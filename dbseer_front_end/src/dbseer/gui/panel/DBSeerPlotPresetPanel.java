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

import dbseer.gui.DBSeerGUI;
import dbseer.gui.actions.CheckPlotTypeAction;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

/**
 * Created by dyoon on 14. 11. 20..
 */
public class DBSeerPlotPresetPanel extends JPanel implements ActionListener
{
	private final int WRAP_COUNT = 3;

	private ArrayList<JCheckBox> boxList = new ArrayList<JCheckBox>();
	private JButton selectAllButton;
	private JButton deselectAllButton;

	private JPanel transactionChartsPanel;
	private JPanel systemChartsPanel;
	private JPanel dbmsChartsPanel;

	public DBSeerPlotPresetPanel()
	{
		initializeGUI();
	}

	private void initializeGUI()
	{
		this.setLayout(new MigLayout("fill, ins 0"));

		selectAllButton = new JButton();
		selectAllButton.setText("Select All");
		selectAllButton.addActionListener(this);

		deselectAllButton = new JButton();
		deselectAllButton.setText("Deselect All");
		deselectAllButton.addActionListener(this);

		transactionChartsPanel = new JPanel(new MigLayout("ins 0, wrap 3"));
		transactionChartsPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Transaction"));
		systemChartsPanel = new JPanel(new MigLayout("ins 0, wrap 4"));
		systemChartsPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "System"));
		dbmsChartsPanel = new JPanel(new MigLayout("ins 0, wrap 4"));
		dbmsChartsPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "DBMS"));

		for (String name : DBSeerGUI.transactionChartNames)
		{
			JCheckBox box = new JCheckBox(new CheckPlotTypeAction(name));
			boxList.add(box);
			transactionChartsPanel.add(box);
		}
		for (String name : DBSeerGUI.systemChartNames)
		{
			JCheckBox box = new JCheckBox(new CheckPlotTypeAction(name));
			boxList.add(box);
			systemChartsPanel.add(box);
		}
		for (String name : DBSeerGUI.dbmsChartNames)
		{
			JCheckBox box = new JCheckBox(new CheckPlotTypeAction(name));
			boxList.add(box);
			dbmsChartsPanel.add(box);
		}
		this.add(transactionChartsPanel, "grow, wrap");
		this.add(systemChartsPanel, "grow, wrap");
		this.add(dbmsChartsPanel, "grow, wrap");

//		int count = 0;
//		for (String name : DBSeerGUI.availableChartNames)
//		{
//			JCheckBox box = new JCheckBox(new CheckPlotTypeAction(name));
//			boxList.add(box);
//			if (++count == WRAP_COUNT || name.equalsIgnoreCase(DBSeerGUI.availableChartNames[DBSeerGUI.availableChartNames.length-1]))
//			{
//				this.add(box, "wrap");
//				count = 0;
//			}
//			else
//			{
//				this.add(box);
//			}
//		}
		this.add(selectAllButton, "split 2");
		this.add(deselectAllButton);
	}

	public ArrayList<JCheckBox> getBoxList()
	{
		return boxList;
	}

	@Override
	public void actionPerformed(ActionEvent actionEvent)
	{
		if (actionEvent.getSource() == selectAllButton)
		{
			for (JCheckBox box : boxList)
			{
				box.setSelected(true);
				for (ActionListener listener : box.getActionListeners())
				{
					listener.actionPerformed(new ActionEvent(box, ActionEvent.ACTION_PERFORMED, null));
				}
			}
		}
		else if (actionEvent.getSource() == deselectAllButton)
		{
			for (JCheckBox box : boxList)
			{
				box.setSelected(false);
				for (ActionListener listener : box.getActionListeners())
				{
					listener.actionPerformed(new ActionEvent(box, ActionEvent.ACTION_PERFORMED, null));
				}
			}
		}
	}
}


