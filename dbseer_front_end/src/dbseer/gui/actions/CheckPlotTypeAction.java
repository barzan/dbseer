package dbseer.gui.actions;

import dbseer.gui.DBSeerPlotControlPanel;

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
			if (box.isSelected())
			{
				DBSeerPlotControlPanel.chartsToDraw.add(box.getText());
			}
			else
			{
				DBSeerPlotControlPanel.chartsToDraw.remove(box.getText());
			}
		}
	}
}
