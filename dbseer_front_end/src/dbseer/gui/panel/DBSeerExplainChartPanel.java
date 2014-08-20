package dbseer.gui.panel;

import dbseer.gui.DBSeerConstants;
import dbseer.gui.actions.ExplainChartAction;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by dyoon on 2014. 6. 10..
 */
public class DBSeerExplainChartPanel extends ChartPanel
{
	private ArrayList<Double> outlierRegion;
	private JTextArea testLog;
	private int startX, startY;
	private int endX, endY;
	private double x,y,height,width;
	private boolean isNewRectangle = true;
	private Rectangle2D selectRectangle = null;
	private JPopupMenu popupMenu = null;

	public DBSeerExplainChartPanel(JFreeChart chart, JTextArea log)
	{
		super(chart);
		outlierRegion = new ArrayList<Double>();
		this.setMouseWheelEnabled(true);
		this.testLog = log;
		this.popupMenu = new JPopupMenu();

		JMenuItem popupItem;
		popupItem = new JMenuItem(new ExplainChartAction("Greater than expected", DBSeerConstants.EXPLAIN_GREATER_THAN,
				testLog, outlierRegion));
		popupMenu.add(popupItem);
		popupItem = new JMenuItem(new ExplainChartAction("Less than expected", DBSeerConstants.EXPLAIN_LESS_THAN,
				testLog, outlierRegion));
		popupMenu.add(popupItem);
		popupItem = new JMenuItem(new ExplainChartAction("Different", DBSeerConstants.EXPLAIN_DIFFERENT,
				testLog, outlierRegion));
		popupMenu.add(popupItem);

		super.setPopupMenu(this.popupMenu);
	}

	public ArrayList<Double> getOutlierRegion()
	{
		return outlierRegion;
	}

	@Override
	public void mousePressed(MouseEvent e)
	{
		if (e.getButton() == MouseEvent.BUTTON3 || this.popupMenu.isShowing())
		{
			return;
		}
		//testLog.append("Mouse pressed: " + e.getX() + ", " + e.getY() + "\n");
		startX = e.getX();
		startY = e.getY();
		endX = e.getX();
		endY = e.getY();

//		if (selectRectangle != null)
//		{
//			Graphics2D g2 = (Graphics2D) this.getGraphics();
//			drawSelectRectangle(g2);
//			this.selectRectangle = new Rectangle2D.Double(e.getX(), e.getY(), 0, 0);
//		}
		this.isNewRectangle = true;
		this.x = e.getX();
		this.y = e.getY();
		this.width = 0;
		this.height = 0;
		repaint();
	}

	@Override
	public void mouseDragged(MouseEvent e)
	{
		if (e.getButton() == MouseEvent.BUTTON3 || this.popupMenu.isShowing())
		{
			return;
		}
		endX = e.getX();
		endY = e.getY();
		Graphics2D g2 = (Graphics2D) this.getGraphics();
//		drawSelectRectangle(g2);
		double xmin = Math.min(startX, endX);
		double ymin = Math.min(startY, endY);
		double xmax = Math.max(startX, endX);
		double ymax = Math.max(startY, endY);
		this.selectRectangle = new Rectangle2D.Double(
				xmin, ymin,
				xmax - xmin, ymax - ymin);
		this.x = xmin;
		this.y = ymin;
		this.width = xmax  - xmin;
		this.height = ymax - ymin;
//		drawSelectRectangle(g2);
//		g2.dispose();

		this.isNewRectangle = false;
		repaint();
	}

	@Override
	public void mouseReleased(MouseEvent e)
	{
		if (e.getButton() == MouseEvent.BUTTON3)
		{
			displayPopupMenu(e.getX(), e.getY());
			return;
		}
		repaint();
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

		//testLog.append("Scanning: (" + smallX + ", " + smallY + "), (" + bigX + ", " + bigY + ")\n");
		//testLog.append("Scanning: X = [" + Math.min(chartSmallX, chartBigX) + ", " + Math.max(chartSmallX, chartBigX) + "]\n");
		//testLog.append("Scanning: Y = [" + Math.min(chartSmallY, chartBigY) + ", " + Math.max(chartSmallY, chartBigY) + "]\n");

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

		outlierRegion.clear();
		for (XYItemEntity entity : foundItem)
		{
			//testLog.append(entity.toString() + "\n");
			//testLog.append("Series = " + entity.getSeriesIndex() + ", ");
			//testLog.append("X = " + entity.getDataset().getX(entity.getSeriesIndex(), entity.getItem()) + ", ");
			//testLog.append("Y = " + entity.getDataset().getY(entity.getSeriesIndex(), entity.getItem()) + "\n");
			outlierRegion.add(entity.getDataset().getX(entity.getSeriesIndex(), entity.getItem()).doubleValue());
		}

		Collections.sort(outlierRegion);
		if (!outlierRegion.isEmpty())
		{
			testLog.setText("");
			testLog.append("Outlier time selected = [");
			for (int i = 0; i < outlierRegion.size(); ++i)
			{
				testLog.append(outlierRegion.get(i).toString());
				if (i < outlierRegion.size() - 1)
				{
					testLog.append(" ");
				}
			}
			testLog.append("]\n");
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

	@Override
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D)g;

		selectRectangle = new Rectangle2D.Double(x, y, width, height);
		g2d.setPaint(Color.black);
		Composite c = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .4f);
		g2d.setComposite(c);
		g2d.fill(selectRectangle);
	}
}
