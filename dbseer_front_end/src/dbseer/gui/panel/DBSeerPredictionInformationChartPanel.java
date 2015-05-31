package dbseer.gui.panel;

import dbseer.gui.DBSeerExceptionHandler;
import dbseer.gui.DBSeerGUI;
import dbseer.gui.chart.DBSeerChartFactory;
import dbseer.gui.user.DBSeerDataSet;
import matlabcontrol.MatlabProxy;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.panel.CrosshairOverlay;
import org.jfree.chart.plot.Crosshair;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleEdge;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * Created by dyoon on 4/29/15.
 */
public class DBSeerPredictionInformationChartPanel extends ChartPanel
{
	private Crosshair verticalCrossHair;

	public DBSeerPredictionInformationChartPanel(JFreeChart chart)
	{
		super(chart);
		verticalCrossHair = new Crosshair(Double.NaN, Color.GRAY, new BasicStroke(0f));
		CrosshairOverlay crosshairOverlay = new CrosshairOverlay();
		crosshairOverlay.addDomainCrosshair(verticalCrossHair);
		this.addOverlay(crosshairOverlay);
	}

	public void renewDataset(DBSeerDataSet dataset)
	{
		MatlabProxy proxy = DBSeerGUI.proxy;

		try
		{
			dataset.loadModelVariable();
			proxy.eval("plotter = Plotter;");
			proxy.eval("plotter.mv = " + dataset.getUniqueModelVariableName() + ";");

			JFreeChart newChart = DBSeerChartFactory.createXYLineChart("ForPrediction", dataset);
			this.setChart(newChart);
		}
		catch (Exception e)
		{
			DBSeerExceptionHandler.handleException(e);
		}
	}
	public Crosshair getVerticalCrossHair()
	{
		return verticalCrossHair;
	}
}
