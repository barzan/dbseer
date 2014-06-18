package dbseer.gui.panel.prediction;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;

/**
 * Created by dyoon on 2014. 6. 18..
 */
public class FlushRatePredictionByCountsPanel extends JPanel
{
	private JLabel ioConfLabel;
	private JTextField ioConfTextField;
	private JLabel whichTransactionToPlotLabel;
	private JTextField whichTransactionToPlotTextField;

	public FlushRatePredictionByCountsPanel()
	{
		initializeGUI();
	}

	private void initializeGUI()
	{
		this.setLayout(new MigLayout("fill"));
		this.add(new JLabel("Prediction: FlushRatePredictionByCounts"), "wrap");
		ioConfLabel = new JLabel("IO Configuration: ");
		ioConfTextField = new JTextField();
		this.add(ioConfLabel, "wrap");
		this.add(ioConfTextField, "growx, wrap");

		whichTransactionToPlotLabel = new JLabel("Which transaction type to plot:");
		whichTransactionToPlotTextField = new JTextField();
		this.add(whichTransactionToPlotLabel, "wrap");
		this.add(whichTransactionToPlotTextField, "growx");
	}

	public String getIOConf()
	{
		return ioConfTextField.getText();
	}

	public String getWhichTransactiontoPlot()
	{
		return whichTransactionToPlotTextField.getText();
	}
}
