package dbseer.gui.dialog;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * Created by dyoon on 2014. 5. 24..
 */
public class DBSeerFileLoadDialog
{
	public static int DIRECTORY_ONLY = 1;
 	public static int FILE_ONLY = 2;
	private int mode;
	private boolean isMacOS;
	private JFileChooser normalDialog;
	private FileDialog macDialog;
	private File file = null;

	public DBSeerFileLoadDialog()
	{
		String OS = System.getProperty("os.name").toLowerCase();

		if (OS.indexOf("mac") >= 0 || OS.indexOf("darwin") >= 0)
		{
			isMacOS = true;
		}
	}

	public void createFileDialog(String title, int mode)
	{
		this.mode = mode;
		if (isMacOS)
		{
			macDialog = new FileDialog(new Frame(), title, FileDialog.LOAD);
		}
		else
		{
			normalDialog = new JFileChooser();
		}
	}

	public File getFile()
	{
		return file;
	}

	public void setDirectory(String directory)
	{
		if (isMacOS)
		{
			macDialog.setDirectory(directory);
		}
		else
		{
			normalDialog.setCurrentDirectory(new File(directory));
		}
	}

	public void showDialog()
	{
		file = null;
		if (isMacOS)
		{
			if (mode == DBSeerFileLoadDialog.DIRECTORY_ONLY)
			{
				System.setProperty("apple.awt.fileDialogForDirectories", "true");
			}
			else
			{
				System.setProperty("apple.awt.fileDialogForDirectories", "false");
			}
			macDialog.setVisible(true);
			if (macDialog.getFile() != null)
			{
				file = new File(macDialog.getDirectory() + macDialog.getFile());
			}
		}
		else
		{
			if (mode == DBSeerFileLoadDialog.DIRECTORY_ONLY)
			{
				normalDialog.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			}
			else
			{
				normalDialog.setFileSelectionMode(JFileChooser.FILES_ONLY);
			}
			int ret = normalDialog.showOpenDialog(new Frame());
			if (ret == JFileChooser.APPROVE_OPTION)
			{
				file = normalDialog.getSelectedFile();
			}
		}
	}
}
