package dbseer.gui.comp;

import javax.swing.*;

/**
 * Created by dyoon on 2014. 6. 22..
 */
public class UserInputValidator
{
	public UserInputValidator()
	{

	}

	public static boolean validateSingleRowMatrix(String input, String fieldName, boolean isEnabled)
	{
		if (!isEnabled) return true;

		boolean matchRegex = input.matches("\\[\\s*\\d*(\\s+\\d+)*\\s*\\]");
		if (!matchRegex)
		{
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
							"It is not a number.", "Error",
					JOptionPane.ERROR_MESSAGE);
		}
		return matchRegex;
	}
}
