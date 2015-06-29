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

import dbseer.comp.MatlabFunctions;
import dbseer.gui.DBSeerConstants;
import dbseer.gui.user.DBSeerDataSet;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

/**
 * Created by dyoon on 5/1/15.
 */
public class DBSeerPredictionWithTPSMixturePanel extends JPanel implements ActionListener
{
	DBSeerDataSet dataset = null;
	private ArrayList<JSlider> mixtureSliders = new ArrayList<JSlider>();
	private ArrayList<JTextField> mixtureFields = new ArrayList<JTextField>();
	private ArrayList<JLabel> transactionLabels = new ArrayList<JLabel>();

	private JButton resetToOriginalMixButton = new JButton("Reset Mixture");
	private JLabel minTPSLabel = new JLabel("Minimum TPS");
	private JLabel maxTPSLabel = new JLabel("Maximum TPS");
	private JLabel selectLabel = new JLabel("Please select a train config.");
	private JSlider minTPSSlider = new JSlider(DBSeerConstants.MIN_PREDICTION_TPS, DBSeerConstants.MAX_PREDICTION_TPS, 0);
	private JSlider maxTPSSlider = new JSlider(DBSeerConstants.MIN_PREDICTION_TPS, DBSeerConstants.MAX_PREDICTION_TPS, 0);
	private JTextField minTPSField = new JTextField(4);
	private JTextField maxTPSField = new JTextField(4);

	private JPanel tpsPanel = new JPanel();
	private JPanel mixturePanel = new JPanel();

	private boolean firstInitialization = false;
	private double[] originalMix;

	private static final int NUM_TRANSACTION_PER_LINE = 3;

	public DBSeerPredictionWithTPSMixturePanel()
	{
		this.setLayout(new MigLayout("align 50% 50%"));
		tpsPanel.setLayout(new MigLayout());
		mixturePanel.setLayout(new MigLayout());

//		this.add(tpsPanel, "wrap");
//		this.add(mixturePanel);
		this.add(selectLabel);

		minTPSField.addKeyListener(new KeyAdapter()
		{
			@Override
			public void keyReleased(KeyEvent keyEvent)
			{
				String newValue = minTPSField.getText();
				minTPSSlider.setValue(0);
				if (!newValue.matches("\\d+(\\.\\d*)?"))
				{
					return;
				}
				double value = Double.parseDouble(newValue);
				minTPSSlider.setValue((int) value);
			}
		});
		maxTPSField.addKeyListener(new KeyAdapter()
		{
			@Override
			public void keyReleased(KeyEvent keyEvent)
			{
				String newValue = maxTPSField.getText();
				maxTPSSlider.setValue(0);
				if (!newValue.matches("\\d+(\\.\\d*)?"))
				{
					return;
				}
				double value = Double.parseDouble(newValue);
				maxTPSSlider.setValue((int) value);
			}
		});
		minTPSSlider.addChangeListener(new ChangeListener()
		{
			@Override
			public void stateChanged(ChangeEvent changeEvent)
			{
				minTPSField.setText(String.format("%d", minTPSSlider.getValue()));
				if (minTPSSlider.getValue() > maxTPSSlider.getValue())
				{
					minTPSLabel.setForeground(Color.red);
					minTPSField.setForeground(Color.red);
					maxTPSLabel.setForeground(Color.red);
					maxTPSField.setForeground(Color.red);
				}
				else
				{
					minTPSLabel.setForeground(Color.black);
					minTPSField.setForeground(Color.black);
					maxTPSLabel.setForeground(Color.black);
					maxTPSField.setForeground(Color.black);
				}
			}
		});
		maxTPSSlider.addChangeListener(new ChangeListener()
		{
			@Override
			public void stateChanged(ChangeEvent changeEvent)
			{
				maxTPSField.setText(String.format("%d", maxTPSSlider.getValue()));
				if (minTPSSlider.getValue() > maxTPSSlider.getValue())
				{
					minTPSLabel.setForeground(Color.red);
					minTPSField.setForeground(Color.red);
					maxTPSLabel.setForeground(Color.red);
					maxTPSField.setForeground(Color.red);
				}
				else
				{
					minTPSLabel.setForeground(Color.black);
					minTPSField.setForeground(Color.black);
					maxTPSLabel.setForeground(Color.black);
					maxTPSField.setForeground(Color.black);
				}
			}
		});

		resetToOriginalMixButton.addActionListener(this);
	}

	public void setDataset(DBSeerDataSet dataset)
	{
		this.dataset = dataset;
		initialize();
	}

	private void initialize()
	{
		if (!firstInitialization)
		{
			this.setLayout(new MigLayout("fill, ins 0"));
			this.remove(selectLabel);
			this.add(tpsPanel, "growx, wrap");
			this.add(mixturePanel, "grow");

			firstInitialization = true;
		}
//		this.setLayout(new MigLayout());

		// remove transaction labels.
		for (JLabel label : transactionLabels)
		{
			mixturePanel.remove(label);
		}
		transactionLabels.clear();

		// remove min/max tps sliders
		tpsPanel.remove(minTPSLabel);
		tpsPanel.remove(maxTPSLabel);
		tpsPanel.remove(minTPSSlider);
		tpsPanel.remove(maxTPSSlider);
		tpsPanel.remove(minTPSField);
		tpsPanel.remove(maxTPSField);

		// remove previous mixture sliders.
		for (JSlider slider : mixtureSliders)
		{
			mixturePanel.remove(slider);
		}
		mixtureSliders.clear();

		// remove field.
		for (JTextField field : mixtureFields)
		{
			mixturePanel.remove(field);
		}
		mixtureFields.clear();

		// get new min/max TPS.
		double maxTPS = MatlabFunctions.getMaxTPS(dataset);
		double minTPS = MatlabFunctions.getMinTPS(dataset);

//		minTPSSlider = new JSlider(DBSeerConstants.MIN_PREDICTION_TPS, DBSeerConstants.MAX_PREDICTION_TPS, (int)minTPS);
//		maxTPSSlider = new JSlider(DBSeerConstants.MIN_PREDICTION_TPS, DBSeerConstants.MAX_PREDICTION_TPS, (int)maxTPS);

		minTPSSlider.setValue((int)minTPS);
		maxTPSSlider.setValue((int)maxTPS);
		minTPSField.setText(String.format("%d", (int) minTPS));
		maxTPSField.setText(String.format("%d", (int) maxTPS));

		tpsPanel.add(minTPSLabel);
		tpsPanel.add(minTPSSlider);
		tpsPanel.add(minTPSField);
		tpsPanel.add(maxTPSLabel);
		tpsPanel.add(maxTPSSlider);
		tpsPanel.add(maxTPSField, "wrap");

		// set borders.
		tpsPanel.setBorder(BorderFactory.createTitledBorder("Min/Max TPS (0 ~ 10000)"));
		mixturePanel.setBorder(BorderFactory.createTitledBorder("Transaction Mix (%)"));

		// get new transaction mix.
		originalMix = MatlabFunctions.getTotalTransactionMix(dataset);

		if (originalMix != null)
		{
			int idx = 0;
			int num = 0;
			for (double m : originalMix)
			{
				JLabel label = new JLabel(dataset.getTransactionTypeNames().get(idx++));
				final JSlider slider = new JSlider(JSlider.HORIZONTAL, 0, 100, (int)Math.round(m * 100));
				final JTextField field = new JTextField(4);
				field.setText(String.format("%.0f", m * 100));

				slider.addChangeListener(new ChangeListener()
				{
					@Override
					public void stateChanged(ChangeEvent changeEvent)
					{
						field.setText(String.format("%d", slider.getValue()));
					}
				});
				field.addKeyListener(new KeyAdapter()
				{
					@Override
					public void keyReleased(KeyEvent keyEvent)
					{
						String newValue = field.getText();
						slider.setValue(0);
						if (!newValue.matches("\\d+(\\.\\d*)?"))
						{
							return;
						}
						double value = Double.parseDouble(newValue);
						slider.setValue((int) value);
					}
				});

				transactionLabels.add(label);
				mixtureSliders.add(slider);
				mixtureFields.add(field);
				mixturePanel.add(label);
				mixturePanel.add(slider);
				if (++num == NUM_TRANSACTION_PER_LINE)
				{
					mixturePanel.add(field, "wrap");
					num = 0;
				}
				else
				{
					mixturePanel.add(field);
				}
				mixturePanel.add(resetToOriginalMixButton, "newline");
			}
		}

		this.revalidate();
	}

	public int getNumTransactions()
	{
		return mixtureSliders.size();
	}

	public String getTransactionMix()
	{
		String mix = "[";
		for (JSlider slider : mixtureSliders)
		{
			mix += slider.getValue();
			mix += " ";
		}
		mix += "]";
		return mix;
	}

	public boolean checkTransactionMix()
	{
		boolean allZero = true;
		for (JSlider slider : mixtureSliders)
		{
			if (slider.getValue() > 0)
			{
				allZero = false;
			}
		}
		return !allZero;
	}

	public int getMaxTPS()
	{
		return maxTPSSlider.getValue();
	}

	public int getMinTPS()
	{
		return minTPSSlider.getValue();
	}

	public void setMixture(int idx, int value)
	{
		mixtureSliders.get(idx).setValue(value);
	}

	@Override
	public void actionPerformed(ActionEvent actionEvent)
	{
		if (actionEvent.getSource() == resetToOriginalMixButton)
		{
			if (originalMix != null)
			{
				int idx = 0;
				for (double m : originalMix)
				{
					mixtureSliders.get(idx++).setValue((int)Math.round(m*100));
				}
			}
		}
	}
}
