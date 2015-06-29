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

import dbseer.gui.DBSeerConstants;
import dbseer.gui.chart.DBSeerChartFactory;
import dbseer.comp.PredictionCenter;
import net.miginfocom.swing.MigLayout;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import javax.swing.*;
import java.awt.*;

/**
 * Created by dyoon on 2014. 6. 9..
 */
public class DBSeerPredictionFrame extends JFrame
{
	private PredictionCenter center;

	public DBSeerPredictionFrame(PredictionCenter center)
	{
		this.center = center;
		initializeGUI();
	}

	public DBSeerPredictionFrame(PredictionCenter center, int chartType)
	{
		this.center = center;
		initializeGUI(chartType);
	}

	private void initializeGUI()
	{
		this.setLayout(new MigLayout("fill"));
		this.setPreferredSize(new Dimension(1024, 768));

		JFreeChart chart = DBSeerChartFactory.createXYLinePredictionChart(center);
		JTable errorTable = DBSeerChartFactory.createErrorTable(center);
		ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new Dimension(1024, 768));
		String title = center.getPredictionDescription();
		if (center.getTestMode() == DBSeerConstants.TEST_MODE_DATASET)
		{
			title += ", Test with Dataset (" + center.getTestDatasetName() + ") ";
		}
		else
		{
			title += ", Test with Mixture & TPS (" + center.getTestMixture() + ") ";
		}
		switch (center.getGroupingType())
		{
			case DBSeerConstants.GROUP_NONE:
				title += "[Group: None] ";
				break;
			case DBSeerConstants.GROUP_REL_DIFF:
				title += "[Group: Rel. diff (" + center.getAllowedRelativeDiff() + ")] ";
				break;
			case DBSeerConstants.GROUP_NUM_CLUSTER:
				title += "[Group: Clustering (" + center.getNumClusters() + ")] ";
				break;
			case DBSeerConstants.GROUP_RANGE:
				title += "[Group: User-specified range] ";
				break;
			default:
				break;
		}
		if (center.getGroupingType() != DBSeerConstants.GROUP_NONE)
		{
			switch (center.getGroupingTarget())
			{
				case DBSeerConstants.GROUP_TARGET_INDIVIDUAL_TRANS_COUNT:
					title += "[Target: Individual transactions]";
					break;
				case DBSeerConstants.GROUP_TARGET_TPS:
					title += "[Target: TPS]";
					break;
				default:
					break;
			}
		}
		this.setTitle(title);
		if (errorTable != null)
		{
			this.add(chartPanel, "grow, wrap");
			this.add(errorTable, "growx");
		}
		else
		{
			this.add(chartPanel, "grow");
		}
	}

	private void initializeGUI(int chartType)
	{
		this.setLayout(new MigLayout("fill"));
		this.setPreferredSize(new Dimension(1024, 768));

		JFreeChart chart = null;
		if (chartType == DBSeerConstants.CHART_XYLINE)
		{
			chart = DBSeerChartFactory.createXYLinePredictionChart(center);
		}
		else if (chartType == DBSeerConstants.CHART_BAR)
		{
			chart = DBSeerChartFactory.createPredictionBarChart(center);
		}

		JTable errorTable = DBSeerChartFactory.createErrorTable(center);
		ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new Dimension(1024, 768));
		String title = center.getPredictionDescription();
		if (center.getTestMode() == DBSeerConstants.TEST_MODE_DATASET)
		{
			title += ", Test with Dataset (" + center.getTestDatasetName() + ") ";
		}
		else
		{
			title += ", Test with Mixture & TPS (" + center.getNormalizedTestMixture() + ") ";
		}
		switch (center.getGroupingType())
		{
			case DBSeerConstants.GROUP_NONE:
				title += "[Group: None] ";
				break;
			case DBSeerConstants.GROUP_REL_DIFF:
				title += "[Group: Rel. diff (" + center.getAllowedRelativeDiff() + ")] ";
				break;
			case DBSeerConstants.GROUP_NUM_CLUSTER:
				title += "[Group: Clustering (" + center.getNumClusters() + ")] ";
				break;
			case DBSeerConstants.GROUP_RANGE:
				title += "[Group: User-specified range] ";
				break;
			default:
				break;
		}
		if (center.getGroupingType() != DBSeerConstants.GROUP_NONE)
		{
			switch (center.getGroupingTarget())
			{
				case DBSeerConstants.GROUP_TARGET_INDIVIDUAL_TRANS_COUNT:
					title += "[Target: Individual transactions]";
					break;
				case DBSeerConstants.GROUP_TARGET_TPS:
					title += "[Target: TPS]";
					break;
				default:
					break;
			}
		}
		this.setTitle(title);
		if (errorTable != null)
		{
			this.add(chartPanel, "grow, wrap");
			this.add(errorTable, "growx");
		}
		else
		{
			this.add(chartPanel, "grow");
		}
	}
}
