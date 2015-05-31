package dbseer.gui.panel;

import dbseer.gui.user.DBSeerConfiguration;
import dbseer.gui.user.DBSeerDataSet;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dyoon on 5/13/15.
 */
public class DBSeerThrottleAnalysisPanel extends JPanel implements ActionListener
{
	private JLabel questionLabel1;
	private JLabel questionLabel2;
	private JLabel questionLabel3;
	private JLabel questionLabel4;
	private JLabel questionLabel5;
	private JLabel questionLabel6;
	private JLabel selectLabel;

	private JComboBox throttleTypeComboBox;
	private JComboBox transactionTypeComboBox;
	private JComboBox latencyTypeComboBox;
	private JSpinner targetLatencySpinner;

	private JPanel questionPanel;
	private JPanel penaltyPanel;

	private JScrollPane penaltyPanelScrollPane;
	private SpinnerNumberModel targetLatencyModel;
	private ArrayList<JLabel> penaltyLabelList;
	private ArrayList<JSpinner> penaltySpinnerList;

	private static String[] throttleTypes = {"overall workload", "individual transactions"};
	private static String[] latencyTypes = {"average", "median", "99% quantile"};

	public static final int THROTTLE_OVERALL = 0;
	public static final int THROTTLE_INDIVIDUAL = 1;

	public static final int LATENCY_AVERAGE = 0;
	public static final int LATENCY_MEDIAN = 1;
	public static final int LATENCY_99QUANTILE = 2;

	private static final double MIN_LATENCY = 0;
	private static final double LATENCY_STEP = 0.01;

	public DBSeerThrottleAnalysisPanel()
	{
		this.setLayout(new MigLayout(""));
		penaltyLabelList = new ArrayList<JLabel>();
		penaltySpinnerList = new ArrayList<JSpinner>();
		initialize();
	}

	private void initialize()
	{
		questionLabel1 = new JLabel("At what rate should the incoming");
		questionLabel2 = new JLabel("be throttled in order to");
		questionLabel3 = new JLabel("achieve the");
		questionLabel4 = new JLabel("latency of");
		questionLabel5 = new JLabel("milliseconds for the");
		questionLabel6 = new JLabel("transactions?");
		selectLabel = new JLabel("Please select a config first.");

		targetLatencyModel = new SpinnerNumberModel(10.00, MIN_LATENCY, 10000, LATENCY_STEP);
		targetLatencySpinner = new JSpinner(targetLatencyModel);
		targetLatencySpinner.addChangeListener(new ChangeListener()
		{
			@Override
			public void stateChanged(ChangeEvent changeEvent)
			{
				JSpinner s = (JSpinner)changeEvent.getSource();
				SpinnerNumberModel model = (SpinnerNumberModel)s.getModel();
				Number n = model.getNumber();
				if (n.doubleValue() <= MIN_LATENCY)
				{
					s.setValue(MIN_LATENCY);
				}
			}
		});
		final JFormattedTextField latencyField = ((JSpinner.NumberEditor)targetLatencySpinner.getEditor()).getTextField();
		NumberFormatter formatter = (NumberFormatter) latencyField.getFormatter();
		DecimalFormat format = new DecimalFormat("0.00");
		formatter.setFormat(format);
		formatter.setAllowsInvalid(false);

		transactionTypeComboBox = new JComboBox();
		throttleTypeComboBox = new JComboBox(throttleTypes);
		throttleTypeComboBox.addActionListener(this);
		latencyTypeComboBox = new JComboBox(latencyTypes);

		questionPanel = new JPanel();
		penaltyPanel = new JPanel();
		questionPanel.setLayout(new MigLayout());
		penaltyPanel.setLayout(new MigLayout("align center, aligny center"));

		penaltyPanelScrollPane = new JScrollPane();
		penaltyPanelScrollPane.setViewportView(penaltyPanel);
		penaltyPanelScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		penaltyPanelScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		penaltyPanelScrollPane.setMinimumSize(new Dimension(720, 120));
		penaltyPanelScrollPane.setMaximumSize(new Dimension(720, 120));
		penaltyPanelScrollPane.setVisible(false);
		penaltyPanelScrollPane.setBorder(BorderFactory.createTitledBorder("Penalty for throttling each transaction type (0 - 1000)"));

		this.add(questionPanel, "wrap");
		this.add(penaltyPanelScrollPane);

		questionPanel.add(questionLabel1, "split 3");
		questionPanel.add(throttleTypeComboBox);
		questionPanel.add(questionLabel2, "wrap");
		questionPanel.add(questionLabel3, "split 7");
		questionPanel.add(latencyTypeComboBox);
		questionPanel.add(questionLabel4);
		questionPanel.add(targetLatencySpinner);
		questionPanel.add(questionLabel5);
		questionPanel.add(transactionTypeComboBox);
		questionPanel.add(questionLabel6);

		penaltyPanel.add(selectLabel);
	}

	public void updateTransactionType(DBSeerConfiguration trainConfig)
	{
		if (trainConfig != null)
		{
			DefaultComboBoxModel newModel = new DefaultComboBoxModel(trainConfig.getDataset().getTransactionTypeNames().toArray());
			transactionTypeComboBox.setModel(newModel);
		}
	}

	public double getTargetLatency()
	{
		Number num = (Number)targetLatencyModel.getValue();
		return num.doubleValue();
	}

	public int getTransactionTypeIndex()
	{
		return transactionTypeComboBox.getSelectedIndex();
	}

	public int getThrottleType()
	{
		return throttleTypeComboBox.getSelectedIndex();
	}

	public int getLatencyType()
	{
		return latencyTypeComboBox.getSelectedIndex();
	}

	public void updatePenaltyPanel(DBSeerConfiguration config)
	{
		DBSeerDataSet dataset = config.getDataset();

		if (dataset != null)
		{
			List<String> transactionTypeNames = dataset.getTransactionTypeNames();
			penaltyPanel.remove(selectLabel);

			for (JLabel label : penaltyLabelList)
			{
				penaltyPanel.remove(label);
			}
			for (JSpinner spinner : penaltySpinnerList)
			{
				penaltyPanel.remove(spinner);
			}

			penaltyLabelList.clear();
			penaltySpinnerList.clear();
			penaltyPanel.setLayout(new MigLayout("wrap 6"));

			for (String name : transactionTypeNames)
			{
				JLabel label = new JLabel(name);
				SpinnerNumberModel numberModel = new SpinnerNumberModel(0, 0, 1000, 1);
				JSpinner spinner = new JSpinner(numberModel);

				penaltyPanel.add(label);
				penaltyPanel.add(spinner);
				penaltyLabelList.add(label);
				penaltySpinnerList.add(spinner);
			}
		}
	}

	public int getPenalty(int index)
	{
		SpinnerNumberModel model = (SpinnerNumberModel) penaltySpinnerList.get(index).getModel();
		Number number = model.getNumber();
		return number.intValue();
	}

	public String getPenaltyMatrix()
	{
		String matrix = "[";
		for (JSpinner spinner : penaltySpinnerList)
		{
			SpinnerNumberModel model = (SpinnerNumberModel) spinner.getModel();
			Number number = model.getNumber();
			matrix += number.intValue();
			matrix += " ";
		}
		matrix += "]";
		return matrix;
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == throttleTypeComboBox)
		{
			if (getThrottleType() == THROTTLE_OVERALL)
			{
				penaltyPanelScrollPane.setVisible(false);
			}
			else if (getThrottleType() == THROTTLE_INDIVIDUAL)
			{
				penaltyPanelScrollPane.setVisible(true);
			}
		}
	}
}
