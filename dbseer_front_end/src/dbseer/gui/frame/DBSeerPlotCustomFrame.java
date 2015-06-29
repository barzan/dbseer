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
import net.miginfocom.swing.MigLayout;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import javax.swing.*;

/**
 * Created by dyoon on 14. 11. 20..
 */
public class DBSeerPlotCustomFrame extends JFrame
{
	private String xAxisName;
	private String yAxisName;

	private ChartPanel chartPanel;

	public DBSeerPlotCustomFrame(String xAxisName, String yAxisName)
	{
		this.setTitle("DBSeer Visualization");
		this.xAxisName = xAxisName;
		this.yAxisName = yAxisName;

		initializeGUI();
	}

	private void initializeGUI()
	{
		this.setLayout(new MigLayout("fill"));

		JFreeChart chart = DBSeerChartFactory.createCustomXYLineChart(xAxisName, yAxisName);
		chartPanel = new ChartPanel(chart);

		this.add(chartPanel, "grow");
	}
}
