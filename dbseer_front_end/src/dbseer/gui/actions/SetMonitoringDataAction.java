package dbseer.gui.actions;

import dbseer.gui.dialog.DBSeerFileLoadDialog;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Created by dyoon on 2014. 5. 24..
 */
public class SetMonitoringDataAction extends AbstractAction
{
	private DBSeerFileLoadDialog loadDialog;

	public SetMonitoringDataAction()
	{
		super("Set Monitoring Data");

		loadDialog = new DBSeerFileLoadDialog();

	}
	@Override
	public void actionPerformed(ActionEvent actionEvent)
	{
		loadDialog.createFileDialog("Select Monitoring Data", DBSeerFileLoadDialog.FILE_ONLY);
		loadDialog.showDialog();

		if (loadDialog.getFile() != null)
		{
			//DBSeerGUI.config.setMonitoringDataPath(loadDialog.getFile().getAbsolutePath());
		}
	}
}
