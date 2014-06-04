package dbseer.gui.panel;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;

/**
 * Created by dyoon on 2014. 6. 3..
 */
public class DBSeerLoginPanel extends JPanel implements ActionListener
{
	private JTextField ipField;
	private JFormattedTextField portField;
	private JTextField idField;
	private JPasswordField passwordField;
	private JButton loginButton;

	public DBSeerLoginPanel()
	{
		initializeGUI();
	}

	private void initializeGUI()
	{
		this.setLayout(new MigLayout());

		JLabel ipAddressLabel = new JLabel("IP Address:");
		JLabel portLabel = new JLabel("Port:");
		JLabel idLabel = new JLabel("ID:");
		JLabel passwordLabel = new JLabel("Password:");

		ipField = new JTextField(20);

		NumberFormatter portFormatter = new NumberFormatter(NumberFormat.getInstance());
		portFormatter.setValueClass(Integer.class);
		portFormatter.setMinimum(0);
		portFormatter.setMaximum(99999);
		portFormatter.setCommitsOnValidEdit(true);

		portField = new JFormattedTextField(portFormatter);
		portField.setColumns(6);
		idField = new JTextField(20);
		passwordField = new JPasswordField(20);

		loginButton = new JButton("Login");

		this.add(ipAddressLabel, "cell 0 0");
		this.add(ipField, "cell 1 0");
		this.add(portLabel, "cell 2 0");
		this.add(portField, "cell 3 0");
		this.add(idLabel, "cell 0 1");
		this.add(idField, "cell 1 1");
		this.add(passwordLabel, "cell 0 2");
		this.add(passwordField, "cell 1 2");
		this.add(loginButton, "cell 3 2");
	}

	@Override
	public void actionPerformed(ActionEvent actionEvent)
	{
		if (actionEvent.getSource() == loginButton)
		{
			// TODO: login to the middleware.
		}
	}
}
