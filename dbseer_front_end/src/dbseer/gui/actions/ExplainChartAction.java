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

		console.setWrapStyleWord(true);
		console.setLineWrap(true);
	}

	@Override
	public void actionPerformed(ActionEvent actionEvent)
	{
		if (outlierRegion.isEmpty())
		{
			return;
		}
		console.setText("");
		console.append("Determining metrics for possible explanation of outliers...\n");

		final double meanDifferenceThreshold = 0.20;
		final double decisionTreeAccThreshold = 90;
		final int normalizedMeanResultColCount = 4;
		final int decisionTreeResultColCount = 3;

		SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>()
		{
			private Object[] result;
			String outlierVar = "";

			private String getPrintableColumnName(String name)
			{
				if (name.startsWith("os"))
				{
					String column = name.substring(2);
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
					return column;
				}
				else if (name.startsWith("dbms"))
				{
					String column = name.substring(4);
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
					return column;
				}
				return name;
			}

			@Override
			protected void done()
			{
				console.append(outlierVar + "\n\n");
				if (result == null)
				{
					console.append("Columns for possible explanation not found.\n");
					return;
				}

				Object[] results = result;
				Object[] theResult = (Object[])results[0];

				Object[] normalizedMeanResult = (Object[])theResult[0];
				Object[] decisionTreeResult = (Object[])theResult[1];

				console.append("Interesting columns found from statistical analysis in the descending order of " +
						"normalized mean difference between normal and outlier regions are as follows (mean difference" +
						" higher than " + meanDifferenceThreshold + " are shown):\n\n");
				int normalizedRowLength = normalizedMeanResult.length / normalizedMeanResultColCount;
				for (int i = 0; i < normalizedRowLength; ++i)
				{
					String fieldName = (String)normalizedMeanResult[i];
					double meanDifference = ((double[])normalizedMeanResult[i+(2*normalizedRowLength)])[0];
					if (Math.abs(meanDifference) < meanDifferenceThreshold)
					{
						continue;
					}
					console.append(getPrintableColumnName(fieldName) +
							" has the normalized average value in outlier region that is ");
					if (meanDifference >= 0)
					{
						console.append(String.format("%.2f", meanDifference) + " HIGHER than normal.");
					}
					else
					{
						console.append(String.format("%.2f", (-1.0*meanDifference)) + " LOWER than normal.");
					}
					console.append("\n");
				}

				console.append("\nInteresting columns found from decision tree analysis in the descending order of " +
						"DT accuracy on training set for classifying normal and outlier regions are as follows " +
						"(accuracy higher than " + decisionTreeAccThreshold + "% are shown):\n\n");
				int decisionTreeRowLength = decisionTreeResult.length / decisionTreeResultColCount;
				for (int i = 0; i < decisionTreeRowLength; ++i)
				{
					String fieldName = (String)decisionTreeResult[i];
					double trainAccuracy = ((double[])decisionTreeResult[i+decisionTreeRowLength])[0] * 100;
					if (trainAccuracy < decisionTreeAccThreshold)
					{
						continue;
					}
					console.append(getPrintableColumnName(fieldName) +
							" has the DT with accuracy of " + String.format("%.2f", trainAccuracy) + "% classifying " +
							"normal and outlier regions correctly.\n");
				}

//				for (int row = 0; row < rowLength; ++row)
//				{

//					Object[] aRow = normalizedMeanResult[row];
//					String fieldName = (String)aRow[0];
//					double meanDifference = ((Double)aRow[2]).doubleValue();
//
//					console.append(fieldName + " " + meanDifference + "\n");
//				}

				/*
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
				*/
			}

			@Override
			protected Void doInBackground() throws Exception
			{
				MatlabProxy proxy = DBSeerGUI.proxy;

				outlierVar = "outlier = [";

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
				result = proxy.returningEval("explainPrototype2(plotter.mv, outlier, " + type + ")", 1);

				return null;
			}
		};

		worker.execute();
	}
}
