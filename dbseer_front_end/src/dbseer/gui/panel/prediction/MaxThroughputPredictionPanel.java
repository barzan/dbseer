package dbseer.gui.panel.prediction;

import dbseer.gui.actions.ManuallyChangeFieldAction;
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

	private JButton changeIOConfButton;
	private JButton changeLockConfButton;

	public MaxThroughputPredictionPanel()
	{
		initializeGUI();
	}

	private void initializeGUI()
	{
		this.setLayout(new MigLayout("fill", "[70%][30%]"));
//		this.add(new JLabel("Prediction: MaxThroughputPrediction"), "wrap");
		ioConfLabel = new JLabel("IO Configuration: ");
		ioConfTextField = new JTextField();
		changeIOConfButton = new JButton(new ManuallyChangeFieldAction(ioConfTextField));
		lockConfLabel = new JLabel("Lock Configuration");
		lockConfTextField = new JTextField();
		changeLockConfButton = new JButton(new ManuallyChangeFieldAction(lockConfTextField));
		this.add(ioConfLabel, "wrap");
		this.add(ioConfTextField, "growx");
		this.add(changeIOConfButton, "wrap");
		this.add(lockConfLabel, "wrap");
		this.add(lockConfTextField, "growx");
		this.add(changeLockConfButton);
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

	public String getLockConf() { return lockConfTextField.getText(); }

	public void setLockConf(String conf)
	{
		lockConfTextField.setText(conf);
		if (!conf.isEmpty())
		{
			lockConfTextField.setEnabled(false);
		}
	}
}
