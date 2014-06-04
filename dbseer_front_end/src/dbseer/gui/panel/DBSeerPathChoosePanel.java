package dbseer.gui.panel;

import dbseer.gui.DBSeerFileLoadDialog;
import dbseer.gui.DBSeerGUI;
import dbseer.gui.frame.DBSeerPlotFrame;
import matlabcontrol.MatlabInvocationException;
import matlabcontrol.MatlabProxy;
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

		openButton = new JButton("Browse");
		pathToDBSeerLabel = new JLabel();
		pathToDBSeerLabel.setText("Choose DBSeer Root Path");
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
				pathToDBSeerLabel.setText(rootPath);
				DBSeerGUI.root = rootPath;
			}
		}
	}
}
