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

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.Collections;
import java.util.Vector;

/**
 * Created by dyoon on 14. 11. 20..
 */
public class DBSeerPlotCustomPanel extends JPanel
{
	private JLabel xAxisLabel;
	private JLabel yAxisLabel;
	private JComboBox xAxisComboBox;
	private JComboBox yAxisComboBox;

	private Vector<String> axisList;

	public DBSeerPlotCustomPanel()
	{
		axisList = new Vector<String>();
		for (String axis : DBSeerPlotControlPanel.axisMap.keySet())
		{
			if (!axis.equalsIgnoreCase("Time"))
				axisList.add(axis);
		}
		Collections.sort(axisList);
		axisList.add(0, "Time");
		initializeGUI();
	}

	private void initializeGUI()
	{
		this.setLayout(new MigLayout());

		xAxisLabel = new JLabel("X-Axis:");
		yAxisLabel = new JLabel("Y-Axis:");
		xAxisComboBox = new JComboBox(axisList);
		yAxisComboBox = new JComboBox(axisList);

		xAxisComboBox.setSelectedIndex(0); // Time
		yAxisComboBox.setSelectedIndex(1); // Avg Latency?

		xAxisComboBox.setMaximumRowCount(20);
		yAxisComboBox.setMaximumRowCount(20);

		this.add(xAxisLabel);
		this.add(xAxisComboBox, "growx, wrap");
		this.add(yAxisLabel);
		this.add(yAxisComboBox, "growx");
	}

	public String getXAxis()
	{
		return (String)xAxisComboBox.getSelectedItem();
	}
	public String getYAxis()
	{
		return (String)yAxisComboBox.getSelectedItem();
	}
}
