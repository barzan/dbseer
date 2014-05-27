package dbseer.gui;

import dbseer.gui.actions.CheckPlotTypeAction;
import dbseer.gui.actions.OpenPlotFrameAction;
import matlabcontrol.MatlabInvocationException;
import matlabcontrol.MatlabProxy;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by dyoon on 2014. 5. 25..
 */
public class DBSeerPlotControlPanel extends JPanel implements ActionListener
{
	public static Set<String> chartsToDraw = new HashSet<String>();
	private JButton plotButton;
	private final int WRAP_COUNT = 5;
	public DBSeerPlotControlPanel()
	{
		initializeGUI();
	}

	private void initializeGUI()
	{
		this.setLayout(new MigLayout());

		plotButton = new JButton();
		plotButton.setText("Load & Plot");
		plotButton.addActionListener(this);
		this.add(plotButton, "dock west");

		int count = 0;
		for (String name : DBSeerGUI.availableCharts)
		{
			JCheckBox box = new JCheckBox(new CheckPlotTypeAction(name));
			if (++count == WRAP_COUNT)
			{
				this.add(box, "wrap");
				count = 0;
			}
			else
			{
				this.add(box);
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent actionEvent)
	{
		if (actionEvent.getSource() == plotButton)
		{
			MatlabProxy proxy = DBSeerGUI.proxy;
			DBSeerConfiguration config = DBSeerGUI.config;
			String dbseerPath = config.getRootPath();

			try
			{
				if (config.isConfigChanged())
				{
					proxy.eval("rmpath " + dbseerPath + ";");
					proxy.eval("rmpath " + dbseerPath + "/common_mat;");
					proxy.eval("rmpath " + dbseerPath + "/predict_mat;");
					proxy.eval("rmpath " + dbseerPath + "/predict_data;");
					proxy.eval("rmpath " + dbseerPath + "/predict_mat/prediction_center;");

					proxy.eval("addpath " + dbseerPath + ";");
					proxy.eval("addpath " + dbseerPath + "/common_mat;");
					proxy.eval("addpath " + dbseerPath + "/predict_mat;");
					proxy.eval("addpath " + dbseerPath + "/predict_data;");
					proxy.eval("addpath " + dbseerPath + "/predict_mat/prediction_center;");

					proxy.eval("plotter = Plotter;");
					proxy.eval("header_path = '" + config.getHeaderPath() + "';");
					proxy.eval("monitor_path = '" + config.getMonitoringDataPath() + "';");
					proxy.eval("trans_count_path = '" + config.getTransCountPath() + "';");
					proxy.eval("avg_latency_path = '" + config.getAverageLatencyPath() + "';");
					proxy.eval("percentile_latency_path = '" + config.getPercentileLatencyPath() + "';");
					proxy.eval("mv = load_mv(header_path, monitor_path, trans_count_path, " +
							"avg_latency_path, percentile_latency_path);");
					proxy.eval("plotter.mv = mv");
				}
			}
			catch (MatlabInvocationException e)
			{
				JOptionPane.showMessageDialog(null, "Error", e.toString(), JOptionPane.ERROR_MESSAGE);
			}
			final String[] charts = DBSeerPlotControlPanel.chartsToDraw.toArray(
					new String[DBSeerPlotControlPanel.chartsToDraw.size()]);

			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					DBSeerPlotFrame plotFrame = new DBSeerPlotFrame(charts);
					plotFrame.pack();
					plotFrame.setVisible(true);
				}
			});
			config.setConfigChanged(false);
		}
	}
}
