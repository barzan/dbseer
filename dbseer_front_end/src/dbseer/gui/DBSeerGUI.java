package dbseer.gui;

import dbseer.gui.frame.DBSeerMainFrame;
import dbseer.gui.model.SharedComboBoxModel;
import matlabcontrol.*;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by dyoon on 2014. 5. 17..
 */
public class DBSeerGUI
{
	// config containing all necessary information for DBSeer tasks.
	public static String root = "";

//	public static SharedComboBoxModel configs = new SharedComboBoxModel(new DefaultListModel());
	public static DefaultListModel configs = new DefaultListModel();

	//public static ArrayList<DBSeerDataProfile> profiles = new ArrayList<DBSeerDataProfile>();
	public static DefaultComboBoxModel profiles = new DefaultComboBoxModel();

	public static DBSeerConfiguration testConfig = null;

	public static DBSeerConfiguration trainConfig = null;

	public static MatlabProxy proxy;

	public static JLabel status = new JLabel();

	public static String[] getProfileNames()
	{
		String[] names = new String[profiles.getSize()];
		for (int i = 0; i < profiles.getSize(); ++i)
		{
			names[i] = ((DBSeerDataProfile)profiles.getElementAt(i)).getName();
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

	private static DBSeerSplash splash;

	public static void main(String[] args)
	{
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
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (UnsupportedLookAndFeelException e) {
					e.printStackTrace();
				}

				MatlabProxyFactoryOptions options = new MatlabProxyFactoryOptions.Builder()
						.setUsePreviouslyControlledSession(true)
						.setHidden(false).build();
				MatlabProxyFactory factory = new MatlabProxyFactory(options);

				try {
					proxy = factory.getProxy();
					proxy.eval("clear all");
				} catch (MatlabConnectionException e) {
					JOptionPane.showMessageDialog(null, e.toString(), e.toString(), JOptionPane.ERROR_MESSAGE);
				} catch (MatlabInvocationException e) {
					JOptionPane.showMessageDialog(null, e.toString(), e.toString(), JOptionPane.ERROR_MESSAGE);
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
