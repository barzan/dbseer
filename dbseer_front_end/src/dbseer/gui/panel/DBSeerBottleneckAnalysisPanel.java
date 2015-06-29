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
