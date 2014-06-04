package dbseer.gui.actions;

import dbseer.gui.DBSeerFileLoadDialog;
import dbseer.gui.DBSeerGUI;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Created by dyoon on 2014. 5. 25..
 */
public class SetPercentileLatencyAction extends AbstractAction
{
	private DBSeerFileLoadDialog loadDialog;

	public SetPercentileLatencyAction()
	{
		super("Set Percentile Latency Data");
		loadDialog = new DBSeerFileLoadDialog();
	}

	@Override
	public void actionPerformed(ActionEvent actionEvent)
	{
		loadDialog.createFileDialog("Select Percentile Latency Data", DBSeerFileLoadDialog.FILE_ONLY);
		loadDialog.showDialog();

		if (loadDialog.getFile() != null)
		{
			//DBSeerGUI.config.setPercentileLatencyPath(loadDialog.getFile().getAbsolutePath());
		}
	}
}
