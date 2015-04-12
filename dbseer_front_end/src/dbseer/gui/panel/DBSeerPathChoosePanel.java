package dbseer.gui.panel;

import dbseer.gui.dialog.DBSeerFileLoadDialog;
import dbseer.gui.DBSeerGUI;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by dyoon on 2014. 5. 18..
 */
public class DBSeerPathChoosePanel extends JPanel implements ActionListener
{
	private JButton openButton;
	private DBSeerFileLoadDialog fileLoadDialog;
	private JLabel pathToDBSeerLabel;

	public DBSeerPathChoosePanel()
	{
		super(new MigLayout());

		fileLoadDialog = new DBSeerFileLoadDialog();

		openButton = new JButton("Change Root Path");
		pathToDBSeerLabel = new JLabel();
		pathToDBSeerLabel.setText("Current DBSeer Root Path: " + DBSeerGUI.userSettings.getDBSeerRootPath());
		pathToDBSeerLabel.setPreferredSize(new Dimension(500, 10));
		openButton.addActionListener(this);

		add(openButton);
		add(pathToDBSeerLabel, "wrap");

	}

	@Override
	public void actionPerformed(ActionEvent actionEvent)
	{
		if (actionEvent.getSource() == openButton)
		{
			fileLoadDialog.createFileDialog("Select DBSeer Root Directory", DBSeerFileLoadDialog.DIRECTORY_ONLY);
			fileLoadDialog.showDialog();
			if (fileLoadDialog.getFile() != null)
			{
				String rootPath = fileLoadDialog.getFile().getAbsolutePath();
				pathToDBSeerLabel.setText("Current DBSeer Root Directory: " + rootPath);
				DBSeerGUI.userSettings.setDBSeerRootPath(rootPath);
			}
		}
	}
}
