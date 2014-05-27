package dbseer.gui;

import matlabcontrol.MatlabInvocationException;
import matlabcontrol.MatlabProxy;
import matlabcontrol.MatlabProxyFactoryOptions;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by dyoon on 2014. 5. 18..
 */
public class DBSeerPathChoosePanel extends JPanel implements ActionListener
{
	private JButton openButton;
	private JButton loadButton;
	private DBSeerFileLoadDialog fileLoadDialog;
	private JLabel pathToDBSeerLabel;
	private JLabel dataLoadStatus;

	public DBSeerPathChoosePanel()
	{
		super(new MigLayout());

		fileLoadDialog = new DBSeerFileLoadDialog();

		openButton = new JButton("Browse");
		pathToDBSeerLabel = new JLabel();
		pathToDBSeerLabel.setText("Choose DBSeer Root Path");
		pathToDBSeerLabel.setPreferredSize(new Dimension(500, 10));
		openButton.addActionListener(this);

		add(openButton);
		add(pathToDBSeerLabel, "wrap");

		loadButton = new JButton("Load & Plot");
		dataLoadStatus = new JLabel();
		dataLoadStatus.setText("Data not loaded.");
		loadButton.addActionListener(this);

		//add(loadButton);
		//add(dataLoadStatus);
	}

	@Override
	public void actionPerformed(ActionEvent actionEvent)
	{
		if (actionEvent.getSource() == openButton)
		{
			fileLoadDialog.createFileDialog("Select DBSeer Root Directory", DBSeerFileLoadDialog.DIRECTORY_ONLY);
			fileLoadDialog.showDialog();
			if (fileLoadDialog.getFile() != null)
			{
				String rootPath = fileLoadDialog.getFile().getAbsolutePath();
				pathToDBSeerLabel.setText(rootPath);
				DBSeerGUI.config.setRootPath(rootPath);
			}
		}
		if (actionEvent.getSource() == loadButton)
		{
			MatlabProxy proxy = DBSeerGUI.proxy;
			DBSeerConfiguration config = DBSeerGUI.config;
			String dbseerPath = config.getRootPath();

			try
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
				proxy.eval("header_path = '" +  config.getHeaderPath() + "';");
				proxy.eval("monitor_path = '" +  config.getMonitoringDataPath() + "';");
				proxy.eval("trans_count_path = '" + config.getTransCountPath() + "';");
				proxy.eval("avg_latency_path = '" + config.getAverageLatencyPath() + "';");
				proxy.eval("percentile_latency_path = '" + config.getPercentileLatencyPath() + "';");
				proxy.eval("mv = load_mv(header_path, monitor_path, trans_count_path, " +
						"avg_latency_path, percentile_latency_path);");
				proxy.eval("plotter.mv = mv");
			}
			catch (MatlabInvocationException e)
			{
				JOptionPane.showMessageDialog(null, "Error", e.toString(), JOptionPane.ERROR_MESSAGE);
			}

			final String[] chartNames = {"RowsChangedPerWriteMB", "AvgCpuUsage", "ContextSwitches"};

			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					DBSeerPlotFrame plotFrame = new DBSeerPlotFrame(chartNames);
					plotFrame.pack();
					plotFrame.setVisible(true);
				}
			});


		}

	}
}
