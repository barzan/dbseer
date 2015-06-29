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

package dbseer.gui.panel;

import dbseer.gui.events.InformationChartMouseListener;
import dbseer.gui.user.DBSeerDataSet;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

/**
 * Created by dyoon on 5/3/15.
 */
public class DBSeerPredictionInformationPanel extends JPanel
{
	private DBSeerPredictionInformationChartPanel chartPanel;
	private InformationChartMouseListener informationChartMouseListener;
	private JLabel selectLabel = new JLabel("Please select a train config.");

	private boolean firstDataset = false;

	public DBSeerPredictionInformationPanel(InformationChartMouseListener informationChartMouseListener)
	{
		this.informationChartMouseListener = informationChartMouseListener;
	}

	public void initialize()
	{
		this.setBorder(BorderFactory.createTitledBorder("Statistics of the selected train config"));
		this.setLayout(new MigLayout("align 50% 50%"));
		this.add(selectLabel);
	}

	public void setDataset(DBSeerDataSet dataset)
	{
		if (!firstDataset)
		{
			this.remove(selectLabel);
			this.setLayout(new MigLayout("fill, ins 0","[grow,fill]"));
			chartPanel = new DBSeerPredictionInformationChartPanel(null);
			chartPanel.addChartMouseListener(informationChartMouseListener);
			informationChartMouseListener.setChartPanel(chartPanel);
			chartPanel.setPreferredSize(new Dimension(640, 300));
			this.add(chartPanel, "grow");
			firstDataset = true;
		}
		chartPanel.renewDataset(dataset);
	}
}
