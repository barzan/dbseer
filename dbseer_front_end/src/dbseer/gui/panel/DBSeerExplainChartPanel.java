package dbseer.gui.panel;

import dbseer.gui.DBSeerConstants;
import dbseer.gui.actions.ExplainChartAction;
import net.miginfocom.swing.MigLayout;
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
	private Set<XYItemEntity> selectedItems;
	private ArrayList<Double> normalRegion;
	private ArrayList<Double> anomalyRegion;

	private JTextArea explainConsole;
	private int startX, startY;
	private int endX, endY;
	private double x,y,height,width;
	private boolean isNewRectangle = true;
	private Rectangle2D selectRectangle = null;
	private JPopupMenu popupMenu = null;

	private JMenuItem showPredicatesMenuItem = null;
	private JMenuItem savePredicatesMenuItem = null;

	private DBSeerExplainControlPanel controlPanel;

	public DBSeerExplainChartPanel(JFreeChart chart, JTextArea log, DBSeerExplainControlPanel controlPanel)
	{
		super(chart);
		normalRegion = new ArrayList<Double>();
		anomalyRegion = new ArrayList<Double>();
		this.setLayout(new MigLayout("fill"));
		this.setMouseWheelEnabled(true);
		this.explainConsole = log;
		this.controlPanel = controlPanel;
		this.popupMenu = this.getPopupMenu();

		JMenuItem popupItem;
//		popupItem = new JMenuItem(new ExplainChartAction("Select as Anomaly (Marked as Black)", DBSeerConstants.EXPLAIN_SELECT_ANOMALY_REGION,
//				this));
//		popupMenu.add(popupItem);
//		popupItem = new JMenuItem(new ExplainChartAction("Select as Normal (Marked as White, Optional)", DBSeerConstants.EXPLAIN_SELECT_NORMAL_REGION,
//				this));
//		popupMenu.add(popupItem);
		popupMenu.insert(new JSeparator(), 0);
		popupItem = new JMenuItem(new ExplainChartAction("Clear All", DBSeerConstants.EXPLAIN_CLEAR_REGION, this));
		popupMenu.insert(popupItem, 0);
		popupItem = new JMenuItem(new ExplainChartAction("Select as Normal (Optional)", DBSeerConstants.EXPLAIN_APPEND_NORMAL_REGION,
				this));
		popupMenu.insert(popupItem, 0);
		popupItem = new JMenuItem(new ExplainChartAction("Select as Anomaly", DBSeerConstants.EXPLAIN_APPEND_ANOMALY_REGION,
				this));
		popupMenu.insert(popupItem, 0);
//		popupMenu.add(popupItem);
//		popupMenu.add(popupItem);
//
//		popupMenu.add(popupItem);
//		popupMenu.addSeparator();
//		popupItem = new JMenuItem(new ExplainChartAction("Explain", DBSeerConstants.EXPLAIN_EXPLAIN, this));
//		popupMenu.add(popupItem);
//		popupMenu.addSeparator();
//		showPredicatesMenuItem = new JMenuItem(new ExplainChartAction("Show Predicates", DBSeerConstants.EXPLAIN_SHOW_PREDICATES, this));
//		showPredicatesMenuItem.setEnabled(false);
//		popupMenu.add(showPredicatesMenuItem);
//		savePredicatesMenuItem = new JMenuItem(new ExplainChartAction("Save Predicates as Causal Model", DBSeerConstants.EXPLAIN_SAVE_PREDICATES, this));
//		savePredicatesMenuItem.setEnabled(false);
//		popupMenu.add(savePredicatesMenuItem);

		super.setPopupMenu(this.popupMenu);
	}

	public JTextArea getExplainConsole()
	{
		return explainConsole;
	}

	public JMenuItem getShowPredicatesMenuItem()
	{
		return showPredicatesMenuItem;
	}

	public JMenuItem getSavePredicatesMenuItem()
	{
		return savePredicatesMenuItem;
	}

	public ArrayList<Double> getNormalRegion()
	{
		return normalRegion;
	}

	public ArrayList<Double> getAnomalyRegion()
	{
		return anomalyRegion;
	}

	public Set<XYItemEntity> getSelectedItems()
	{
		return selectedItems;
	}

	public void clearRectangle()
	{
		this.width = 0;
		this.height = 0;
	}

	public DBSeerExplainControlPanel getControlPanel()
	{
		return controlPanel;
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
//		this.selectRectangle = new Rectangle2D.Double(
//				xmin, ymin,
//				xmax - xmin, ymax - ymin);
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

		selectedItems = foundItem;

//		outlierRegion.clear();
//		for (XYItemEntity entity : foundItem)
//		{
//			//testLog.append(entity.toString() + "\n");
//			//testLog.append("Series = " + entity.getSeriesIndex() + ", ");
//			//testLog.append("X = " + entity.getDataset().getX(entity.getSeriesIndex(), entity.getItem()) + ", ");
//			//testLog.append("Y = " + entity.getDataset().getY(entity.getSeriesIndex(), entity.getItem()) + "\n");
//			outlierRegion.add(entity.getDataset().getX(entity.getSeriesIndex(), entity.getItem()).doubleValue());
//		}
//
//		Collections.sort(outlierRegion);
//		if (!outlierRegion.isEmpty())
//		{
//			testLog.setText("");
//			testLog.append("Outlier time selected = [");
//			for (int i = 0; i < outlierRegion.size(); ++i)
//			{
//				testLog.append(outlierRegion.get(i).toString());
//				if (i < outlierRegion.size() - 1)
//				{
//					testLog.append(" ");
//				}
//			}
//			testLog.append("]\n");
//		}
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
