package dbseer.gui.frame;

import dbseer.comp.TransactionReader;
import dbseer.gui.DBSeerExceptionHandler;
import dbseer.gui.DBSeerGUI;
import dbseer.gui.user.DBSeerDataSet;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;

/**
 * Created by dyoon on 14. 11. 26..
 */
public class DBSeerShowTransactionExampleFrame extends JFrame implements ActionListener
{
	private JTextArea textArea = new JTextArea(40, 100);
	private JButton closeButton;
	private JButton nextButton;

	private int type;
	private int nextIndex;

	public DBSeerShowTransactionExampleFrame(int type)
	{
		this.type = type;
		nextIndex = 0;
		initializeGUI();
		this.setTitle("View Transaction Examples");
	}

	private void initializeGUI()
	{
		this.setLayout(new MigLayout("fill"));
		DefaultCaret caret = (DefaultCaret)textArea.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

		textArea.setEditable(false);
		textArea.setLineWrap(true);
		JScrollPane scrollPane = new JScrollPane(textArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setViewportView(textArea);
		scrollPane.setAutoscrolls(false);
		this.add(scrollPane, "grow, wrap");

		nextButton = new JButton("Next");
		nextButton.addActionListener(this);
		this.add(nextButton, "split 2, align center");

		closeButton = new JButton("Close");
		closeButton.addActionListener(this);
		this.add(closeButton);

		try
		{
			String firstSample = DBSeerGUI.middlewareSocket.requestTransactionSample(type, nextIndex);
			if (firstSample == null)
			{
				textArea.setText("An example for this transaction type is not available.");
				nextButton.setEnabled(false);
			}
			else
			{
				String output = String.format("<Example #%d>\n", nextIndex + 1);
				output += firstSample;
				textArea.setText(output);
				nextIndex++;
			}
		}
		catch (IOException e)
		{
			DBSeerExceptionHandler.handleException(e);
		}
	}

	@Override
	public void actionPerformed(ActionEvent actionEvent)
	{
		final JFrame frame = this;
		if (actionEvent.getSource() == closeButton)
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					frame.dispose();
				}
			});
		}
		else if (actionEvent.getSource() == nextButton)
		{
			String sample = null;
			try
			{
				sample = DBSeerGUI.middlewareSocket.requestTransactionSample(type, nextIndex);
			}
			catch (IOException e)
			{
				DBSeerExceptionHandler.handleException(e);
			}

			String output = textArea.getText();
			output += "\n\n";

			if (sample == null)
			{
				output += "<End of transaction examples>";
				textArea.setText(output);
				nextButton.setEnabled(false);
			}
			else
			{
				output += String.format("<Example #%d>\n", nextIndex + 1);
				output += sample;
				textArea.setText(output);
				nextIndex++;
			}
		}
	}
}
