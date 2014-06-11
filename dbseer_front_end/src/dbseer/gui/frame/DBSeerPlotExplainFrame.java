package dbseer.gui.frame;

import dbseer.gui.panel.DBSeerExplainChartPanel;
import net.miginfocom.swing.MigLayout;
import org.jfree.chart.JFreeChart;

import javax.swing.*;
import javax.swing.text.DefaultCaret;

/**
 * Created by dyoon on 2014. 6. 10..
 */
public class DBSeerPlotExplainFrame extends JFrame
{
	private JFreeChart chart;
	private JTextArea testLog = new JTextArea(300, 50);
	public DBSeerPlotExplainFrame(JFreeChart chart)
	{
		this.setTitle("Explain");
		this.chart = chart;
		initializeGUI();
	}

	private void initializeGUI()
	{
		this.setLayout(new MigLayout());
		DBSeerExplainChartPanel chartPanel = new DBSeerExplainChartPanel(chart, testLog);
		this.add(chartPanel);

		DefaultCaret caret = (DefaultCaret)testLog.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

		JScrollPane logPane = new JScrollPane(testLog, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		logPane.setViewportView(testLog);
		logPane.setAutoscrolls(true);
		this.add(logPane);
	}

}
