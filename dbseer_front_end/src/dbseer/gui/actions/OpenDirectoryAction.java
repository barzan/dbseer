package dbseer.gui.actions;

import dbseer.gui.DBSeerConfiguration;
import dbseer.gui.DBSeerFileLoadDialog;
import dbseer.gui.DBSeerGUI;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Created by dyoon on 2014. 5. 26..
 */
public class OpenDirectoryAction extends AbstractAction
{
	private DBSeerFileLoadDialog loadDialog;

	public OpenDirectoryAction()
	{
		super("Open Directory");

		loadDialog = new DBSeerFileLoadDialog();
	}

	@Override
	public void actionPerformed(ActionEvent actionEvent)
	{
		DBSeerConfiguration config = DBSeerGUI.config;
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
						config.setMonitoringDataPath(file);
					}
					else if (fileLower.contains("header"))
					{
						config.setHeaderPath(file);
					}
					else if (fileLower.contains("avg") || fileLower.contains("average"))
					{
						config.setAverageLatencyPath(file);
					}
					else if (fileLower.contains("count"))
					{
						config.setTransCountPath(file);
					}
					else if (fileLower.contains("prctile") || fileLower.contains("percentile"))
					{
						config.setPercentileLatencyPath(file);
					}
				}
			}
		}
	}
}
