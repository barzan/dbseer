package dbseer.gui.actions;

import dbseer.gui.frame.DBSeerEditCausalModelFrame;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Created by dyoon on 5/18/15.
 */
public class EditCausalModelAction extends AbstractAction
{
	public EditCausalModelAction()
	{
		super("View/Edit Causal Models");
	}

	@Override
	public void actionPerformed(ActionEvent actionEvent)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				DBSeerEditCausalModelFrame editFrame = new DBSeerEditCausalModelFrame();
				editFrame.pack();
				editFrame.setVisible(true);
			}
		});
	}
}
