package dbseer.gui.actions;

import dbseer.gui.DBSeerFileLoadDialog;
import dbseer.gui.DBSeerGUI;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Created by dyoon on 2014. 5. 25..
 */
public class SetHeaderAction extends AbstractAction
{
	private DBSeerFileLoadDialog loadDialog;

	public SetHeaderAction()
	{
		super("Set Header M-File");
		loadDialog = new DBSeerFileLoadDialog();
	}
	@Override
	public void actionPerformed(ActionEvent actionEvent)
	{
		loadDialog.createFileDialog("Select Average Latency Data", DBSeerFileLoadDialog.FILE_ONLY);
		loadDialog.showDialog();

		if (loadDialog.getFile() != null)
		{
			DBSeerGUI.config.setHeaderPath(loadDialog.getFile().getAbsolutePath());
		}
	}
}
