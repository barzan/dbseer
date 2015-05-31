package dbseer.gui.events;

import dbseer.gui.panel.DBSeerPredictionInformationChartPanel;
import dbseer.gui.panel.DBSeerPredictionWithTPSMixturePanel;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.DataUtilities;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleEdge;

import java.awt.geom.Rectangle2D;

/**
 * Created by dyoon on 5/4/15.
 */
public class InformationChartMouseListener implements ChartMouseListener
{
	DBSeerPredictionInformationChartPanel chartPanel;
	DBSeerPredictionWithTPSMixturePanel tpsMixturePanel;

	public InformationChartMouseListener(DBSeerPredictionWithTPSMixturePanel tpsMixturePanel)
	{
		this.tpsMixturePanel = tpsMixturePanel;
	}

	public void setChartPanel(DBSeerPredictionInformationChartPanel chartPanel)
	{
		this.chartPanel = chartPanel;
	}

	@Override
	public void chartMouseClicked(ChartMouseEvent event)
	{
		Rectangle2D dataArea = chartPanel.getScreenDataArea();
		JFreeChart chart = event.getChart();
		XYPlot plot = (XYPlot) chart.getPlot();
		ValueAxis xAxis = plot.getDomainAxis();
		int x = (int)Math.round(xAxis.java2DToValue(event.getTrigger().getX(), dataArea,
				RectangleEdge.BOTTOM));

		XYDataset dataset = plot.getDataset();
		int maxX = DatasetUtilities.findMaximumDomainValue(dataset).intValue();
		if (x>=1 && x<=maxX)
		{
			int seriesCount = dataset.getSeriesCount();
			int[] mixtures = new int[seriesCount-1];
			int total = 0;
			for (int i=0;i<seriesCount-1;++i)
			{
				mixtures[i] = (int)dataset.getYValue(i, x-1);
				total += mixtures[i];
			}
			for (int i=0;i<seriesCount-1;++i)
			{
				mixtures[i] = (int)Math.round((double)mixtures[i] / (double)total * 100.0);
				tpsMixturePanel.setMixture(i, mixtures[i]);
			}
		}
	}

	@Override
	public void chartMouseMoved(ChartMouseEvent event)
	{
		Rectangle2D dataArea = chartPanel.getScreenDataArea();
		JFreeChart chart = event.getChart();
		XYPlot plot = (XYPlot) chart.getPlot();
		ValueAxis xAxis = plot.getDomainAxis();
		double x = xAxis.java2DToValue(event.getTrigger().getX(), dataArea,
				RectangleEdge.BOTTOM);
		chartPanel.getVerticalCrossHair().setValue(x);
	}
}
