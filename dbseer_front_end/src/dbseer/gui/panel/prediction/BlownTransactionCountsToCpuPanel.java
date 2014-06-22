package dbseer.gui.panel.prediction;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;

/**
 * Created by dyoon on 2014. 6. 22..
 */
public class BlownTransactionCountsToCpuPanel extends JPanel
{
	public BlownTransactionCountsToCpuPanel()
	{
		initializeGUI();
	}

	private void initializeGUI()
	{
		this.setLayout(new MigLayout("fill"));
		this.add(new JLabel("Prediction: BlownTransactionCountsToCpu"));
	}
}
