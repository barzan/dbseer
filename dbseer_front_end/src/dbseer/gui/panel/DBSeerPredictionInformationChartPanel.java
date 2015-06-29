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

import dbseer.gui.DBSeerExceptionHandler;
import dbseer.gui.DBSeerGUI;
import dbseer.gui.chart.DBSeerChartFactory;
import dbseer.gui.user.DBSeerDataSet;
import dbseer.stat.StatisticalPackageRunner;
import matlabcontrol.MatlabProxy;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.panel.CrosshairOverlay;
import org.jfree.chart.plot.Crosshair;

import java.awt.*;

/**
 * Created by dyoon on 4/29/15.
 */
public class DBSeerPredictionInformationChartPanel extends ChartPanel
{
	private Crosshair verticalCrossHair;

	public DBSeerPredictionInformationChartPanel(JFreeChart chart)
	{
		super(chart);
		verticalCrossHair = new Crosshair(Double.NaN, Color.GRAY, new BasicStroke(0f));
		CrosshairOverlay crosshairOverlay = new CrosshairOverlay();
		crosshairOverlay.addDomainCrosshair(verticalCrossHair);
		this.addOverlay(crosshairOverlay);
	}

	public void renewDataset(DBSeerDataSet dataset)
	{
		StatisticalPackageRunner runner = DBSeerGUI.runner;

		try
		{
			dataset.loadModelVariable();
			runner.eval("plotter = Plotter;");
			runner.eval("plotter.mv = " + dataset.getUniqueModelVariableName() + ";");

			JFreeChart newChart = DBSeerChartFactory.createXYLineChart("ForPrediction", dataset);
			this.setChart(newChart);
		}
		catch (Exception e)
		{
			DBSeerExceptionHandler.handleException(e);
		}
	}
	public Crosshair getVerticalCrossHair()
	{
		return verticalCrossHair;
	}
}
