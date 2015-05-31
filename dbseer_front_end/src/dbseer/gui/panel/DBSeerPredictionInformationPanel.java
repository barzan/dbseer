package dbseer.gui.panel;

import dbseer.gui.events.InformationChartMouseListener;
import dbseer.gui.user.DBSeerDataSet;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

/**
 * Created by dyoon on 5/3/15.
 */
public class DBSeerPredictionInformationPanel extends JPanel
{
	private DBSeerPredictionInformationChartPanel chartPanel;
	private InformationChartMouseListener informationChartMouseListener;
	private JLabel selectLabel = new JLabel("Please select a train config.");

	private boolean firstDataset = false;

	public DBSeerPredictionInformationPanel(InformationChartMouseListener informationChartMouseListener)
	{
		this.informationChartMouseListener = informationChartMouseListener;
	}

	public void initialize()
	{
		this.setBorder(BorderFactory.createTitledBorder("Statistics of the selected train config"));
		this.setLayout(new MigLayout("align 50% 50%"));
		this.add(selectLabel);
	}

	public void setDataset(DBSeerDataSet dataset)
	{
		if (!firstDataset)
		{
			this.remove(selectLabel);
			this.setLayout(new MigLayout("fill, ins 0","[grow,fill]"));
			chartPanel = new DBSeerPredictionInformationChartPanel(null);
			chartPanel.addChartMouseListener(informationChartMouseListener);
			informationChartMouseListener.setChartPanel(chartPanel);
			chartPanel.setPreferredSize(new Dimension(640, 300));
			this.add(chartPanel, "grow");
			firstDataset = true;
		}
		chartPanel.renewDataset(dataset);
	}
}
