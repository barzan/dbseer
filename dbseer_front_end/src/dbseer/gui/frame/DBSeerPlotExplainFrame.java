package dbseer.gui.frame;

import dbseer.gui.DBSeerGUI;
import dbseer.gui.panel.DBSeerExplainChartPanel;
import matlabcontrol.MatlabProxy;
import net.miginfocom.swing.MigLayout;
import org.jfree.chart.JFreeChart;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

/**
 * Created by dyoon on 2014. 6. 10..
 */
public class DBSeerPlotExplainFrame extends JFrame implements ActionListener
{
	private JFreeChart chart;
	private JTextArea testLog = new JTextArea(20, 50);
	private JButton explainButton;
	private DBSeerExplainChartPanel chartPanel;

	public DBSeerPlotExplainFrame(JFreeChart chart)
	{
		this.setTitle("Explain");
		this.chart = chart;
		initializeGUI();
	}

	private void initializeGUI()
	{
		this.setLayout(new MigLayout("fill"));
		chartPanel = new DBSeerExplainChartPanel(chart, testLog);
		this.add(chartPanel, "grow, wrap");

		DefaultCaret caret = (DefaultCaret)testLog.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

		testLog.setEditable(false);
		JScrollPane logPane = new JScrollPane(testLog, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		logPane.setViewportView(testLog);
		logPane.setAutoscrolls(true);
		logPane.setBorder(BorderFactory.createTitledBorder("Explain Console"));
		this.add(logPane, "grow, wrap");
	}

	@Override
	public void actionPerformed(ActionEvent actionEvent)
	{
		if (actionEvent.getSource() == this.explainButton)
		{
			final ArrayList<Double> outlier = chartPanel.getOutlierRegion();
			testLog.append("Determining metrics for possible explanation of outliers...\n");

			SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>()
			{
				private Object[] result;

				@Override
				protected void done()
				{
					for (Object obj : result)
					{
						String[] columns = (String[])obj;
						for (String column : columns)
						{
							testLog.append(column + "\n");
						}
					}
				}

				@Override
				protected Void doInBackground() throws Exception
				{
					MatlabProxy proxy = DBSeerGUI.proxy;

					String outlierVar = "outlier = [";

					for (int i = 0; i < outlier.size(); ++i)
					{
						outlierVar += outlier.get(i).intValue();
						if (i == outlier.size() - 1)
						{
							outlierVar += "];";
						}
						else
						{
							outlierVar += " ";
						}
					}

					proxy.eval(outlierVar);
					result = proxy.returningEval("explainPrototype(plotter.mv, outlier)", 1);

					return null;
				}
			};

			worker.execute();
		}
	}
}
