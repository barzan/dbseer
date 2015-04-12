package dbseer.gui.panel.prediction;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;

/**
 * Created by dyoon on 2014. 6. 22..
 */
public class TransactionCountsToCpuByCountsPanel extends JPanel
{
	private JLabel whichTransactionToPlotLabel;
	private JTextField whichTransactionToPlotTextField;

	public TransactionCountsToCpuByCountsPanel()
	{
		initializeGUI();
	}

	private void initializeGUI()
	{
		this.setLayout(new MigLayout("fill"));
//		this.add(new JLabel("Prediction: TransactionCountsToCpuByCounts"), "wrap");

		whichTransactionToPlotLabel = new JLabel("Which transaction type to plot:");
		whichTransactionToPlotTextField = new JTextField();
		this.add(whichTransactionToPlotLabel, "wrap");
		this.add(whichTransactionToPlotTextField, "growx");
	}

	public String getWhichTransactiontoPlot()
	{
		return whichTransactionToPlotTextField.getText();
	}
}
