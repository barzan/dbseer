package dbseer.gui.actions;

import dbseer.gui.frame.DBSeerShowQueryFrame;
import dbseer.gui.user.DBSeerDataSet;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Created by dyoon on 14. 11. 26..
 */
public class ShowQueryAction extends AbstractAction
{
	private DBSeerDataSet dataset;
	private int series;
	private int category;
	private double[] timestamp;
	private boolean showAll;

	public ShowQueryAction()
	{
		super("Show Queries");
		this.showAll = false;
	}

	public void setDataset(DBSeerDataSet dataset)
	{
		this.dataset = dataset;
	}

	public void setSeries(int series)
	{
		this.series = series;
	}

	public void setCategory(int category)
	{
		this.category = category;
	}

	public void setTimestamp(double[] timestamp)
	{
		this.timestamp = timestamp;
	}

	public boolean isShowAll()
	{
		return showAll;
	}

	public void setShowAll(boolean showAll)
	{
		this.showAll = showAll;
	}

	@Override
	public void actionPerformed(ActionEvent actionEvent)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				DBSeerShowQueryFrame queryFrame = new DBSeerShowQueryFrame(dataset, series, category, timestamp, showAll);
				if (queryFrame.isQueryAvailable())
				{
					queryFrame.pack();
					queryFrame.setVisible(true);
				}
				else
				{
					JOptionPane.showMessageDialog(null, "Queries for the highlighted point are not available.", "Warning",
							JOptionPane.WARNING_MESSAGE);
				}
			}
		});
	}
}
