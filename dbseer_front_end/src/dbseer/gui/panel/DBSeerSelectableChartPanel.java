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

package dbseer.gui.panel;

import dbseer.gui.DBSeerGUI;
import dbseer.gui.actions.ShowQueryAction;
import dbseer.gui.chart.DBSeerXYLineAndShapeRenderer;
import dbseer.gui.frame.DBSeerPlotExplainFrame;
import dbseer.gui.frame.DBSeerPlotPresetFrame;
import dbseer.gui.user.DBSeerDataSet;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.PieSectionEntity;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import org.jfree.util.*;
import org.jfree.util.SortOrder;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

/**
 * Created by dyoon on 2014. 6. 10..
 */
public class DBSeerSelectableChartPanel extends ChartPanel implements ChartMouseListener
{
	private final Border lineBorder = BorderFactory.createLineBorder(Color.BLACK, 5);
	private final Insets insets = lineBorder.getBorderInsets(this);
	private final EmptyBorder emptyBorder = new EmptyBorder(insets);

	private JMenuItem showQueriesMenuItem;
	private ShowQueryAction showQueryAction;
	private JFreeChart chart;
	private DBSeerDataSet dataset;
	private String chartName;
	private boolean isTransactionSampleChart;
	private double[] timestamp;
	private int lastSeries = -1;
	private int lastCategory = -1;
	private int maxTransactionSeries = -1;

	public DBSeerSelectableChartPanel(JFreeChart chart, DBSeerDataSet dataset, String chartName, double[] timestamp)
	{
		super(chart);
		this.setBorder(emptyBorder);
		this.setMouseWheelEnabled(true);
		this.addChartMouseListener(this);
		this.dataset = dataset;
		this.chart = chart;
		this.chartName = chartName;
		this.timestamp = timestamp;
		this.isTransactionSampleChart = false;
		this.maxTransactionSeries = this.dataset.getNumTransactionTypes();
		for (String name : DBSeerGUI.transactionSampleCharts)
		{
			if (name.equals(this.chartName))
			{
				this.isTransactionSampleChart = true;
				break;
			}
		}

		if (this.isTransactionSampleChart)
		{
			JPopupMenu popupMenu = this.getPopupMenu();
			showQueryAction = new ShowQueryAction();
			showQueryAction.setDataset(this.dataset);
			showQueryAction.setTimestamp(this.timestamp);
			if (this.chartName.equals("CombinedAvgLatency"))
			{
				showQueryAction.setShowAll(true);
			}
			showQueriesMenuItem = new JMenuItem(showQueryAction);
			showQueriesMenuItem.setEnabled(false);
			popupMenu.insert(showQueriesMenuItem, 0);
		}
	}

	@Override
	public JFreeChart getChart()
	{
		return chart;
	}

	@Override
	public void mouseEntered(MouseEvent e)
	{
		super.mouseEntered(e);
		this.setBorder(lineBorder);
	}

	@Override
	public void mouseExited(MouseEvent e)
	{
		super.mouseExited(e);
		this.setBorder(emptyBorder);
	}

	@Override
	public void mousePressed(MouseEvent e)
	{
		super.mousePressed(e);

		Rectangle2D origArea = this.getScreenDataArea();
		Plot plot = chart.getPlot();

		if (!(plot instanceof XYPlot))
		{
			return;
		}

		XYPlot xyPlot = chart.getXYPlot();
		String origDomainAxisLabel = xyPlot.getDomainAxis().getLabel();

		if (SwingUtilities.isRightMouseButton(e))
		{
			return;
		}
		for (DBSeerSelectableChartPanel panel : DBSeerPlotPresetFrame.chartPanels)
		{
			if (panel != this)
			{
				Plot otherPlot = panel.getChart().getPlot();
				if (!(otherPlot instanceof XYPlot))
				{
					continue;
				}
				Rectangle2D otherArea = panel.getScreenDataArea();
				XYPlot otherXYPlot = panel.getChart().getXYPlot();
				String otherDomainAxisLabel = otherXYPlot.getDomainAxis().getLabel();

				if (origDomainAxisLabel.equalsIgnoreCase(otherDomainAxisLabel))
				{
					double origRangeX = origArea.getMaxX() - origArea.getMinX();
					double origRangeY = origArea.getMaxY() - origArea.getMinY();
					double otherRangeX = otherArea.getMaxX() - otherArea.getMinX();
					double otherRangeY = otherArea.getMaxY() - otherArea.getMinY();

					double syncX = otherArea.getMinX() + (e.getX() - origArea.getMinX()) / origRangeX * otherRangeX;
					double syncY = otherArea.getMinY() + (e.getY() - origArea.getMinY()) / origRangeY * otherRangeY;
					MouseEvent syncEvent = new MouseEvent(this, 0, 0, 0, (int)syncX, (int)syncY, 1, false);
					panel.syncMousePressed(syncEvent);
				}
			}
		}
	}

	public void syncMousePressed(MouseEvent e)
	{
		super.mousePressed(e);
	}

	@Override
	public void mouseDragged(MouseEvent e)
	{
		super.mouseDragged(e);

		Rectangle2D origArea = this.getScreenDataArea();
		Plot plot = chart.getPlot();

		if (!(plot instanceof XYPlot))
		{
			return;
		}

		XYPlot xyPlot = chart.getXYPlot();
		String origDomainAxisLabel = xyPlot.getDomainAxis().getLabel();

		if (SwingUtilities.isRightMouseButton(e))
		{
			return;
		}
		for (DBSeerSelectableChartPanel panel : DBSeerPlotPresetFrame.chartPanels)
		{
			if (panel != this)
			{
				Plot otherPlot = panel.getChart().getPlot();
				if (!(otherPlot instanceof XYPlot))
				{
					continue;
				}
				Rectangle2D otherArea = panel.getScreenDataArea();
				XYPlot otherXYPlot = panel.getChart().getXYPlot();
				String otherDomainAxisLabel = otherXYPlot.getDomainAxis().getLabel();

				if (origDomainAxisLabel.equalsIgnoreCase(otherDomainAxisLabel))
				{
					double origRangeX = origArea.getMaxX() - origArea.getMinX();
					double origRangeY = origArea.getMaxY() - origArea.getMinY();
					double otherRangeX = otherArea.getMaxX() - otherArea.getMinX();
					double otherRangeY = otherArea.getMaxY() - otherArea.getMinY();

					double syncX = otherArea.getMinX() + (e.getX() - origArea.getMinX()) / origRangeX * otherRangeX;
					double syncY = otherArea.getMinY() + (e.getY() - origArea.getMinY()) / origRangeY * otherRangeY;
					MouseEvent syncEvent = new MouseEvent(this, 0, 0, 0, (int)syncX, (int)syncY, 1, false);
					panel.syncMouseDragged(syncEvent);
				}
			}
		}
	}

	public void syncMouseDragged(MouseEvent e)
	{
		super.mouseDragged(e);
	}

	@Override
	public void mouseReleased(MouseEvent e)
	{
		super.mouseReleased(e);

		Rectangle2D origArea = this.getScreenDataArea();
		Plot plot = chart.getPlot();

		if (!(plot instanceof XYPlot))
		{
			return;
		}

		XYPlot xyPlot = chart.getXYPlot();
		String origDomainAxisLabel = xyPlot.getDomainAxis().getLabel();

		if (SwingUtilities.isRightMouseButton(e))
		{
			return;
		}
		for (DBSeerSelectableChartPanel panel : DBSeerPlotPresetFrame.chartPanels)
		{
			if (panel != this)
			{
				Plot otherPlot = panel.getChart().getPlot();
				if (!(otherPlot instanceof XYPlot))
				{
					continue;
				}
				Rectangle2D otherArea = panel.getScreenDataArea();
				XYPlot otherXYPlot = panel.getChart().getXYPlot();
				String otherDomainAxisLabel = otherXYPlot.getDomainAxis().getLabel();

				if (origDomainAxisLabel.equalsIgnoreCase(otherDomainAxisLabel))
				{
					double origRangeX = origArea.getMaxX() - origArea.getMinX();
					double origRangeY = origArea.getMaxY() - origArea.getMinY();
					double otherRangeX = otherArea.getMaxX() - otherArea.getMinX();
					double otherRangeY = otherArea.getMaxY() - otherArea.getMinY();

					double syncX = otherArea.getMinX() + (e.getX() - origArea.getMinX()) / origRangeX * otherRangeX;
					double syncY = otherArea.getMinY() + (e.getY() - origArea.getMinY()) / origRangeY * otherRangeY;
					MouseEvent syncEvent = new MouseEvent(this, 0, 0, 0, (int)syncX, (int)syncY, 1, false);
					panel.syncMouseReleased(syncEvent);
				}
			}
		}
	}

	public void syncMouseReleased(MouseEvent e)
	{
		super.mouseReleased(e);
	}

	@Override
	public void mouseClicked(MouseEvent event)
	{
		super.mouseClicked(event);

		if (SwingUtilities.isLeftMouseButton(event) && event.getClickCount() == 2)
		{
			final JFreeChart chartToExplain = getChart();
			if (chartToExplain.getPlot() instanceof PiePlot)
			{
				return;
			}
			DBSeerXYLineAndShapeRenderer renderer = (DBSeerXYLineAndShapeRenderer)chartToExplain.getXYPlot().getRenderer();
			renderer.setLastSeriesAndCategory(-1, -1);

			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						DBSeerPlotExplainFrame newFrame = new DBSeerPlotExplainFrame((JFreeChart)chartToExplain.clone());
//						newFrame.setPreferredSize(new Dimension(1280,800));
						newFrame.pack();
						newFrame.setVisible(true);
					}
					catch(CloneNotSupportedException e)
					{
						JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
					}
				}
			});
		}
	}

	@Override
	public void chartMouseClicked(ChartMouseEvent chartMouseEvent)
	{
		ChartEntity entity = chartMouseEvent.getEntity();
		MouseEvent mouseEvent = chartMouseEvent.getTrigger();

		if (SwingUtilities.isLeftMouseButton(mouseEvent) && entity != null && entity instanceof PieSectionEntity)
		{
			java.util.List<String> names = dataset.getTransactionTypeNames();
			PieSectionEntity pieSectionEntity = (PieSectionEntity)entity;
			int idx = pieSectionEntity.getSectionIndex();

			String name = (String)JOptionPane.showInputDialog(null, "Enter the name for this transaction type", "Transaction Type",
					JOptionPane.PLAIN_MESSAGE, null, null, "");

			if (name != null)
			{
				if (names.contains(name) && !names.get(idx).equals(name) && !name.isEmpty())
				{
					JOptionPane.showMessageDialog(null, "Please enter a different name for the transaction type.\nEach name has to be unique.",
							"Warning", JOptionPane.WARNING_MESSAGE);
				}
				else
				{
					PieDataset oldDataset = pieSectionEntity.getDataset();
					DefaultPieDataset newDataset = new DefaultPieDataset();

					PiePlot plot = (PiePlot)chart.getPlot();
					String oldName = (String)oldDataset.getKey(idx);
					names.set(idx, name);

					for (int i = 0; i < oldDataset.getItemCount(); ++i)
					{
						String key = (String)oldDataset.getKey(i);
						Number number = oldDataset.getValue(i);

						if (key.equals(oldName))
						{
							if (name.isEmpty())
								newDataset.setValue("Transaction Type " + (i+1), number);
							else
								newDataset.setValue(name, number);
						}
						else
						{
							newDataset.setValue(key, number);
						}
					}
					((DefaultPieDataset)oldDataset).clear();
					plot.setDataset(newDataset);
				}
			}
		}
	}

	@Override
	public void chartMouseMoved(ChartMouseEvent chartMouseEvent)
	{
		ChartEntity entity = chartMouseEvent.getEntity();

//		System.out.println(entity.toString());
		if (entity != null)
		{
			if (entity instanceof PieSectionEntity)
			{
				PieSectionEntity pieSectionEntity = (PieSectionEntity) entity;
				int index = pieSectionEntity.getSectionIndex();

				PiePlot plot = (PiePlot) chart.getPlot();

				int sectionCount = plot.getDataset().getItemCount();

				for (int i = 0; i < sectionCount; ++i)
				{
					String key = (String) plot.getDataset().getKey(i);
					if (i == index)
					{
						plot.setExplodePercent(key, 0.20);
					}
					else
					{
						plot.setExplodePercent(key, 0.0);
					}
				}
				lastSeries = index;
				lastCategory = -1;

				showQueryAction.setSeries(lastSeries);
				showQueryAction.setCategory(lastCategory);
				showQueriesMenuItem.setEnabled(true);
			}

			if (entity instanceof XYItemEntity)
			{
				XYItemEntity xyItemEntity = (XYItemEntity)entity;
				XYPlot plot = chart.getXYPlot();
				DBSeerXYLineAndShapeRenderer renderer = (DBSeerXYLineAndShapeRenderer)plot.getRenderer();
				if (isTransactionSampleChart && xyItemEntity.getSeriesIndex() < maxTransactionSeries)
				{
					renderer.setLastSeriesAndCategory(xyItemEntity.getSeriesIndex(), xyItemEntity.getItem());
					lastSeries = xyItemEntity.getSeriesIndex();
					lastCategory = xyItemEntity.getItem();
					showQueryAction.setSeries(lastSeries);
					showQueryAction.setCategory(lastCategory);
					showQueriesMenuItem.setEnabled(true);
					this.setRefreshBuffer(true);
					this.repaint();
				}
			}
		}
	}
}
