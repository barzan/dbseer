package dbseer.gui.panel.prediction;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;

/**
 * Created by dyoon on 2014. 6. 22..
 */
public class TransactionCountsToIOPanel extends JPanel
{
	public TransactionCountsToIOPanel()
	{
		initializeGUI();
	}

	private void initializeGUI()
	{
		this.setLayout(new MigLayout("fill"));
		this.add(new JLabel("Prediction: TransactionCountsToIO"));
	}
}
