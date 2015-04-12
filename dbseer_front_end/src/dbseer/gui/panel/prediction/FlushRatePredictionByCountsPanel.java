package dbseer.gui.panel.prediction;

import dbseer.gui.actions.ManuallyChangeFieldAction;
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

	private JButton changeIOConfButton;

	public FlushRatePredictionByCountsPanel()
	{
		initializeGUI();
	}

	private void initializeGUI()
	{
		this.setLayout(new MigLayout("fill", "[70%][30%]"));
//		this.add(new JLabel("Prediction: FlushRatePredictionByCounts"), "wrap");
		ioConfLabel = new JLabel("IO Configuration: ");
		ioConfTextField = new JTextField();
		changeIOConfButton = new JButton(new ManuallyChangeFieldAction(ioConfTextField));
		this.add(ioConfLabel, "wrap");
		this.add(ioConfTextField, "growx");
		this.add(changeIOConfButton, "wrap");

		whichTransactionToPlotLabel = new JLabel("Which transaction type to plot:");
		whichTransactionToPlotTextField = new JTextField();
		this.add(whichTransactionToPlotLabel, "wrap");
		this.add(whichTransactionToPlotTextField, "growx");
	}

	public String getIOConf()
	{
		return ioConfTextField.getText();
	}

	public void setIOConf(String conf)
	{
		ioConfTextField.setText(conf);
		if (!conf.isEmpty())
		{
			ioConfTextField.setEnabled(false);
		}
	}

	public String getWhichTransactiontoPlot()
	{
		return whichTransactionToPlotTextField.getText();
	}
}
