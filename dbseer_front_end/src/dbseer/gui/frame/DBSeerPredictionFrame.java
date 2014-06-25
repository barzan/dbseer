package dbseer.gui.frame;

import dbseer.gui.DBSeerConstants;
import dbseer.gui.chart.DBSeerChartFactory;
import dbseer.gui.comp.PredictionCenter;
import dbseer.gui.dialog.DBSeerFileLoadDialog;
import net.miginfocom.swing.MigLayout;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import javax.swing.*;
import java.awt.*;

/**
 * Created by dyoon on 2014. 6. 9..
 */
public class DBSeerPredictionFrame extends JFrame
{
	private PredictionCenter center;

	public DBSeerPredictionFrame(PredictionCenter center)
	{
		this.center = center;
		initializeGUI();
	}

	private void initializeGUI()
	{
		this.setLayout(new MigLayout("fill"));
		this.setPreferredSize(new Dimension(1024, 768));

		JFreeChart chart = DBSeerChartFactory.createXYLinePredictionChart(center);
		JTable errorTable = DBSeerChartFactory.createErrorTable();
		ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new Dimension(1024, 768));
		String title = center.getPrediction();
		if (center.getTestMode() == DBSeerConstants.TEST_MODE_DATASET)
		{
			title += ", Test with Dataset (" + center.getTestDatasetName() + ") ";
		}
		else
		{
			title += ", Test with Mixture & TPS (" + center.getTestMixture() + ") ";
		}
		switch (center.getGroupingType())
		{
			case DBSeerConstants.GROUP_NONE:
				title += "[Group: None] ";
				break;
			case DBSeerConstants.GROUP_REL_DIFF:
				title += "[Group: Rel. diff (" + center.getAllowedRelativeDiff() + ")] ";
				break;
			case DBSeerConstants.GROUP_NUM_CLUSTER:
				title += "[Group: Clustering (" + center.getNumClusters() + ")] ";
				break;
			case DBSeerConstants.GROUP_RANGE:
				title += "[Group: User-specified range] ";
				break;
			default:
				break;
		}
		switch (center.getGroupingTarget())
		{
			case DBSeerConstants.GROUP_TARGET_INDIVIDUAL_TRANS_COUNT:
				title += "[Target: Individual transactions]";
				break;
			case DBSeerConstants.GROUP_TARGET_TPS:
				title += "[Target: TPS]";
				break;
			default:
				break;
		}
		this.setTitle(title);
		if (errorTable != null)
		{
			this.add(chartPanel, "grow, wrap");
			this.add(errorTable, "growx");
		}
		else
		{
			this.add(chartPanel, "grow");
		}
	}
}
