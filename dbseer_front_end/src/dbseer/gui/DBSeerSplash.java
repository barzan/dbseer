package dbseer.gui;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by dyoon on 2014. 5. 25..
 */
public class DBSeerSplash extends JWindow
{
	public DBSeerSplash() throws InvocationTargetException, InterruptedException
	{
		Dimension screenSize =
				Toolkit.getDefaultToolkit().getScreenSize();
		JLabel status = new JLabel("initializing MATLAB...");
		Font labelFont = status.getFont();

		status.setFont(new Font(labelFont.getName(), Font.PLAIN, 24));
		Dimension labelSize = status.getPreferredSize();
		this.setBounds(screenSize.width/2 - (labelSize.width/2),
				screenSize.height/2 - (labelSize.height/2), 400,200);
		status.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
		this.getContentPane().add(status, BorderLayout.CENTER);

		pack();
		this.setLocationRelativeTo(null);
		setVisible(true);
	}
}
