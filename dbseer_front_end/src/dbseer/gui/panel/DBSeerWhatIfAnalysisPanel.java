package dbseer.gui.panel;

import dbseer.gui.user.DBSeerConfiguration;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;

/**
 * Created by dyoon on 5/8/15.
 */
public class DBSeerWhatIfAnalysisPanel extends JPanel implements ActionListener
{
	private JComboBox predictionTargetComboBox;
	private JComboBox mixtureComboBox;
	private JComboBox mixtureRatioComboBox;
	private JComboBox transactionTypeComboBox;
	private JComboBox changeDirectionComboBox;
	private JSpinner workloadRatioSpinner;

	private SpinnerNumberModel workloadRatioModel;

	private JLabel questionLabel1;
	private JLabel questionLabel2;
	private JLabel questionLabel3;
	private JLabel questionLabel4;
	private JLabel questionLabel5;
	private JLabel questionLabel6;
	private JLabel questionLabel7;

	private JLabel differentLabel1;
	private JLabel differentLabel2;

	private JPanel questionPanel;
	private JPanel differentMixturePanel;

	private DBSeerPerformancePredictionPanel predictionPanel;

	public static String[] predictionTargets = {"latency", "disk I/O (read/write)", "CPU usage", "disk flush rate"};
	public static String[] actualPredictions = {"WhatIfAnalysisLatency",
			"WhatIfAnalysisIO", "WhatIfAnalysisCPU", "WhatIfAnalysisFlushRate"};
	private static String[] mixtureOptions = {"the same mixture of different transaction types as",
			"a different mixture of transactions than"};
	private static String[] ratioOptions = {"No", "0.25x", "0.5x", "2x", "3x", "4x", "5x", "10x", "Only"};
	private static String[] changeOptions = {"increases", "decreases"};
	public static double[] ratios = {0.0, 0.25, 0.5, 2.0, 3.0, 4.0, 5.0, 10,0, 1.0};

	public static final int TARGET_LATENCY = 0;
	public static final int TARGET_IO = 1;
	public static final int TARGET_CPU = 2;
	public static final int TARGET_FLUSH_RATE = 3;

	public static final int MIXTURE_SAME = 0;
	public static final int MIXTURE_DIFFERENT = 1;

	public static final int RATIO_NO = 0;
	public static final int RATIO_QUARTER = 1;
	public static final int RATIO_HALF = 2;
	public static final int RATIO_TWO = 3;
	public static final int RATIO_THREE = 4;
	public static final int RATIO_FOUR = 5;
	public static final int RATIO_FIVE = 6;
	public static final int RATIO_TEN = 7;
	public static final int RATIO_ONLY = 8;

	public static final int CHANGE_INCREASE = 0;
	public static final int CHANGE_DECREASE = 1;

	public DBSeerWhatIfAnalysisPanel(DBSeerPerformancePredictionPanel predictionPanel)
	{
		this.predictionPanel = predictionPanel;
		this.setLayout(new MigLayout("fill"));
		initialize();
	}

	private void initialize()
	{
		predictionTargetComboBox = new JComboBox(DBSeerWhatIfAnalysisPanel.predictionTargets);
		mixtureComboBox = new JComboBox(DBSeerWhatIfAnalysisPanel.mixtureOptions);
		changeDirectionComboBox = new JComboBox(DBSeerWhatIfAnalysisPanel.changeOptions);

		mixtureComboBox.addActionListener(this);

		workloadRatioModel = new SpinnerNumberModel(100, 0, 10000, 1);
		workloadRatioSpinner = new JSpinner(workloadRatioModel);

		final JFormattedTextField workloadRatioField = ((JSpinner.NumberEditor)workloadRatioSpinner.getEditor()).getTextField();
		NumberFormatter formatter = (NumberFormatter) workloadRatioField.getFormatter();
		DecimalFormat format = new DecimalFormat("0");
		formatter.setFormat(format);
		formatter.setAllowsInvalid(false);

		questionPanel = new JPanel();
		questionPanel.setLayout(new MigLayout());

		differentMixturePanel = new JPanel();
		differentMixturePanel.setLayout(new MigLayout());
		differentMixturePanel.setVisible(false);
		differentMixturePanel.setBorder(BorderFactory.createTitledBorder("Different mixture than the current workload"));

		questionLabel1 = new JLabel("What will be the");
		questionLabel2 = new JLabel("of my database if the number of transactions");
		questionLabel3 = new JLabel("per second");
		questionLabel4 = new JLabel("by");
		questionLabel5 = new JLabel("% compared to the current workload, and ");
		questionLabel6 = new JLabel("if the new workload consists of");
		questionLabel7 = new JLabel("the current workload?");

		this.add(questionPanel, "wrap");

		differentLabel1 = new JLabel("With");
		differentLabel2 = new JLabel("transactions.");
		mixtureRatioComboBox = new JComboBox(ratioOptions);
		transactionTypeComboBox = new JComboBox();

		this.add(differentMixturePanel);

		questionPanel.add(questionLabel1, "split 4");
		questionPanel.add(predictionTargetComboBox, "spanx");
		questionPanel.add(questionLabel2);
		questionPanel.add(questionLabel3, "wrap");
		questionPanel.add(changeDirectionComboBox, "split 4");
		questionPanel.add(questionLabel4);
		questionPanel.add(workloadRatioSpinner);
		questionPanel.add(questionLabel5, "wrap");
		questionPanel.add(questionLabel6, "split 3");
		questionPanel.add(mixtureComboBox);
		questionPanel.add(questionLabel7);

		differentMixturePanel.add(differentLabel1);
		differentMixturePanel.add(mixtureRatioComboBox);
		differentMixturePanel.add(transactionTypeComboBox);
		differentMixturePanel.add(differentLabel2);
	}

	public String getPredictionTarget()
	{
		return (String)predictionTargetComboBox.getSelectedItem();
	}

	public int getSelectedPredictionTargetIndex()
	{
		return predictionTargetComboBox.getSelectedIndex();
	}

	public int getWorkloadRatio()
	{
		int base = 100;
		Number num = (Number)workloadRatioModel.getValue();
		if (changeDirectionComboBox.getSelectedIndex() == CHANGE_INCREASE)
		{
			base += num.intValue();
		}
		else if (changeDirectionComboBox.getSelectedIndex() == CHANGE_DECREASE)
		{
			base -= num.intValue();
			if (base < 0) base = 0;
		}
		return base;
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == mixtureComboBox)
		{
			if (mixtureComboBox.getSelectedIndex() == DBSeerWhatIfAnalysisPanel.MIXTURE_SAME)
			{
				differentMixturePanel.setVisible(false);
			}
			else if (mixtureComboBox.getSelectedIndex() == DBSeerWhatIfAnalysisPanel.MIXTURE_DIFFERENT)
			{
				DBSeerConfiguration trainConfig = predictionPanel.getTrainConfig();
				if (trainConfig != null)
				{
					updateTransactionType(trainConfig);
				}
				differentMixturePanel.setVisible(true);
			}
		}
	}

	public void updateTransactionType(DBSeerConfiguration trainConfig)
	{
		if (trainConfig != null)
		{
			DefaultComboBoxModel newModel = new DefaultComboBoxModel(trainConfig.getDataset().getTransactionTypeNames().toArray());
			transactionTypeComboBox.setModel(newModel);
		}
	}

	public int getMixtureType()
	{
		return mixtureComboBox.getSelectedIndex();
	}

	public int getTransactionType()
	{
		return transactionTypeComboBox.getSelectedIndex();
	}

	public int getMixtureRatio()
	{
		return mixtureRatioComboBox.getSelectedIndex();
	}
}
