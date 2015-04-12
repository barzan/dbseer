package dbseer.gui.frame;

import dbseer.comp.TransactionReader;
import dbseer.comp.data.Transaction;
import dbseer.gui.user.DBSeerDataSet;
import dbseer.gui.user.DBSeerTransactionSample;
import dbseer.gui.user.DBSeerTransactionSampleList;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * Created by dyoon on 14. 11. 26..
 */
public class DBSeerShowQueryFrame extends JFrame implements ActionListener
{
	private final DBSeerDataSet dataset;
	private final int series;
	private final int category;
	private final double[] timestamp;
	private JTextArea textArea = new JTextArea(40, 100);
	private JButton closeButton;
	private JButton nextButton;
	private boolean isQueryAvailable;
	private boolean showAll;

	private int currentCategory = 0;
	private String statementOffsetFile;
	private TransactionReader reader = null;

	public DBSeerShowQueryFrame(DBSeerDataSet dataset, int series, int category, double[] timestamp, boolean showAll)
	{
		this.dataset = dataset;
		this.series = series;
		this.category = category;
		this.timestamp = timestamp;
		this.isQueryAvailable = false;
		this.showAll = showAll;
		this.currentCategory = 0;
		initializeGUI();
		this.setTitle("Queries from Selected Point");
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

		List<String> statementOffsetFileList = dataset.getStatementOffsetFileList();

		statementOffsetFile = statementOffsetFileList.get(series);

		// for pie chart (e.g. transaction mix)
		if (category == -1)
		{
			while (currentCategory < timestamp.length)
			{
				int time = (int) timestamp[currentCategory];
				reader = new TransactionReader(dataset.getTransactionFilePath(),
						dataset.getQueryFilePath(), dataset.getStatementFilePath(),
						statementOffsetFile, time);
				if (!reader.initialize())
				{
					isQueryAvailable = false;
					break;
				}
				else
				{
					String tx = reader.getNextTransaction();
					if (tx == "")
					{
						++currentCategory;
					}
					else
					{
						String output = getTransactionInformation() + tx;
						textArea.setText(output);
						isQueryAvailable = true;
						break;
					}
				}
			}
		}
		else
		{
			if (showAll)
			{
				String output = "";
				nextButton.setEnabled(false);

				for (int i = 0; i < statementOffsetFileList.size(); ++i)
				{
					String offsetFile = statementOffsetFileList.get(i);
					int time = (int) timestamp[category];
					reader = new TransactionReader(dataset.getTransactionFilePath(), dataset.getQueryFilePath(),
							dataset.getStatementFilePath(),
							offsetFile, time);

					if (reader.initialize())
					{
						String tx = reader.getNextTransaction();
						if (tx != "")
						{
							output += "<< " + dataset.getTransactionTypeNames().get(i) + " >>\n";
							output += getTransactionInformation();
							output += tx;
							output += "\n\n";
							isQueryAvailable = true;
						}
					}
				}
				textArea.setText(output);
			}
			else
			{
				int time = (int) timestamp[category];
				reader = new TransactionReader(dataset.getTransactionFilePath(), dataset.getQueryFilePath(),
						dataset.getStatementFilePath(),
						statementOffsetFile, time);
				if (!reader.initialize())
				{
					isQueryAvailable = false;
				}
				else
				{
					String tx = reader.getNextTransaction();
					if (tx.isEmpty())
					{
						isQueryAvailable = false;
					}
					else
					{
						String output = getTransactionInformation() + tx;
						textArea.setText(output);
						isQueryAvailable = true;
					}
				}
			}
		}

//		List<DBSeerTransactionSampleList> sampleLists = dataset.getTransactionSampleLists();
//		DBSeerTransactionSampleList sampleList = sampleLists.get(series);
//		List<DBSeerTransactionSample> samples = sampleList.getSamples();
//		if (category == -1)
//		{
//			DBSeerTransactionSample sample = samples.get(0);
//			textArea.setText(sample.getStatement());
//			isQueryAvailable = true;
//		}
//		else
//		{
//			int time = (int) timestamp[category];
//			if (showAll)
//			{
//				String output = "";
//				for (int i = 0; i < sampleLists.size(); ++i)
//				{
//					DBSeerTransactionSampleList list = sampleLists.get(i);
//					samples = list.getSamples();
//
//					for (DBSeerTransactionSample sample : samples)
//					{
//						if (sample.getTimestamp() == time)
//						{
//							output += "<< " + dataset.getTransactionTypeNames().get(i) + " >>\n";
//							output += sample.getStatement();
//							output += "\n\n";
////							textArea.setText("<< " + dataset.getTransactionTypeNames().get(i) " >>\n");
////							textArea.setText(sample.getStatement());
//							isQueryAvailable = true;
//							break;
//						}
//					}
//				}
//				textArea.setText(output);
//			}
//			else
//			{
//				for (DBSeerTransactionSample sample : samples)
//				{
//					if (sample.getTimestamp() == time)
//					{
//						textArea.setText(sample.getStatement());
//						isQueryAvailable = true;
//						break;
//					}
//				}
//			}
//		}
	}

	private String getTransactionInformation()
	{
		return String.format("- Tx ID: %d, Username: %s, Latency: %d ms\n", reader.getLastTransactionId(),
				reader.getLastTransactionUser(), reader.getLastTransactionLatency());
	}

	public boolean isQueryAvailable()
	{
		return isQueryAvailable;
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
			if (category == -1)
			{
				if (reader != null)
				{
					String output = reader.getNextTransaction();
					while (output == "" && currentCategory < timestamp.length)
					{
						++currentCategory;
						int time = (int)timestamp[currentCategory];
						reader = new TransactionReader(dataset.getTransactionFilePath(),
								dataset.getQueryFilePath(), dataset.getStatementFilePath(),
								statementOffsetFile, time);
						reader.initialize();
						output = reader.getNextTransaction();
					}
					if (output != "")
					{
						String orig = textArea.getText();
						orig += "\n << Next Transaction >> \n";
						orig += getTransactionInformation();
						orig += output;
						textArea.setText(orig);
					}
					else
					{
						String orig = textArea.getText();
						orig += "\n << End of Transactions >> \n";
						textArea.setText(orig);
						nextButton.setEnabled(false);
					}
				}
			}
			else
			{
				if (reader != null)
				{
					String output = reader.getNextTransaction();
					if (output != "")
					{
						String orig = textArea.getText();
						orig += "\n << Next Transaction >> \n";
						orig += getTransactionInformation();
						orig += output;
						textArea.setText(orig);
					}
					else
					{
						String orig = textArea.getText();
						orig += "\n << End of Transactions >> \n";
						textArea.setText(orig);
						nextButton.setEnabled(false);
					}
				}
			}
		}
	}
}
