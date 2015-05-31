package dbseer.gui;

import matlabcontrol.MatlabInvocationException;
import matlabcontrol.MatlabProxy;

import javax.swing.*;
import java.util.StringTokenizer;

/**
 * Created by dyoon on 5/1/15.
 *
 * Handles both Java + Matlab exceptions and show a dialog to the user.
 */
public class DBSeerExceptionHandler
{
	public static void handleException(Exception e)
	{
		if (e instanceof MatlabInvocationException)
		{
			if (!DBSeerGUI.isProxyRenewing)
			{
				String errorMsg = addLineBreaksToMessage(e.getMessage() + ":\n" + getLastError(), 120);

				JOptionPane.showMessageDialog(null, errorMsg, "MATLAB Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
		else
		{
			JOptionPane.showMessageDialog(null, e.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}

	public static void handleException(Exception e, String title)
	{
		if (e instanceof MatlabInvocationException)
		{
			if (!DBSeerGUI.isProxyRenewing)
			{
				String errorMsg = addLineBreaksToMessage(e.getMessage() + ":\n" + getLastError(), 120);

				JOptionPane.showMessageDialog(null, errorMsg, "MATLAB Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
		else
		{
			JOptionPane.showMessageDialog(null, e.getMessage(), title,
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}

	public static String getLastError()
	{
		MatlabProxy proxy = DBSeerGUI.proxy;

		String errorMessage = "";
		try
		{
			proxy.eval("dbseer_lasterror = lasterror;");
			Object[] returnObj = proxy.returningEval("dbseer_lasterror.message", 1);
			errorMessage = (String)returnObj[0];
		}
		catch (MatlabInvocationException e)
		{
			JOptionPane.showMessageDialog(null, "Failed to retrieve the last error from MATLAB", "Error",
					JOptionPane.ERROR_MESSAGE);
		}
		return errorMessage;
	}

	private static String addLineBreaksToMessage(String input, int maxLineLength)
	{
		StringTokenizer tok = new StringTokenizer(input, " ");
		StringBuilder output = new StringBuilder(input.length());
		int lineLen = 0;
		while (tok.hasMoreTokens()) {
			String word = tok.nextToken();

			if (lineLen + word.length() > maxLineLength) {
				output.append("\n");
				lineLen = 0;
			}
			output.append(word);
			output.append(" ");
			lineLen += word.length() + 1;
		}
		return output.toString();
	}
}
