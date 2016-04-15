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
import java.nio.channels.UnresolvedAddressException;
import java.util.StringTokenizer;

/**
 * Created by dyoon on 5/1/15.
 *
 * Handles both Java + Matlab exceptions and show a dialog to the user.
 */
public class DBSeerExceptionHandler
{
	public static void showDialog(String msg)
	{
		JOptionPane.showMessageDialog(DBSeerGUI.mainFrame, msg, "Message",
				JOptionPane.INFORMATION_MESSAGE);
	}

	public static void handleException(Exception e)
	{
		try
		{
			if (e instanceof MatlabInvocationException)
			{
				if (!DBSeerGUI.isProxyRenewing)
				{
					String errorMsg = addLineBreaksToMessage(e.getMessage() + ":\n" + getLastMatLabError(), 120);

					JOptionPane.showMessageDialog(DBSeerGUI.mainFrame, errorMsg, "MATLAB Error",
							JOptionPane.ERROR_MESSAGE);
				}
			}
			else if (e instanceof OctaveEvalException)
			{
				String errorMsg = addLineBreaksToMessage(e.getMessage() + ":\n" + getLastOctaveError(), 120);

				JOptionPane.showMessageDialog(DBSeerGUI.mainFrame, errorMsg, "Octave Error",
						JOptionPane.ERROR_MESSAGE);
			}
			else
			{
				if (e instanceof NullPointerException)
				{
					JOptionPane.showMessageDialog(DBSeerGUI.mainFrame, "NullPointerException: consult the stack trace for debugging.",
							"Error",
							JOptionPane.ERROR_MESSAGE);
					e.printStackTrace();
				}
				else if (e instanceof UnresolvedAddressException)
				{
					JOptionPane.showMessageDialog(DBSeerGUI.mainFrame, "IP Address cannot be resolved.",
							"Error",
							JOptionPane.ERROR_MESSAGE);
				}
				else
				{
					JOptionPane.showMessageDialog(DBSeerGUI.mainFrame, e.getMessage(), "Error",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		}
		catch (Exception e1)
		{
			e1.printStackTrace();
		}
	}

	public static void handleException(Exception e, String title)
	{
		try
		{
			if (e instanceof MatlabInvocationException)
			{
				if (!DBSeerGUI.isProxyRenewing)
				{
					String errorMsg = addLineBreaksToMessage(e.getMessage() + ":\n" + getLastMatLabError(), 120);

					JOptionPane.showMessageDialog(DBSeerGUI.mainFrame, errorMsg, "MATLAB Error",
							JOptionPane.ERROR_MESSAGE);
				}
			}
			else if (e instanceof OctaveEvalException)
			{
				String errorMsg = addLineBreaksToMessage(e.getMessage() + ":\n" + getLastOctaveError(), 120);

				JOptionPane.showMessageDialog(DBSeerGUI.mainFrame, errorMsg, "Octave Error",
						JOptionPane.ERROR_MESSAGE);
			}
			else
			{
				JOptionPane.showMessageDialog(DBSeerGUI.mainFrame, e.getMessage(), title,
						JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			}
		}
		catch (Exception e1)
		{
			e1.printStackTrace();
		}
	}

	public static String getLastMatLabError() throws Exception
	{
		StatisticalPackageRunner runner = DBSeerGUI.runner;

		String errorMessage = "";
		runner.eval("dbseer_lasterror = lasterror;");
		errorMessage = runner.getVariableString("dbseer_lasterror.message");
		return errorMessage;
	}

	public static String getLastOctaveError() throws Exception
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
