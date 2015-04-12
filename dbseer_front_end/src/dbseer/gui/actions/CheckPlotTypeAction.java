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
