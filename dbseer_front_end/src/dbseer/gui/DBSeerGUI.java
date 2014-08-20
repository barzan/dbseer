package dbseer.gui;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.io.StreamException;
import dbseer.gui.frame.DBSeerMainFrame;
import dbseer.gui.user.DBSeerConfiguration;
import dbseer.gui.user.DBSeerDataSet;
import dbseer.gui.user.DBSeerUserSettings;
import dbseer.gui.xml.XStreamHelper;
import dbseer.middleware.MiddlewareSocket;
import dbseer.comp.DataCenter;
import matlabcontrol.*;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;

/**
 * Created by dyoon on 2014. 5. 17..
 */
public class DBSeerGUI
{
	private static final String DEFAULT_SETTING_FILE_PATH = "./settings.xml";

	public static String settingsPath = DEFAULT_SETTING_FILE_PATH;

	@XStreamAlias("dbseer")
	public static DBSeerUserSettings userSettings = new DBSeerUserSettings();

//	public static SharedComboBoxModel configs = new SharedComboBoxModel(new DefaultListModel());
	public static DefaultListModel configs = new DefaultListModel();

	//public static ArrayList<DBSeerDataProfile> datasets = new ArrayList<DBSeerDataProfile>();
	public static DefaultListModel datasets = new DefaultListModel();

	public static DBSeerConfiguration testConfig = null;

	public static DBSeerConfiguration trainConfig = null;

	public static MatlabProxy proxy = null;

	public static JLabel status = new JLabel();

	public static JLabel middlewareStatus = new JLabel();

	public static MiddlewareSocket middlewareSocket = new MiddlewareSocket();

	public static DBSeerDataSet currentDataset = null;

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
			"WorkingSetSize",
			"WorkingSetSize2",
			"LatencyPerTPS",
			"LatencyPerLocktime"
	};

	public static final String[] availablePredictions = {
			"FlushRatePredictionByTPS",
			"FlushRatePredictionByCounts",
			"MaxThroughputPrediction",
			"TransactionCountsToCpuByTPS",
			"TransactionCountsToCpuByCounts",
			"TransactionCountsToIO",
			"TransactionCountsToLatency",
			"TransactionCountsWaitTimeToLatency",
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

		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
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

				MatlabProxyFactoryOptions options = new MatlabProxyFactoryOptions.Builder()
						.setUsePreviouslyControlledSession(true)
						.setHidden(false).build();
				MatlabProxyFactory factory = new MatlabProxyFactory(options);

				try
				{
					proxy = factory.getProxy();
					proxy.eval("clear all");
				}
				catch (MatlabConnectionException e)
				{
					JOptionPane.showMessageDialog(null, e.toString(), "Matlab proxy error", JOptionPane.ERROR_MESSAGE);
				}
				catch (MatlabInvocationException e)
				{
					JOptionPane.showMessageDialog(null, e.toString(), "Matlab proxy error", JOptionPane.ERROR_MESSAGE);
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
				DBSeerMainFrame mainFrame = new DBSeerMainFrame();
				SwingUtilities.updateComponentTreeUI(mainFrame);
				mainFrame.pack();
				mainFrame.setVisible(true);
			}
		});
	}

}
