package dbseer.gui.panel;

import dbseer.gui.DBSeerDataProfile;
import dbseer.gui.DBSeerGUI;
import dbseer.gui.frame.DBSeerPlotFrame;
import dbseer.gui.actions.CheckPlotTypeAction;

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
	private ArrayList<JCheckBox> boxList = new ArrayList<JCheckBox>();
	private JPanel buttonPanel;
	private JButton plotButton;
	private JButton selectAllButton;
	private JButton deselectAllButton;
	private JComboBox profileComboBox;
	private final int WRAP_COUNT = 3;

	public DBSeerPlotControlPanel()
	{
		initializeGUI();
	}

	private void initializeGUI()
	{
		this.setLayout(new MigLayout());

		buttonPanel = new JPanel();
		buttonPanel.setLayout(new MigLayout());
		buttonPanel.setPreferredSize(new Dimension(300, 200));

		plotButton = new JButton();
		plotButton.setText("Load & Plot");
		plotButton.addActionListener(this);

		selectAllButton = new JButton();
		selectAllButton.setText("Select All");
		selectAllButton.addActionListener(this);

		deselectAllButton = new JButton();
		deselectAllButton.setText("Deselect All");
		deselectAllButton.addActionListener(this);

		profileComboBox = new JComboBox(DBSeerGUI.profiles);
		profileComboBox.setBorder(BorderFactory.createTitledBorder("Choose a profile"));
		profileComboBox.setPreferredSize(new Dimension(250,100));

		buttonPanel.add(profileComboBox, "dock north");
		buttonPanel.add(plotButton, "cell 0 0");
		buttonPanel.add(selectAllButton, "cell 1 0");
		buttonPanel.add(deselectAllButton, "cell 2 0");

		this.add(buttonPanel, "dock west");

		int count = 0;
		for (String name : DBSeerGUI.availableCharts)
		{
			JCheckBox box = new JCheckBox(new CheckPlotTypeAction(name));
			boxList.add(box);
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
			final DBSeerDataProfile profile = (DBSeerDataProfile)profileComboBox.getSelectedItem();

			if (profile == null)
			{
				return;
			}

			plotButton.setEnabled(false);
			DBSeerGUI.status.setText("Plotting...");

			SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>()
			{
				final String[] charts = DBSeerPlotControlPanel.chartsToDraw.toArray(
						new String[DBSeerPlotControlPanel.chartsToDraw.size()]);
				@Override
				protected Void doInBackground() throws Exception
				{
					MatlabProxy proxy = DBSeerGUI.proxy;

					String dbseerPath = DBSeerGUI.root;

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
						profile.loadProfile();
//							proxy.eval("header_path = '" + profile.getHeaderPath() + "';");
//							proxy.eval("monitor_path = '" + profile.getMonitoringDataPath() + "';");
//							proxy.eval("trans_count_path = '" + profile.getTransCountPath() + "';");
//							proxy.eval("avg_latency_path = '" + profile.getAverageLatencyPath() + "';");
//							proxy.eval("percentile_latency_path = '" + profile.getPercentileLatencyPath() + "';");
//							proxy.eval("[header monitor avglat prclat trcount diffMonitor] = load_stats(header_path, " +
//									"monitor_path, trans_count_path, avg_latency_path, percentile_latency_path, " +
//									"0, 0, true);");
						proxy.eval("[mvGrouped mvUngrouped] = load_mv(" +
								profile.getUniqueVariableName() + ".header," +
								profile.getUniqueVariableName() + ".monitor," +
								profile.getUniqueVariableName() + ".averageLatency," +
								profile.getUniqueVariableName() + ".percentileLatency," +
								profile.getUniqueVariableName() + ".transactionCount," +
								profile.getUniqueVariableName() + ".diffedMonitor);");
						proxy.eval("plotter.mv = mvUngrouped;");

					}
					catch (MatlabInvocationException e)
					{
						JOptionPane.showMessageDialog(null, "Error", e.toString(), JOptionPane.ERROR_MESSAGE);
					}
					return null;
				}

				@Override
				protected void done()
				{
					SwingUtilities.invokeLater(new Runnable()
					{
						@Override
						public void run()
						{
							DBSeerPlotFrame plotFrame = new DBSeerPlotFrame(charts);
							plotFrame.pack();
							plotFrame.setVisible(true);
							plotButton.setEnabled(true);
							plotButton.requestFocus();
							DBSeerGUI.status.setText("");
						}
					});
				}
			};

			worker.execute();
		}
		else if (actionEvent.getSource() == selectAllButton)
		{
			for (JCheckBox box : boxList)
			{
				box.setSelected(true);
				for (ActionListener listener : box.getActionListeners())
				{
					listener.actionPerformed(new ActionEvent(box, ActionEvent.ACTION_PERFORMED, null));
				}
			}
		}
		else if (actionEvent.getSource() == deselectAllButton)
		{
			for (JCheckBox box : boxList)
			{
				box.setSelected(false);
				for (ActionListener listener : box.getActionListeners())
				{
					listener.actionPerformed(new ActionEvent(box, ActionEvent.ACTION_PERFORMED, null));
				}
			}
		}
	}
}
