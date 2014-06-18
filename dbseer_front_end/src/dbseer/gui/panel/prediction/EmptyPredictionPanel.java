package dbseer.gui.panel.prediction;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;

/**
 * Created by dyoon on 2014. 6. 13..
 */
public class EmptyPredictionPanel extends JPanel
{
	public EmptyPredictionPanel()
	{
		initializeGUI();
	}

	private void initializeGUI()
	{
		this.setLayout(new MigLayout());
		this.add(new JLabel("Choose a prediction"));
	}
}
