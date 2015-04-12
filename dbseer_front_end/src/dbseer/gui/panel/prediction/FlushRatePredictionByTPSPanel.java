package dbseer.gui.panel.prediction;

import dbseer.gui.actions.ManuallyChangeFieldAction;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

/**
 * Created by dyoon on 2014. 6. 13..
 */
public class FlushRatePredictionByTPSPanel extends JPanel
{
	private JLabel ioConfLabel;
	private JTextField ioConfTextField;
	private JButton changeIOConfButton;

	public FlushRatePredictionByTPSPanel()
	{
		initializeGUI();
	}

	private void initializeGUI()
	{
		this.setLayout(new MigLayout("fill", "[70%][30%]"));
//		this.add(new JLabel("Prediction: FlushRatePredictionByTPS"), "wrap");
		ioConfLabel = new JLabel("IO Configuration: ");
		ioConfTextField = new JTextField();
		changeIOConfButton = new JButton(new ManuallyChangeFieldAction(ioConfTextField));
		this.add(ioConfLabel, "wrap");
		this.add(ioConfTextField, "growx");
		this.add(changeIOConfButton);
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
}
