package dbseer.gui.frame;

import dbseer.gui.panel.DBSeerExplainChartPanel;
import net.miginfocom.swing.MigLayout;
import org.jfree.chart.JFreeChart;

import javax.swing.*;

/**
 * Created by dyoon on 2014. 6. 10..
 */
public class DBSeerPlotExplainFrame extends JFrame
{
	private JFreeChart chart;
	public DBSeerPlotExplainFrame(JFreeChart chart)
	{
		this.setTitle("Explain");
		this.chart = chart;
		initializeGUI();
	}

	private void initializeGUI()
	{
		this.setLayout(new MigLayout());
		DBSeerExplainChartPanel chartPanel = new DBSeerExplainChartPanel(chart);
		this.add(chartPanel);
	}

}
