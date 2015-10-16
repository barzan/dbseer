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

/**
 * Created by dyoon on 10/1/15.
 */
public class DBSeerLiveDataSet extends DBSeerDataSet
{
	public DBSeerLiveDataSet()
	{
		super();
		tableModel = new DBSeerDataSetTableModel(null, new String[]{"Name", "Value"}, false);
	}

	@Override
	protected Object readResolve()
	{
		Object obj = super.readResolve();
		tableModel = new DBSeerDataSetTableModel(null, new String[]{"Name", "Value"}, false);
		return obj;
	}
}
