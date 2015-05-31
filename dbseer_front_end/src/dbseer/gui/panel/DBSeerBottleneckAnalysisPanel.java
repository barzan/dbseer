package dbseer.gui.panel;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;

/**
 * Created by dyoon on 5/9/15.
 */
public class DBSeerBottleneckAnalysisPanel extends JPanel
{
	public static final int BOTTLENECK_MAX_THROUGHPUT = 0;
	public static final int BOTTLENECK_RESOURCE = 1;

	public static String[] questions = {
			"maximum sustainable throughput",
			"bottleneck resource"
	};

	public static String[] actualFunctions = {
			"BottleneckAnalysisMaxThroughput",
			"BottleneckAnalysisResource"
	};


	private JLabel questionLabel;
	private JLabel questionLabel1;
	private JComboBox questionComboBox;

	public DBSeerBottleneckAnalysisPanel()
	{
		this.setLayout(new MigLayout("fillx"));
		initialize();
	}

	private void initialize()
	{
		questionComboBox = new JComboBox(DBSeerBottleneckAnalysisPanel.questions);

		questionLabel = new JLabel("What is the");
		questionLabel1 = new JLabel("in my database?");

		this.add(questionLabel, "split 3");
		this.add(questionComboBox);
		this.add(questionLabel1);
	}

	public int getSelectedQuestion()
	{
		return questionComboBox.getSelectedIndex();
	}
}
