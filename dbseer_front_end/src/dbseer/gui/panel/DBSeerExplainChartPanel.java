package dbseer.gui.panel;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by dyoon on 2014. 6. 10..
 */
public class DBSeerExplainChartPanel extends ChartPanel
{
	private JTextArea testLog;
	int startX, startY;
	int endX, endY;
	private Rectangle2D selectRectangle = null;

	public DBSeerExplainChartPanel(JFreeChart chart, JTextArea log)
	{
		super(chart);
		this.setMouseWheelEnabled(true);
		this.testLog = log;
	}

	@Override
	public void mousePressed(MouseEvent e)
	{
		testLog.append("Mouse pressed: " + e.getX() + ", " + e.getY() + "\n");
		startX = e.getX();
		startY = e.getY();
		endX = e.getX();
		endY = e.getY();

		if (selectRectangle != null)
		{
			Graphics2D g2 = (Graphics2D) this.getGraphics();
			drawSelectRectangle(g2);
			this.selectRectangle = new Rectangle2D.Double(e.getX(), e.getY(), 0, 0);
		}
	}

	@Override
	public void mouseDragged(MouseEvent e)
	{
		endX = e.getX();
		endY = e.getY();
		Graphics2D g2 = (Graphics2D) this.getGraphics();
		drawSelectRectangle(g2);
		double xmin = Math.min(startX, endX);
		double ymin = Math.min(startY, endY);
		double xmax = Math.max(startX, endX);
		double ymax = Math.max(startY, endY);
		this.selectRectangle = new Rectangle2D.Double(
				xmin, ymin,
				xmax - xmin, ymax - ymin);

		drawSelectRectangle(g2);
		g2.dispose();
	}

	@Override
	public void mouseReleased(MouseEvent e)
	{
		int smallX = Math.min(startX, endX);
		int smallY = Math.min(startY, endY);
		int bigX = Math.max(startX, endX);
		int bigY = Math.max(startY, endY);

		XYPlot plot = this.getChart().getXYPlot();
		Rectangle2D plotArea = this.getScreenDataArea();
		double chartSmallX = plot.getDomainAxis().java2DToValue(smallX, plotArea, plot.getDomainAxisEdge());
		double chartBigX = plot.getDomainAxis().java2DToValue(bigX, plotArea, plot.getDomainAxisEdge());
		double chartSmallY = plot.getRangeAxis().java2DToValue(smallY, plotArea, plot.getRangeAxisEdge());
		double chartBigY = plot.getRangeAxis().java2DToValue(bigY, plotArea, plot.getRangeAxisEdge());

		double minXValue = Math.min(chartSmallX, chartBigX);
		double maxXValue = Math.max(chartSmallX, chartBigX);
		double minYValue = Math.min(chartSmallY, chartBigY);
		double maxYValue = Math.max(chartSmallY, chartBigY);

		testLog.append("Scanning: (" + smallX + ", " + smallY + "), (" + bigX + ", " + bigY + ")\n");
		testLog.append("Scanning: X = [" + Math.min(chartSmallX, chartBigX) + ", " + Math.max(chartSmallX, chartBigX) + "]\n");
		testLog.append("Scanning: Y = [" + Math.min(chartSmallY, chartBigY) + ", " + Math.max(chartSmallY, chartBigY) + "]\n");

		Set<XYItemEntity> foundItem = new HashSet<XYItemEntity>();

		XYDataset dataset = this.getChart().getXYPlot().getDataset() ;
		EntityCollection collection = this.getChartRenderingInfo().getEntityCollection();

		for (Object obj : collection.getEntities())
		{
			if (obj instanceof XYItemEntity)
			{
				XYItemEntity entity = (XYItemEntity)obj;
				int series = entity.getSeriesIndex();
				int idx = entity.getItem();
				if (dataset.getX(series, idx).doubleValue() >= minXValue &&
						dataset.getX(series, idx).doubleValue() <= maxXValue &&
						dataset.getY(series, idx).doubleValue() >= minYValue &&
						dataset.getY(series, idx).doubleValue() <= maxYValue
						)
				{
					foundItem.add(entity);
				}
			}
		}

//		for (int x = smallX; x <= bigX; ++x)
//		{
//			for (int y = smallY; y <= bigY; ++y)
//			{
//				ChartEntity entity = this.getEntityForPoint(x, y);
//				if (entity instanceof XYItemEntity)
//				{
//					foundItem.add((XYItemEntity)entity);
//				}
//			}
//		}

		for (XYItemEntity entity : foundItem)
		{
			//testLog.append(entity.toString() + "\n");
			testLog.append("Series = " + entity.getSeriesIndex() + ", ");
			testLog.append("X = " + entity.getDataset().getX(entity.getSeriesIndex(), entity.getItem()) + ", ");
			testLog.append("Y = " + entity.getDataset().getY(entity.getSeriesIndex(), entity.getItem()) + "\n");
		}
	}

	private void drawSelectRectangle(Graphics2D g2)
	{
		if (selectRectangle != null)
		{
			g2.setXORMode(Color.GRAY);
			g2.fill(this.selectRectangle);
			g2.setPaintMode();
		}
	}
}
