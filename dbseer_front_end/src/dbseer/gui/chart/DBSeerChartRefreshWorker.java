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

package dbseer.gui.chart;

import dbseer.gui.DBSeerGUI;
import dbseer.gui.user.DBSeerDataSet;
import dbseer.stat.StatisticalPackageRunner;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dyoon on 10/7/15.
 */
public class DBSeerChartRefreshWorker extends SwingWorker<String, DBSeerChart>
{
	private List<DBSeerChart> charts;
	private DBSeerDataSet dataset;
	private volatile boolean run;

	public DBSeerChartRefreshWorker(List<DBSeerChart> charts, DBSeerDataSet dataset)
	{
		this.charts = charts;
		this.dataset = dataset;
		this.run = true;
	}

	public void stop()
	{
		this.run = false;
	}

	@Override
	protected String doInBackground() throws Exception
	{
		long delay;
		long timeSlept;

		while (this.run)
		{
			try
			{
				delay = DBSeerGUI.liveMonitorRefreshRate * 1000;
				timeSlept = 0;
				while (timeSlept < delay)
				{
					timeSlept += 250;
					Thread.sleep(250);
					if (!this.run)
					{
						return null;
					}
				}
				if (!this.run)
				{
					return null;
				}
				StatisticalPackageRunner runner = DBSeerGUI.runner;
				boolean isLoadSuccess = false;
				try
				{
					runner.eval("plotter = Plotter;");
					isLoadSuccess = dataset.loadDataset(true);
					if (isLoadSuccess)
					{
						runner.eval("[mvGrouped mvUngrouped] = load_mv2(" +
								dataset.getUniqueVariableName() + ".header," +
								dataset.getUniqueVariableName() + ".monitor," +
								dataset.getUniqueVariableName() + ".averageLatency," +
								dataset.getUniqueVariableName() + ".percentileLatency," +
								dataset.getUniqueVariableName() + ".transactionCount," +
								dataset.getUniqueVariableName() + ".diffedMonitor," +
								dataset.getUniqueVariableName() + ".statementStat);");
						runner.eval("plotter.mv = mvUngrouped;");
					}
				}
				catch (Exception e)
				{
					JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				}

				if (isLoadSuccess)
				{
					for (DBSeerChart chart : charts)
					{
						if (chart.getName().equalsIgnoreCase("TransactionMix"))
						{
							DefaultPieDataset newDataset = DBSeerChartFactory.getPieDataset(chart.getName(), dataset);
							chart.setPieDataset(newDataset);
							publish(chart);
						}
						else if (chart.getName().equalsIgnoreCase("Custom"))
						{
							XYSeriesCollection newDataset = DBSeerChartFactory.getCustomXYSeriesCollection(chart.getXAxisName(), chart.getYAxisName());
							chart.setXYDataset(newDataset);
							publish(chart);
						}
						else
						{
							XYSeriesCollection newDataset = DBSeerChartFactory.getXYSeriesCollection(chart.getName(), dataset);
							chart.setXYDataset(newDataset);
							publish(chart);
						}
					}
				}
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		return null;
	}

	@Override
	protected void process(List<DBSeerChart> list)
	{
		for (DBSeerChart chart : list)
		{
			if (chart.getName().equalsIgnoreCase("TransactionMix"))
			{
				DefaultPieDataset dataset = chart.getPieDataset();
				PiePlot piePlot = (PiePlot) chart.getChart().getPlot();
				piePlot.setDataset(dataset);
			}
			else
			{
				XYSeriesCollection dataset = chart.getXYDataset();
				chart.getChart().getXYPlot().setDataset(dataset);
			}
		}
	}
}
