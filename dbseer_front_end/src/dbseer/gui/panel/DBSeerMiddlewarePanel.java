package dbseer.gui.panel;

import com.sun.codemodel.internal.JOp;
import dbseer.comp.DataCenter;
import dbseer.gui.DBSeerConstants;
import dbseer.gui.DBSeerGUI;
import dbseer.gui.xml.XStreamHelper;
import dbseer.middleware.MiddlewareSocket;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by dyoon on 2014. 6. 3..
 */
public class DBSeerMiddlewarePanel extends JPanel implements ActionListener
{
	private JTextField ipField;
	private JFormattedTextField portField;
	private JTextField idField;
	private JPasswordField passwordField;
	private JButton loginButton;

	private JButton startMonitoringButton;
	private JButton stopMonitoringButton;

	public DBSeerMiddlewarePanel()
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

		DecimalFormat portFormatter = new DecimalFormat();

		portFormatter.setMaximumFractionDigits(0);
		portFormatter.setMaximumIntegerDigits(5);
		portFormatter.setMinimumIntegerDigits(1);
		portFormatter.setDecimalSeparatorAlwaysShown(false);
		portFormatter.setGroupingUsed(false);

		portField = new JFormattedTextField(portFormatter);
		portField.setColumns(6);
		portField.setText("3334"); // default port.
		idField = new JTextField(20);
		passwordField = new JPasswordField(20);

		loginButton = new JButton("Login");
		loginButton.addActionListener(this);
		startMonitoringButton = new JButton("Start Monitoring");
		startMonitoringButton.addActionListener(this);
		stopMonitoringButton = new JButton("Stop Monitoring");
		stopMonitoringButton.addActionListener(this);

		startMonitoringButton.setEnabled(false);
		stopMonitoringButton.setEnabled(false);

		ipField.setText(DBSeerGUI.userSettings.getLastMiddlewareIP());
		portField.setText(String.valueOf(DBSeerGUI.userSettings.getLastMiddlewarePort()));
		idField.setText(DBSeerGUI.userSettings.getLastMiddlewareID());

		this.add(ipAddressLabel, "cell 0 0 2 1, split 4");
		this.add(ipField);
		this.add(portLabel);
		this.add(portField);
		this.add(idLabel, "cell 0 1");
		this.add(idField, "cell 1 1");
		this.add(passwordLabel, "cell 0 2");
		this.add(passwordField, "cell 1 2");
		this.add(loginButton, "cell 0 3 2 1, split 3");
		this.add(startMonitoringButton);
		this.add(stopMonitoringButton);
	}

	@Override
	public void actionPerformed(ActionEvent actionEvent)
	{
		MiddlewareSocket socket = DBSeerGUI.middlewareSocket;
		if (actionEvent.getSource() == loginButton)
		{
			// TODO: login to the middleware.
			String ip = ipField.getText();
			int port = Integer.parseInt(portField.getText());
			String id = idField.getText();
			String password = String.valueOf(passwordField.getPassword());

			try
			{
				socket.connect(ip, port);
				if (socket.login(id, password))
				{
					if (socket.isMonitoring())
					{
						DBSeerGUI.middlewareStatus.setText("Middleware Connected: " + socket.getIp() + ":" + socket.getPort() + " as " +
								socket.getId() + " (Monitoring)");
					}
					else
					{
						DBSeerGUI.middlewareStatus.setText("Middleware Connected: " + socket.getIp() + ":" + socket.getPort() + " as " +
								socket.getId());
					}

					// save last login credentials
					DBSeerGUI.userSettings.setLastMiddlewareIP(socket.getIp());
					DBSeerGUI.userSettings.setLastMiddlewarePort(socket.getPort());
					DBSeerGUI.userSettings.setLastMiddlewareID(socket.getId());

					XStreamHelper xmlHelper = new XStreamHelper();
					xmlHelper.toXML(DBSeerGUI.userSettings, DBSeerGUI.settingsPath);

					startMonitoringButton.setEnabled(true);
				}
				else
				{
					JOptionPane.showMessageDialog(null, socket.getErrorMessage(), "Middleware Login Error", JOptionPane.ERROR_MESSAGE);
				}
			}
			catch (Exception e)
			{
				JOptionPane.showMessageDialog(null, e.getMessage(), "Middleware Login Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		else if (actionEvent.getSource() == startMonitoringButton)
		{
			try
			{
				if (socket.startMonitoring())
				{
					DBSeerGUI.middlewareStatus.setText("Middleware Connected: " + socket.getIp() + ":" + socket.getPort() + " as " +
							socket.getId() + " (Monitoring)");
					startMonitoringButton.setEnabled(false);
					stopMonitoringButton.setEnabled(true);
				}
				else
				{
					JOptionPane.showMessageDialog(null, socket.getErrorMessage(), "Middleware Monitoring Error", JOptionPane.ERROR_MESSAGE);
				}
			}
			catch (IOException e)
			{
				JOptionPane.showMessageDialog(null, e.getMessage(), "Middleware Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		else if (actionEvent.getSource() == stopMonitoringButton)
		{
			try
			{
				startMonitoringButton.setEnabled(true);
				stopMonitoringButton.setEnabled(false);

				if (!socket.isMonitoring())
				{
					return;
				}

				File rawDatasetDir = new File(DBSeerConstants.RAW_DATASET_PATH);
				if (!rawDatasetDir.exists())
				{
					rawDatasetDir.mkdirs();
				}

				String datasetName = (String)JOptionPane.showInputDialog(this, "Enter the name of new dataset", "New Dataset",
						JOptionPane.PLAIN_MESSAGE, null, null, "NewDataset");

				File newRawDatasetDir = new File (DBSeerConstants.RAW_DATASET_PATH + File.separator + datasetName);
				while (newRawDatasetDir.exists())
				{
					datasetName = (String)JOptionPane.showInputDialog(this, datasetName + " already exists.\n" +
									"Enter the name of new dataset", "New Dataset",
							JOptionPane.PLAIN_MESSAGE, null, null, "NewDataset");
					newRawDatasetDir = new File (DBSeerConstants.RAW_DATASET_PATH + File.separator + datasetName);
				}
				newRawDatasetDir.mkdirs();

				if (socket.stopMonitoring())
				{
					DBSeerGUI.middlewareStatus.setText("Middleware Connected: " + socket.getIp() + ":" + socket.getPort() + " as " +
							socket.getId());

					byte[] monitoringData = socket.getMonitoringData();
					byte[] buf = new byte[8192];
					int length = 0;

					ByteArrayInputStream byteStream = new ByteArrayInputStream(monitoringData);
					ZipInputStream zipInputStream = new ZipInputStream(byteStream);
					ZipEntry entry = null;

					// unzip the monitor package.
					while ((entry = zipInputStream.getNextEntry()) != null)
					{
						File entryFile = new File(newRawDatasetDir + File.separator + entry.getName());
						new File(entryFile.getParent()).mkdirs();

						FileOutputStream out = new FileOutputStream(newRawDatasetDir + File.separator + entry.getName());

						try
						{
							while ((length = zipInputStream.read(buf, 0, 8192)) >= 0)
							{
								out.write(buf, 0, length);
							}
						}
						catch (EOFException e)
						{
							// do nothing
						}

						//zipInputStream.closeEntry();
						out.flush();
						out.close();
					}
					zipInputStream.close();

					// process the dataset
					DataCenter dc = new DataCenter(DBSeerConstants.RAW_DATASET_PATH, datasetName, true);
					if (!dc.parseLogs())
					{
						JOptionPane.showMessageDialog(null, "Failed to parse received monitoring logs", "Error", JOptionPane.ERROR_MESSAGE);
					}

					if (!dc.processDataset())
					{
						JOptionPane.showMessageDialog(null, "Failed to process received dataset", "Error", JOptionPane.ERROR_MESSAGE);
						DBSeerGUI.status.setText("");
					}
					DBSeerGUI.status.setText("");
				}
				else
				{
					JOptionPane.showMessageDialog(null, socket.getErrorMessage(), "Middleware Monitoring Error", JOptionPane.ERROR_MESSAGE);
				}
			}
			catch (IOException e)
			{
				JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				DBSeerGUI.status.setText("");
			}
		}
	}
}
