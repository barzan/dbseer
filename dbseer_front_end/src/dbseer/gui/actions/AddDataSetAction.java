package dbseer.gui.actions;

import dbseer.gui.panel.DBSeerDatasetListPanel;
import dbseer.gui.user.DBSeerDataSet;
import dbseer.gui.DBSeerGUI;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Created by dyoon on 2014. 6. 3..
 */
public class AddDataSetAction extends AbstractAction
{
	private DBSeerDataSet profile;
	private JFrame frame;
	private JList list; // JList to update
	private DBSeerDatasetListPanel panel;

	public AddDataSetAction(DBSeerDataSet profile, JFrame frame, JList list, DBSeerDatasetListPanel panel)
	{
		super("Add Dataset");
		this.profile = profile;
		this.frame = frame;
		this.list = list;
		this.panel = panel;
	}

	@Override
	public void actionPerformed(ActionEvent actionEvent)
	{
//		SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>()
//		{
//			@Override
//			protected Void doInBackground() throws Exception
//			{
//				profile.setFromTable();
//				DBSeerGUI.datasets.addElement(profile);
//				list.setListData(DBSeerGUI.datasets.toArray());
//				list.invalidate();
//				return null;
//			}
//
//			@Override
//			protected void done()
//			{
//				frame.dispose();
//			}
//		};
//
//		worker.execute();

		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				if (profile.validateTable())
				{
					profile.setFromTable();
					DBSeerGUI.datasets.addElement(profile);
					frame.dispose();
				}
			}
		});

		if (DBSeerGUI.datasets.size() != 0)
		{
			panel.getEditButton().setEnabled(true);
		}
	}
}
