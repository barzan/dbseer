package dbseer.gui.panel;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

/**
 * Created by dyoon on 2014. 6. 10..
 */
public class DBSeerExplainChartPanel extends ChartPanel
{
	public DBSeerExplainChartPanel(JFreeChart chart)
	{
		super(chart);
		this.setMouseWheelEnabled(true);
	}
}
