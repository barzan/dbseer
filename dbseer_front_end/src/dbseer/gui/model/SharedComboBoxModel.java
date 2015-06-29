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

package dbseer.gui.model;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

/**
 * Created by dyoon on 2014. 6. 9..
 */
public class SharedComboBoxModel extends DefaultComboBoxModel
{
	private DefaultListModel listModel;
	private final ListDataListener listener = new ListDataListener()
	{
		@Override
		public void intervalAdded(ListDataEvent listDataEvent)
		{
			fireIntervalAdded(this, listDataEvent.getIndex0(), listDataEvent.getIndex1());
		}

		@Override
		public void intervalRemoved(ListDataEvent listDataEvent)
		{
			int index0 = listDataEvent.getIndex0();
			int index1 = listDataEvent.getIndex1();

			setSelectedItem(null);

			for (int i = 0; i < listModel.getSize(); ++i)
			{
				if (listModel.get(i) == getSelectedItem())
				{
					setSelectedItem(getElementAt(i));
				}
			}
			fireIntervalRemoved(this, index0, index1);
		}

		@Override
		public void contentsChanged(ListDataEvent listDataEvent)
		{
			fireContentsChanged(this, listDataEvent.getIndex0(), listDataEvent.getIndex1());
		}
	};

	public SharedComboBoxModel(DefaultListModel model)
	{
		this.setListModel(model);
	}

	public DefaultListModel getListModel()
	{
		return listModel;
	}

	public void setListModel(DefaultListModel model)
	{
		if (this.listModel != null)
		{
			this.listModel.removeListDataListener(listener);
		}
		this.listModel = model;
		listModel.addListDataListener(listener);
	}

	@Override
	public int getSize()
	{
		return listModel.getSize();
	}

	@Override
	public Object getElementAt(int i)
	{
		return listModel.getElementAt(i);
	}

	@Override
	public void addElement(Object o)
	{
		listModel.addElement(o);
	}

//	@Override
//	public void addListDataListener(ListDataListener listDataListener)
//	{
//		listModel.addListDataListener(listDataListener);
//	}
//
//	@Override
//	public void removeListDataListener(ListDataListener listDataListener)
//	{
//		listModel.removeListDataListener(listDataListener);
//	}
}
