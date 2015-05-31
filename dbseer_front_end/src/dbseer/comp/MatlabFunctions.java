package dbseer.comp;

import dbseer.gui.DBSeerExceptionHandler;
import dbseer.gui.DBSeerGUI;
import dbseer.gui.user.DBSeerDataSet;
import matlabcontrol.MatlabProxy;

/**
 * Created by dyoon on 5/1/15.
 */
public class MatlabFunctions
{
	// get transaction mix at 'time'
	public static double[] getTransactionMix(DBSeerDataSet dataset, long time)
	{
		MatlabProxy proxy = DBSeerGUI.proxy;
		double[] mix = null;

		try
		{
			String mv = dataset.getUniqueModelVariableName();
			proxy.eval(String.format("dbseer_tx_mix = %s.clientIndividualSubmittedTrans(%d,:) ./ " +
					"%s.clientTotalSubmittedTrans(%d);", mv, time, mv, time));
			mix = (double[])proxy.getVariable("dbseer_tx_mix");
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
		MatlabProxy proxy = DBSeerGUI.proxy;
		double[] mix = null;

		try
		{
			String mv = dataset.getUniqueModelVariableName();
			proxy.eval(String.format("dbseer_tx_mix = sum(%s.clientIndividualSubmittedTrans, 1) ./ " +
					"sum(%s.clientTotalSubmittedTrans);", mv, mv));
			mix = (double[])proxy.getVariable("dbseer_tx_mix");
		}
		catch(Exception e)
		{
			DBSeerExceptionHandler.handleException(e);
		}
		return mix;
	}

	public static double getMaxTPS(DBSeerDataSet dataset)
	{
		MatlabProxy proxy = DBSeerGUI.proxy;
		double[] maxTPS = null;

		try
		{
			String mv = dataset.getUniqueModelVariableName();
			proxy.eval(String.format("dbseer_max_tps = max(%s.clientTotalSubmittedTrans);", mv));
			maxTPS = (double[])proxy.getVariable("dbseer_max_tps");
		}
		catch(Exception e)
		{
			DBSeerExceptionHandler.handleException(e);
		}
		return maxTPS[0];
	}

	public static double getMinTPS(DBSeerDataSet dataset)
	{
		MatlabProxy proxy = DBSeerGUI.proxy;
		double[] minTPS = null;

		try
		{
			String mv = dataset.getUniqueModelVariableName();
			proxy.eval(String.format("dbseer_min_tps = min(%s.clientTotalSubmittedTrans);", mv));
			minTPS = (double[])proxy.getVariable("dbseer_min_tps");
		}
		catch(Exception e)
		{
			DBSeerExceptionHandler.handleException(e);
		}
		return minTPS[0];
	}

	public static int getTotalRows(DBSeerDataSet dataset)
	{
		MatlabProxy proxy = DBSeerGUI.proxy;
		double[] totalRows = null;

		try
		{
			String mv = dataset.getUniqueModelVariableName();
			proxy.eval(String.format("dbseer_total_rows = size(%s.clientTotalSubmittedTrans, 1);", mv));
			totalRows = (double[])proxy.getVariable("dbseer_total_rows");
		}
		catch(Exception e)
		{
			DBSeerExceptionHandler.handleException(e);
		}
		return (int)totalRows[0];
	}
}
