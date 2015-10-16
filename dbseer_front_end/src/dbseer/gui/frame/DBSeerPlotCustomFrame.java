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

import dbseer.gui.chart.DBSeerChart;
import dbseer.gui.chart.DBSeerChartFactory;
import dbseer.gui.chart.DBSeerChartRefreshWorker;
import dbseer.gui.user.DBSeerDataSet;
import net.miginfocom.swing.MigLayout;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

/**
 * Created by dyoon on 14. 11. 20..
 */
public class DBSeerPlotCustomFrame extends JFrame
{
	private String xAxisName;
	private String yAxisName;

	private ChartPanel chartPanel;
	private DBSeerDataSet dataset;

	public DBSeerPlotCustomFrame(String xAxisName, String yAxisName, DBSeerDataSet dataset)
	{
		this.setTitle("DBSeer Visualization");
		this.xAxisName = xAxisName;
		this.yAxisName = yAxisName;
		this.dataset = dataset;

		initializeGUI();
	}

	private void initializeGUI()
	{
		this.setLayout(new MigLayout("fill"));

		JFreeChart chart = DBSeerChartFactory.createCustomXYLineChart(xAxisName, yAxisName);
		chartPanel = new ChartPanel(chart);

		this.add(chartPanel, "grow");

		ArrayList<DBSeerChart> charts = new ArrayList<DBSeerChart>();
		DBSeerChart newChart = new DBSeerChart("Custom", chart);
		newChart.setXAxisName(xAxisName);
		newChart.setYAxisName(yAxisName);
		charts.add(newChart);

		// if live dataset, launch the chart refresher.
		if (dataset.getLive())
		{
			final DBSeerChartRefreshWorker refresher = new DBSeerChartRefreshWorker(charts, dataset);
			this.addWindowListener(new WindowAdapter()
			{
				@Override
				public void windowClosed(WindowEvent windowEvent)
				{
					refresher.stop();
					refresher.cancel(false);
					super.windowClosed(windowEvent);
				}
			});

			refresher.execute();
		}
	}
}
