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

package dbseer.comp;

import dbseer.gui.DBSeerConstants;
import dbseer.gui.DBSeerExceptionHandler;
import dbseer.gui.DBSeerGUI;
import dbseer.gui.user.DBSeerConfiguration;
import dbseer.gui.user.DBSeerDataSet;
import dbseer.stat.StatisticalPackageRunner;

import javax.swing.*;
import java.util.List;

/**
 * Created by dyoon on 2014. 6. 17..
 */
public class PredictionCenter
{
	private StatisticalPackageRunner runner;

	private String prediction = "";
	private String predictionDescription = "";
	private String dbseerPath;

	private DBSeerConfiguration trainConfig;
	private DBSeerDataSet testDataset;

	private String testMixture = "[]";
	private String groupRange = "[]";
	private String transactionTypesToGroup = "[]";
	private String ioConfiguration = "[]";
	private String lockConfiguration = "[]";
	private String transactionTypeToPlot = "1";
	private String throttlePenalty = "[]";

	private boolean learnLock = true;
	private boolean throttleIndividualTransactions = false;

	private int testMode = DBSeerConstants.TEST_MODE_DATASET;
	private int groupingType = DBSeerConstants.GROUP_NONE;
	private int lockType = DBSeerConstants.LOCK_WAITTIME;

	private int groupingTarget = DBSeerConstants.GROUP_TARGET_INDIVIDUAL_TRANS_COUNT;
	private int numClusters = 1;
	private int throttleTargetTransactionIndex;
	private int throttleLatencyType;

	private double allowedRelativeDiff = 0.0;
	private double testMinFrequency = 0.0;
	private double testMinTPS = 0.0;
	private double testMaxTPS = 0.0;
	private double testManualMinTPS = 0.0;
	private double testManualMaxTPS = 0.0;
	private double testWorkloadRatio = 1.0;
	private double throttleTargetLatency = 0.0;

	private static double EPSILON = 0.000001;

	public PredictionCenter(StatisticalPackageRunner runner, String prediction, String dbseerPath)
	{
		this.runner = runner;
		for (int i = 0; i < DBSeerGUI.availablePredictions.length; ++i)
		{
			if (prediction.equalsIgnoreCase(DBSeerGUI.availablePredictions[i]))
			{
				this.prediction = DBSeerGUI.actualPredictionFunctions[i];
				this.predictionDescription = DBSeerGUI.availablePredictions[i];
			}
		}
		this.dbseerPath = dbseerPath;
	}

	public PredictionCenter(StatisticalPackageRunner runner, String prediction, String dbseerPath, boolean actualPrediction)
	{
		this.runner = runner;
		if (actualPrediction)
		{
			this.prediction = prediction;
			this.predictionDescription = prediction;
		}
		else
		{
			for (int i = 0; i < DBSeerGUI.availablePredictions.length; ++i)
			{
				if (prediction.equalsIgnoreCase(DBSeerGUI.availablePredictions[i]))
				{
					this.prediction = DBSeerGUI.actualPredictionFunctions[i];
					this.predictionDescription = DBSeerGUI.availablePredictions[i];
				}
			}
		}
		this.dbseerPath = dbseerPath;
	}

	public boolean run()
	{
		if (this.prediction == "")
		{
			JOptionPane.showMessageDialog(null, "Prediction Task Undefined.", "Error",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
		try
		{
			// For Lock Prediction and MaxThroughputPrediciton, use julia
			if (this.prediction == "LockPrediction")
			{
				String cmd = "julia -e cd(\"" + dbseerPath + "/predict_mat/julia\");";
				cmd = cmd + "include(\"julia_init.jl\");";
				cmd = cmd + "include(\"load_mat_data.jl\");";
				cmd = cmd + "title,legends,Xdata,Ydata,Xlabel,Ylabel,meanAbsError,meanRelError,errorHeader,extra=lockPrediction(pc);";
				cmd = cmd + "include(\"write_mat_lockP.jl\");";
				runner.eval("pc.initialize");
				runner.eval("save_mat_data('" + dbseerPath + "',pc);");
				Process p = Runtime.getRuntime().exec(cmd);
				int exitVal = p.waitFor();
				runner.eval("read_mat_lockP;");
			}
			else if (this.prediction == "MaxThroughputPrediction")
			{
				String cmd = "julia -e cd(\"" + dbseerPath + "/predict_mat/julia\");";
				cmd = cmd + "include(\"julia_init.jl\");";
				cmd = cmd + "include(\"load_mat_data.jl\");";
				cmd = cmd + "title,legends,Xdata,Ydata,Xlabel,Ylabel,meanAbsError,meanRelError,errorHeader,extra=maxThroughputPrediction(pc);";
				cmd = cmd + "include(\"write_mat_maxT.jl\");";
				runner.eval("pc.initialize");
				runner.eval("save_mat_data('" + dbseerPath + "',pc);");
				Process p = Runtime.getRuntime().exec(cmd);
				int exitVal = p.waitFor();
				runner.eval("read_mat_maxT;");
			}
			else if (this.prediction == "BottleneckAnalysisMaxThroughput")
			{
				String cmd = "julia -e cd(\"" + dbseerPath + "/predict_mat/julia\");";
				cmd = cmd + "include(\"julia_init.jl\");";
				cmd = cmd + "include(\"load_mat_data.jl\");";
				cmd = cmd + "title,legends,Xdata,Ydata,Xlabel,Ylabel,meanAbsError,meanRelError,errorHeader,extra=bottleneckAnalysisMaxThroughput(pc);";
				cmd = cmd + "include(\"write_mat_bottleneck_maxT.jl\");";
				runner.eval("pc.initialize");
				runner.eval("save_mat_data('" + dbseerPath + "',pc);");
				Process p = Runtime.getRuntime().exec(cmd);
				int exitVal = p.waitFor();
				runner.eval("read_mat_bottleneck_maxT;");
			}
			else if (this.prediction == "BottleneckAnalysisResource")
			{
				String cmd = "julia -e cd(\"" + dbseerPath + "/predict_mat/julia\");";
				cmd = cmd + "include(\"julia_init.jl\");";
				cmd = cmd + "include(\"load_mat_data.jl\");";
				cmd = cmd + "title,legends,Xdata,Ydata,Xlabel,Ylabel,meanAbsError,meanRelError,errorHeader,extra=bottleneckAnalysisResource(pc);";
				cmd = cmd + "include(\"write_mat_bottleneck_res.jl\");";
				runner.eval("pc.initialize");
				runner.eval("save_mat_data('" + dbseerPath + "',pc);");
				Process p = Runtime.getRuntime().exec(cmd);
				int exitVal = p.waitFor();
				runner.eval("read_mat_bottleneck_res;");
			}
			else
			{
				runner.eval("[title legends Xdata Ydata Xlabel Ylabel meanAbsError meanRelError errorHeader extra] = pc.performPrediction;");
			}
		}
		catch (Exception e)
		{
			DBSeerExceptionHandler.handleException(e);
			return false;
		}
		return true;
	}

	public boolean initialize()
	{
		try
		{
			runner.eval("rmpath " + dbseerPath + ";");
			runner.eval("rmpath " + dbseerPath + "/common_mat;");
			runner.eval("rmpath " + dbseerPath + "/predict_mat;");
			runner.eval("rmpath " + dbseerPath + "/predict_data;");
			runner.eval("rmpath " + dbseerPath + "/predict_mat/prediction_center;");
			runner.eval("rmpath " + dbseerPath + "/predict_mat/julia;");

			runner.eval("addpath " + dbseerPath + ";");
			runner.eval("addpath " + dbseerPath + "/common_mat;");
			runner.eval("addpath " + dbseerPath + "/predict_mat;");
			runner.eval("addpath " + dbseerPath + "/predict_data;");
			runner.eval("addpath " + dbseerPath + "/predict_mat/prediction_center;");
			runner.eval("addpath " + dbseerPath + "/predict_mat/julia;");

			runner.eval("pc = PredictionCenter;");
			if (!trainConfig.initialize())
			{
				return false;
			}
			runner.eval("pc.trainConfig = " + trainConfig.getUniqueVariableName() + ";");

			if (testMode == DBSeerConstants.TEST_MODE_DATASET)
			{
				String mappedTransactionType = trainConfig.mapTransactionTypes(testDataset);
				if (mappedTransactionType == null)
				{
					return false;
				}
				runner.eval("pc.testConfig = PredictionConfig;");
				runner.eval("pc.testConfig.transactionType = " + mappedTransactionType + ";");
				testDataset.loadDataset();
				runner.eval("pc.testConfig.addDataset(" + testDataset.getUniqueVariableName() + ");");

				if (groupingType == DBSeerConstants.GROUP_RANGE)
				{
					runner.eval("groups = " + groupRange + ";");
					runner.eval("groupingStrategy = struct('groups', groups);");
					runner.eval("pc.testConfig.groupingStrategy = groupingStrategy;");
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
					runner.eval(groupParams);
					runner.eval("groupingStrategy = struct('groupParams', groupParams);");
					runner.eval("pc.testConfig.groupingStrategy = groupingStrategy;");
				}
				runner.eval("pc.testConfig.initialize;");
			}
			else if (testMode == DBSeerConstants.TEST_MODE_MIXTURE_TPS)
			{
				runner.eval("pc.testMixture = " + testMixture + ";");
				runner.eval("pc.testMinTPS = " + testManualMinTPS + ";");
				runner.eval("pc.testMaxTPS = " + testManualMaxTPS + ";");
				runner.eval("pc.testWorkloadRatio = " + testWorkloadRatio + ";");
			}
			runner.eval("pc.ioConf = " + ioConfiguration + ";");
			runner.eval("pc.lockConf = " + lockConfiguration + ";");
			runner.eval("pc.whichTransactionToPlot = " + transactionTypeToPlot + ";");
			if (learnLock)
			{
				runner.eval("pc.learnLock = true;");
			}
			else
			{
				runner.eval("pc.learnLock = false;");
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
			runner.eval("pc.throttleTargetTransactionIndex = " + throttleTargetTransactionIndex + ";");
			runner.eval("pc.throttleTargetLatency = " + throttleTargetLatency + ";");
			runner.eval("pc.throttlePenalty = " + throttlePenalty + ";");
			runner.eval("pc.throttleLatencyType = " + throttleLatencyType + ";");
			if (throttleIndividualTransactions)
			{
				runner.eval("pc.throttleIndividualTransactions = true;");
			}
			else
			{
				runner.eval("pc.throttleIndividualTransactions = false;");
			}
			runner.eval("pc.lockType = " + lockTypeString + ";");
			runner.eval("pc.testMode = " + testMode + ";" );
			runner.eval("pc.taskName = '" + prediction + "';");
		}
		catch (Exception e)
		{
			DBSeerExceptionHandler.handleException(e);
			return false;
		}

		return true;
	}

	public String getLastError()
	{
		String errorMessage = "";
		runner.eval("dbseer_lasterror = lasterror;");
		errorMessage = runner.getVariableString("dbseer_lasterror.message");
//			Object[] returnObj = runner.returningEval("dbseer_lasterror.message", 1);
//			errorMessage = (String)returnObj[0];
		return errorMessage;
	}

	public void printWhatIfAnalysisCPUResult(JTextArea textArea, DBSeerConfiguration trainConfig)
	{
		textArea.setText("");
		String output = "";

		try
		{
			Object[] results = (Object[])runner.getVariableCell("extra");
			double[] avgCPU = (double[])results[0];
			double[] transactionCount = (double[])results[1];

			output += String.format("The current workload: %d transactions per second.\n" +
							"The changed workload: %d transactions per second.\n\n",
					Math.round(transactionCount[0]), Math.round(transactionCount[1]));

			double avg = (avgCPU[0] < 0 || transactionCount[0] == 0 ? 0 : avgCPU[0]);

			output += String.format("Average CPU Usage will be %.1f%%.\n", avg);

			textArea.setText(output);
		}
		catch (Exception e)
		{
			DBSeerExceptionHandler.handleException(e);
		}
	}

	public void printWhatIfAnalysisIOResult(JTextArea textArea, DBSeerConfiguration trainConfig)
	{
		textArea.setText("");
		String output = "";

		try
		{
			Object[] results = (Object[])runner.getVariableCell("extra");
			double[] readIO = (double[])results[0];
			double[] writeIO = (double[])results[1];
			double[] transactionCount = (double[])results[2];

			output += String.format("The current workload: %d transactions per second.\n" +
							"The changed workload: %d transactions per second.\n\n",
					Math.round(transactionCount[0]), Math.round(transactionCount[1]));

			double read = (readIO[0] < 0 || transactionCount[0] == 0 ? 0 : readIO[0]);
			double write = (writeIO[0] < 0 || transactionCount[0] == 0 ? 0 : writeIO[0]);

			output += String.format("Average Disk I/O will be {Read = %.4f MB/s, Write = %.4fMB/s}.\n",
					read, write);

			textArea.setText(output);
		}
		catch (Exception e)
		{
			DBSeerExceptionHandler.handleException(e);
		}
	}

	public void printWhatIfAnalysisFlushRateResult(JTextArea textArea, DBSeerConfiguration trainConfig)
	{
		textArea.setText("");
		String output = "";

		try
		{
			Object[] results = (Object[])runner.getVariableCell("extra");
			double[] avgFlushRate = (double[])results[0];
			double[] transactionCount = (double[])results[1];

			double avg = (avgFlushRate[0] < 0 || transactionCount[0] == 0 ? 0 : avgFlushRate[0]);

			output += String.format("The current workload: %d transactions per second.\n" +
							"The changed workload: %d transactions per second.\n\n",
					Math.round(transactionCount[0]), Math.round(transactionCount[1]));

			output += String.format("Average Disk Flush Rate will be %.1f pages per second.", avg);

			textArea.setText(output);
		}
		catch (Exception e)
		{
			DBSeerExceptionHandler.handleException(e);
		}
	}

	public void printWhatIfAnalysisLatencyResult(JTextArea textArea, DBSeerConfiguration trainConfig)
	{
		DBSeerDataSet dataset = trainConfig.getDataset();
		textArea.setText("");
		String output = "";

		List<String> transactionTypes = dataset.getTransactionTypeNames();

		try
		{
			Object[] results = (Object[])runner.getVariableCell("extra");
			double[] avgLatencies = (double[])results[0];
			double[] medianLatencies = (double[])results[1];
			double[] q99Latencies = (double[])results[2];
			double[] overallLatencies = (double[])results[3];
			double[] transactionCount = (double[])results[4];

			output += String.format("The current workload: %d transactions per second.\n" +
							"The changed workload: %d transactions per second.\n\n",
					Math.round(transactionCount[0]), Math.round(transactionCount[1]));

			if (Math.round(transactionCount[1]) == 0)
			{
				output += "The latency prediction is invalid with the changed workload of 0 transactions per second.";
				textArea.setText(output);
				return;
			}

			if (overallLatencies[0] < 0) overallLatencies[0] = 0;
			if (overallLatencies[1] < 0) overallLatencies[1] = 0;
			if (overallLatencies[2] < 0) overallLatencies[2] = 0;

			overallLatencies[2] = validateQuantileLatency(overallLatencies[2], overallLatencies[0], overallLatencies[1]);

			output += String.format("The overall latency of transactions will be {Avg = %.1f milliseconds, Median = %.1f milliseconds," +
							" 99%% Quantile = %.1f milliseconds}.\n\n",
					overallLatencies[0]*1000, overallLatencies[1]*1000, overallLatencies[2]*1000);

			int idx = 0;
			for (String transaction : transactionTypes)
			{
				double avg = avgLatencies[idx]*1000;
				double median = medianLatencies[idx]*1000;
				double q99 = q99Latencies[idx]*1000;

				if (avg<0 || transactionCount[0] == 0) avg = 0;
				if (median<0 || transactionCount[0] == 0) median = 0;
				if (q99<0 || transactionCount[0] == 0) q99 = 0;

				q99 = validateQuantileLatency(q99, avg, median);

				output += String.format("The latency of '%s' transactions will be {Avg = %.1f milliseconds, Median = %.1f milliseconds, 99%% Quantile = %.1f milliseconds}.\n",
						transaction, avg, median, q99);
				++idx;
			}

			textArea.setText(output);
		}
		catch (Exception e)
		{
			DBSeerExceptionHandler.handleException(e);
		}
	}

	public void printBottleneckAnalysisMaxThroughputResult(JTextArea textArea, DBSeerConfiguration trainConfig)
	{
		textArea.setText("");
		String output = "";

		try
		{
			Object[] results = (Object[])runner.getVariableCell("extra");
			double[] maxThroughput = (double[])results[0];

			if (maxThroughput[0] < 0) maxThroughput[0] = 0;

			output = String.format("Your maximum throughput will be %d transactions per second.",
					Math.round(maxThroughput[0]));

			textArea.setText(output);
		}
		catch (Exception e)
		{
			DBSeerExceptionHandler.handleException(e);
		}
	}

	public void printBottleneckAnalysisResourceResult(JTextArea textArea, DBSeerConfiguration trainConfig)
	{
		textArea.setText("");
		String output = "";

		try
		{
			Object[] results = (Object[])runner.getVariableCell("extra");
			double[] result = (double[])results[0];
			double minThroughput = result[0];
			String resource = (String)results[1];

			if (minThroughput < 0) minThroughput = 0;

			output = String.format("Your bottleneck resource is %s with the maximum throughput of %d transactions per second.",
					resource, Math.round(minThroughput));

			textArea.setText(output);
		}
		catch (Exception e)
		{
			DBSeerExceptionHandler.handleException(e);
		}
	}

	public void printBlameAnalysisCPUResult(JTextArea textArea, DBSeerConfiguration trainConfig)
	{
		DBSeerDataSet dataset = trainConfig.getDataset();
		textArea.setText("");
		String output;

		List<String> transactionTypes = dataset.getTransactionTypeNames();

		try
		{
			Object[] results = (Object[])runner.getVariableCell("extra");
			double[] transactionIndex = (double[])results[0];
			int index = (int)transactionIndex[0];

			output = String.format("The transaction type most responsible for the high CPU usage is '%s'.",
					transactionTypes.get(index-1));

			textArea.setText(output);
		}
		catch (Exception e)
		{
			DBSeerExceptionHandler.handleException(e);
		}
	}

	public void printBlameAnalysisIOResult(JTextArea textArea, DBSeerConfiguration trainConfig)
	{
		DBSeerDataSet dataset = trainConfig.getDataset();
		textArea.setText("");
		String output;

		List<String> transactionTypes = dataset.getTransactionTypeNames();

		try
		{
			Object[] results = (Object[])runner.getVariableCell("extra");
			double[] transactionIndex = (double[])results[0];
			int readIndex = (int)transactionIndex[0];
			int writeIndex = (int)transactionIndex[1];

			output = String.format("The transaction type most responsible for the high disk read is '%s'.\n",
					transactionTypes.get(readIndex-1));
			output += String.format("The transaction type most responsible for the high disk write is '%s'.\n",
					transactionTypes.get(writeIndex-1));

			textArea.setText(output);
		}
		catch (Exception e)
		{
			DBSeerExceptionHandler.handleException(e);
		}
	}

	public void printBlameAnalysisLockResult(JTextArea textArea, DBSeerConfiguration trainConfig)
	{
		DBSeerDataSet dataset = trainConfig.getDataset();
		textArea.setText("");
		String output;

		List<String> transactionTypes = dataset.getTransactionTypeNames();

		try
		{
			Object[] results = (Object[])runner.getVariableCell("extra");
			double[] transactionIndex = (double[])results[0];
			int index = (int)transactionIndex[0];

			output = String.format("The transaction type most responsible for lock contention is '%s'.",
					transactionTypes.get(index-1));

			textArea.setText(output);
		}
		catch (Exception e)
		{
			DBSeerExceptionHandler.handleException(e);
		}
	}

	public void printThrottlingAnalysisOverallResult(JTextArea textArea, DBSeerConfiguration trainConfig)
	{
		DBSeerDataSet dataset = trainConfig.getDataset();
		textArea.setText("");
		String output;

		List<String> transactionTypes = dataset.getTransactionTypeNames();
		try
		{
			Object[] results = (Object[])runner.getVariableCell("extra");
			double[] resultVector = (double[])results[0];
			double[] indexVector = (double[])results[1];
			double[] targetVector = (double[])results[2];
			int throttleTPS = (int)resultVector[0];
			int transactionIndex = (int)indexVector[0];
			double targetLatency = targetVector[0] * 1000;


			if (throttleTPS >= 15000)
			{
				output = String.format("There is no need to throttle any transactions in order to guarantee " +
						"a latency of %.0f milliseconds for '%s'.", targetLatency, transactionTypes.get(transactionIndex-1));
			}
			else if (throttleTPS == 0)
			{
				output = String.format("You will not be able to achieve a latency of %.0f milliseconds for '%s'.",
						targetLatency, transactionTypes.get(transactionIndex - 1));
			}
			else
			{
				output = String.format("You need to accept no more than %d transactions per second in order to " +
								"guarantee a latency of %.0f milliseconds for '%s'.", throttleTPS, targetLatency,
						transactionTypes.get(transactionIndex - 1));
			}

			textArea.setText(output);
		}
		catch (Exception e)
		{
			DBSeerExceptionHandler.handleException(e);
		}
	}

	public void printThrottlingAnalysisIndividualResult(JTextArea textArea, DBSeerConfiguration trainConfig)
	{
		DBSeerDataSet dataset = trainConfig.getDataset();
		textArea.setText("");
		String output = "";

		List<String> transactionTypes = dataset.getTransactionTypeNames();
		try
		{
			Object[] results = (Object[])runner.getVariableCell("extra");
			double[] returnFlags = (double[])results[0];
			int returnVal = (int)returnFlags[0];
			if (returnVal == 0)
			{
				double[] indexVector = (double[])results[1];
				double[] targetVector = (double[])results[2];
				int transactionIndex = (int)indexVector[0];
				double targetLatency = targetVector[0] * 1000;

				output = String.format("There is no need to throttle any transactions given the current workload in order to guarantee " +
						"a latency of %.0f milliseconds for '%s'.", targetLatency, transactionTypes.get(transactionIndex-1));
			}
			else if (returnVal == 1)
			{
				output = "We have failed to find a valid solution for this question.";
			}
			else if (returnVal == 2)
			{
				double[] solutionVector = (double[])results[1];
				double[] indexVector = (double[])results[2];
				double[] targetVector = (double[])results[3];
				int transactionIndex = (int)indexVector[0];
				double targetLatency = targetVector[0] * 1000;

				output = String.format("In order to guarantee a latency of %.0f milliseconds for '%s':\n\n", targetLatency,
						transactionTypes.get(transactionIndex-1));

				double sum = 0;
				for (double sol : solutionVector)
				{
					if (sol <= this.EPSILON)
					{
						sol = 0.0;
					}
					sum += sol;
				}
				if (sum == 0)
				{
					output = String.format("You will not be able to achieve a latency of %.0f milliseconds for '%s'.",
							targetLatency, transactionTypes.get(transactionIndex - 1));

					textArea.setText(output);
					return;
				}

				int idx = 0;
				for (double sol : solutionVector)
				{
					if (sol <= this.EPSILON)
					{
						sol = 0.0;
					}
					output += String.format("You need to accept no more than %.0f '%s' transactions per second.\n",
							sol, transactionTypes.get(idx));
					++idx;
				}
			}
			textArea.setText(output);
		}
		catch (Exception e)
		{
			DBSeerExceptionHandler.handleException(e);
		}
	}

	public String getPrediction()
	{
		return prediction;
	}

	public String getPredictionDescription()
	{
		return predictionDescription;
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

	public DBSeerConfiguration getTrainConfig()
	{
		return trainConfig;
	}

	public void setPredictionDescription(String predictionDescription)
	{
		this.predictionDescription = predictionDescription;
	}

	public double getTestWorkloadRatio()
	{
		return testWorkloadRatio;
	}

	public void setTestWorkloadRatio(double testWorkloadRatio)
	{
		this.testWorkloadRatio = testWorkloadRatio;
	}

	public String getNormalizedTestMixture()
	{
		String str = "";
		try
		{
			runner.eval("dbseer_mixture = pc.testMixture;");
			double[] mixture = runner.getVariableDouble("dbseer_mixture");
			str = "[";
			for (int i=0;i<mixture.length;++i)
			{
				str += String.format("%.3f", mixture[i]);
				if (i < mixture.length-1)
				{
					str += " ";
				}
			}
			str += "]";
		}
		catch (Exception e)
		{
			DBSeerExceptionHandler.handleException(e);
		}
		return str;
	}

	public void setThrottleTargetTransactionIndex(int index)
	{
		throttleTargetTransactionIndex = index + 1;
	}

	public void setThrottleTargetLatency(double latency)
	{
		throttleTargetLatency = (double)latency / 1000.0; // convert millisecond to second.
	}

	public void setThrottlePenalty(String matrix)
	{
		throttlePenalty = matrix;
	}

	public void setThrottleIndividualTransactions(boolean option)
	{
		throttleIndividualTransactions = option;
	}

	public void setThrottleLatencyType(int type)
	{
		throttleLatencyType = type;
	}

	private double validateLatency(double val)
	{
		return (val < 0.0) ? 0 : val;
	}

	private double validateQuantileLatency(double latency, double v1, double v2)
	{
		if (latency < v1 && latency < v2)
		{
			if (v1 > v2) return v1;
			else return v2;
		}
		return latency;
	}
}
