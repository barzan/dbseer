package dbseer.gui.panel.prediction;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;

/**
 * Created by dyoon on 2014. 6. 22..
 */
public class PhysicalReadPredictionPanel extends JPanel
{
	public PhysicalReadPredictionPanel()
	{
		initializeGUI();
	}

	private void initializeGUI()
	{
		this.setLayout(new MigLayout("fill"));
		this.add(new JLabel("(This prediction only supports test with dataset.)"));
//		this.add(new JLabel("Prediction: PhysicalReadPrediction (Only supports test with dataset)"));
	}
}
