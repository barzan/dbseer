package dbseer.gui.actions;

import dbseer.gui.dialog.DBSeerFileLoadDialog;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Created by dyoon on 2014. 5. 25..
 */
public class SetAverageLatencyAction extends AbstractAction
{
	private DBSeerFileLoadDialog loadDialog;

	public SetAverageLatencyAction()
	{
		super("Set Average Latency Data");

		loadDialog = new DBSeerFileLoadDialog();
	}

	@Override
	public void actionPerformed(ActionEvent actionEvent)
	{
		loadDialog.createFileDialog("Select Average Latency Data", DBSeerFileLoadDialog.FILE_ONLY);
		loadDialog.showDialog();

		if (loadDialog.getFile() != null)
		{
			//DBSeerGUI.config.setAverageLatencyPath(loadDialog.getFile().getAbsolutePath());
		}
	}
}
