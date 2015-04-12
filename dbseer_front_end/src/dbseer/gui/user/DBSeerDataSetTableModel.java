package dbseer.gui.user;

import javax.swing.table.DefaultTableModel;

/**
 * Created by dyoon on 2014. 5. 25..
 *
 * Table model blocking user input.
 */
public class DBSeerDataSetTableModel extends DefaultTableModel
{
	private boolean useEntireDataSet = true;

	public DBSeerDataSetTableModel(Object obj, String[] strings)
	{
		super((Object[][]) obj, strings);
	}

	public void setUseEntireDataSet(boolean useEntireDataSet)
	{
		this.useEntireDataSet = useEntireDataSet;
	}

	@Override
	public boolean isCellEditable(int row, int col)
	{
		if ( col != 0 )
		{
			if (useEntireDataSet)
			{
				if (row == DBSeerDataSet.TYPE_START_INDEX || row == DBSeerDataSet.TYPE_END_INDEX)
				{
					return false;
				}
			}
			if (row == DBSeerDataSet.TYPE_AVERAGE_LATENCY ||
					row == DBSeerDataSet.TYPE_HEADER ||
					row == DBSeerDataSet.TYPE_MONITORING_DATA ||
					row == DBSeerDataSet.TYPE_PERCENTILE_LATENCY ||
					row == DBSeerDataSet.TYPE_TRANSACTION_COUNT ||
					row == DBSeerDataSet.TYPE_NUM_TRANSACTION_TYPE)
			{
				return false;
			}
			return true;
		}
		else
			return false;
	}
}
