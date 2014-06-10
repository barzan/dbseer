package dbseer.gui.frame;

import dbseer.gui.chart.DBSeerChartFactory;
import net.miginfocom.swing.MigLayout;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import javax.swing.*;

/**
 * Created by dyoon on 2014. 6. 9..
 */
public class DBSeerPredictionFrame extends JFrame
{
	private String[] chartNames;
	private String workload;
	private int numCharts;
	private int numChartInRow;

	public DBSeerPredictionFrame(String[] chartNames, String workload)
	{
		this.chartNames = chartNames;
		this.workload = workload;
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
			JFreeChart chart = DBSeerChartFactory.createXYLinePredictionChart(chartName, workload);
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
