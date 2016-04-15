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

package dbseer.gui.chart;

import dbseer.gui.DBSeerExceptionHandler;
import dbseer.gui.DBSeerGUI;
import dbseer.comp.PredictionCenter;
import dbseer.gui.panel.DBSeerPlotControlPanel;
import dbseer.gui.user.DBSeerDataSet;
import dbseer.stat.MatlabRunner;
import dbseer.stat.OctaveRunner;
import dbseer.stat.StatisticalPackageRunner;
import dk.ange.octave.type.OctaveCell;
import dk.ange.octave.type.OctaveDouble;
import dk.ange.octave.type.OctaveObject;
import dk.ange.octave.type.OctaveString;
import matlabcontrol.MatlabInvocationException;
import matlabcontrol.MatlabProxy;
import matlabcontrol.extensions.MatlabNumericArray;
import matlabcontrol.extensions.MatlabTypeConverter;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.PieSectionLabelGenerator;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.AttributedString;
import java.text.DecimalFormat;

/**
 * Created by dyoon on 2014. 5. 25..
 */
public class DBSeerChartFactory
{
	public static final int STYLE_LINE = 1;
	public static final int STYLE_DASH = 2;
	public static final int STYLE_DOT = 3;

	public static double[] timestamp;

	public static XYSeriesCollection getXYSeriesCollection(String chartName, DBSeerDataSet dataset) throws Exception
	{
		StatisticalPackageRunner runner = DBSeerGUI.runner;

		runner.eval("[title legends Xdata Ydata Xlabel Ylabel timestamp] = plotter.plot" + chartName + ";");

		String title = runner.getVariableString("title");
		Object[] legends = (Object[])runner.getVariableCell("legends");
		Object[] xCellArray = (Object[])runner.getVariableCell("Xdata");
		Object[] yCellArray = (Object[])runner.getVariableCell("Ydata");
		String xLabel = runner.getVariableString("Xlabel");
		String yLabel = runner.getVariableString("Ylabel");

		timestamp = runner.getVariableDouble("timestamp");

		XYSeriesCollection XYdataSet = new XYSeriesCollection();

		int numLegends = legends.length;
		int numXCellArray = xCellArray.length;
		int numYCellArray = yCellArray.length;
		int dataCount = 0;

		if (numXCellArray != numYCellArray)
		{
			JOptionPane.showMessageDialog(null, "The number of X dataset and Y dataset does not match.",
					"The number of X dataset and Y dataset does not match.", JOptionPane.ERROR_MESSAGE);
			System.out.println(numXCellArray + " : " + numYCellArray);
			return null;
		}

		java.util.List<String> transactionNames = dataset.getTransactionTypeNames();

		for (int i = 0; i < numLegends; ++i)
		{
			String legend = (String)legends[i];
			for (int j = 0; j < transactionNames.size(); ++j)
			{
				if (legend.contains("Type " + (j+1)))
				{
					legends[i] = legend.replace("Type " + (j+1), transactionNames.get(j));
					break;
				}
			}
		}

		for (int i = 0; i < numYCellArray; ++i)
		{
			double[] xArray = (double[])xCellArray[i];
			int row = 0, col = 0;
			int xLength = 0;

			runner.eval("yArraySize = size(Ydata{" + (i+1) + "});");
			runner.eval("yArray = Ydata{" + (i+1) + "};");
			double[] yArraySize = runner.getVariableDouble("yArraySize");
			double[] yArray = runner.getVariableDouble("yArray");

			xLength = xArray.length;
			row = (int)yArraySize[0];
			col = (int)yArraySize[1];

			for (int c = 0; c < col; ++c)
			{
				XYSeries series;
				String legend = "";
				int legendIdx = (dataCount >= numLegends) ? numLegends - 1 : dataCount;
				if (legendIdx >= 0)
				{
					legend = (String)legends[legendIdx];
				}
				if (numLegends == 0)
				{
					series = new XYSeries("Data " + dataCount+1);
				}
				else if (dataCount >= numLegends)
				{
					series = new XYSeries(legend + (dataCount+1));
				}
				else
				{
					series = new XYSeries(legend);
				}

				for (int r = 0; r < row; ++r)
				{
					int xRow = (r >= xLength) ? xLength - 1 : r;
					double yValue = yArray[r+c*row];
					// remove negatives
					if (yValue < 0)
					{
						yValue = 0;
					}
					series.add(xArray[xRow], yValue);
				}
				XYdataSet.addSeries(series);
				++dataCount;
			}
		}

		return XYdataSet;
	}

	public static DefaultPieDataset getPieDataset(String chartName, DBSeerDataSet dataset) throws Exception
	{
		StatisticalPackageRunner runner = DBSeerGUI.runner;

		runner.eval("[title legends Xdata Ydata Xlabel Ylabel timestamp] = plotter.plot" + chartName + ";");

		String title = runner.getVariableString("title");
		Object[] legends = (Object[])runner.getVariableCell("legends");
		Object[] xCellArray = (Object[])runner.getVariableCell("Xdata");
		Object[] yCellArray = (Object[])runner.getVariableCell("Ydata");
		String xLabel = runner.getVariableString("Xlabel");
		String yLabel = runner.getVariableString("Ylabel");
		timestamp = runner.getVariableDouble("timestamp");

		DefaultPieDataset pieDataSet = new DefaultPieDataset();

		int numLegends = legends.length;
		int numXCellArray = xCellArray.length;
		int numYCellArray = yCellArray.length;
		int dataCount = 0;

		if (numXCellArray != numYCellArray)
		{
			JOptionPane.showMessageDialog(null, "The number of X dataset and Y dataset does not match.",
					"The number of X dataset and Y dataset does not match.", JOptionPane.ERROR_MESSAGE);
			return null;
		}

		final java.util.List<String> transactionTypeNames = dataset.getTransactionTypeNames();

		for (int i = 0; i < numYCellArray; ++i)
		{
			double[] xArray = (double[])xCellArray[i];
			runner.eval("yArraySize = size(Ydata{" + (i+1) + "});");
			runner.eval("yArray = Ydata{" + (i+1) + "};");
			double[] yArraySize = runner.getVariableDouble("yArraySize");
			double[] yArray = runner.getVariableDouble("yArray");

			int xLength = xArray.length;
			int row = (int)yArraySize[0];
			int col = (int)yArraySize[1];

			for (int c = 0; c < col; ++c)
			{
				if (c < transactionTypeNames.size())
				{
					String name = transactionTypeNames.get(c);
					if (!name.isEmpty())
					{
						pieDataSet.setValue(name, yArray[c]);
					}
					else
					{
						pieDataSet.setValue("Transaction Type " + (c+1), yArray[c]);
					}
				}
			}
		}

		return pieDataSet;
	}

	public static JFreeChart createXYLineChart(String chartName, DBSeerDataSet dataset) throws Exception
	{
		StatisticalPackageRunner runner = DBSeerGUI.runner;

		runner.eval("[title legends Xdata Ydata Xlabel Ylabel timestamp] = plotter.plot" + chartName + ";");

		String title = runner.getVariableString("title");
		Object[] legends = (Object[])runner.getVariableCell("legends");
		Object[] xCellArray = (Object[])runner.getVariableCell("Xdata");
		Object[] yCellArray = (Object[])runner.getVariableCell("Ydata");
		String xLabel = runner.getVariableString("Xlabel");
		String yLabel = runner.getVariableString("Ylabel");

		timestamp = runner.getVariableDouble("timestamp");

		XYSeriesCollection XYdataSet = new XYSeriesCollection();

		int numLegends = legends.length;
		int numXCellArray = xCellArray.length;
		int numYCellArray = yCellArray.length;
		int dataCount = 0;

		if (numXCellArray != numYCellArray)
		{
			JOptionPane.showMessageDialog(null, "The number of X dataset and Y dataset does not match.",
					"The number of X dataset and Y dataset does not match.", JOptionPane.ERROR_MESSAGE);
			System.out.println(numXCellArray + " : " + numYCellArray);
			return null;
		}

		java.util.List<String> transactionNames = dataset.getTransactionTypeNames();

		for (int i = 0; i < numLegends; ++i)
		{
			String legend = (String)legends[i];
			for (int j = 0; j < transactionNames.size(); ++j)
			{
				if (legend.contains("Type " + (j+1)))
				{
					legends[i] = legend.replace("Type " + (j+1), transactionNames.get(j));
					break;
				}
			}
		}

		for (int i = 0; i < numYCellArray; ++i)
		{
			double[] xArray = (double[])xCellArray[i];
			int row = 0, col = 0;
			int xLength = 0;

			runner.eval("yArraySize = size(Ydata{" + (i+1) + "});");
			runner.eval("yArray = Ydata{" + (i+1) + "};");
			double[] yArraySize = runner.getVariableDouble("yArraySize");
			double[] yArray = runner.getVariableDouble("yArray");

			xLength = xArray.length;
			row = (int)yArraySize[0];
			col = (int)yArraySize[1];

			for (int c = 0; c < col; ++c)
			{
				XYSeries series;
				String legend = "";
				int legendIdx = (dataCount >= numLegends) ? numLegends - 1 : dataCount;
				if (legendIdx >= 0)
				{
					legend = (String)legends[legendIdx];
				}
				if (numLegends == 0)
				{
					series = new XYSeries("Data " + dataCount+1);
				}
				else if (dataCount >= numLegends)
				{
					series = new XYSeries(legend + (dataCount+1));
				}
				else
				{
					series = new XYSeries(legend);
				}

				for (int r = 0; r < row; ++r)
				{
					int xRow = (r >= xLength) ? xLength - 1 : r;
					double yValue = yArray[r+c*row];
					// remove negatives
					if (yValue < 0)
					{
						yValue = 0;
					}
					series.add(xArray[xRow], yValue);
				}
				XYdataSet.addSeries(series);
				++dataCount;
			}
		}

		JFreeChart chart = ChartFactory.createXYLineChart(title, xLabel, yLabel, XYdataSet);
		boolean isTransactionSampleChart = false;
		for (String name : DBSeerGUI.transactionSampleCharts)
		{
			if (name.equals(chartName))
			{
				isTransactionSampleChart = true;
				break;
			}
		}

		// Renderer to highlight selected normal or outlier points.
		if (isTransactionSampleChart)
		{
			chart.getXYPlot().setRenderer(new DBSeerXYLineAndShapeRenderer(timestamp, dataset));
		}
		else
		{
			chart.getXYPlot().setRenderer(new DBSeerXYLineAndShapeRenderer());

		}

		chart.getXYPlot().getDomainAxis().setUpperMargin(0);

		return chart;
	}

	public static JFreeChart createPieChart(String chartName, DBSeerDataSet dataset) throws Exception
	{
		StatisticalPackageRunner runner = DBSeerGUI.runner;

		runner.eval("[title legends Xdata Ydata Xlabel Ylabel timestamp] = plotter.plot" + chartName + ";");

		String title = runner.getVariableString("title");
		Object[] legends = (Object[])runner.getVariableCell("legends");
		Object[] xCellArray = (Object[])runner.getVariableCell("Xdata");
		Object[] yCellArray = (Object[])runner.getVariableCell("Ydata");
		String xLabel = runner.getVariableString("Xlabel");
		String yLabel = runner.getVariableString("Ylabel");
		timestamp = runner.getVariableDouble("timestamp");

		DefaultPieDataset pieDataSet = new DefaultPieDataset();

		int numLegends = legends.length;
		int numXCellArray = xCellArray.length;
		int numYCellArray = yCellArray.length;
		int dataCount = 0;

		if (numXCellArray != numYCellArray)
		{
			JOptionPane.showMessageDialog(null, "The number of X dataset and Y dataset does not match.",
					"The number of X dataset and Y dataset does not match.", JOptionPane.ERROR_MESSAGE);
			return null;
		}

		final java.util.List<String> transactionTypeNames = dataset.getTransactionTypeNames();

		for (int i = 0; i < numYCellArray; ++i)
		{
			double[] xArray = (double[])xCellArray[i];
			runner.eval("yArraySize = size(Ydata{" + (i+1) + "});");
			runner.eval("yArray = Ydata{" + (i+1) + "};");
			double[] yArraySize = runner.getVariableDouble("yArraySize");
			double[] yArray = runner.getVariableDouble("yArray");

			int xLength = xArray.length;
			int row = (int)yArraySize[0];
			int col = (int)yArraySize[1];

			for (int c = 0; c < col; ++c)
			{
				if (c < transactionTypeNames.size())
				{
					String name = transactionTypeNames.get(c);
					if (!name.isEmpty())
					{
//							pieDataSet.setValue(name, new Double(yArray.getRealValue(0, c)));
						pieDataSet.setValue(name, yArray[c]);
					}
					else
					{
//							pieDataSet.setValue("Transaction Type " + (c+1), yArray.getRealValue(0, c));
						pieDataSet.setValue("Transaction Type " + (c+1), yArray[c]);
					}
				}
			}
		}

		JFreeChart chart = ChartFactory.createPieChart(title, pieDataSet, true, true, false);
		PiePlot plot = (PiePlot)chart.getPlot();
		plot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0}: {1} ({2})", new DecimalFormat("0"),
				new DecimalFormat("0%")));
		plot.setLegendLabelGenerator(new PieSectionLabelGenerator()
		{
			@Override
			public String generateSectionLabel(PieDataset pieDataset, Comparable comparable)
			{
				return (String)comparable;
			}

			@Override
			public AttributedString generateAttributedSectionLabel(PieDataset pieDataset, Comparable comparable)
			{
				return null;
			}
		});
		return chart;
	}

	public static JFreeChart createXYLinePredictionChart(PredictionCenter center) throws Exception
	{
		StatisticalPackageRunner runner = DBSeerGUI.runner;

		String title = runner.getVariableString("title");
		Object[] legends = (Object[])runner.getVariableCell("legends");
		Object[] xCellArray = (Object[])runner.getVariableCell("Xdata");
		Object[] yCellArray = (Object[])runner.getVariableCell("Ydata");
		String xLabel = runner.getVariableString("Xlabel");
		String yLabel = runner.getVariableString("Ylabel");

		XYSeriesCollection dataSet = new XYSeriesCollection();

		int numLegends = legends.length;
		int numXCellArray = xCellArray.length;
		int numYCellArray = yCellArray.length;
		int dataCount = 0;

		if (numXCellArray != numYCellArray)
		{
			JOptionPane.showMessageDialog(null, "The number of X dataset and Y dataset does not match.",
					"The number of X dataset and Y dataset does not match.", JOptionPane.ERROR_MESSAGE);
			System.out.println(numXCellArray + " : " + numYCellArray);
			return null;
		}

		final java.util.List<String> transactionNames = center.getTrainConfig().getDataset(0).getTransactionTypeNames();
		for (int i = 0; i < numLegends; ++i)
		{
			String legend = (String)legends[i];
			for (int j = 0; j < transactionNames.size(); ++j)
			{
				if (legend.contains("Type " + (j+1)))
				{
					legends[i] = legend.replace("Type " + (j+1), transactionNames.get(j));
					break;
				}
			}
		}
		for (int j = 0; j < transactionNames.size(); ++j)
		{
			if (xLabel.contains("Type " + (j+1)))
			{
				xLabel = xLabel.replace("Type " + (j+1), transactionNames.get(j));
				break;
			}
		}
		for (int j = 0; j < transactionNames.size(); ++j)
		{
			if (yLabel.contains("Type " + (j+1)))
			{
				yLabel = yLabel.replace("Type " + (j+1), transactionNames.get(j));
				break;
			}
		}

		for (int i = 0; i < numYCellArray; ++i)
		{
			double[] xArray = (double[])xCellArray[i];
			runner.eval("yArraySize = size(Ydata{" + (i+1) + "});");
			runner.eval("yArray = Ydata{" + (i+1) + "};");
			double[] yArraySize = runner.getVariableDouble("yArraySize");
			double[] yArray = runner.getVariableDouble("yArray");

			int xLength = xArray.length;
			int row = (int)yArraySize[0];
			int col = (int)yArraySize[1];

			for (int c = 0; c < col; ++c)
			{
				XYSeries series;
				int legendIdx = (dataCount >= numLegends) ? numLegends - 1 : dataCount;
				String legend = (String)legends[legendIdx];
				if (numLegends == 0)
				{
					series = new XYSeries("Data " + dataCount+1);
				}
				else if (dataCount >= numLegends)
				{
					series = new XYSeries(legend + (dataCount+1));
				}
				else
				{
					series = new XYSeries(legend);
				}

				for (int r = 0; r < row; ++r)
				{
					int xRow = (r >= xLength) ? xLength - 1 : r;
					double yValue = yArray[r+c*row];
					// remove negatives & NaN & infs.
					if (yValue < 0 || yValue == Double.NaN ||
							yValue == Double.POSITIVE_INFINITY || yValue == Double.NEGATIVE_INFINITY)
					{
						yValue = 0.0;
					}
					series.add(xArray[xRow], yValue);
				}
				dataSet.addSeries(series);
				++dataCount;
			}
		}

		JFreeChart chart = ChartFactory.createXYLineChart(title, xLabel, yLabel, dataSet);

		// change 'predicted' data to have dotted lines.
		BasicStroke dashStroke = toStroke(STYLE_DASH);
		BasicStroke dotStroke = toStroke(STYLE_DOT);
		BasicStroke lineStroke = toStroke(STYLE_LINE);
		for (int i = 0; i < dataSet.getSeriesCount(); ++i)
		{
			String legend = (String)dataSet.getSeriesKey(i);
			XYPlot plot = chart.getXYPlot();
			XYItemRenderer renderer = plot.getRenderer();
			if (legend.contains("predicted") || legend.contains("Predicted"))
			{
				renderer.setSeriesStroke(i, dotStroke);
			}
			else
			{
				renderer.setSeriesStroke(i, lineStroke);
			}
		}

		return chart;
	}

	public static JFreeChart createPredictionBarChart(PredictionCenter center) throws Exception
	{
		StatisticalPackageRunner runner = DBSeerGUI.runner;

		String title = runner.getVariableString("title");
		Object[] legends = (Object[])runner.getVariableCell("legends");
		Object[] xCellArray = (Object[])runner.getVariableCell("Xdata");
		Object[] yCellArray = (Object[])runner.getVariableCell("Ydata");
		String xLabel = runner.getVariableString("Xlabel");
		String yLabel = runner.getVariableString("Ylabel");

		DefaultCategoryDataset dataset = new DefaultCategoryDataset();

		int numLegends = legends.length;
		int numXCellArray = xCellArray.length;
		int numYCellArray = yCellArray.length;
		int dataCount = 0;

		final java.util.List<String> transactionNames = center.getTrainConfig().getDataset(0).getTransactionTypeNames();
		for (int i = 0; i < numLegends; ++i)
		{
			String legend = (String)legends[i];
			for (int j = 0; j < transactionNames.size(); ++j)
			{
				if (legend.contains("Type " + (j + 1)))
				{
					legends[i] = legend.replace("Type " + (j + 1), transactionNames.get(j));
					break;
				}
			}
		}
		for (int j = 0; j < transactionNames.size(); ++j)
		{
			if (xLabel.contains("Type " + (j + 1)))
			{
				xLabel = xLabel.replace("Type " + (j + 1), transactionNames.get(j));
				break;
			}
		}
		for (int j = 0; j < transactionNames.size(); ++j)
		{
			if (yLabel.contains("Type " + (j + 1)))
			{
				yLabel = yLabel.replace("Type " + (j + 1), transactionNames.get(j));
				break;
			}
		}

		for (int i = 0; i < numYCellArray; ++i)
		{
			runner.eval("yArraySize = size(Ydata{" + (i+1) + "});");
			runner.eval("yArray = Ydata{" + (i+1) + "};");
			double[] yArraySize = runner.getVariableDouble("yArraySize");
			double[] yArray = runner.getVariableDouble("yArray");

			int row = (int)yArraySize[0];
			int col = (int)yArraySize[1];

			for (int c = 0; c < col; ++c)
			{
				String category = "";
				int legendIdx = (dataCount >= numLegends) ? numLegends - 1 : dataCount;
				String legend = (String)legends[legendIdx];
				if (numLegends == 0)
				{
					category = "Data " + dataCount + 1;
				}
				else if (dataCount >= numLegends)
				{
					category = legend + (dataCount + 1);
				}
				else
				{
					category = legend;
				}

				for (int r = 0; r < row; ++r)
				{
					double yValue = yArray[r+c*row];
					// remove negatives.
					if (yValue < 0 || yValue == Double.NaN ||
							yValue == Double.POSITIVE_INFINITY || yValue == Double.NEGATIVE_INFINITY)
					{
						yValue = 0.0;
					}

					dataset.addValue(yValue, category, "");
				}
				++dataCount;
			}
		}

		JFreeChart chart = ChartFactory.createBarChart(title, xLabel, yLabel, dataset);

		return chart;
	}

	public static JTable createErrorTable(PredictionCenter center) throws Exception
	{
		JTable errorTable = null;
		DefaultTableModel errorTableModel = null;
		StatisticalPackageRunner runner = DBSeerGUI.runner;

		Object[] maeList = (Object[])runner.getVariableCell("meanAbsError");
		Object[] mreList = (Object[])runner.getVariableCell("meanRelError");
		Object[] headers = (Object[])runner.getVariableCell("errorHeader");

		if (maeList.length > 0 || mreList.length > 0)
		{
			errorTableModel = new DefaultTableModel()
			{
				@Override
				public boolean isCellEditable(int row, int column)
				{
					return false;
				}
			};
			//errorTable = new JTable();
			errorTableModel.addColumn(null, new String[]{"", "MAE", "MRE"}); // first empty column

			final java.util.List<String> transactionNames = center.getTrainConfig().getDataset(0).getTransactionTypeNames();
			for (int i = 0; i < maeList.length; ++i)
			{
				Object maeObj = maeList[i];
				Object mreObj = mreList[i];

				double[] mae = (double[])maeObj;
				double[] mre = (double[])mreObj;

				String header = (String)headers[i];
				for (int j = 0; j < transactionNames.size(); ++j)
				{
					if (header.contains("Type " + (j+1)))
					{
						headers[i] = header.replace("Type " + (j+1), transactionNames.get(j));
						break;
					}
				}

				errorTableModel.addColumn(null, new Object[]{headers[i], String.format("%.3f", mae[0]), String.format("%.3f", mre[0])});
			}
			errorTable = new JTable(errorTableModel);
		}

		return errorTable;
	}

	public static XYSeriesCollection getCustomXYSeriesCollection(String xAxisName, String yAxisName) throws Exception
	{
		StatisticalPackageRunner runner = DBSeerGUI.runner;

		runner.eval("[Xdata Ydata] = plotter.plotCustom('" +
				DBSeerPlotControlPanel.axisMap.get(xAxisName) +
				"', '" +
				DBSeerPlotControlPanel.axisMap.get(yAxisName) + "');");

		Object[] xCellArray = (Object[])runner.getVariableCell("Xdata");
		Object[] yCellArray = (Object[])runner.getVariableCell("Ydata");

		XYSeriesCollection dataSet = new XYSeriesCollection();

		int numXCellArray = xCellArray.length;
		int numYCellArray = yCellArray.length;

		if (numXCellArray != numYCellArray)
		{
			JOptionPane.showMessageDialog(null, "The number of X dataset and Y dataset does not match.",
					"The number of X dataset and Y dataset does not match.", JOptionPane.ERROR_MESSAGE);
			System.out.println(numXCellArray + " : " + numYCellArray);
			return null;
		}

		for (int i = 0; i < numYCellArray; ++i)
		{
			double[] xArray = (double[])xCellArray[i];

			runner.eval("yArraySize = size(Ydata{" + (i+1) + "});");
			runner.eval("yArray = Ydata{" + (i+1) + "};");
			double[] yArraySize = runner.getVariableDouble("yArraySize");
			double[] yArray = runner.getVariableDouble("yArray");

			int xLength = xArray.length;
			int row = (int)yArraySize[0];
			int col = (int)yArraySize[1];

			for (int c = 0; c < col; ++c)
			{
				XYSeries series = new XYSeries(yAxisName);

				for (int r = 0; r < row; ++r)
				{
					int xRow = (r >= xLength) ? xLength - 1 : r;
					series.add(xArray[xRow], yArray[r+c*row]);
				}
				dataSet.addSeries(series);
			}
		}

		return dataSet;
	}

	public static JFreeChart createCustomXYLineChart(String xAxisName, String yAxisName) throws Exception
	{
		StatisticalPackageRunner runner = DBSeerGUI.runner;

		runner.eval("[Xdata Ydata] = plotter.plotCustom('" +
				DBSeerPlotControlPanel.axisMap.get(xAxisName) +
				"', '" +
				DBSeerPlotControlPanel.axisMap.get(yAxisName) + "');");

		String title = "Custom Plot";

		Object[] xCellArray = (Object[])runner.getVariableCell("Xdata");
		Object[] yCellArray = (Object[])runner.getVariableCell("Ydata");
		String xLabel = xAxisName;
		String yLabel = yAxisName;

		XYSeriesCollection dataSet = new XYSeriesCollection();

		int numXCellArray = xCellArray.length;
		int numYCellArray = yCellArray.length;
		int dataCount = 0;

		if (numXCellArray != numYCellArray)
		{
			JOptionPane.showMessageDialog(null, "The number of X dataset and Y dataset does not match.",
					"The number of X dataset and Y dataset does not match.", JOptionPane.ERROR_MESSAGE);
			System.out.println(numXCellArray + " : " + numYCellArray);
			return null;
		}

		for (int i = 0; i < numYCellArray; ++i)
		{
			double[] xArray = (double[])xCellArray[i];

			runner.eval("yArraySize = size(Ydata{" + (i+1) + "});");
			runner.eval("yArray = Ydata{" + (i+1) + "};");
			double[] yArraySize = runner.getVariableDouble("yArraySize");
			double[] yArray = runner.getVariableDouble("yArray");

			int xLength = xArray.length;
			int row = (int)yArraySize[0];
			int col = (int)yArraySize[1];

			for (int c = 0; c < col; ++c)
			{
				XYSeries series = new XYSeries(yAxisName + " " + (c+1));

				for (int r = 0; r < row; ++r)
				{
					int xRow = (r >= xLength) ? xLength - 1 : r;
					series.add(xArray[xRow], yArray[r+c*row]);
				}
				dataSet.addSeries(series);
				++dataCount;
			}
		}

		JFreeChart chart = ChartFactory.createXYLineChart(title, xLabel, yLabel, dataSet);
		for (int i=0; i<dataCount; ++i)
		{
			// Renderer to highlight selected normal or outlier points.
			chart.getXYPlot().setRenderer(i, new DBSeerXYLineAndShapeRenderer());
		}

		return chart;
	}

	private static BasicStroke toStroke(int style)
	{
		BasicStroke result = null;

		float lineWidth = 1.5f;
		float dash[] = {5.0f};
		float dot[] = {lineWidth};

		if (style == STYLE_LINE)
		{
			result = new BasicStroke(lineWidth);
		}
		else if (style == STYLE_DASH)
		{
			result = new BasicStroke(lineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f);
		}
		else if (style == STYLE_DOT)
		{
			result = new BasicStroke(lineWidth * 2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 2.0f, dot, 0.0f);
		}
		return result;
	}
}
