package dbseer.gui.actions;

import dbseer.gui.DBSeerFileLoadDialog;
import dbseer.gui.DBSeerGUI;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Created by dyoon on 2014. 5. 25..
 */
public class SetTransactionCountAction extends AbstractAction
{
	private DBSeerFileLoadDialog loadDialog;

	public SetTransactionCountAction()
	{
		super("Set Transaction Count");
		loadDialog = new DBSeerFileLoadDialog();
	}
	@Override
	public void actionPerformed(ActionEvent actionEvent)
	{
		loadDialog.createFileDialog("Select Transaction Count Data", DBSeerFileLoadDialog.FILE_ONLY);
		loadDialog.showDialog();

		if (loadDialog.getFile() != null)
		{
			DBSeerGUI.config.setTransCountPath(loadDialog.getFile().getAbsolutePath());
		}
	}
}
