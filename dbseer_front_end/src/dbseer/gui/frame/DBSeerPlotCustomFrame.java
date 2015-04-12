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
