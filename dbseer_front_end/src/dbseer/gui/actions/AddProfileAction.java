package dbseer.gui.actions;

import dbseer.gui.DBSeerDataProfile;
import dbseer.gui.DBSeerGUI;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Created by dyoon on 2014. 6. 3..
 */
public class AddProfileAction extends AbstractAction
{
	private DBSeerDataProfile profile;
	private JFrame frame;
	private JList list; // JList to update

	public AddProfileAction(DBSeerDataProfile profile, JFrame frame, JList list)
	{
		super("Add Profile");
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
//				DBSeerGUI.profiles.addElement(profile);
//				list.setListData(DBSeerGUI.profiles.toArray());
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
				DBSeerGUI.profiles.addElement(profile);
				//list.setListData(DBSeerGUI.profiles.toArray());
				frame.dispose();
			}
		});

	}
}
