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
