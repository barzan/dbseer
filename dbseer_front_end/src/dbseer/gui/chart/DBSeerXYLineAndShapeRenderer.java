package dbseer.gui.chart;

import dbseer.comp.TransactionReader;
import dbseer.gui.user.DBSeerDataSet;
import dbseer.gui.user.DBSeerTransactionSample;
import dbseer.gui.user.DBSeerTransactionSampleList;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by dyoon on 14. 11. 17..
 */
public class DBSeerXYLineAndShapeRenderer extends XYLineAndShapeRenderer
{

	private int lastSeries = -1;
	private int lastCategory = -1;

	private Set<XYItemEntity> selectedNormal = null;
	private Set<XYItemEntity> selectedAnomaly = null;

	public DBSeerXYLineAndShapeRenderer()
	{
		super();
	}

	public void setLastSeriesAndCategory(int series, int category)
	{
		this.lastSeries = series;
		this.lastCategory = category;
	}

	@Override
	public Paint getItemOutlinePaint(int row, int column)
	{
		if (row == this.lastSeries && column == this.lastCategory)
		{
			return Color.BLACK;
		}
		return super.getItemOutlinePaint(row, column);
	}

	@Override
	public Stroke getItemOutlineStroke(int row, int column)
	{
		if (row == this.lastSeries && column == this.lastCategory)
		{
			return new BasicStroke(8.0f);
		}
		return super.getItemOutlineStroke(row, column);
	}

//	public DBSeerXYLineAndShapeRenderer(double[] timestamp, java.util.List<DBSeerTransactionSampleList> sampleLists)
	public DBSeerXYLineAndShapeRenderer(double[] timestamp, DBSeerDataSet dataset)
	{
		final java.util.List<DBSeerTransactionSampleList> theSampleLists = dataset.getTransactionSampleLists();
		final double[] timestamps = timestamp;
		this.setBaseToolTipGenerator(new XYToolTipGenerator()
		{
			@Override
			public String generateToolTip(XYDataset xyDataset, int series, int category)
			{
				int maxLength = 80;
//				java.util.List<String> statementOffsetFileList = theDataset.getStatementOffsetFileList();
//				if (series >= statementOffsetFileList.size())
//				{
//					return null;
//				}
//				String statementOffsetFile = statementOffsetFileList.get(series);
//				int time = (int)timestamps[category];
//				TransactionReader reader = new TransactionReader(theDataset.getQueryFilePath(), theDataset.getStatementFilePath(),
//						statementOffsetFile, time);
//				if (!reader.initialize())
//				{
//					return null;
//				}
//				else
//				{
//					return reader.getNextTransaction();
//				}

				if (series >= theSampleLists.size())
				{
					return null;
				}
				DBSeerTransactionSampleList list = theSampleLists.get(series);
				int time = (int)timestamps[category];
				ArrayList<DBSeerTransactionSample> samples = list.getSamples();
				for (DBSeerTransactionSample sample : samples)
				{
					if (sample.getTimestamp() == time)
					{
						String statement = sample.getStatement();
						if (statement.length() > maxLength)
						{
							return String.format("%.80s...", statement);
						}
						else
						{
							return statement;
						}
					}
				}
				return null;
			}
		});
	}

	public void clearNormal()
	{
		selectedNormal = null;
	}

	public void clearAnomaly()
	{
		selectedAnomaly = null;
	}

	public void setSelectedNormal(Set<XYItemEntity> selected)
	{
		selectedNormal = selected;
		if (selectedAnomaly != null)
		{
			HashSet<XYItemEntity> found = new HashSet<XYItemEntity>();
			for (XYItemEntity entity : selectedAnomaly)
			{
				for (XYItemEntity other : selectedNormal)
				{
					if (entity.getSeriesIndex() == other.getSeriesIndex() &&
							entity.getItem() == other.getItem())
					{
						found.add(entity);
						break;
					}
				}
			}
			selectedAnomaly.removeAll(found);
		}
	}

	public void setSelectedAnomaly(Set<XYItemEntity> selected)
	{
		selectedAnomaly = selected;
		if (selectedNormal != null)
		{
			HashSet<XYItemEntity> found = new HashSet<XYItemEntity>();
			for (XYItemEntity entity : selectedNormal)
			{
				for (XYItemEntity other : selectedAnomaly)
				{
					if (entity.getSeriesIndex() == other.getSeriesIndex() &&
							entity.getItem() == other.getItem())
					{
						found.add(entity);
						break;
					}
				}
			}
			selectedNormal.removeAll(found);
		}
	}

	public Set<XYItemEntity> getSelectedNormal()
	{
		return selectedNormal;
	}

	public Set<XYItemEntity> getSelectedAnomaly()
	{
		return selectedAnomaly;
	}

	@Override
	public Paint getItemPaint(int row, int column)
	{
		if (selectedNormal != null)
		{
			for (XYItemEntity entity : selectedNormal)
			{
				if (entity.getSeriesIndex() == row && entity.getItem() == column)
				{
					return Color.WHITE;
//					return Color.BLUE; // temp
				}
			}
		}
		if (selectedAnomaly != null)
		{
			for (XYItemEntity entity : selectedAnomaly)
			{
				if (entity.getSeriesIndex() == row && entity.getItem() == column)
				{
					return Color.BLACK;
//					return Color.RED; // temp
				}
			}
		}
		return super.getItemPaint(row, column);
	}
}
