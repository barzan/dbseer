package dbseer.gui.panel.prediction;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;

/**
 * Created by dyoon on 2014. 6. 21..
 */
public class MaxThroughputPredictionPanel extends JPanel
{
	private JLabel ioConfLabel;
	private JTextField ioConfTextField;
	private JLabel lockConfLabel;
	private JTextField lockConfTextField;

	public MaxThroughputPredictionPanel()
	{
		initializeGUI();
	}

	private void initializeGUI()
	{
		this.setLayout(new MigLayout("fill"));
		this.add(new JLabel("Prediction: MaxThroughputPrediction"), "wrap");
		ioConfLabel = new JLabel("IO Configuration: ");
		ioConfTextField = new JTextField();
		lockConfLabel = new JLabel("Lock Configuration");
		lockConfTextField = new JTextField();
		this.add(ioConfLabel, "wrap");
		this.add(ioConfTextField, "growx, wrap");
		this.add(lockConfLabel, "wrap");
		this.add(lockConfTextField, "growx");
	}

	public String getIOConf()
	{
		return ioConfTextField.getText();
	}

	public void setIOConf(String conf)
	{
		ioConfTextField.setText(conf);
	}

	public String getLockConf() { return lockConfTextField.getText(); }

	public void setLockConf(String conf)
	{
		lockConfTextField.setText(conf);
	}
}
