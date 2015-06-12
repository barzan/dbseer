package dbseer.gui;

import dbseer.stat.OctaveRunner;
import dbseer.stat.StatisticalPackageRunner;
import dk.ange.octave.OctaveEngine;
import dk.ange.octave.exception.OctaveEvalException;
import dk.ange.octave.type.OctaveDouble;
import dk.ange.octave.type.OctaveString;
import dk.ange.octave.type.OctaveStruct;
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
				String errorMsg = addLineBreaksToMessage(e.getMessage() + ":\n" + getLastMatLabError(), 120);

				JOptionPane.showMessageDialog(null, errorMsg, "MATLAB Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
		else if (e instanceof OctaveEvalException)
		{
			String errorMsg = addLineBreaksToMessage(e.getMessage() + ":\n" + getLastOctaveError(), 120);

			JOptionPane.showMessageDialog(null, errorMsg, "Octave Error",
					JOptionPane.ERROR_MESSAGE);
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
				String errorMsg = addLineBreaksToMessage(e.getMessage() + ":\n" + getLastMatLabError(), 120);

				JOptionPane.showMessageDialog(null, errorMsg, "MATLAB Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
		else if (e instanceof OctaveEvalException)
		{
			String errorMsg = addLineBreaksToMessage(e.getMessage() + ":\n" + getLastOctaveError(), 120);

			JOptionPane.showMessageDialog(null, errorMsg, "Octave Error",
					JOptionPane.ERROR_MESSAGE);
		}
		else
		{
			JOptionPane.showMessageDialog(null, e.getMessage(), title,
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}

	public static String getLastMatLabError()
	{
		StatisticalPackageRunner runner = DBSeerGUI.runner;

		String errorMessage = "";
		runner.eval("dbseer_lasterror = lasterror;");
		errorMessage = runner.getVariableString("dbseer_lasterror.message");
		return errorMessage;
	}

	public static String getLastOctaveError()
	{
		StatisticalPackageRunner runner = DBSeerGUI.runner;
		OctaveEngine engine = OctaveRunner.getInstance().getEngine();

		String errorMessage = "";
		runner.eval("dbseer_lasterror = lasterror");
		runner.eval("dbseer_lasterror_message = dbseer_lasterror.message");
		runner.eval("dbseer_lasterror_file = dbseer_lasterror.stack.file");
		runner.eval("dbseer_lasterror_line = dbseer_lasterror.stack.line");
		errorMessage = ((OctaveString)engine.get("dbseer_lasterror_message")).getString();

		OctaveString errorFile = (OctaveString) engine.get("dbseer_lasterror_file");
		OctaveDouble errorLine = (OctaveDouble) engine.get("dbseer_lasterror_line");
		if (errorFile != null)
		{
			errorMessage += " in " + errorFile.getString();
		}
		if (errorLine != null)
		{
			errorMessage += " at " + String.format("%.0f", errorLine.get(1));
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
