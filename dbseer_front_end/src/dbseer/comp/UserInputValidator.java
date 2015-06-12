package dbseer.comp;

import dbseer.gui.DBSeerGUI;
import dbseer.stat.StatisticalPackageRunner;
import matlabcontrol.MatlabInvocationException;
import matlabcontrol.MatlabProxy;

import javax.swing.*;

/**
 * Created by dyoon on 2014. 6. 22..
 */
public class UserInputValidator
{
	public UserInputValidator()
	{

	}

	public static boolean matchMatrixDimension(String m1, String m2)
	{
		int d1 = 0;
		int d2 = 0;
		String[] tokens = m1.trim().split("[\\[\\]\\s]+");
		for (String token : tokens)
		{
			if (!token.isEmpty())
			{
				++d1;
			}
		}

		tokens = m2.trim().split("[\\[\\]\\s]+");
		for (String token : tokens)
		{
			if (!token.isEmpty())
			{
				++d2;
			}
		}

		if (d1 == d2) return true;
		return false;
	}

	public static boolean validateMatlabMatrix(String input, String fieldName, boolean isEnabled)
	{
		if (!isEnabled) return true;

		StatisticalPackageRunner runner = DBSeerGUI.runner;

		return runner.eval("validate_test = " + input + ";");
	}

	public static boolean validateSingleRowMatrix(String input, String fieldName, boolean isEnabled)
	{
		if (!isEnabled) return true;

		boolean matchRegex = input.matches("\\[\\s*(\\d+(.\\d+)?)*(\\s+(\\d+(.\\d+)?)+)*\\s*\\]");
		if (!matchRegex)
		{
			System.out.println(input);
			JOptionPane.showMessageDialog(null, "Data validation error at " + fieldName + ".\n" +
							"It is not a valid single row matrix.", "Error",
					JOptionPane.ERROR_MESSAGE);
		}
		return matchRegex;
	}

	public static boolean validateNumber(String input, String fieldName, boolean isEnabled)
	{
		if (!isEnabled) return true;

		boolean matchRegex = input.matches("\\d+(.\\d+)?");
		if (!matchRegex)
		{
			JOptionPane.showMessageDialog(null, "Data validation error at " + fieldName + ".\n" +
							"It must be a positive number.", "Error",
					JOptionPane.ERROR_MESSAGE);
		}
		return matchRegex;
	}

	public static boolean validateSingleRowMatrix(String input)
	{
		return input.matches("\\[\\s*(\\d+(.\\d+)?)*(\\s+(\\d+(.\\d+)?)+)*\\s*\\]");
	}

	public static boolean validateNumber(String input)
	{
		return input.matches("\\d+(.\\d+)?");
	}
}
