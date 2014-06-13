package dbseer.gui.panel;

import dbseer.gui.user.DBSeerConfiguration;
import dbseer.gui.DBSeerGUI;
import dbseer.gui.actions.CheckPredictionBoxAction;
import dbseer.gui.frame.DBSeerPredictionFrame;
import dbseer.gui.model.SharedComboBoxModel;
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
 * Created by dyoon on 2014. 6. 3..
 */
public class DBSeerPredictionControlPanel extends JPanel implements ActionListener
{
	public static Set<String> predictionSet = new HashSet<String>();

	private ArrayList<JCheckBox> boxList = new ArrayList<JCheckBox>();
	private JComboBox trainConfigComboBox;
	private JComboBox testConfigComboBox;
	private JComboBox workloadComboBox;
	private JButton predictionButton;
	private JButton selectAllButton;
	private JButton deselectAllButton;

	private JPanel controlPanel;

	private static final int WRAP_COUNT = 2;

	public DBSeerPredictionControlPanel()
	{
		initializeGUI();
	}

	private void initializeGUI()
	{
	 	this.setLayout(new MigLayout());

		trainConfigComboBox = new JComboBox(new SharedComboBoxModel(DBSeerGUI.configs));
		testConfigComboBox = new JComboBox(new SharedComboBoxModel(DBSeerGUI.configs));
		workloadComboBox = new JComboBox(DBSeerGUI.availableWorkloads);

		trainConfigComboBox.setBorder(BorderFactory.createTitledBorder("Train Config"));
		testConfigComboBox.setBorder(BorderFactory.createTitledBorder("Test Config"));
		workloadComboBox.setBorder(BorderFactory.createTitledBorder("Workload"));
		trainConfigComboBox.setPreferredSize(new Dimension(250,100));
		testConfigComboBox.setPreferredSize(new Dimension(250,100));
		workloadComboBox.setPreferredSize(new Dimension(250,100));

		predictionButton = new JButton("Predict");
		selectAllButton = new JButton("Select All");
		deselectAllButton = new JButton("Deselect All");

		predictionButton.addActionListener(this);
		selectAllButton.addActionListener(this);
		deselectAllButton.addActionListener(this);

		controlPanel = new JPanel();
		controlPanel.setLayout(new MigLayout());
		controlPanel.setPreferredSize(new Dimension(300,200));

		controlPanel.add(trainConfigComboBox, "dock north");
		controlPanel.add(testConfigComboBox, "dock north");
		controlPanel.add(workloadComboBox, "dock north");

		controlPanel.add(predictionButton, "cell 0 0");
		controlPanel.add(selectAllButton, "cell 1 0");
		controlPanel.add(deselectAllButton, "cell 2 0");

		this.add(controlPanel, "dock west");

		int count = 0;
		for (String name : DBSeerGUI.availablePredictions)
		{
			JCheckBox box = new JCheckBox(new CheckPredictionBoxAction(name));
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
		if (actionEvent.getSource() == predictionButton)
		{
			final DBSeerConfiguration trainConfig = (DBSeerConfiguration)trainConfigComboBox.getSelectedItem();
			final DBSeerConfiguration testConfig = (DBSeerConfiguration)testConfigComboBox.getSelectedItem();
			final String workload = (String)workloadComboBox.getSelectedItem();

			if (trainConfig == null || testConfig == null)
			{
				return;
			}

			predictionButton.setEnabled(false);
			DBSeerGUI.status.setText("Performing prediction...");

			SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>()
			{
				final String[] predictions = DBSeerPredictionControlPanel.predictionSet.toArray(
						new String[DBSeerPredictionControlPanel.predictionSet.size()]);

				@Override
				protected Void doInBackground() throws Exception
				{
					MatlabProxy proxy = DBSeerGUI.proxy;
					String dbseerPath = DBSeerGUI.userSettings.getDBSeerRootPath();

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

						testConfig.initialize();
						trainConfig.initialize();

						proxy.eval("pc = PredictionCenter;");
						proxy.eval("pc.testConfig = " + testConfig.getUniqueVariableName() + ";");
						proxy.eval("pc.trainConfig = " + trainConfig.getUniqueVariableName() + ";");
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
							DBSeerPredictionFrame predictionFrame = new DBSeerPredictionFrame(predictions, workload);
							predictionFrame.pack();
							predictionFrame.setVisible(true);
							predictionButton.setEnabled(true);
							predictionButton.requestFocus();
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
