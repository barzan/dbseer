package dbseer.gui.chart;

import dbseer.gui.DBSeerGUI;
import matlabcontrol.MatlabInvocationException;
import matlabcontrol.MatlabProxy;
import matlabcontrol.extensions.MatlabNumericArray;
import matlabcontrol.extensions.MatlabTypeConverter;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;

/**
 * Created by dyoon on 2014. 5. 25..
 */
public class DBSeerChartFactory
{
	public static JFreeChart createXYLineChart(String chartName)
	{
		MatlabProxy proxy = DBSeerGUI.proxy;
		try
		{
			proxy.eval("[title legends Xdata Ydata Xlabel Ylabel] = plotter.plot" + chartName + ";");

			String title = (String)proxy.getVariable("title");
			String[] legends = (String[])proxy.getVariable("legends");
			MatlabTypeConverter converter = new MatlabTypeConverter(proxy);
//			MatlabNumericArray xArray = converter.getNumericArray("Xdata");
//			MatlabNumericArray yArray = converter.getNumericArray("Ydata");

			Object[] xCellArray = (Object[])proxy.getVariable("Xdata");
			Object[] yCellArray = (Object[])proxy.getVariable("Ydata");
			String xLabel = (String)proxy.getVariable("Xlabel");
			String yLabel = (String)proxy.getVariable("Ylabel");

			//double[] xArray = (double[])xCellArray[0]; // assuming only 1 array for X.

			XYSeriesCollection dataSet = new XYSeriesCollection();

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
//
//				for (int j = 0; j < numRows; ++j)
//				{
//					series.add(xArray.getRealValue(j, 0), yArray.getRealValue(j, i));
//				}
//				dataSet.addSeries(series);
			}

			JFreeChart chart = ChartFactory.createXYLineChart(title, xLabel, yLabel, dataSet);

			return chart;
		}
		catch (MatlabInvocationException e)
		{
			JOptionPane.showMessageDialog(null, e.toString(), e.toString(), JOptionPane.ERROR_MESSAGE);
		}

		return null;
	}
}
