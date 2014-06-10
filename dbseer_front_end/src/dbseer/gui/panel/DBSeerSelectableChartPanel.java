package dbseer.gui.panel;

import dbseer.gui.frame.DBSeerPlotExplainFrame;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseEvent;

/**
 * Created by dyoon on 2014. 6. 10..
 */
public class DBSeerSelectableChartPanel extends ChartPanel
{
	private final Border lineBorder = BorderFactory.createLineBorder(Color.BLACK, 5);
	private final Insets insets = lineBorder.getBorderInsets(this);
	private final EmptyBorder emptyBorder = new EmptyBorder(insets);

	public DBSeerSelectableChartPanel(JFreeChart chart)
	{
		super(chart);
		this.setBorder(emptyBorder);
		this.setMouseWheelEnabled(true);
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
	public void mouseClicked(MouseEvent event)
	{
		super.mouseClicked(event);

		if (SwingUtilities.isLeftMouseButton(event))
		{
			final JFreeChart chartToExplain = getChart();
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					DBSeerPlotExplainFrame newFrame = new DBSeerPlotExplainFrame(chartToExplain);
					newFrame.pack();
					newFrame.setVisible(true);
				}
			});
		}
	}
}
