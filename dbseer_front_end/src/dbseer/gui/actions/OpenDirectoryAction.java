package dbseer.gui.actions;

import dbseer.gui.user.DBSeerDataSet;
import dbseer.gui.dialog.DBSeerFileLoadDialog;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.*;

/**
 * Created by dyoon on 2014. 5. 26..
 */
public class OpenDirectoryAction extends AbstractAction
{
	private DBSeerDataSet profile;
	private DBSeerFileLoadDialog loadDialog;

	public OpenDirectoryAction(DBSeerDataSet profile)
	{
		super("Import from Directory");

		this.profile = profile;
		loadDialog = new DBSeerFileLoadDialog();
	}

	@Override
	public void actionPerformed(ActionEvent actionEvent)
	{
		//DBSeerConfiguration config = DBSeerGUI.config;
		loadDialog.createFileDialog("Import from Directory", DBSeerFileLoadDialog.DIRECTORY_ONLY);
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
					else if (fileLower.contains("alllogs-t"))
					{
						profile.setTransactionFilePath(file);
					}
					else if (fileLower.contains("alllogs-q"))
					{
						profile.setQueryFilePath(file);
					}
					else if (fileLower.contains("alllogs-s"))
					{
						profile.setStatementFilePath(file);
					}
					else if (fileLower.contains("header"))
					{
						profile.setHeaderPath(file);
					}
					else if (fileLower.contains("avg") || fileLower.contains("average"))
					{
						profile.setAverageLatencyPath(file);
						File avgLatencyFile = new File(file);
						try
						{
							BufferedReader reader = new BufferedReader(new FileReader(avgLatencyFile));
							String line = reader.readLine(); // read first line.
							String[] tokens = line.trim().split("\\s+");
							int numTransactionType = tokens.length - 1;
							profile.setNumTransactionTypes(numTransactionType);
						}
						catch (FileNotFoundException e)
						{
							JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
						}
						catch (IOException e)
						{
							JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
						}
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
