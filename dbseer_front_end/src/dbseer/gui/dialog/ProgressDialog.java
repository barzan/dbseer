package dbseer.gui.dialog;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

/**
 * Created by dyoon on 2014. 7. 22..
 */
public class ProgressDialog extends JDialog
{
	private JProgressBar progressBar;

	public ProgressDialog(Frame owner, String title)
	{
		super(owner, title, true);
		initializeGUI();
	}

	private void initializeGUI()
	{
		this.setLayout(new MigLayout("fillx"));
		this.setSize(200, 75);

		progressBar = new JProgressBar(0, 100);
		progressBar.setIndeterminate(true);

		this.add(progressBar, "align center");
	}
}
