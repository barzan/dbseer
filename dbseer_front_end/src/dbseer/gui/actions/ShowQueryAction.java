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

package dbseer.gui.actions;

import dbseer.gui.frame.DBSeerShowQueryFrame;
import dbseer.gui.frame.DBSeerShowTransactionExampleFrame;
import dbseer.gui.user.DBSeerDataSet;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Created by dyoon on 14. 11. 26..
 */
public class ShowQueryAction extends AbstractAction
{
	private DBSeerDataSet dataset;
	private int series;
	private int category;
	private double[] timestamp;
	private boolean showAll;

	public ShowQueryAction()
	{
		super("Show Queries");
		this.showAll = false;
	}

	public void setDataset(DBSeerDataSet dataset)
	{
		this.dataset = dataset;
	}

	public void setSeries(int series)
	{
		this.series = series;
	}

	public void setCategory(int category)
	{
		this.category = category;
	}

	public void setTimestamp(double[] timestamp)
	{
		this.timestamp = timestamp;
	}

	public boolean isShowAll()
	{
		return showAll;
	}

	public void setShowAll(boolean showAll)
	{
		this.showAll = showAll;
	}

	@Override
	public void actionPerformed(ActionEvent actionEvent)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				if (dataset.getLive())
				{
					if (category < 0)
					{
						DBSeerShowTransactionExampleFrame sampleFrame = new DBSeerShowTransactionExampleFrame(series);
						sampleFrame.pack();
						sampleFrame.setVisible(true);
					}
				}
				else
				{
					DBSeerShowQueryFrame queryFrame = new DBSeerShowQueryFrame(dataset, series, category, timestamp, showAll);
					if (queryFrame.isQueryAvailable())
					{
						queryFrame.pack();
						queryFrame.setVisible(true);
					}
					else
					{
						JOptionPane.showMessageDialog(null, "Queries for the highlighted point are not available.", "Warning",
								JOptionPane.WARNING_MESSAGE);
					}
				}
			}
		});
	}
}
