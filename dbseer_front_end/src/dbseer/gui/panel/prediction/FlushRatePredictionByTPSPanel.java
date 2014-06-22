package dbseer.gui.panel.prediction;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;

/**
 * Created by dyoon on 2014. 6. 13..
 */
public class FlushRatePredictionByTPSPanel extends JPanel
{
	private JLabel ioConfLabel;
	private JTextField ioConfTextField;

	public FlushRatePredictionByTPSPanel()
	{
		initializeGUI();
	}

	private void initializeGUI()
	{
		this.setLayout(new MigLayout("fill"));
		this.add(new JLabel("Prediction: FlushRatePredictionByTPS"), "wrap");
		ioConfLabel = new JLabel("IO Configuration: ");
		ioConfTextField = new JTextField();
		this.add(ioConfLabel, "wrap");
		this.add(ioConfTextField, "growx");
	}

	public String getIOConf()
	{
		return ioConfTextField.getText();
	}

	public void setIOConf(String conf)
	{
		ioConfTextField.setText(conf);
	}
}
