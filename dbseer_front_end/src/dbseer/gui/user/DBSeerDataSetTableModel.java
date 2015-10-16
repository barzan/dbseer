/*
 * Copyright 2013 Barzan Mozafari
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dbseer.gui.user;

import javax.swing.table.DefaultTableModel;

/**
 * Created by dyoon on 2014. 5. 25..
 *
 * Table model blocking user input.
 */
public class DBSeerDataSetTableModel extends DefaultTableModel
{
	protected boolean useEntireDataSet = true;
	protected boolean editable = true;

	public DBSeerDataSetTableModel(Object obj, String[] strings, boolean editable)
	{
		super((Object[][]) obj, strings);
		this.editable = editable;
	}

	public void setUseEntireDataSet(boolean useEntireDataSet)
	{
		this.useEntireDataSet = useEntireDataSet;
	}

	@Override
	public boolean isCellEditable(int row, int col)
	{
		if (!editable)
		{
			return false;
		}

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
