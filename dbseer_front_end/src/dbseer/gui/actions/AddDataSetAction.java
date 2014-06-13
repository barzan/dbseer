package dbseer.gui.actions;

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

	public AddDataSetAction(DBSeerDataSet profile, JFrame frame, JList list)
	{
		super("Add Dataset");
		this.profile = profile;
		this.frame = frame;
		this.list = list;
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
				profile.setFromTable();
				//System.out.println(profile.getMonitoringDataPath());
				DBSeerGUI.datasets.addElement(profile);
				//list.setListData(DBSeerGUI.datasets.toArray());
				frame.dispose();
			}
		});

	}
}
