package dbseer.gui.actions;

import dbseer.gui.DBSeerGUI;
import matlabcontrol.MatlabProxy;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by dyoon on 2014. 8. 18..
 */
public class ExplainChartAction extends AbstractAction
{
	private JTextArea console;
	private String name;
	private int type;
	private ArrayList<Double> outlierRegion;

	public ExplainChartAction(String name, int type, JTextArea console, ArrayList<Double> outlierRegion)
	{
		super(name);
		this.name = name;
		this.console = console;
		this.type = type;
		this.outlierRegion = outlierRegion;
	}

	@Override
	public void actionPerformed(ActionEvent actionEvent)
	{
		if (outlierRegion.isEmpty())
		{
			return;
		}
		console.append("Determining metrics for possible explanation of outliers...\n");

		SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>()
		{
			private Object[] result;

			@Override
			protected void done()
			{
				if (result == null)
				{
					console.append("Columns for possible explanation not found.\n");
					return;
				}

				String[] columns = (String[])result[0];
				double[] meanDifference = (double[])result[1];

				// process possible explanation columns.
				Map<String, Double> cpuMap = new HashMap<String, Double>();
				Map<String, Double> osMap = new HashMap<String, Double>();
				Map<String, Double> dbmsMap = new HashMap<String, Double>();

				for (int i = 0; i < columns.length; ++i)
				{
					if (columns[i].startsWith("cpu"))
					{
						cpuMap.put(columns[i], meanDifference[i]);
					}
					else if (columns[i].startsWith("os"))
					{
						String column = columns[i].substring(2);
						for (int j = 1; j < column.length(); ++j)
						{
							char letter = column.charAt(j);
							if (Character.isUpperCase(letter))
							{
								column = new StringBuilder(column).replace(j, j+1, " " + letter).toString();
								++j;
								while (j < column.length() && Character.isUpperCase(column.charAt(j)))
								{
									++j;
								}
							}
						}
						osMap.put(column, meanDifference[i]);
					}
					else if (columns[i].startsWith("dbms"))
					{
						String column = columns[i].substring(4);
						for (int j = 1; j < column.length(); ++j)
						{
							char letter = column.charAt(j);
							if (Character.isUpperCase(letter))
							{
								column = new StringBuilder(column).replace(j, j+1, " " + letter).toString();
								++j;
								while (j < column.length() && Character.isUpperCase(column.charAt(j)))
								{
									++j;
								}
							}
						}
						dbmsMap.put(column, meanDifference[i]);
					}
				}


				if (cpuMap.size() > 0)
				{
					console.append("\n");
					console.append("There are possible explanations found in CPU statistics:\n");
					for (Map.Entry<String, Double> entry : cpuMap.entrySet())
					{
						String column = entry.getKey();
						double meanDiff = entry.getValue().doubleValue() * 100; // %
						console.append(" '" + column + "'");
						if (meanDiff > 0)
						{
							console.append(" is greater than expected:\n");
						}
						else if (meanDiff < 0)
						{
							console.append(" is less than expected:\n");
						}
						console.append("    The outlier region has changed its total average by " +
								String.format("%.2f", meanDiff) + "% from the average in normal region.\n");
					}
				}

				if (osMap.size() > 0)
				{
					console.append("\n");
					console.append("There are possible explanations found in OS statistics:\n");
					for (Map.Entry<String, Double> entry : osMap.entrySet())
					{
						String column = entry.getKey();
						double meanDiff = entry.getValue().doubleValue() * 100; // %
						console.append(" '" + column + "'");
						if (meanDiff > 0)
						{
							console.append(" is greater than expected:\n");
						}
						else if (meanDiff < 0)
						{
							console.append(" is less than expected:\n");
						}
						console.append("    The outlier region has changed its total average by " +
								String.format("%.2f", meanDiff) + "% from the average in normal region.\n");
					}
				}

				if (dbmsMap.size() > 0)
				{
					console.append("\n");
					console.append("There are possible explanations found in DBMS statistics:\n");
					for (Map.Entry<String, Double> entry : dbmsMap.entrySet())
					{
						String column = entry.getKey();
						double meanDiff = entry.getValue().doubleValue() * 100; // %
						console.append(" '" + column + "'");
						if (meanDiff > 0)
						{
							console.append(" is greater than expected:\n");
						}
						else if (meanDiff < 0)
						{
							console.append(" is less than expected:\n");
						}
						console.append("    The outlier region has changed its total average by " +
								String.format("%.2f", meanDiff) + "% from the average in normal region.\n");
					}
				}
			}

			@Override
			protected Void doInBackground() throws Exception
			{
				MatlabProxy proxy = DBSeerGUI.proxy;

				String outlierVar = "outlier = [";

				for (int i = 0; i < outlierRegion.size(); ++i)
				{
					outlierVar += outlierRegion.get(i).intValue();
					if (i == outlierRegion.size() - 1)
					{
						outlierVar += "];";
					}
					else
					{
						outlierVar += " ";
					}
				}

				proxy.eval(outlierVar);
				result = proxy.returningEval("explainPrototype(plotter.mv, outlier, " + type + ")", 2);

				return null;
			}
		};

		worker.execute();
	}
}
