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

import dbseer.gui.DBSeerGUI;
import dbseer.gui.panel.DBSeerExplainChartPanel;
import dbseer.gui.panel.DBSeerExplainControlPanel;
import matlabcontrol.MatlabProxy;
import net.miginfocom.swing.MigLayout;
import org.jfree.chart.JFreeChart;

import javax.swing.*;
import javax.swing.plaf.DimensionUIResource;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

/**
 * Created by dyoon on 2014. 6. 10..
 */
public class DBSeerPlotExplainFrame extends JFrame implements ActionListener
{
	private JFreeChart chart;
	private JTextArea testLog = new JTextArea(10, 50);
	private DBSeerExplainChartPanel chartPanel;
	private DBSeerExplainControlPanel controlPanel;

	public DBSeerPlotExplainFrame(JFreeChart chart)
	{
		this.setTitle("DBSherlock");
		this.chart = chart;
		initializeGUI();
	}

	private void initializeGUI()
	{
		this.setLayout(new MigLayout("fill, ins 5"));
		controlPanel = new DBSeerExplainControlPanel();
		chartPanel = new DBSeerExplainChartPanel(chart, testLog, controlPanel);
//		chartPanel.setPreferredSize(new Dimension(640, 480));
		controlPanel.setChartPanel(chartPanel);
		controlPanel.initialize();
		this.add(chartPanel, "grow");
		this.add(controlPanel, "grow");

//		DefaultCaret caret = (DefaultCaret)testLog.getCaret();
//		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
//
//		testLog.setEditable(false);
//		JScrollPane logPane = new JScrollPane(testLog, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
//				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
//		logPane.setViewportView(testLog);
//		logPane.setAutoscrolls(true);
//		logPane.setBorder(BorderFactory.createTitledBorder("Explanation"));
//		this.add(logPane, "grow");
	}

	@Override
	public void actionPerformed(ActionEvent actionEvent)
	{
		// this is now performed at ExplainChartAction...
		/*
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
					result = proxy.returningEval("explainPrototype2(plotter.mv, outlier)", 1);

					return null;
				}
			};

			worker.execute();
		}
		*/
	}
}
