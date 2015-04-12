package dbseer.gui.actions;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Created by dyoon on 14. 11. 24..
 */
public class ManuallyChangeFieldAction extends AbstractAction
{
	JTextField field;
	public ManuallyChangeFieldAction(JTextField field)
	{
		super("Change manually");
		this.field = field;
	}
	@Override
	public void actionPerformed(ActionEvent actionEvent)
	{
		field.setEnabled(true);
	}
}
