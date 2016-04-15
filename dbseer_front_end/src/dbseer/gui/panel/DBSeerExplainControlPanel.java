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

import dbseer.gui.DBSeerConstants;
import dbseer.gui.DBSeerGUI;
import dbseer.gui.actions.ExplainChartAction;
import dbseer.gui.frame.DBSeerCausalModelFrame;
import dbseer.gui.user.DBSeerCausalModel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

/**
 * Created by dyoon on 14. 11. 27..
 */
public class DBSeerExplainControlPanel extends JPanel implements MouseListener
{
	private JButton updateExplanationButton;
	private JButton explainButton;
	private JButton togglePredicateButton;
	private JButton savePredicateButton;
	private JLabel confidenceThresholdLabel;
	private JLabel percentLabel;
	private JTextField confidenceThresholdTextField;
	private JList predicateList;
	private JList explanationList;
	private JPanel statusPanel;
	private JScrollPane predicateScrollPane;
	private JScrollPane explanationScrollPane;

	private DefaultListModel predicateListModel;
	private DefaultListModel explanationListModel;

	private DBSeerExplainChartPanel chartPanel;

	private ArrayList<DBSeerCausalModel> explanations;

	public DBSeerExplainControlPanel()
	{
		explanations = new ArrayList<DBSeerCausalModel>();
	}

	public void initialize()
	{
		this.setLayout(new MigLayout("ins 1", "", "[][grow, fill][][grow, fill][][][]"));

		predicateList = new JList();
		explanationList = new JList();

		predicateList.setVisibleRowCount(12);
		explanationList.setVisibleRowCount(12);
		explanationList.addMouseListener(this);

		predicateListModel = new DefaultListModel();
		explanationListModel = new DefaultListModel();

		predicateList.setModel(predicateListModel);
		explanationList.setModel(explanationListModel);

		predicateScrollPane = new JScrollPane();
		explanationScrollPane = new JScrollPane();

		predicateScrollPane.setViewportView(predicateList);
		predicateScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		predicateScrollPane.setBorder(BorderFactory.createTitledBorder("Explanatory Predicates"));
		explanationScrollPane.setViewportView(explanationList);
		explanationScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		explanationScrollPane.setBorder(BorderFactory.createTitledBorder("Possible Causes (Confidence in parentheses)"));

		confidenceThresholdLabel = new JLabel("Confidence Threshold (0-100%):");
		confidenceThresholdTextField = new JTextField(7);
		confidenceThresholdTextField.setText(String.valueOf(DBSeerConstants.EXPLAIN_DEFAULT_CONFIDENCE_THRESHOLD));

		updateExplanationButton = new JButton(new ExplainChartAction("Update", DBSeerConstants.EXPLAIN_UPDATE_EXPLANATIONS, chartPanel));
		explainButton = new JButton(new ExplainChartAction("Explain", DBSeerConstants.EXPLAIN_EXPLAIN, chartPanel));
		togglePredicateButton = new JButton(new ExplainChartAction("Toggle Explanatory Predicates: Absolute/Relative", DBSeerConstants.EXPLAIN_TOGGLE_PREDICATES, chartPanel));
		savePredicateButton = new JButton(new ExplainChartAction("Save Predicates as a New Causal Model", DBSeerConstants.EXPLAIN_SAVE_PREDICATES, chartPanel));

		statusPanel = new JPanel();
		statusPanel.setLayout(new MigLayout("fill, ins 0"));
		statusPanel.add(DBSeerGUI.explainStatus, "grow");

		this.add(confidenceThresholdLabel, "split 3");
		this.add(confidenceThresholdTextField);
		this.add(updateExplanationButton, "wrap");
		this.add(explanationScrollPane, "grow, spanx, wrap");
		this.add(togglePredicateButton, "align right, wrap");
		this.add(predicateScrollPane, "grow, spanx, wrap");
		this.add(explainButton, "growx, wrap, hmax pref");
		this.add(savePredicateButton, "growx, wrap, hmax pref");
		DBSeerGUI.explainStatus.setMinimumSize(new Dimension(30,20));
		this.add(DBSeerGUI.explainStatus, "growx, spanx");
//		this.add(statusPanel, "spanx, grow");
	}

	public JList getPredicateList()
	{
		return predicateList;
	}

	public JList getExplanationList()
	{
		return explanationList;
	}

	public DefaultListModel getPredicateListModel()
	{
		return predicateListModel;
	}

	public DefaultListModel getExplanationListModel()
	{
		return explanationListModel;
	}

	public JTextField getConfidenceThresholdTextField()
	{
		return confidenceThresholdTextField;
	}

	public void setChartPanel(DBSeerExplainChartPanel chartPanel)
	{
		this.chartPanel = chartPanel;
	}

	public ArrayList<DBSeerCausalModel> getExplanations()
	{
		return explanations;
	}

	@Override
	public void mouseClicked(MouseEvent mouseEvent)
	{
		// disabled for now
		if (mouseEvent.getClickCount() == 2)
		{
			int index = explanationList.getSelectedIndex();
			if (index >= 0)
			{
				final DBSeerCausalModel causalModel = explanations.get(index);
				SwingUtilities.invokeLater(new Runnable()
				{
					@Override
					public void run()
					{
						DBSeerCausalModelFrame frame = new DBSeerCausalModelFrame(causalModel);
						frame.setPreferredSize(new Dimension(1280, 800));
						frame.pack();
						frame.setVisible(true);
					}
				});
			}
		}
	}

	@Override
	public void mousePressed(MouseEvent mouseEvent)
	{

	}

	@Override
	public void mouseReleased(MouseEvent mouseEvent)
	{

	}

	@Override
	public void mouseEntered(MouseEvent mouseEvent)
	{

	}

	@Override
	public void mouseExited(MouseEvent mouseEvent)
	{

	}
}
