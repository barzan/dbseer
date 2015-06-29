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

package dbseer.gui.frame;

import dbseer.gui.chart.DBSeerChartFactory;
import dbseer.gui.panel.DBSeerDatasetListPanel;
import dbseer.gui.panel.DBSeerSelectableChartPanel;
import dbseer.gui.user.DBSeerDataSet;
import net.miginfocom.swing.MigLayout;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import javax.swing.*;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

/**
 * Created by dyoon on 2014. 5. 25..
 */
public class DBSeerPlotPresetFrame extends JFrame
{
	public static ArrayList<DBSeerSelectableChartPanel> chartPanels = new ArrayList<DBSeerSelectableChartPanel>();
	private DBSeerDataSet dataset;
	private String[] chartNames;
	private int numCharts;
	private int numChartInRow;

	public DBSeerPlotPresetFrame(String[] chartNames, DBSeerDataSet dataset)
	{
		this.setTitle("DBSeer Visualization");
		this.chartNames = chartNames;
		this.dataset = dataset;
		numCharts = chartNames.length;
		numChartInRow = (int)Math.ceil(Math.sqrt(numCharts));
		initializeGUI();
	}

	private void initializeGUI()
	{
		chartPanels.clear();
		this.setLayout(new MigLayout("fill"));
		int count = 0;
		for (String chartName : chartNames)
		{
			JFreeChart chart;
			if (chartName.equalsIgnoreCase("TransactionMix"))
			{
				chart = DBSeerChartFactory.createPieChart(chartName, dataset);
			}
			else
			{
				chart = DBSeerChartFactory.createXYLineChart(chartName, dataset);
			}
			double[] timestamp = new double[DBSeerChartFactory.timestamp.length];
			for (int i = 0; i < DBSeerChartFactory.timestamp.length; ++i)
			{
				timestamp[i] = DBSeerChartFactory.timestamp[i];
			}


			DBSeerSelectableChartPanel chartPanel = new DBSeerSelectableChartPanel(chart, dataset, chartName, timestamp);
			chartPanels.add(chartPanel);
			if (++count == numChartInRow)
			{
				this.add(chartPanel, "grow, wrap");
				count = 0;
			}
			else
			{
				this.add(chartPanel, "grow");
			}
		}
	}
}
