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

import dbseer.gui.DBSeerExceptionHandler;
import dbseer.gui.DBSeerGUI;
import dbseer.gui.user.DBSeerDataSet;
import dbseer.stat.StatisticalPackageRunner;
import matlabcontrol.MatlabProxy;

/**
 * Created by dyoon on 5/1/15.
 */
public class MatlabFunctions
{
	// get transaction mix at 'time'
	public static double[] getTransactionMix(DBSeerDataSet dataset, long time)
	{
		StatisticalPackageRunner runner = DBSeerGUI.runner;
		double[] mix = null;

		try
		{
			String mv = dataset.getUniqueModelVariableName();
			runner.eval(String.format("dbseer_tx_mix = %s.clientIndividualSubmittedTrans(%d,:) ./ " +
					"%s.clientTotalSubmittedTrans(%d);", mv, time, mv, time));
			mix = runner.getVariableDouble("dbseer_tx_mix");
		}
		catch(Exception e)
		{
			DBSeerExceptionHandler.handleException(e);
		}
		return mix;
	}

	// get total transaction mix.
	public static double[] getTotalTransactionMix(DBSeerDataSet dataset)
	{
		StatisticalPackageRunner runner = DBSeerGUI.runner;
		double[] mix = null;

		try
		{
			String mv = dataset.getUniqueModelVariableName();
//			runner.eval(String.format("dbseer_tx_mix = sum(%s.clientIndividualSubmittedTrans, 1) ./ " +
//					"sum(%s.clientTotalSubmittedTrans);", mv, mv));
			runner.eval(String.format("dbseer_tx_mix = getMixture(%s);", mv));
			mix = runner.getVariableDouble("dbseer_tx_mix");
		}
		catch(Exception e)
		{
			DBSeerExceptionHandler.handleException(e);
		}
		return mix;
	}

	public static double getMaxTPS(DBSeerDataSet dataset)
	{
		StatisticalPackageRunner runner = DBSeerGUI.runner;
		double[] maxTPS = null;

		try
		{
			String mv = dataset.getUniqueModelVariableName();
			runner.eval(String.format("dbseer_max_tps = max(%s.clientTotalSubmittedTrans);", mv));
			maxTPS = runner.getVariableDouble("dbseer_max_tps");
		}
		catch(Exception e)
		{
			DBSeerExceptionHandler.handleException(e);
		}
		return maxTPS[0];
	}

	public static double getMinTPS(DBSeerDataSet dataset)
	{
		StatisticalPackageRunner runner = DBSeerGUI.runner;
		double[] minTPS = null;

		try
		{
			String mv = dataset.getUniqueModelVariableName();
			runner.eval(String.format("dbseer_min_tps = min(%s.clientTotalSubmittedTrans);", mv));
			minTPS = runner.getVariableDouble("dbseer_min_tps");
		}
		catch(Exception e)
		{
			DBSeerExceptionHandler.handleException(e);
		}
		return minTPS[0];
	}

	public static int getTotalRows(DBSeerDataSet dataset)
	{
		StatisticalPackageRunner runner = DBSeerGUI.runner;
		double[] totalRows = null;

		try
		{
			String mv = dataset.getUniqueModelVariableName();
			runner.eval(String.format("dbseer_total_rows = size(%s.clientTotalSubmittedTrans, 1);", mv));
			totalRows = runner.getVariableDouble("dbseer_total_rows");
		}
		catch(Exception e)
		{
			DBSeerExceptionHandler.handleException(e);
		}
		return (int)totalRows[0];
	}
}
