package dbseer.gui.actions;

import dbseer.gui.DBSeerPlotFrame;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Created by dyoon on 2014. 5. 25..
 */
public class OpenPlotFrameAction extends AbstractAction
{
	private DBSeerPlotFrame plotFrame;

	@Override
	public void actionPerformed(ActionEvent actionEvent)
	{
		plotFrame.pack();
		plotFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		plotFrame.setVisible(true);
	}
}
