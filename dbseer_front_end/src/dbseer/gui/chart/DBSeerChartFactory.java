package dbseer.gui.chart;

import dbseer.gui.DBSeerGUI;
import dbseer.comp.PredictionCenter;
import dbseer.gui.panel.DBSeerPlotControlPanel;
import dbseer.gui.user.DBSeerDataSet;
import matlabcontrol.MatlabInvocationException;
import matlabcontrol.MatlabProxy;
import matlabcontrol.extensions.MatlabNumericArray;
import matlabcontrol.extensions.MatlabTypeConverter;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYDataImageAnnotation;
import org.jfree.chart.labels.PieSectionLabelGenerator;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.AttributedString;
import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by dyoon on 2014. 5. 25..
 */
public class DBSeerChartFactory
{
	public static final int STYLE_LINE = 1;
	public static final int STYLE_DASH = 2;
	public static final int STYLE_DOT = 3;

	public static double[] timestamp;

	public static JFreeChart createXYLineChart(String chartName, DBSeerDataSet dataset)
	{
		MatlabProxy proxy = DBSeerGUI.proxy;
		try
		{
			proxy.eval("[title legends Xdata Ydata Xlabel Ylabel timestamp] = plotter.plot" + chartName + ";");

			String title = (String)proxy.getVariable("title");
			String[] legends = (String[])proxy.getVariable("legends");
			MatlabTypeConverter converter = new MatlabTypeConverter(proxy);
//			MatlabNumericArray xArray = converter.getNumericArray("Xdata");
//			MatlabNumericArray yArray = converter.getNumericArray("Ydata");

			Object[] xCellArray = (Object[])proxy.getVariable("Xdata");
			Object[] yCellArray = (Object[])proxy.getVariable("Ydata");
			String xLabel = (String)proxy.getVariable("Xlabel");
			String yLabel = (String)proxy.getVariable("Ylabel");

			timestamp = (double[])proxy.getVariable("timestamp");

			//double[] xArray = (double[])xCellArray[0]; // assuming only 1 array for X.

			XYSeriesCollection XYdataSet = new XYSeriesCollection();

			//int[] xArrayLengths = xArray.getLengths();
			//int[] yArrayLengths = yArray.getLengths();
			//int numRows = (xArrayLengths[0] < yArrayLengths[0]) ? xArrayLengths[0] : yArrayLengths[0];
			//int numData = yArrayLengths[1];
			//int xLength = xArray.length;
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
				String legend = legends[i];
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
				MatlabNumericArray yArray = converter.getNumericArray("Ydata{" + (i+1) + "}" );
				int xLength = xArray.length;
				int[] yArrayLengths = yArray.getLengths();
				int row = yArrayLengths[0];
				int col = yArrayLengths[1];

				for (int c = 0; c < col; ++c)
				{
					XYSeries series;
					int legendIdx = (dataCount >= numLegends) ? numLegends - 1 : dataCount;
					if (numLegends == 0)
					{
						series = new XYSeries("Data " + dataCount+1);
					}
					else if (dataCount >= numLegends)
					{
						series = new XYSeries(legends[legendIdx] + (dataCount+1));
					}
					else
					{
						series = new XYSeries(legends[legendIdx]);
					}

					for (int r = 0; r < row; ++r)
					{
						int xRow = (r >= xLength) ? xLength - 1 : r;
						series.add(xArray[xRow], yArray.getRealValue(r, c));
					}
					XYdataSet.addSeries(series);
					++dataCount;
				}
//
//				for (int j = 0; j < numRows; ++j)
//				{
//					series.add(xArray.getRealValue(j, 0), yArray.getRealValue(j, i));
//				}
//				dataSet.addSeries(series);
			}

			// Temp
//			XYSeries abnormalSeries = new XYSeries("Abnormal");
//			XYSeries normalSeries = new XYSeries("Normal");
//			XYdataSet.addSeries(abnormalSeries);
//			XYdataSet.addSeries(normalSeries);

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

//			for (int i=0; i<dataCount; ++i)
//			{
				// Renderer to highlight selected normal or outlier points.
				if (isTransactionSampleChart)
				{
//					chart.getXYPlot().setRenderer(new DBSeerXYLineAndShapeRenderer(timestamp, dataset.getTransactionSampleLists()));
					chart.getXYPlot().setRenderer(new DBSeerXYLineAndShapeRenderer(timestamp, dataset));
				}
				else
				{
					chart.getXYPlot().setRenderer(new DBSeerXYLineAndShapeRenderer());

					// TEMP
//					chart.getXYPlot().getRenderer(0).setSeriesPaint(0, Color.BLACK);
//					chart.getXYPlot().getRenderer(0).setSeriesPaint(1, Color.RED);
//					chart.getXYPlot().getRenderer(0).setSeriesPaint(2, Color.BLUE);
//
//					chart.getXYPlot().getRenderer(0).setSeriesShape(0, new Rectangle(-2, -2, 5, 5));
//					chart.getXYPlot().getRenderer(0).setSeriesShape(1, new Rectangle(-2, -2, 5, 5));
//					chart.getXYPlot().getRenderer(0).setSeriesShape(2, new Rectangle(-2, -2, 5, 5));
				}
//			}

			return chart;
		}
		catch (MatlabInvocationException e)
		{
			JOptionPane.showMessageDialog(null, e.getMessage(), "Matlab Proxy Error", JOptionPane.ERROR_MESSAGE);
		}

		return null;
	}

	public static JFreeChart createPieChart(String chartName, DBSeerDataSet dataset)
	{
		MatlabProxy proxy = DBSeerGUI.proxy;
		try
		{
			proxy.eval("[title legends Xdata Ydata Xlabel Ylabel timestamp] = plotter.plot" + chartName + ";");

			String title = (String)proxy.getVariable("title");
			String[] legends = (String[])proxy.getVariable("legends");
			MatlabTypeConverter converter = new MatlabTypeConverter(proxy);

			Object[] xCellArray = (Object[])proxy.getVariable("Xdata");
			Object[] yCellArray = (Object[])proxy.getVariable("Ydata");
			String xLabel = (String)proxy.getVariable("Xlabel");
			String yLabel = (String)proxy.getVariable("Ylabel");
			timestamp = (double[])proxy.getVariable("timestamp");

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
				MatlabNumericArray yArray = converter.getNumericArray("Ydata{" + (i+1) + "}" );
				int xLength = xArray.length;
				int[] yArrayLengths = yArray.getLengths();
				int row = yArrayLengths[0];
				int col = yArrayLengths[1];

				for (int c = 0; c < col; ++c)
				{
					if (c < transactionTypeNames.size())
					{
						String name = transactionTypeNames.get(c);
						if (!name.isEmpty())
						{
							pieDataSet.setValue(name, new Double(yArray.getRealValue(0, c)));
						}
						else
						{
							pieDataSet.setValue("Transaction Type " + (c+1), yArray.getRealValue(0, c));
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
		catch (MatlabInvocationException e)
		{
			JOptionPane.showMessageDialog(null, e.getMessage(), "Matlab Proxy Error", JOptionPane.ERROR_MESSAGE);
		}

		return null;
	}

	public static JFreeChart createXYLinePredictionChart(PredictionCenter center)
	{
		MatlabProxy proxy = DBSeerGUI.proxy;
		try
		{
//			center.run(); // now done in the DBSeerPredictionControlPanel.

			String title = (String)proxy.getVariable("title");
			String[] legends = (String[])proxy.getVariable("legends");
			MatlabTypeConverter converter = new MatlabTypeConverter(proxy);

			Object[] xCellArray = (Object[])proxy.getVariable("Xdata");
			Object[] yCellArray = (Object[])proxy.getVariable("Ydata");
			String xLabel = (String)proxy.getVariable("Xlabel");
			String yLabel = (String)proxy.getVariable("Ylabel");

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
				String legend = legends[i];
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
				MatlabNumericArray yArray = converter.getNumericArray("Ydata{" + (i+1) + "}" );
				int xLength = xArray.length;
				int[] yArrayLengths = yArray.getLengths();
				int row = yArrayLengths[0];
				int col = yArrayLengths[1];

				for (int c = 0; c < col; ++c)
				{
					XYSeries series;
					int legendIdx = (dataCount >= numLegends) ? numLegends - 1 : dataCount;
					if (numLegends == 0)
					{
						series = new XYSeries("Data " + dataCount+1);
					}
					else if (dataCount >= numLegends)
					{
						series = new XYSeries(legends[legendIdx] + (dataCount+1));
					}
					else
					{
						series = new XYSeries(legends[legendIdx]);
					}

					for (int r = 0; r < row; ++r)
					{
						int xRow = (r >= xLength) ? xLength - 1 : r;
						series.add(xArray[xRow], yArray.getRealValue(r, c));
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
		catch (MatlabInvocationException e)
		{
			JOptionPane.showMessageDialog(null, e.getMessage(), "Matlab Proxy Error", JOptionPane.ERROR_MESSAGE);
		}

		return null;
	}

	public static JTable createErrorTable(PredictionCenter center)
	{
		JTable errorTable = null;
		DefaultTableModel errorTableModel = null;
		MatlabProxy proxy = DBSeerGUI.proxy;

		try
		{
			Object[] maeList = (Object[]) proxy.getVariable("meanAbsError");
			Object[] mreList = (Object[]) proxy.getVariable("meanRelError");
			String[] headers = (String[]) proxy.getVariable("errorHeader");

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

					String header = headers[i];
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
		}
		catch (MatlabInvocationException e)
		{
			JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}

		return errorTable;
	}

	public static JFreeChart createCustomXYLineChart(String xAxisName, String yAxisName)
	{
		MatlabProxy proxy = DBSeerGUI.proxy;
		try
		{
			proxy.eval("[Xdata Ydata] = plotter.plotCustom('" +
					DBSeerPlotControlPanel.axisMap.get(xAxisName) +
					"', '" +
					DBSeerPlotControlPanel.axisMap.get(yAxisName) + "');");

			String title = "Custom Plot";
			MatlabTypeConverter converter = new MatlabTypeConverter(proxy);

			Object[] xCellArray = (Object[])proxy.getVariable("Xdata");
			Object[] yCellArray = (Object[])proxy.getVariable("Ydata");
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
				MatlabNumericArray yArray = converter.getNumericArray("Ydata{" + (i+1) + "}" );
				int xLength = xArray.length;
				int[] yArrayLengths = yArray.getLengths();
				int row = yArrayLengths[0];
				int col = yArrayLengths[1];

				for (int c = 0; c < col; ++c)
				{
					XYSeries series = new XYSeries(yAxisName);

					for (int r = 0; r < row; ++r)
					{
						int xRow = (r >= xLength) ? xLength - 1 : r;
						series.add(xArray[xRow], yArray.getRealValue(r, c));
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
		catch (MatlabInvocationException e)
		{
			JOptionPane.showMessageDialog(null, e.getMessage(), "Matlab Proxy Error", JOptionPane.ERROR_MESSAGE);
		}

		return null;
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
