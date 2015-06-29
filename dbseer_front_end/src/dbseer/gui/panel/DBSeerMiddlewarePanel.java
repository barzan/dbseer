/*
 * Copyright 2013 Barzan Mozafari
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dbseer.gui.panel;

import dbseer.comp.DataCenter;
import dbseer.gui.DBSeerConstants;
import dbseer.gui.DBSeerExceptionHandler;
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
	private JButton logInOutButton;
	private JLabel refreshRateLabel;
	private JFormattedTextField refreshRateField;

	private JButton applyRefreshRateButton;
	public JButton startMonitoringButton;
	public JButton stopMonitoringButton;

	private boolean isLoggedIn = false;

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

		logInOutButton = new JButton("Login");
		logInOutButton.addActionListener(this);
		startMonitoringButton = new JButton("Start Monitoring");
		startMonitoringButton.addActionListener(this);
		stopMonitoringButton = new JButton("Stop Monitoring");
		stopMonitoringButton.addActionListener(this);

		startMonitoringButton.setEnabled(false);
		stopMonitoringButton.setEnabled(false);

		ipField.setText(DBSeerGUI.userSettings.getLastMiddlewareIP());
		portField.setText(String.valueOf(DBSeerGUI.userSettings.getLastMiddlewarePort()));
		idField.setText(DBSeerGUI.userSettings.getLastMiddlewareID());

		NumberFormatter formatter = new NumberFormatter(NumberFormat.getIntegerInstance());
		formatter.setMinimum(1);
		formatter.setMaximum(120);
		formatter.setAllowsInvalid(false);

		refreshRateLabel = new JLabel("Monitoring Refresh Rate:");
		refreshRateField = new JFormattedTextField(formatter);
		JLabel refreshRateRangeLabel = new JLabel("(1~120 sec)");

		refreshRateField.setText("1");
		applyRefreshRateButton = new JButton("Apply");
		applyRefreshRateButton.addActionListener(this);

		this.add(ipAddressLabel, "cell 0 0 2 1, split 4");
		this.add(ipField);
		this.add(portLabel);
		this.add(portField);
		this.add(idLabel, "cell 0 1");
		this.add(idField, "cell 1 1");
		this.add(passwordLabel, "cell 0 2");
		this.add(passwordField, "cell 1 2");
		this.add(refreshRateLabel, "cell 0 3");
		this.add(refreshRateField, "cell 1 3, growx, split 3");
		this.add(refreshRateRangeLabel);
		this.add(applyRefreshRateButton, "growx");
		this.add(logInOutButton, "cell 0 4 2 1, growx, split 3");
		this.add(startMonitoringButton, "growx");
		this.add(stopMonitoringButton, "growx");
	}

	public void setLogin()
	{
		ipField.setEnabled(false);
		idField.setEnabled(false);
		passwordField.setEnabled(false);
		portField.setEnabled(false);
		logInOutButton.setText("Logout");
		isLoggedIn = true;
	}

	public void setLogout()
	{
		ipField.setEnabled(true);
		idField.setEnabled(true);
		passwordField.setEnabled(true);
		portField.setEnabled(true);
		logInOutButton.setText("Login");
		isLoggedIn = false;
	}

	@Override
	public void actionPerformed(ActionEvent actionEvent)
	{
		final MiddlewareSocket socket = DBSeerGUI.middlewareSocket;
		if (actionEvent.getSource() == logInOutButton)
		{
			if (!isLoggedIn)
			{

				String ip = ipField.getText();
				int port = Integer.parseInt(portField.getText());
				String id = idField.getText();
				String password = String.valueOf(passwordField.getPassword());

				try
				{
					if (!socket.connect(ip, port))
					{
						return;
					}
					if (socket.login(id, password))
					{
						if (socket.isMonitoring())
						{
							DBSeerGUI.middlewareStatus.setText("Middleware Connected: " + socket.getIp() + ":" + socket.getPort() + " as " +
									socket.getId() + " (Monitoring)");
							startMonitoringButton.setEnabled(false);
							stopMonitoringButton.setEnabled(true);
						}
						else
						{
							DBSeerGUI.middlewareStatus.setText("Middleware Connected: " + socket.getIp() + ":" + socket.getPort() + " as " +
									socket.getId());
							startMonitoringButton.setEnabled(true);
							stopMonitoringButton.setEnabled(false);
						}

						// save last login credentials
						DBSeerGUI.userSettings.setLastMiddlewareIP(socket.getIp());
						DBSeerGUI.userSettings.setLastMiddlewarePort(socket.getPort());
						DBSeerGUI.userSettings.setLastMiddlewareID(socket.getId());

						XStreamHelper xmlHelper = new XStreamHelper();
						xmlHelper.toXML(DBSeerGUI.userSettings, DBSeerGUI.settingsPath);

						this.setLogin();
					}
					else
					{
//						JOptionPane.showMessageDialog(null, socket.getErrorMessage(), "Middleware Login Error", JOptionPane.ERROR_MESSAGE);
					}
				}
				catch (Exception e)
				{
					DBSeerExceptionHandler.handleException(e, "Middleware Login Error");
//				JOptionPane.showMessageDialog(null, e.getMessage(), "Middleware Login Error", JOptionPane.ERROR_MESSAGE);
				}
			}
			else // log out
			{
				try
				{
					socket.disconnect();
					startMonitoringButton.setEnabled(false);
					stopMonitoringButton.setEnabled(false);
				}
				catch (Exception e)
				{
					DBSeerExceptionHandler.handleException(e);
				}
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

				boolean getData = true;
				File newRawDatasetDir = null;

				if (datasetName == null)
				{
					getData = false;
				}
				else
				{
					newRawDatasetDir = new File(DBSeerConstants.RAW_DATASET_PATH + File.separator + datasetName);
					while (newRawDatasetDir.exists())
					{
						datasetName = (String) JOptionPane.showInputDialog(this, datasetName + " already exists.\n" +
										"Enter the name of new dataset", "New Dataset",
								JOptionPane.PLAIN_MESSAGE, null, null, "NewDataset");
						newRawDatasetDir = new File(DBSeerConstants.RAW_DATASET_PATH + File.separator + datasetName);
					}
					newRawDatasetDir.mkdirs();
				}

				final boolean downloadData = getData;
				final File datasetDir = newRawDatasetDir;
				final String datasetNameFinal = datasetName;

				SwingWorker<Void, Void> datasetWorker = new SwingWorker<Void, Void>()
				{
					@Override
					protected Void doInBackground() throws Exception
					{
						if (downloadData)
						{
							DBSeerGUI.status.setText("Downloading the monitoring data...");
						}
						if (socket.stopMonitoring(downloadData))
						{
							DBSeerGUI.middlewareStatus.setText("Middleware Connected: " + socket.getIp() + ":" + socket.getPort() + " as " +
									socket.getId());
							DBSeerGUI.liveMonitorPanel.reset();

							if (!downloadData)
							{
								return null;
							}

							byte[] monitoringData = socket.getMonitoringData();
							byte[] buf = new byte[8192];
							int length = 0;

							ByteArrayInputStream byteStream = new ByteArrayInputStream(monitoringData);
							ZipInputStream zipInputStream = new ZipInputStream(byteStream);
							ZipEntry entry = null;

							// unzip the monitor package.
							while ((entry = zipInputStream.getNextEntry()) != null)
							{
								File entryFile = new File(datasetDir + File.separator + entry.getName());
								new File(entryFile.getParent()).mkdirs();

								FileOutputStream out = new FileOutputStream(datasetDir + File.separator + entry.getName());

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


							int confirm = JOptionPane.showConfirmDialog(null,
									"The monitoring data has been downloaded.\nDo you want to proceed and process the downloaded dataset?",
									"Warning",
									JOptionPane.YES_NO_OPTION);

							if (confirm == JOptionPane.YES_OPTION)
							{
								// process the dataset
								DBSeerGUI.status.setText("Processing the dataset...");
								DataCenter dc = new DataCenter(DBSeerConstants.RAW_DATASET_PATH, datasetNameFinal, true);
								if (!dc.parseLogs())
								{
									JOptionPane.showMessageDialog(null, "Failed to parse received monitoring logs", "Error", JOptionPane.ERROR_MESSAGE);
								}

								if (!dc.processDataset())
								{
									JOptionPane.showMessageDialog(null, "Failed to process received dataset", "Error", JOptionPane.ERROR_MESSAGE);
								}

							}
						}
						else
						{
							JOptionPane.showMessageDialog(null, socket.getErrorMessage(), "Middleware Monitoring Error", JOptionPane.ERROR_MESSAGE);
						}
						return null;
					}

					@Override
					protected void done()
					{
						DBSeerGUI.status.setText("");
					}
				};
				datasetWorker.execute();
			}
			catch (Exception e)
			{
				JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				DBSeerGUI.status.setText("");
			}
		}
		else if (actionEvent.getSource() == applyRefreshRateButton)
		{
			int rate = Integer.parseInt(refreshRateField.getText());
			DBSeerGUI.liveMonitorRefreshRate = rate;
		}
	}
}
