package dbseer.gui;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.io.StreamException;
import dbseer.comp.LiveMonitoringThread;
import dbseer.comp.data.LiveMonitor;
import dbseer.gui.frame.DBSeerMainFrame;
import dbseer.gui.panel.DBSeerLiveMonitorPanel;
import dbseer.gui.panel.DBSeerMiddlewarePanel;
import dbseer.gui.user.DBSeerConfiguration;
import dbseer.gui.user.DBSeerDataSet;
import dbseer.gui.user.DBSeerUserSettings;
import dbseer.gui.xml.XStreamHelper;
import dbseer.middleware.MiddlewareSocket;
import dbseer.stat.MatlabRunner;
import dbseer.stat.OctaveRunner;
import dbseer.stat.StatisticalPackageRunner;
import dk.ange.octave.exception.OctaveIOException;
import dk.ange.octave.type.Octave;
import matlabcontrol.*;
import org.ini4j.Ini;

import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by dyoon on 2014. 5. 17..
 */
public class DBSeerGUI
{
	private static final String DEFAULT_SETTING_FILE_PATH = "./settings.xml";

	private static final String DEFAULT_INI_FILE_PATH = "./dbseer.ini";

	public static DBSeerMainFrame mainFrame;

	public static String settingsPath = DEFAULT_SETTING_FILE_PATH;

	@XStreamAlias("dbseer")
	public static DBSeerUserSettings userSettings = new DBSeerUserSettings();

//	public static SharedComboBoxModel configs = new SharedComboBoxModel(new DefaultListModel());
	public static DefaultListModel configs = new DefaultListModel();

	//public static ArrayList<DBSeerDataProfile> datasets = new ArrayList<DBSeerDataProfile>();
	public static DefaultListModel datasets = new DefaultListModel();

	public static DBSeerConfiguration testConfig = null;

	public static DBSeerConfiguration trainConfig = null;

	public static StatisticalPackageRunner runner = null;

	public static JLabel status = new JLabel();

	public static JLabel explainStatus = new JLabel();

	public static JLabel middlewareStatus = new JLabel();

	public static MiddlewareSocket middlewareSocket = new MiddlewareSocket();

	public static DBSeerDataSet currentDataset = null;

	public static DBSeerMiddlewarePanel middlewarePanel = null;

	public static LiveMonitor liveMonitor = new LiveMonitor();

	public static DBSeerLiveMonitorPanel liveMonitorPanel = new DBSeerLiveMonitorPanel();

	public static LiveMonitoringThread liveMonitoringThread = new LiveMonitoringThread();

	public static boolean isProxyRenewing = false;

	public static int liveMonitorRefreshRate = 1;

	public static int whichStatisticalPackageToUse = DBSeerConstants.STAT_MATLAB;

	public static String[] getProfileNames()
	{
		String[] names = new String[datasets.getSize()];
		for (int i = 0; i < datasets.getSize(); ++i)
		{
			names[i] = ((DBSeerDataSet) datasets.getElementAt(i)).getName();
		}
		return names;
	}

	public static final String[] availableWorkloads = {
			"TPCC",
			"LOCK1"
	};

	public static final String[] availableChartNames = {
			"CPU (usr) Usage per Core",
			"CPU (sys) Usage per Core",
			"CPU (usr) Usage Variance",
			"Average CPU Usage",
			"Transaction Statistics",
			"Context Switches",
			"Disk Writes (MB)",
			"Disk Writes (MB) v2",
			"Disk Writes (#)",
			"Disk Writes (#) v2",
			"Disk Reads (MB)",
			"Disk Reads (#)",
			"Disk Cache Miss Ratio",
			"Rows changed vs. Time",
			"Rows changed vs. Disk Writes (MB)",
			"Rows Changed vs. Disk Writes (#)",
			"Network Statistics",
			"Lock Analysis",
			"Combined Average Latency",
			"Overall Latency",
			"Latency vs. CPU",
			"Latency vs. CPU (99% Quantile)",
			"Latency vs. CPU (Median)",
			"Buffer Pool Statistics",
			"Handler Statistics",
			"Latency vs. Throughput",
			"Latency vs. Throughput (99% Quantile)",
			"Latency vs. Throughput (Median)",
			"Latency vs. Lock Time",
			"Latency vs. Lock Time (99% Quantile)",
			"Latency vs. Lock Time (Median)",
			"Transaction Mix"
	};

	public static final String[] transactionChartNames = {
			"Transaction Mix",
			"Transaction Statistics",
			"Latency vs. CPU",
			"Latency vs. CPU (99% Quantile)",
			"Latency vs. CPU (Median)",
			"Latency vs. Throughput",
			"Latency vs. Throughput (99% Quantile)",
			"Latency vs. Throughput (Median)",
			"Latency vs. Lock Time",
			"Latency vs. Lock Time (99% Quantile)",
			"Latency vs. Lock Time (Median)",
			"Combined Average Latency",
			"Overall Latency"
	};

	public static final String[] transactionSampleCharts = {
			"TPSCommitRollback",
//			"CombinedAvgLatency",
//			"LatencyOverall",
			"LatencyVersusCPU",
			"LatencyVersusCPU99",
			"LatencyVersusCPUMedian",
			"LatencyPerTPS",
			"LatencyPerTPS99",
			"LatencyPerTPSMedian",
			"LatencyPerLocktime",
			"LatencyPerLocktime99",
			"LatencyPerLocktimeMedian",
			"CombinedAvgLatency",
			"TransactionMix"
	};

	public static final String[] systemChartNames = {
			"CPU (usr) Usage per Core",
			"CPU (sys) Usage per Core",
			"CPU (usr) Usage Variance",
			"Average CPU Usage",
			"Context Switches",
			"Disk Writes (MB)",
			"Disk Writes (MB) v2",
			"Disk Writes (#)",
			"Disk Writes (#) v2",
			"Disk Reads (MB)",
			"Disk Reads (#)",
			"Disk Cache Miss Ratio",
			"Network Statistics"
	};

	public static final String[] dbmsChartNames = {
			"Rows changed vs. Time",
			"Rows changed vs. Disk Writes (MB)",
			"Rows Changed vs. Disk Writes (#)",
			"Lock Analysis",
			"Buffer Pool Statistics",
			"Handler Statistics"
	};

	public static final String[] availableCharts = {
			"IndividualCoreUsageUser",
			"IndividualCoreUsageSys",
			"InterCoreStandardDeviation",
			"AvgCpuUsage",
			"TPSCommitRollback",
			"ContextSwitches",
			"DiskWriteMB",
			"DiskWriteMB_friendly",
			"DiskWriteNum",
			"DiskWriteNum_friendly",
			"DiskReadMB",
			"DiskReadNum",
			"CacheHit",
			"RowsChangedOverTime",
			"RowsChangedPerWriteMB",
			"RowsChangedPerWriteNo",
			"Network",
			"LockAnalysis",
			"CombinedAvgLatency",
			"LatencyOverall",
			"LatencyVersusCPU",
			"LatencyVersusCPU99",
			"LatencyVersusCPUMedian",
			"WorkingSetSize",
			"WorkingSetSize2",
			"LatencyPerTPS",
			"LatencyPerTPS99",
			"LatencyPerTPSMedian",
			"LatencyPerLocktime",
			"LatencyPerLocktime99",
			"LatencyPerLocktimeMedian",
			"TransactionMix"
	};

	public static final String[] availablePredictions = {
			"Disk Flush Rate by TPS",
			"Disk Flush Rate by Individual Transactions",
			"Max Throughput",
			"CPU by TPS",
			"CPU by Individual Transactions",
			"IO by TPS",
			"Latency",
			"Latency (99% Quantile)",
			"Latency (Median)",
			"Latency (With Lock Waits)",
			"Latency (With Lock Waits, 99% Quantile)",
			"Latency (With Lock Waits, Median)",
			"CPU by Blown Transaction Counts",
			"IO by Blown Transaction Counts",
			"Log Writes",
			"Physical Disk Read",
			"Lock"
	};

	public static final String[] actualPredictionFunctions = {
			"FlushRatePredictionByTPS",
			"FlushRatePredictionByCounts",
			"MaxThroughputPrediction",
			"TransactionCountsToCpuByTPS",
			"TransactionCountsToCpuByCounts",
			"TransactionCountsToIO",
			"TransactionCountsToLatency",
			"TransactionCountsToLatency99",
			"TransactionCountsToLatencyMedian",
			"TransactionCountsWaitTimeToLatency",
			"TransactionCountsWaitTimeToLatency99",
			"TransactionCountsWaitTimeToLatencyMedian",
			"BlownTransactionCountsToCpu",
			"BlownTransactionCountsToIO",
			"LinearPrediction",
			"PhysicalReadPrediction",
			"LockPrediction"
	};

	public static final String[] availablePredictionTestModes = {
			"Dataset",
			"Mixture + TPS"
	};

	private static DBSeerSplash splash;

	private static MatlabProxyFactoryOptions options;

	public static void main(String[] args)
	{
//		Temp: testing data center;
		try
		{
			SwingUtilities.invokeAndWait(new Runnable()
			{
				@Override
				public void run()
				{
					try {
						splash = new DBSeerSplash();
					} catch (InvocationTargetException e) {
						e.printStackTrace();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			});
		}
		catch (Exception e)
		{

		}

		// check dbseer.ini for statistical package to use.
		String iniPath;
		if (args.length == 1)
		{
			iniPath = args[0];
		}
		else
		{
			iniPath = DEFAULT_INI_FILE_PATH;
		}

		try
		{
			checkStatPackage(iniPath);
		}
		catch (IOException e)
		{
			DBSeerExceptionHandler.handleException(e);
			return;
		}

		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//					WebLookAndFeel.install();
				}
				catch (ClassNotFoundException e)
				{
					e.printStackTrace();
				}
				catch (InstantiationException e)
				{
					e.printStackTrace();
				}
				catch (IllegalAccessException e)
				{
					e.printStackTrace();
				}
				catch (UnsupportedLookAndFeelException e)
				{
					e.printStackTrace();
				}

				String title;
				switch (whichStatisticalPackageToUse)
				{
					case DBSeerConstants.STAT_MATLAB:
					{
						runner = MatlabRunner.getInstance();
						runner.eval("mat_version = version;");
						String version = runner.getVariableString("mat_version");
						String[] versionDigits = version.split("\\.");
						int firstDigit = Integer.parseInt(versionDigits[0]);
						int secondDigit = Integer.parseInt(versionDigits[1]);
						if (firstDigit < 7)
						{
							String msg = String.format("DBSeer requires MATLAB 7.5 (r2007b) or higher (The current version of MATLAB is %s).\nProgram will be terminated.", version);
							JOptionPane.showMessageDialog(null, msg, "Error",
									JOptionPane.ERROR_MESSAGE);
							System.exit(-1);
						}
						else if (firstDigit == 7 && secondDigit < 5)
						{
							String msg = String.format("DBSeer requires MATLAB 7.5 (r2007b) or higher (The current version of MATLAB is %s).\nProgram will be terminated.", version);
							JOptionPane.showMessageDialog(null, msg, "Error",
									JOptionPane.ERROR_MESSAGE);
							System.exit(-1);
						}
						title = String.format("DBSeer Console [Statistical Package: MATLAB %s]", version);
						break;
					}
					case DBSeerConstants.STAT_OCTAVE:
					{
						try
						{
							runner = OctaveRunner.getInstance();
						}
						// Exit the program if Octave is not found or old-version.
						catch (OctaveIOException e)
						{
							JOptionPane.showMessageDialog(null, "The binary 'octave' for Octave not found.\nProgram will be terminated.", "Error",
									JOptionPane.ERROR_MESSAGE);
							System.exit(-1);
						}
						String version = OctaveRunner.getInstance().getVersion();

						int versionDigit = Integer.parseInt(version.substring(0, version.indexOf('.')));
						if (versionDigit < 4)
						{
							String msg = String.format("DBSeer requires Octave 4.0.0 or higher (The current version of Octave is %s).\nProgram will be terminated.", version);
							JOptionPane.showMessageDialog(null, msg, "Error",
									JOptionPane.ERROR_MESSAGE);
							System.exit(-1);
						}

						title = String.format("DBSeer Console [Statistical Package: Octave %s]", OctaveRunner.getInstance().getVersion());
						break;
					}
					default:
						runner = null;
						return;
				}

				splash.setText("Loading user settings...");

				XStreamHelper xmlHelper = new XStreamHelper();
				try
				{
					DBSeerUserSettings setting = (DBSeerUserSettings) xmlHelper.fromXML(settingsPath);
					if (setting != null)
					{
						DBSeerGUI.userSettings = setting;
						DBSeerGUI.configs = DBSeerGUI.userSettings.getConfigs();
						DBSeerGUI.datasets = DBSeerGUI.userSettings.getDatasets();
					}
				}
				catch (StreamException e)
				{
					 // file not found..
					splash.setText("Default setting file not found...");
				}

				splash.dispose();
				mainFrame = new DBSeerMainFrame(title);
				SwingUtilities.updateComponentTreeUI(mainFrame);
				mainFrame.pack();
				mainFrame.setVisible(true);
//				mainFrame.setPreferredSize(mainFrame.getSize());

			}
		});
	}

	private static void checkStatPackage(String path) throws IOException
	{
		Ini ini = new Ini();
		File iniFile = new File(path);
		if (!iniFile.exists())
		{
			return;
		}

		ini.load(new FileReader(iniFile));
		Ini.Section dbseerSection = ini.get("dbseer");
		String statPackage = dbseerSection.get("stat_package");

		if (statPackage.equalsIgnoreCase("matlab"))
		{
			whichStatisticalPackageToUse = DBSeerConstants.STAT_MATLAB;
		}
		else if (statPackage.equalsIgnoreCase("octave"))
		{
			whichStatisticalPackageToUse = DBSeerConstants.STAT_OCTAVE;
		}
		else
		{
			// default package is MATLAB.
			whichStatisticalPackageToUse = DBSeerConstants.STAT_MATLAB;
		}
	}


	public static void repaintMainFrame()
	{
		mainFrame.repaint(50L);
		mainFrame.pack();
		mainFrame.setPreferredSize(mainFrame.getSize());
	}

	public static void resetStatRunner()
	{
		if (runner instanceof MatlabRunner)
		{
			MatlabRunner matlabRunner = (MatlabRunner) runner;
			try
			{
				matlabRunner.resetProxy();
			}
			catch (Exception e)
			{
				DBSeerExceptionHandler.handleException(e);
			}
		}
		else if (runner instanceof OctaveRunner)
		{
			OctaveRunner octaveRunner = (OctaveRunner) runner;
			try
			{
				octaveRunner.resetRunner();
			}
			catch (Exception e)
			{
				DBSeerExceptionHandler.handleException(e);
			}
		}
	}
}
