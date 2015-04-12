package dbseer.gui.actions;

import dbseer.comp.DataCenter;
import dbseer.gui.DBSeerGUI;
import dbseer.gui.dialog.DBSeerFileLoadDialog;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;

/**
 * Created by dyoon on 2014. 7. 22..
 */
public class ProcessDatasetDirectoryWithoutDBSCANAction extends AbstractAction
{
	private DBSeerFileLoadDialog loadDialog;

	public ProcessDatasetDirectoryWithoutDBSCANAction()
	{
		super("Process Dataset Without DBSCAN");

		loadDialog = new DBSeerFileLoadDialog();
	}

	@Override
	public void actionPerformed(ActionEvent actionEvent)
	{
		loadDialog.createFileDialog("Open Directory for Processing without DBSCAN", DBSeerFileLoadDialog.DIRECTORY_ONLY);
		loadDialog.showDialog();

		if (loadDialog.getFile() != null)
		{
			final String directory = loadDialog.getFile().getAbsolutePath();

			File topDirectory = new File(directory);
			final String[] subDirectories = topDirectory.list(new FilenameFilter()
			{
				@Override
				public boolean accept(File file, String s)
				{
					return new File(file, s).isDirectory();
				}
			});

//			for (String dir : subDirectories)
//			{
//				System.out.println(directory + File.separator + dir);
//			}
			DBSeerGUI.status.setText("Processing Dataset without DBSCAN...");
//			final ProgressDialog dialog = new ProgressDialog(null, "Processing dataset");
//			dialog.setLocationRelativeTo(null);
			SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>()
			{
				@Override
				protected Void doInBackground() throws Exception
				{
					DataCenter dc = new DataCenter(directory, false);
					if (!dc.parseLogs())
					{
						System.out.println("Parsing log failure");
					}
					else
					{
						dc.processDataset();
					}
					//dc.performDBSCAN();

//					for (String dir : subDirectories)
//					{
//						if (dir.contains("processed"))
//						{
//							continue;
//						}
//						System.out.println("Processing sub-directory: " + dir);
//						dc = new DataCenter(directory + File.separator + dir, false);
//						if (!dc.parseLogs())
//						{
//							System.out.println("Parsing log failure");
//						}
//						else
//						{
//							dc.processDataset();
//						}
//						//dc.performDBSCAN();
//					}
					return null;
				}

				@Override
				protected void done()
				{
					DBSeerGUI.status.setText("");
					JOptionPane.showMessageDialog(null, "Dataset has been processed.");
				}
			};

			worker.execute();
		}
	}
}
