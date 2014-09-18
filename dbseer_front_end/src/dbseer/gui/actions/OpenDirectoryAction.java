package dbseer.gui.actions;

import dbseer.gui.user.DBSeerDataSet;
import dbseer.gui.dialog.DBSeerFileLoadDialog;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Created by dyoon on 2014. 5. 26..
 */
public class OpenDirectoryAction extends AbstractAction
{
	private DBSeerDataSet profile;
	private DBSeerFileLoadDialog loadDialog;

	public OpenDirectoryAction(DBSeerDataSet profile)
	{
		super("Open Directory");

		this.profile = profile;
		loadDialog = new DBSeerFileLoadDialog();
	}

	@Override
	public void actionPerformed(ActionEvent actionEvent)
	{
		//DBSeerConfiguration config = DBSeerGUI.config;
		loadDialog.createFileDialog("Open Directory", DBSeerFileLoadDialog.DIRECTORY_ONLY);
		loadDialog.showDialog();

		if (loadDialog.getFile() != null)
		{
			String[] files = loadDialog.getFile().list();
			String directory = loadDialog.getFile().getAbsolutePath();
			if (files != null)
			{
				for (String file : files)
				{
					String fileLower = file.toLowerCase();

					file = directory + "/" + file;
					if (fileLower.contains("mon"))
					{
						profile.setMonitoringDataPath(file);
					}
					else if (fileLower.contains("header"))
					{
						profile.setHeaderPath(file);
					}
					else if (fileLower.contains("avg") || fileLower.contains("average"))
					{
						profile.setAverageLatencyPath(file);
					}
					else if (fileLower.contains("trans_count"))
					{
						profile.setTransCountPath(file);
					}
					else if (fileLower.contains("prctile") || fileLower.contains("percentile"))
					{
						profile.setPercentileLatencyPath(file);
					}
					else if (fileLower.contains("stmt_count"))
					{
						profile.setStatementStatPath(file);
					}
					else if (fileLower.contains("page"))
					{
						profile.setPageInfoPath(file);
					}
				}
			}
		}
	}
}
