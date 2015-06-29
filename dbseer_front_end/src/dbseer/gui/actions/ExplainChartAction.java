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

package dbseer.gui.actions;

import dbseer.comp.UserInputValidator;
import dbseer.gui.DBSeerConstants;
import dbseer.gui.DBSeerExceptionHandler;
import dbseer.gui.DBSeerGUI;
import dbseer.gui.chart.DBSeerXYLineAndShapeRenderer;
import dbseer.gui.panel.DBSeerExplainChartPanel;
import dbseer.gui.user.DBSeerCausalModel;
import dbseer.gui.user.DBSeerPredicate;
import dbseer.stat.OctaveRunner;
import dbseer.stat.StatisticalPackageRunner;
import dk.ange.octave.type.Octave;
import dk.ange.octave.type.OctaveDouble;
import dk.ange.octave.type.OctaveString;
import matlabcontrol.MatlabProxy;
import org.jfree.chart.entity.XYItemEntity;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by dyoon on 2014. 8. 18..
 */
public class ExplainChartAction extends AbstractAction
{
	private static double confidenceThreshold = 20.0;

	private String name;
	private String causalModelPath;
	private int type;
	private DBSeerExplainChartPanel panel;
	private JTextArea console;
	private boolean isPredicateAbsolute;

	private ArrayList<DBSeerCausalModel> explanations;

	public ExplainChartAction(String name, int type, DBSeerExplainChartPanel panel)
	{
		super(name);
		this.name = name;
		this.type = type;
		this.panel = panel;
		// temp
		this.causalModelPath = DBSeerGUI.userSettings.getDBSeerRootPath() + File.separator + "causal_models";
		this.console = panel.getExplainConsole();
		this.explanations = panel.getControlPanel().getExplanations();
		this.isPredicateAbsolute = true;
	}

	@Override
	public void actionPerformed(ActionEvent actionEvent)
	{
		if (type == DBSeerConstants.EXPLAIN_SELECT_NORMAL_REGION)
		{
			Set<XYItemEntity> selectedItems = panel.getSelectedItems();
			ArrayList<Double> region = panel.getNormalRegion();
			region.clear();

			for (XYItemEntity item : selectedItems)
			{
				region.add(item.getDataset().getX(item.getSeriesIndex(), item.getItem()).doubleValue());
			}
			ArrayList<Double> otherRegion = panel.getAnomalyRegion();
			otherRegion.removeAll(region);
			DBSeerXYLineAndShapeRenderer renderer = (DBSeerXYLineAndShapeRenderer) panel.getChart().getXYPlot().getRenderer();
			renderer.setSelectedNormal(selectedItems);
			panel.clearRectangle();
			panel.setRefreshBuffer(true);
			panel.repaint();
		}
		else if (type == DBSeerConstants.EXPLAIN_APPEND_NORMAL_REGION)
		{
			DBSeerXYLineAndShapeRenderer renderer = (DBSeerXYLineAndShapeRenderer) panel.getChart().getXYPlot().getRenderer();
			Set<XYItemEntity> previousItems = renderer.getSelectedNormal();
			Set<XYItemEntity> selectedItems = panel.getSelectedItems();
			ArrayList<Double> region = panel.getNormalRegion();

			for (XYItemEntity item : selectedItems)
			{
				region.add(item.getDataset().getX(item.getSeriesIndex(), item.getItem()).doubleValue());
			}
			ArrayList<Double> otherRegion = panel.getAnomalyRegion();
			otherRegion.removeAll(region);
			if (previousItems != null)
			{
				previousItems.addAll(selectedItems);
				renderer.setSelectedNormal(previousItems);
			}
			else
			{
				renderer.setSelectedNormal(selectedItems);
			}
			panel.clearRectangle();
			panel.setRefreshBuffer(true);
			panel.repaint();
		}
		else if (type == DBSeerConstants.EXPLAIN_SELECT_ANOMALY_REGION)
		{
			Set<XYItemEntity> selectedItems = panel.getSelectedItems();
			ArrayList<Double> region = panel.getAnomalyRegion();
			region.clear();

			for (XYItemEntity item : selectedItems)
			{
				region.add(item.getDataset().getX(item.getSeriesIndex(), item.getItem()).doubleValue());
			}
			ArrayList<Double> otherRegion = panel.getNormalRegion();
			otherRegion.removeAll(region);
			DBSeerXYLineAndShapeRenderer renderer = (DBSeerXYLineAndShapeRenderer) panel.getChart().getXYPlot().getRenderer();
			renderer.setSelectedAnomaly(selectedItems);
			panel.clearRectangle();
			panel.setRefreshBuffer(true);
			panel.repaint();
		}
		else if (type == DBSeerConstants.EXPLAIN_APPEND_ANOMALY_REGION)
		{
			DBSeerXYLineAndShapeRenderer renderer = (DBSeerXYLineAndShapeRenderer) panel.getChart().getXYPlot().getRenderer();
			Set<XYItemEntity> previousItems = renderer.getSelectedAnomaly();
			Set<XYItemEntity> selectedItems = panel.getSelectedItems();
			ArrayList<Double> region = panel.getAnomalyRegion();

			for (XYItemEntity item : selectedItems)
			{
				region.add(item.getDataset().getX(item.getSeriesIndex(), item.getItem()).doubleValue());
			}
			ArrayList<Double> otherRegion = panel.getNormalRegion();
			otherRegion.removeAll(region);
			if (previousItems != null)
			{
				previousItems.addAll(selectedItems);
				renderer.setSelectedAnomaly(previousItems);
			}
			else
			{
				renderer.setSelectedAnomaly(selectedItems);
			}
			panel.clearRectangle();
			panel.setRefreshBuffer(true);
			panel.repaint();
		}
		else if (type == DBSeerConstants.EXPLAIN_CLEAR_REGION)
		{
			ArrayList<Double> region = panel.getNormalRegion();
			region.clear();
			region = panel.getAnomalyRegion();
			region.clear();

			DBSeerXYLineAndShapeRenderer renderer = (DBSeerXYLineAndShapeRenderer) panel.getChart().getXYPlot().getRenderer();
			renderer.clearAnomaly();
			renderer.clearNormal();

			DefaultListModel explanationListModel = panel.getControlPanel().getExplanationListModel();
			explanationListModel.clear();
			DefaultListModel predicateListModel = panel.getControlPanel().getPredicateListModel();
			predicateListModel.clear();

			panel.clearRectangle();
			panel.setRefreshBuffer(true);
			panel.repaint();

		}
		else if (type == DBSeerConstants.EXPLAIN_EXPLAIN)
		{
			explain();
		}
		else if (type == DBSeerConstants.EXPLAIN_TOGGLE_PREDICATES)
		{
			togglePredicates();
		}
		else if (type == DBSeerConstants.EXPLAIN_SAVE_PREDICATES)
		{
			savePredicates();
		}
		else if (type == DBSeerConstants.EXPLAIN_UPDATE_EXPLANATIONS)
		{
			updateExplanations();
		}
	}

	private void updateExplanations()
	{
		JTextField confidenceThresholdTextField = panel.getControlPanel().getConfidenceThresholdTextField();
		if (!UserInputValidator.validateNumber(confidenceThresholdTextField.getText().trim(), "Confidence Threshold", true))
		{
			return;
		}
		confidenceThreshold = Double.parseDouble(confidenceThresholdTextField.getText().trim());
		if (confidenceThreshold < 0 || confidenceThreshold > 100)
		{
			JOptionPane.showMessageDialog(null, "Confidence threshold must be between 1 and 100.",
					"Warning", JOptionPane.WARNING_MESSAGE);
			return;
		}

		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				DefaultListModel explanationListModel = panel.getControlPanel().getExplanationListModel();
				explanationListModel.clear();

				int rank = 1;
				for (DBSeerCausalModel explanation : explanations)
				{
					if (explanation.getConfidence() > confidenceThreshold)
					{
						String output = String.format("%d. %s\n", rank++, explanation.toString());
						explanationListModel.addElement(output);
					}
				}
			}
		});
	}

	private void explain()
	{
		try
		{
			if (panel.getAnomalyRegion().isEmpty())
			{
				JOptionPane.showMessageDialog(null, "Please select an anomaly region.", "Warning", JOptionPane.WARNING_MESSAGE);
				return;
			}

//			console.setText("Analyzing data for explanation... ");
			DBSeerGUI.explainStatus.setText("Analyzing data for explanation...");

			final StatisticalPackageRunner runner = DBSeerGUI.runner;
			final DBSeerExplainChartPanel explainPanel = this.panel;
			final String causalModelPath = this.causalModelPath;
			final JTextArea console = this.console;
			final ExplainChartAction action = this;

			SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>()
			{
				String normalIdx = "";
				String anomalyIdx = "";

				HashSet<Integer> normalRegion = new HashSet<Integer>();
				HashSet<Integer> anomalyRegion = new HashSet<Integer>();

				@Override
				protected Void doInBackground() throws Exception
				{
					for (Double d : explainPanel.getNormalRegion())
					{
						normalRegion.add(d.intValue());
					}
					for (Double d : explainPanel.getAnomalyRegion())
					{
						anomalyRegion.add(d.intValue());
					}

					for (Integer i : normalRegion)
					{
						normalIdx = normalIdx + i.toString() + " ";
					}
					for (Integer i : anomalyRegion)
					{
						anomalyIdx = anomalyIdx + i.toString() + " ";
					}

					runner.eval("normal_idx = [" + normalIdx + "];");
					runner.eval("anomaly_idx = [" + anomalyIdx + "];");
					runner.eval("[predicates explanations] = explainPerformance(plotter.mv, anomaly_idx, normal_idx, '" +
							causalModelPath + "', 500, 0.2, 10);");

					return null;
				}

				@Override
				protected void done()
				{
					try
					{
						DBSeerGUI.explainStatus.setText("");
						printExplanations();
					}
					catch (Exception e)
					{
						JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
						e.printStackTrace();
					}
				}
			};
			worker.execute();
		}
		catch(Exception e)
		{
			DBSeerExceptionHandler.handleException(e);
		}
	}

	public void printExplanations()
	{
		final StatisticalPackageRunner runner = DBSeerGUI.runner;
		int explanationColumnCount = 6;
		int predicateColumnCount = 5;

		int explanationRowCount = 0;
		int predicateRowCount = 0;
		double maxConfidence = Double.MIN_VALUE;

		this.explanations.clear();
		DefaultListModel explanationListModel = panel.getControlPanel().getExplanationListModel();
		explanationListModel.clear();

		JTextField confidenceThresholdTextField = panel.getControlPanel().getConfidenceThresholdTextField();
		if (!UserInputValidator.validateNumber(confidenceThresholdTextField.getText().trim(), "Confidence Threshold", true))
		{
			return;
		}
		confidenceThreshold = Double.parseDouble(confidenceThresholdTextField.getText().trim());
		if (confidenceThreshold < 0 || confidenceThreshold > 100)
		{
			JOptionPane.showMessageDialog(null, "Confidence threshold must be between 1 and 100.",
					"Warning", JOptionPane.WARNING_MESSAGE);
			return;
		}

		try
		{
			Object[] explanations = (Object[])runner.getVariableCell("explanations");
			explanationRowCount = explanations.length / explanationColumnCount;
			for (int r = 0; r < explanationRowCount; ++r)
			{
				String causeName = (String)explanations[r];
				double[] confidence = (double[])explanations[r+explanationRowCount*1];
				Object[] predicates = (Object[])explanations[r+explanationRowCount*5];

				if (runner instanceof OctaveRunner)
				{
					for (int i = 0;i < predicates.length; ++i)
					{
						if (predicates[i] instanceof OctaveString)
						{
							OctaveString str = (OctaveString)predicates[i];
							predicates[i] = str.getString();
						}
						else if (predicates[i] instanceof OctaveDouble)
						{
							OctaveDouble val = (OctaveDouble)predicates[i];
							predicates[i] = val.getData();
						}
					}
				}

				DBSeerCausalModel explanation = new DBSeerCausalModel(causeName, confidence[0]);
				explanation.getPredicates();

				predicateRowCount = predicates.length / predicateColumnCount;
				for (int p = 0; p < predicateRowCount; ++p)
				{
					String predicateName = (String)predicates[p];
					double[] bounds = (double[])predicates[p+predicateRowCount*1];
					DBSeerPredicate predicate = new DBSeerPredicate(predicateName, bounds[0], bounds[1]);
					explanation.getPredicates().add(predicate);
				}

				this.explanations.add(explanation);

				if (maxConfidence < confidence[0])
				{
					maxConfidence = confidence[0];
				}
			}
		}
		catch(Exception e)
		{
			DBSeerExceptionHandler.handleException(e);
		}

//		double[] mockupConf = {82.74, 21.49, 14.23, 7.86};

		int rank = 1;
		for (DBSeerCausalModel explanation : explanations)
		{
			if (explanation.getConfidence() > confidenceThreshold)
			{
//				String output = String.format("%d. %s\n", rank++, explanation.toString());
				// temp
//				if (rank <= 4)
//				{
//					String output = String.format("%d. %s (%.2f%%)\n", rank, explanation.getCause(), mockupConf[rank - 1]);
//					explanationListModel.addElement(output);
//					rank++;
//				}
//				else
//				{
//					String output = String.format("%d. %s\n", rank++, explanation.toString());
//					explanationListModel.addElement(output);
//				}
				String output = String.format("%d. %s\n", rank++, explanation.toString());
				explanationListModel.addElement(output);
			}
		}

		if (maxConfidence < confidenceThreshold)
		{
			String output = String.format("There are no possible causes with the confidence higher than threshold (%.2f%%).\n",
					confidenceThreshold);
			console.append(output);
			console.append("Showing the current predicates.\n");
		}

		printPredicates();
	}

	public void togglePredicates()
	{
		DefaultListModel predicateListModel = panel.getControlPanel().getPredicateListModel();
		if  (predicateListModel.getSize() == 0)
		{
			return;
		}
		this.isPredicateAbsolute = !this.isPredicateAbsolute;
		printPredicates();
	}

	public void printPredicates()
	{
		final StatisticalPackageRunner runner = DBSeerGUI.runner;
		int predicateColumnCount = 5;

		DefaultListModel predicateListModel = panel.getControlPanel().getPredicateListModel();
		predicateListModel.clear();

		try
		{
			Object[] predicates = (Object[])runner.getVariableCell("predicates");
			int predicateRowCount = predicates.length / predicateColumnCount;
			String[] predicateNames = new String[predicateRowCount];
			double[] lowerBounds = new double[predicateRowCount];
			double[] upperBounds = new double[predicateRowCount];
			double[] relativeRatio = new double[predicateRowCount];

			if (predicateRowCount == 0)
			{
				console.append("There is no valid predicate.\n");
				return;
			}

			for (int r = 0; r < predicateRowCount; ++r)
			{
				predicateNames[r] = (String)predicates[r];
				if (predicateNames[r].contains("numTrans_"))
				{
					Pattern p = Pattern.compile("\\d+");
					Matcher m = p.matcher(predicateNames[r]);
					if (m.find())
					{
						int txIndex = Integer.parseInt(m.group());

						predicateNames[r] = String.format("# of '%s' transactions",
								DBSeerGUI.currentDataset.getTransactionTypeNames().get(txIndex-1));
					}
				}
				double[] range = (double[])predicates[r+predicateRowCount*1];
				double[] ratio = (double[])predicates[r+predicateRowCount*2];
				lowerBounds[r] = range[0];
				upperBounds[r] = range[1];
				relativeRatio[r] = ratio[0];
			}

			// print x greater than y first.
			for (int r = 0; r < predicateRowCount; ++r)
			{
				if (!Double.isInfinite(lowerBounds[r]) && Double.isInfinite(upperBounds[r]))
				{
					String output = null;
					if (isPredicateAbsolute)
						output = String.format("%s > %.2f\n", predicateNames[r], lowerBounds[r]);
					else
					{
						String higher_lower = (relativeRatio[r] > 0) ? "HIGHER" : "LOWER";
						if (relativeRatio[r] == 0)
						{
							output = String.format("%s is EQUAL in abnormal and normal regions", predicateNames[r]);
						}
						else
						{
							output = String.format("%s is %.2f%% %s than normal", predicateNames[r], Math.abs(relativeRatio[r]), higher_lower);
						}
					}
					predicateListModel.addElement(output);
				}
			}

			// print x less than y.
			for (int r = 0; r < predicateRowCount; ++r)
			{
				if (Double.isInfinite(lowerBounds[r]) && !Double.isInfinite(upperBounds[r]))
				{
					String output = null;
					if (isPredicateAbsolute)
						output = String.format("%s < %.2f\n", predicateNames[r], upperBounds[r]);
					else
					{
						String higher_lower = (relativeRatio[r] > 0) ? "HIGHER" : "LOWER";
						if (relativeRatio[r] == 0)
						{
							output = String.format("%s is EQUAL in abnormal and normal regions", predicateNames[r]);
						}
						else
						{
							output = String.format("%s is %.2f%% %s than normal", predicateNames[r], Math.abs(relativeRatio[r]), higher_lower);
						}
					}
					predicateListModel.addElement(output);
				}
			}

			// print a < x < b
			for (int r = 0; r < predicateRowCount; ++r)
			{
				if (!Double.isInfinite(lowerBounds[r]) && !Double.isInfinite(upperBounds[r]))
				{
					String output = null;
					if (isPredicateAbsolute)
						output = String.format("%.2f < %s < %.2f\n", lowerBounds[r], predicateNames[r], upperBounds[r]);
					else
					{
						String higher_lower = (relativeRatio[r] > 0) ? "HIGHER" : "LOWER";
						if (relativeRatio[r] == 0)
						{
							output = String.format("%s is EQUAL in abnormal and normal regions", predicateNames[r]);
						}
						else
						{
							output = String.format("%s is %.2f%% %s than normal", predicateNames[r], Math.abs(relativeRatio[r]), higher_lower);
						}
					}
					predicateListModel.addElement(output);
				}
			}
		}
		catch(Exception e)
		{
			DBSeerExceptionHandler.handleException(e);
		}
	}

	private void savePredicates()
	{
		DefaultListModel predicateListModel = panel.getControlPanel().getPredicateListModel();
		if (predicateListModel.getSize() == 0)
		{
			JOptionPane.showMessageDialog(null, "There are no predicates to save.\nPlease generate predicates first.", "Warning", JOptionPane.WARNING_MESSAGE);
			return;
		}
		final StatisticalPackageRunner runner = DBSeerGUI.runner;
		try
		{
			String cause = (String) JOptionPane.showInputDialog(null, "Enter the cause for predicates ", "New Causal Model",
					JOptionPane.PLAIN_MESSAGE, null, null, "New Causal Model");

			if (cause == null || cause.isEmpty())
			{
				JOptionPane.showMessageDialog(null, "Please enter the cause correctly to save the causal model", "Warning", JOptionPane.WARNING_MESSAGE);
				return;
			}

			cause = cause.trim();

			if (cause == "" || cause.isEmpty())
			{
				JOptionPane.showMessageDialog(null, "Please enter the cause correctly to save the causal model", "Warning", JOptionPane.WARNING_MESSAGE);
				return;
			}

			String path = cause;
			String actualPath = causalModelPath + File.separator + cause + ".mat";
			boolean exist = false;
			int idx = 0;

			File checkFile = new File(actualPath);
			while (checkFile.exists())
			{
				exist = true;
				++idx;
				actualPath = causalModelPath + File.separator + cause + "-" + idx + ".mat";
				checkFile = new File(actualPath);
			}

			if (exist)
			{
				path = cause + "-" + idx;
			}

			runner.eval("createCausalModel('" + causalModelPath + "','" + path + "','" + cause + "', predicates);");

			String output = String.format("A causal model with the cause '%s' has been saved as: \n%s", cause,
					actualPath);

			JOptionPane.showMessageDialog(null, output, "Information", JOptionPane.INFORMATION_MESSAGE);

		}
		catch (Exception e)
		{
			DBSeerExceptionHandler.handleException(e);
		}
	}
}
