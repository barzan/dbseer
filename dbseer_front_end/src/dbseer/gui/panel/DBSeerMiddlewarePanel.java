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
import dbseer.comp.process.transaction.TransactionLogProcessor;
import dbseer.comp.process.transaction.mysql.MySQLTransactionLogProcessor;
import dbseer.comp.process.live.LiveLogProcessor;
import dbseer.comp.process.system.SystemLogProcessor;
import dbseer.comp.process.system.dstat.DstatSystemLogProcessor;
import dbseer.gui.DBSeerConstants;
import dbseer.gui.DBSeerExceptionHandler;
import dbseer.gui.DBSeerGUI;
import dbseer.gui.actions.OpenDirectoryAction;
import dbseer.gui.actions.SaveSettingsAction;
import dbseer.gui.user.DBSeerDataSet;
import dbseer.gui.xml.XStreamHelper;
import dbseer.middleware.MiddlewareClientRunner;
import dbseer.middleware.event.MiddlewareClientEvent;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.io.FileUtils;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by dyoon on 2014. 6. 3..
 */
public class DBSeerMiddlewarePanel extends JPanel implements ActionListener, Observer
{
	private String id;
	private String password;
	private String ip;
	private int port;
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

//	private String liveDatasetPath;
	private String currentDatasetPath;
	private MiddlewareClientRunner runner;
	private LiveLogProcessor liveLogProcessor;
//	private TransactionLogProcessor transactionLogProcessor;
//	private SystemLogProcessor systemLogProcessor;

	private ArrayList<DBSeerDataSet> currentDatasets = new ArrayList<DBSeerDataSet>();


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
		portField.setText("3555"); // default port.
		idField = new JTextField(20);
		passwordField = new JPasswordField(20);

		logInOutButton = new JButton("Login");
		logInOutButton.addActionListener(this);
		startMonitoringButton = new JButton("Start Monitoring");
		startMonitoringButton.addActionListener(this);
		stopMonitoringButton = new JButton("Stop Monitoring");
		stopMonitoringButton.addActionListener(this);

		startMonitoringButton.setEnabled(true);
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
		this.add(idLabel, "cell 0 2");
		this.add(idField, "cell 1 2");
		this.add(passwordLabel, "cell 0 3");
		this.add(passwordField, "cell 1 3");
		this.add(refreshRateLabel, "cell 0 4");
		this.add(refreshRateField, "cell 1 4, growx, split 3");
		this.add(refreshRateRangeLabel);
		this.add(applyRefreshRateButton, "growx, wrap");
//		this.add(logInOutButton, "cell 0 2 2 1, growx, split 3");
		this.add(startMonitoringButton);
		this.add(stopMonitoringButton);
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
		try
		{
			if (actionEvent.getSource() == startMonitoringButton)
			{
				id = idField.getText();
				password = String.valueOf(passwordField.getPassword());
				ip = ipField.getText();
				port = Integer.parseInt(portField.getText());
//				liveDatasetPath = DBSeerGUI.userSettings.getDBSeerRootPath() + File.separator +
//						DBSeerConstants.LIVE_DATASET_PATH;
				String date = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
				currentDatasetPath = DBSeerGUI.userSettings.getDBSeerRootPath() + File.separator +
					DBSeerConstants.ROOT_DATASET_PATH + File.separator + date;

				final File newDatasetDirectory = new File(currentDatasetPath);

				// create new dataset directory
				FileUtils.forceMkdir(newDatasetDirectory);

				if (newDatasetDirectory == null || !newDatasetDirectory.isDirectory())
				{
					JOptionPane.showMessageDialog(DBSeerGUI.mainFrame,
							String.format("We could not create the dataset directory: %s", currentDatasetPath),
							"Message", JOptionPane.PLAIN_MESSAGE);
					return;
				}

				if (runner != null)
				{
					runner.stop();
				}

				DBSeerGUI.liveMonitorPanel.reset();
				DBSeerGUI.liveMonitorInfo.reset();

				DBSeerGUI.middlewareStatus.setText("Middleware: Connecting...");
				startMonitoringButton.setEnabled(false);
				stopMonitoringButton.setEnabled(false);

				runner = new MiddlewareClientRunner(id, password, ip, port, currentDatasetPath, this);
				runner.run();

				int sleepCount = 0;
				while (liveLogProcessor == null || !liveLogProcessor.isStarted())
				{
					Thread.sleep(250);
					sleepCount += 250;
					if (sleepCount > 5000)
					{
						JOptionPane.showMessageDialog(DBSeerGUI.mainFrame,
								String.format("Failed to receive live logs."),
								"Message", JOptionPane.PLAIN_MESSAGE);
						runner.stop();
						liveLogProcessor = null;
						return;
					}
				}

				currentDatasets.clear();

				String[] servers = liveLogProcessor.getServers();
				for (String s : servers)
				{
					DBSeerDataSet newDataset = new DBSeerDataSet();
					newDataset.setName(date + "_" + s);
					OpenDirectoryAction openDir = new OpenDirectoryAction(newDataset);
					openDir.openWithoutDialog(new File(newDatasetDirectory + File.separator + s));
					DBSeerGUI.datasets.addElement(newDataset);
					newDataset.setCurrent(true);
					currentDatasets.add(newDataset);
				}
				if (servers.length > 1)
				{
					DBSeerDataSet newDataset = new DBSeerDataSet();
					newDataset.setName(date + "_all");
					OpenDirectoryAction openDir = new OpenDirectoryAction(newDataset);
					openDir.openWithoutDialog(newDatasetDirectory);
					DBSeerGUI.datasets.addElement(newDataset);
					newDataset.setCurrent(true);
					currentDatasets.add(newDataset);
				}

				// save last middleware connection
				DBSeerGUI.userSettings.setLastMiddlewareIP(ip);
				DBSeerGUI.userSettings.setLastMiddlewarePort(port);

				XStreamHelper xmlHelper = new XStreamHelper();
				xmlHelper.toXML(DBSeerGUI.userSettings, DBSeerGUI.settingsPath);
			}
			else if (actionEvent.getSource() == stopMonitoringButton)
			{
				int stopMonitoring = JOptionPane.showConfirmDialog(DBSeerGUI.mainFrame, "Do you really want to stop monitoring?",
						"Stop Monitoring", JOptionPane.YES_NO_OPTION);

				if (stopMonitoring == JOptionPane.YES_OPTION)
				{
					if (runner != null)
					{
						runner.stop();
					}
					if (liveLogProcessor != null)
					{
						liveLogProcessor.stop();
					}

					for (DBSeerDataSet dataset : currentDatasets)
					{
						dataset.setCurrent(false);
					}
					boolean isRemoved = false;
					if (DBSeerGUI.dbscan == null ||
							(DBSeerGUI.dbscan != null && !DBSeerGUI.dbscan.isInitialized()))
					{
						JOptionPane.showMessageDialog(DBSeerGUI.mainFrame,
								String.format("Not enough transactions for clustering. You need at least %d transactions. Datasets are removed.", DBSeerGUI.settings.dbscanInitPts),
								"Message", JOptionPane.PLAIN_MESSAGE);

						for (DBSeerDataSet dataset : currentDatasets)
						{
							DBSeerGUI.datasets.removeElement(dataset);
						}
						isRemoved = true;
					}

					if (!isRemoved)
					{
						if (liveLogProcessor != null && !liveLogProcessor.isTxWritingStarted())
						{
							JOptionPane.showMessageDialog(DBSeerGUI.mainFrame,
									String.format("Live monitoring has not written any transactions yet. Datasets are removed."),
									"Message", JOptionPane.PLAIN_MESSAGE);

							for (DBSeerDataSet dataset : currentDatasets)
							{
								DBSeerGUI.datasets.removeElement(dataset);
							}
						}
					}
					currentDatasets.clear();

					if (liveLogProcessor != null)
					{
						liveLogProcessor = null;
					}

					DBSeerGUI.liveMonitorPanel.reset();

					startMonitoringButton.setEnabled(true);
					stopMonitoringButton.setEnabled(false);
					DBSeerGUI.middlewareStatus.setText("Middleware: Not Connected");
				}
//				if (DBSeerGUI.dbscan == null ||
//						(DBSeerGUI.dbscan != null && !DBSeerGUI.dbscan.isInitialized()))
//				{
//					JOptionPane.showMessageDialog(DBSeerGUI.mainFrame,
//							String.format("Not enough transactions for clustering. You need at least %d transactions. Dataset is not saved.", DBSeerGUI.settings.dbscanInitPts),
//							"Message", JOptionPane.PLAIN_MESSAGE);
//
//					DBSeerGUI.liveMonitorPanel.reset();
//					DBSeerGUI.liveMonitorInfo.reset();
//
//					startMonitoringButton.setEnabled(true);
//					stopMonitoringButton.setEnabled(false);
//					DBSeerGUI.middlewareStatus.setText("Middleware: Not Connected");
//
//					return;
//				}
//				if (!liveLogProcessor.isTxWritingStarted())
//				{
//					JOptionPane.showMessageDialog(DBSeerGUI.mainFrame,
//							String.format("Live monitoring has not written any transactions yet. Dataset is not saved."),
//							"Message", JOptionPane.PLAIN_MESSAGE);
//
//					DBSeerGUI.liveMonitorPanel.reset();
//					DBSeerGUI.liveMonitorInfo.reset();
//
//					startMonitoringButton.setEnabled(true);
//					stopMonitoringButton.setEnabled(false);
//					DBSeerGUI.middlewareStatus.setText("Middleware: Not Connected");
//
//					return;
//				}
//
//				int saveResult = JOptionPane.showConfirmDialog(DBSeerGUI.mainFrame, "Do you want to save the monitored data?",
//						"Save monitored data as a dataset", JOptionPane.YES_NO_OPTION);
//
//				if (saveResult == JOptionPane.YES_OPTION)
//				{
//					String date = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
//					String newDatasetPath = DBSeerGUI.userSettings.getDBSeerRootPath() + File.separator +
//							DBSeerConstants.ROOT_DATASET_PATH + File.separator + date;
//					File liveDatasetDirectory = new File(liveDatasetPath);
//					final File newDatasetDirectory = new File(newDatasetPath);
//
//					// create new dataset directory
//					FileUtils.forceMkdir(newDatasetDirectory);
//
//					// copy dataset
//					FileUtils.copyDirectory(liveDatasetDirectory, newDatasetDirectory, false);
//
//					// show message dialog.
//					JOptionPane.showMessageDialog(DBSeerGUI.mainFrame,
//							String.format("Dataset has been successfully saved under '%s'", newDatasetDirectory.getCanonicalPath()),
//							"Message", JOptionPane.PLAIN_MESSAGE);
//
//					String[] servers = liveLogProcessor.getServers();
//					for (String s : servers)
//					{
//						DBSeerDataSet newDataset = new DBSeerDataSet();
//						newDataset.setName(date + "_" + s);
//						OpenDirectoryAction openDir = new OpenDirectoryAction(newDataset);
//						openDir.openWithoutDialog(new File(newDatasetDirectory + File.separator + s));
//						DBSeerGUI.datasets.addElement(newDataset);
//					}
//					if (servers.length > 1)
//					{
//						DBSeerDataSet newDataset = new DBSeerDataSet();
//						newDataset.setName(date + "_all");
//						OpenDirectoryAction openDir = new OpenDirectoryAction(newDataset);
//						openDir.openWithoutDialog(newDatasetDirectory);
//						DBSeerGUI.datasets.addElement(newDataset);
//					}
//					SaveSettingsAction saveSettings = new SaveSettingsAction();
//					saveSettings.actionPerformed(new ActionEvent(this, 0, ""));
//				}

			}
			else if (actionEvent.getSource() == applyRefreshRateButton)
			{
				int rate = Integer.parseInt(refreshRateField.getText());
				DBSeerGUI.liveMonitorRefreshRate = rate;
			}
		}
		catch (Exception e)
		{
			DBSeerExceptionHandler.handleException(e);
		}
		// old implementation
		/*
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
						if (socket.isMonitoring(true))
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
						JOptionPane.showMessageDialog(null, socket.getErrorMessage(), "Middleware Login Error", JOptionPane.ERROR_MESSAGE);
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

				if (!socket.isMonitoring(false))
				{
					return;
				}

				final String datasetRootPath = DBSeerGUI.userSettings.getDBSeerRootPath() + File.separator +
						DBSeerConstants.ROOT_DATASET_PATH;
				final String liveDatasetPath = DBSeerGUI.userSettings.getDBSeerRootPath() + File.separator +
						DBSeerConstants.LIVE_DATASET_PATH;

				final File rawDatasetDir = new File(datasetRootPath);
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
//					newRawDatasetDir = new File(DBSeerConstants.RAW_DATASET_PATH + File.separator + datasetName);
					newRawDatasetDir = new File(datasetRootPath + File.separator + datasetName);
					while (newRawDatasetDir.exists())
					{
						datasetName = (String) JOptionPane.showInputDialog(this, datasetName + " already exists.\n" +
										"Enter the name of new dataset", "New Dataset",
								JOptionPane.PLAIN_MESSAGE, null, null, "NewDataset");
						newRawDatasetDir = new File(datasetRootPath + File.separator + datasetName);
					}
					newRawDatasetDir.mkdirs();
				}

				final boolean downloadData = getData;
				final File datasetDir = newRawDatasetDir;
				final String datasetNameFinal = datasetName;
				final JPanel middlewarePanel = this;
				final JButton logButton = logInOutButton;
				final JButton startButton = startMonitoringButton;

				SwingWorker<Void, Void> datasetWorker = new SwingWorker<Void, Void>()
				{
					@Override
					protected Void doInBackground() throws Exception
					{
						if (downloadData)
						{
							DBSeerGUI.status.setText("Stopping monitoring...");
							middlewarePanel.setEnabled(false);
							logButton.setEnabled(false);
							startButton.setEnabled(false);
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

//							File logFile = socket.getLogFile();
//							byte[] buf = new byte[8192];
//							int length = 0;
//
//							FileInputStream byteStream = new FileInputStream(logFile);
//							ZipInputStream zipInputStream = new ZipInputStream(byteStream);
//							ZipEntry entry = null;
//
//							// unzip the monitor package.
//							while ((entry = zipInputStream.getNextEntry()) != null)
//							{
//								File entryFile = new File(liveDatasetPath + File.separator + entry.getName());
//								new File(entryFile.getParent()).mkdirs();
//
//								FileOutputStream out = new FileOutputStream(liveDatasetPath + File.separator + entry.getName());
//
//								try
//								{
//									while ((length = zipInputStream.read(buf, 0, 8192)) >= 0)
//									{
//										out.write(buf, 0, length);
//									}
//								}
//								catch (EOFException e)
//								{
//									// do nothing
//								}
//
//								//zipInputStream.closeEntry();
//								out.flush();
//								out.close();
//							}
//							zipInputStream.close();

							// move dataset from 'temp' to user-specified directory
							File liveDir = new File(DBSeerGUI.userSettings.getDBSeerRootPath() + File.separator + DBSeerConstants.LIVE_DATASET_PATH);
							File[] files = liveDir.listFiles();
							for (File f : files)
							{
								FileUtils.moveFileToDirectory(f, datasetDir, false);
							}

							// We may not need to process the data after all?
//							int confirm = JOptionPane.showConfirmDialog(null,
//									"The monitoring data has been downloaded.\n" +
//											"Do you want to proceed and process the downloaded dataset?",
//									"Warning",
//									JOptionPane.YES_NO_OPTION);
//
//							if (confirm == JOptionPane.YES_OPTION)
//							{
//								// process the dataset
//								DBSeerGUI.status.setText("Processing the dataset...");
//								DataCenter dc = new DataCenter(DBSeerConstants.ROOT_DATASET_PATH, datasetNameFinal, true);
//								if (!dc.parseLogs())
//								{
//									JOptionPane.showMessageDialog(null, "Failed to parse received monitoring logs", "Error", JOptionPane.ERROR_MESSAGE);
//								}
//
//								if (!dc.processDataset())
//								{
//									JOptionPane.showMessageDialog(null, "Failed to process received dataset", "Error", JOptionPane.ERROR_MESSAGE);
//								}
//							}
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
						middlewarePanel.setEnabled(true);
						logButton.setEnabled(true);
						startButton.setEnabled(true);
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
		*/
	}

	@Override
	public void update(Observable o, Object arg)
	{
		MiddlewareClientEvent event = (MiddlewareClientEvent) arg;
		if (event.event == MiddlewareClientEvent.IS_MONITORING)
		{
			startMonitoringButton.setEnabled(false);
			stopMonitoringButton.setEnabled(true);
			DBSeerGUI.middlewareStatus.setText("Middleware: Monitoring @ " + ip + ":" + port);

			liveLogProcessor = new LiveLogProcessor(currentDatasetPath, event.serverStr);
			try
			{
				liveLogProcessor.start();
			}
			catch (Exception e)
			{
				DBSeerExceptionHandler.handleException(e);
				try
				{
					liveLogProcessor.stop();
					liveLogProcessor.reset();
				}
				catch (Exception e1)
				{
					DBSeerExceptionHandler.handleException(e1);
				}
			}
		}
		else if (event.event == MiddlewareClientEvent.IS_NOT_MONITORING)
		{
			startMonitoringButton.setEnabled(true);
			stopMonitoringButton.setEnabled(false);
			DBSeerGUI.liveMonitorPanel.reset();
			DBSeerGUI.liveMonitorInfo.reset();
			DBSeerGUI.middlewareStatus.setText("Middleware: Not Connected");
			try
			{
				if (liveLogProcessor != null)
				{
					liveLogProcessor.stop();
					liveLogProcessor.reset();
				}
			}
			catch (Exception e)
			{
				DBSeerExceptionHandler.handleException(e);
			}

			if (!event.serverStr.isEmpty())
			{
				DBSeerExceptionHandler.showDialog(event.serverStr);
			}
		}
		else if (event.event == MiddlewareClientEvent.ERROR)
		{
			if (event.e != null)
			{
				DBSeerExceptionHandler.handleException(event.e);
			}
			else
			{
				DBSeerExceptionHandler.showDialog("Something went wrong with the middleware. :(");
			}
			startMonitoringButton.setEnabled(true);
			stopMonitoringButton.setEnabled(false);
			DBSeerGUI.liveMonitorPanel.reset();
			DBSeerGUI.liveMonitorInfo.reset();
			DBSeerGUI.middlewareStatus.setText("Middleware: Not Connected");
			try
			{
				if (liveLogProcessor != null)
				{
					liveLogProcessor.stop();
					liveLogProcessor.reset();
				}
			}
			catch (Exception e)
			{
				DBSeerExceptionHandler.handleException(e);
			}
		}
	}
}
