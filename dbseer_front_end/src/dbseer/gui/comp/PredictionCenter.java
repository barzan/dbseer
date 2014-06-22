package dbseer.gui.comp;

import dbseer.gui.DBSeerConstants;
import dbseer.gui.user.DBSeerConfiguration;
import dbseer.gui.user.DBSeerDataSet;
import matlabcontrol.MatlabInvocationException;
import matlabcontrol.MatlabProxy;

import javax.swing.*;

/**
 * Created by dyoon on 2014. 6. 17..
 */
public class PredictionCenter
{
	private MatlabProxy proxy;

	private String prediction;
	private String dbseerPath;

	private DBSeerConfiguration trainConfig;
	private DBSeerDataSet testDataset;

	private String testMixture = "[]";
	private String groupRange = "[]";
	private String transactionTypesToGroup = "[]";
	private String ioConfiguration = "[]";
	private String lockConfiguration = "[]";
	private String transactionTypeToPlot = "1";

	private boolean learnLock = true;

	private int testMode = DBSeerConstants.TEST_MODE_DATASET;
	private int groupingType = DBSeerConstants.GROUP_NONE;
	private int lockType = DBSeerConstants.LOCK_WAITTIME;

	private int groupingTarget = DBSeerConstants.GROUP_TARGET_INDIVIDUAL_TRANS_COUNT;
	private int numClusters = 1;

	private double allowedRelativeDiff = 0.0;
	private double testMinFrequency = 0.0;
	private double testMinTPS = 0.0;
	private double testMaxTPS = 0.0;
	private double testManualMinTPS = 0.0;
	private double testManualMaxTPS = 0.0;

	public PredictionCenter(MatlabProxy proxy, String prediction, String dbseerPath)
	{
		this.proxy = proxy;
		this.prediction = prediction;
		this.dbseerPath = dbseerPath;
	}

	public void run()
	{
		try
		{
			proxy.eval("[title legends Xdata Ydata Xlabel Ylabel] = pc.performPrediction;");
		}
		catch (MatlabInvocationException e)
		{
			JOptionPane.showMessageDialog(null, this.getLastError(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	public void initialize()
	{
		try
		{
			proxy.eval("rmpath " + dbseerPath + ";");
			proxy.eval("rmpath " + dbseerPath + "/common_mat;");
			proxy.eval("rmpath " + dbseerPath + "/predict_mat;");
			proxy.eval("rmpath " + dbseerPath + "/predict_data;");
			proxy.eval("rmpath " + dbseerPath + "/predict_mat/prediction_center;");

			proxy.eval("addpath " + dbseerPath + ";");
			proxy.eval("addpath " + dbseerPath + "/common_mat;");
			proxy.eval("addpath " + dbseerPath + "/predict_mat;");
			proxy.eval("addpath " + dbseerPath + "/predict_data;");
			proxy.eval("addpath " + dbseerPath + "/predict_mat/prediction_center;");

			proxy.eval("pc = PredictionCenter;");
			trainConfig.initialize();
			proxy.eval("pc.trainConfig = " + trainConfig.getUniqueVariableName() + ";");

			if (testMode == DBSeerConstants.TEST_MODE_DATASET)
			{
				proxy.eval("pc.testConfig = PredictionConfig;");
				proxy.eval("pc.testConfig.transactionType = pc.trainConfig.transactionType;");
				testDataset.loadDataset();
				proxy.eval("pc.testConfig.addDataset(" + testDataset.getUniqueVariableName() + ");");

				if (groupingType == DBSeerConstants.GROUP_RANGE)
				{
					proxy.eval("groups = " + groupRange + ";");
					proxy.eval("groupingStrategy = struct('groups', groups);");
					proxy.eval("pc.testConfig.groupingStrategy = groupingStrategy;");
				}
				else if (groupingType == DBSeerConstants.GROUP_REL_DIFF ||
						groupingType == DBSeerConstants.GROUP_NUM_CLUSTER)
				{
					String groupParams = "groupParams = struct('groupByTPSinsteadOfIndivCounts', ";
					if (groupingTarget == DBSeerConstants.GROUP_TARGET_INDIVIDUAL_TRANS_COUNT)
					{
						groupParams += "false, ";
						groupParams += ("'byWhichTranTypes', " + transactionTypesToGroup + ", ");
					}
					else if (groupingTarget == DBSeerConstants.GROUP_TARGET_TPS)
					{
						groupParams += "true, ";
					}

					if (groupingType == DBSeerConstants.GROUP_REL_DIFF)
					{
						groupParams += ("'allowedRelativeDiff', " + String.valueOf(allowedRelativeDiff) + ", ");
					}
					else if (groupingType == DBSeerConstants.GROUP_NUM_CLUSTER)
					{
						groupParams += ("'nClusters', " + String.valueOf(numClusters) + ", ");
					}

					groupParams += ("'minFreq', " + String.valueOf(testMinFrequency) + ", ");
					groupParams += ("'minTPS', " + String.valueOf(testMinTPS) + ", ");
					groupParams += ("'maxTPS', " + String.valueOf(testMaxTPS) + ");");

					//System.out.println(groupParams);
					proxy.eval(groupParams);
					proxy.eval("groupingStrategy = struct('groupParams', groupParams);");
					proxy.eval("pc.testConfig.groupingStrategy = groupingStrategy;");
				}
				proxy.eval("pc.testConfig.initialize;");
			}
			else if (testMode == DBSeerConstants.TEST_MODE_MIXTURE_TPS)
			{
				proxy.eval("pc.testMixture = " + testMixture + ";");
				proxy.eval("pc.testMinTPS = " + testManualMinTPS + ";");
				proxy.eval("pc.testMaxTPS = " + testManualMaxTPS + ";");
			}
			proxy.eval("pc.ioConf = " + ioConfiguration + ";");
			proxy.eval("pc.lockConf = " + lockConfiguration + ";");
			proxy.eval("pc.whichTransactionToPlot = " + transactionTypeToPlot + ";");
			if (learnLock)
			{
				proxy.eval("pc.learnLock = true;");
			}
			else
			{
				proxy.eval("pc.learnLock = false;");
			}
			String lockTypeString = "";
			switch(lockType)
			{
				case DBSeerConstants.LOCK_WAITTIME:
					lockTypeString = "'waitTime'";
					break;
				case DBSeerConstants.LOCK_NUMLOCKS:
					lockTypeString = "'numberOfLocks'";
					break;
				case DBSeerConstants.LOCK_NUMCONFLICTS:
					lockTypeString = "'numberOfConflicts'";
					break;
				default:
					lockTypeString = "'waitTime'";
					break;
			}
			proxy.eval("pc.lockType = " + lockTypeString + ";");
			proxy.eval("pc.testMode = " + testMode + ";" );
			proxy.eval("pc.taskName = '" + prediction + "';");
		}
		catch (MatlabInvocationException e)
		{
			JOptionPane.showMessageDialog(null, this.getLastError(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	public String getLastError()
	{
		String errorMessage = "";
		try
		{
			proxy.eval("dbseer_lasterror = lasterror;");
			Object[] returnObj = proxy.returningEval("dbseer_lasterror.message", 1);
			errorMessage = (String)returnObj[0];
		}
		catch (MatlabInvocationException e)
		{
			JOptionPane.showMessageDialog(null, "Error", "Failed to retrieve the last error from MATLAB",
					JOptionPane.ERROR_MESSAGE);
		}
		return errorMessage;
	}

	public String getPrediction()
	{
		return prediction;
	}

	public String getTestDatasetName()
	{
		return testDataset.getName();
	}

	public String getTestMixture()
	{
		return testMixture;
	}

	public double getTestManualMinTPS()
	{
		return testManualMinTPS;
	}

	public double getTestManualMaxTPS()
	{
		return testManualMaxTPS;
	}

	public int getTestMode()
	{
		return testMode;
	}

	public int getGroupingType()
	{
		return groupingType;
	}

	public int getGroupingTarget()
	{
		return groupingTarget;
	}

	public int getNumClusters()
	{
		return numClusters;
	}

	public double getAllowedRelativeDiff()
	{
		return allowedRelativeDiff;
	}

	public void setTrainConfig(DBSeerConfiguration config)
	{
		this.trainConfig = config;
	}

	public void setTestDataset(DBSeerDataSet dataset)
	{
		this.testDataset = dataset;
	}

	public void setTestMode(int testMode)
	{
		this.testMode = testMode;
	}

	public void setGroupingType(int groupingType)
	{
		this.groupingType = groupingType;
	}

	public void setGroupingTarget(int groupingTarget)
	{
		this.groupingTarget = groupingTarget;
	}

	public void setNumClusters(int numClusters)
	{
		this.numClusters = numClusters;
	}

	public void setAllowedRelativeDiff(double allowedRelativeDiff)
	{
		this.allowedRelativeDiff = allowedRelativeDiff;
	}

	public void setTestMinFrequency(double testMinFrequency)
	{
		this.testMinFrequency = testMinFrequency;
	}

	public void setTestMinTPS(double testMinTPS)
	{
		this.testMinTPS = testMinTPS;
	}

	public void setTestMaxTPS(double testMaxTPS)
	{
		this.testMaxTPS = testMaxTPS;
	}

	public void setGroupRange(String groupRange)
	{
		this.groupRange = groupRange;
	}

	public void setTransactionTypesToGroup(String transactionTypesToGroup)
	{
		this.transactionTypesToGroup = transactionTypesToGroup;
	}

	public void setIoConfiguration(String ioConfiguration)
	{
		this.ioConfiguration = ioConfiguration;
	}

	public void setLockConfiguration(String lockConfiguration) { this.lockConfiguration = lockConfiguration; }

	public void setTestManualMinTPS(double testManualMinTPS)
	{
		this.testManualMinTPS = testManualMinTPS;
	}

	public void setTestManualMaxTPS(double testManualMaxTPS)
	{
		this.testManualMaxTPS = testManualMaxTPS;
	}

	public void setTestMixture(String testMixture)
	{
		this.testMixture = testMixture;
	}

	public void setTransactionTypeToPlot(String transactionTypeToPlot)
	{
		this.transactionTypeToPlot = transactionTypeToPlot;
	}

	public boolean isLearnLock()
	{
		return learnLock;
	}

	public void setLearnLock(boolean learnLock)
	{
		this.learnLock = learnLock;
	}

	public int getLockType()
	{
		return lockType;
	}

	public void setLockType(int lockType)
	{
		this.lockType = lockType;
	}
}
