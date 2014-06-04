package dbseer.gui.frame;

import dbseer.gui.chart.DBSeerChartFactory;
import net.miginfocom.swing.MigLayout;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import javax.swing.*;

/**
 * Created by dyoon on 2014. 5. 25..
 */
public class DBSeerPlotFrame extends JFrame
{
	private String[] chartNames;
	private int numCharts;
	private int numChartInRow;

	public DBSeerPlotFrame(String[] chartNames)
	{
		this.chartNames = chartNames;
		numCharts = chartNames.length;
		numChartInRow = (int)Math.ceil(Math.sqrt(numCharts));
		initializeGUI();
	}

	private void initializeGUI()
	{
		this.setLayout(new MigLayout());
		int count = 0;
		for (String chartName : chartNames)
		{
			JFreeChart chart = DBSeerChartFactory.createXYLineChart(chartName);
			ChartPanel chartPanel = new ChartPanel(chart);
			if (++count == numChartInRow)
			{
				this.add(chartPanel, "wrap");
				count = 0;
			}
			else
			{
				this.add(chartPanel);
			}
		}
	}
}
