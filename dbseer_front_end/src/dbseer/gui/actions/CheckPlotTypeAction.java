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

package dbseer.gui.actions;

import dbseer.gui.DBSeerGUI;
import dbseer.gui.panel.DBSeerPlotControlPanel;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Created by dyoon on 2014. 5. 26..
 */
public class CheckPlotTypeAction extends AbstractAction
{
	private String name = "";

	public CheckPlotTypeAction(String name)
	{
		super(name);
		this.name = name;
	}

	@Override
	public void actionPerformed(ActionEvent actionEvent)
	{
		Object source = actionEvent.getSource();
		if (source instanceof JCheckBox)
		{
			JCheckBox box = (JCheckBox)source;
			String chartFunction = this.findFunction(box.getText());
			if (box.isSelected())
			{
				if (chartFunction != null)
					DBSeerPlotControlPanel.chartsToDraw.add(chartFunction);
			}
			else
			{
				if (chartFunction != null)
					DBSeerPlotControlPanel.chartsToDraw.remove(chartFunction);
			}
		}
	}

	private String findFunction(String name)
	{
		for (int i = 0; i < DBSeerGUI.availableChartNames.length; ++i)
		{
			if (name.equalsIgnoreCase(DBSeerGUI.availableChartNames[i]))
			{
				return DBSeerGUI.availableCharts[i];
			}
		}
		return null;
	}
}
