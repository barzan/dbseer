package dbseer.gui.user;

import javax.swing.table.DefaultTableModel;

/**
 * Created by dyoon on 2014. 5. 25..
 *
 * Table model blocking user input.
 */
public class DBSeerConfigurationTableModel extends DefaultTableModel
{
	public DBSeerConfigurationTableModel(Object obj, String[] strings)
	{
		super((Object[][]) obj, strings);
	}


	@Override
	public boolean isCellEditable(int row, int col)
	{
		if ( col != 0 )
			return true;
		else
			return false;
	}
}
