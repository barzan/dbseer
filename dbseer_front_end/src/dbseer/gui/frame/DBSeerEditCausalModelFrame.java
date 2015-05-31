package dbseer.gui.frame;

import dbseer.gui.DBSeerExceptionHandler;
import dbseer.gui.DBSeerGUI;
import dbseer.gui.user.DBSeerCausalModel;
import dbseer.gui.user.DBSeerPredicate;
import dbseer.gui.user.DBSeerUserSettings;
import matlabcontrol.MatlabInvocationException;
import matlabcontrol.MatlabProxy;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by dyoon on 5/18/15.
 */
public class DBSeerEditCausalModelFrame extends JFrame implements ActionListener, MouseListener
{
	private static String[] tableHeaders = {
			"Filename",
			"Cause"
	};

	private JTable causalModelTable;
	private DefaultTableModel tableModel;

	private ArrayList<String> causalModelFilenames;
	private ArrayList<String> causes;

	private JButton renameCauseButton;
	private JButton deleteCauseButton;
	private JButton closeButton;

	public DBSeerEditCausalModelFrame()
	{
		this.setLayout(new MigLayout("fill, align center"));
		causalModelTable = new JTable(new DefaultTableModel(null, new String[]{"Filename", "Cause"}){
			@Override
			public boolean isCellEditable(int i, int i1)
			{
				return false;
			}
		});

		causalModelTable.setRowHeight(20);
		causalModelTable.setFillsViewportHeight(true);
		causalModelTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		causalModelTable.addMouseListener(this);

		tableModel = (DefaultTableModel)causalModelTable.getModel();
		causalModelFilenames = new ArrayList<String>();
		causes = new ArrayList<String>();

		renameCauseButton = new JButton("Rename Cause");
		deleteCauseButton = new JButton("Delete");
		closeButton = new JButton("Close");

		renameCauseButton.addActionListener(this);
		deleteCauseButton.addActionListener(this);
		closeButton.addActionListener(this);

		updateCausalModelList();

		initialize();

		String causalModelDirPath = DBSeerGUI.userSettings.getDBSeerRootPath() + File.separator + "causal_models";
		this.setTitle(String.format("Causal Models (Located under '%s' directory)", causalModelDirPath));
	}

	private void initialize()
	{
		JScrollPane tableScrollPane = new JScrollPane(causalModelTable);

		this.add(tableScrollPane, "grow, wrap");
		this.add(renameCauseButton, "split 3, align center");
		this.add(deleteCauseButton);
		this.add(closeButton);
	}

	private void updateCausalModelList()
	{
		String causalModelDirPath = DBSeerGUI.userSettings.getDBSeerRootPath() + File.separator + "causal_models";
		File causalModelDirectory = new File(causalModelDirPath);

		if (!causalModelDirectory.exists())
		{
			JOptionPane.showMessageDialog(null, "Causal model directory does not exist!\nPlease make sure that the root path has been " +
							"configured correctly.", "Warning",
					JOptionPane.WARNING_MESSAGE);
			return;
		}

		// remove all rows from the table first.
		for (int i = 0; i < tableModel.getRowCount(); ++i)
		{
			tableModel.removeRow(i);
		}
		tableModel.setRowCount(0);
		tableModel.fireTableDataChanged();
		causalModelFilenames.clear();
		causes.clear();

		File[] files = causalModelDirectory.listFiles();
		Arrays.sort(files);

		for (File file : files)
		{
			if (!file.isDirectory())
			{
				String filename = file.getName();
				// check .mat extension
				String extension = "";
				int dotIndex = filename.lastIndexOf('.');
				if (dotIndex >= 0)
				{
					extension = filename.substring(dotIndex + 1);
				}
				if (extension.equalsIgnoreCase("mat"))
				{
					causalModelFilenames.add(filename);

					// get 'cause'
					MatlabProxy proxy = DBSeerGUI.proxy;

					try
					{
						proxy.eval("addpath " + causalModelDirPath + ";");
						proxy.eval(String.format("dbseer_causal_model = load('%s');", filename));
						String cause = (String) proxy.getVariable("dbseer_causal_model.model.cause");
						causes.add(cause);

						tableModel.addRow(new String[]{filename, cause});
						tableModel.fireTableDataChanged();
					}
					catch (Exception e)
					{
						DBSeerExceptionHandler.handleException(e);
					}
				}
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent event)
	{

		if (event.getSource() == closeButton)
		{
			final JFrame frame = this;
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					frame.dispose();
				}
			});
		}
		else if (event.getSource() == deleteCauseButton)
		{
			int selectedRow = causalModelTable.getSelectedRow();
			if (selectedRow == -1)
			{
				JOptionPane.showMessageDialog(null, "Please select a causal model that you want to delete.", "Warning",
						JOptionPane.WARNING_MESSAGE);
				return;
			}
			else
			{
				int confirm = JOptionPane.showConfirmDialog(null,
						"This will delete the selected causal model permanently from the system!\n Do you want to proceed?",
						"Warning",
						JOptionPane.YES_NO_OPTION);

				if (confirm == JOptionPane.YES_OPTION)
				{
					String causalModelDirPath = DBSeerGUI.userSettings.getDBSeerRootPath() + File.separator + "causal_models";
					String filename = causalModelFilenames.get(selectedRow);
					File modelToDelete = new File(causalModelDirPath + File.separator + filename);
					if (modelToDelete.delete())
					{
						JOptionPane.showMessageDialog(null, "The selected causal model has been deleted successfully.", "",
								JOptionPane.INFORMATION_MESSAGE);
						updateCausalModelList();
					}
					else
					{
						JOptionPane.showMessageDialog(null, "Failed to delete the selected causal model from the system.", "Warning",
								JOptionPane.WARNING_MESSAGE);
						return;
					}
				}
			}
		}
		else if (event.getSource() == renameCauseButton)
		{
			int selectedRow = causalModelTable.getSelectedRow();
			if (selectedRow == -1)
			{
				JOptionPane.showMessageDialog(null, "Please select a causal model that you want to rename.", "Warning",
						JOptionPane.WARNING_MESSAGE);
				return;
			}
			else
			{
				String newCause = (String)JOptionPane.showInputDialog(this, "Enter the new cause for the selected causal model.", "RenewCause the cause",
						JOptionPane.PLAIN_MESSAGE, null, null, causes.get(selectedRow));
				if (newCause == null || newCause.trim().isEmpty())
				{
					JOptionPane.showMessageDialog(null, "Please enter a non-empty string for the cause.", "Warning",
							JOptionPane.WARNING_MESSAGE);
					return;
				}
				newCause = newCause.trim();

				// get 'cause'
				MatlabProxy proxy = DBSeerGUI.proxy;
				String causalModelDirPath = DBSeerGUI.userSettings.getDBSeerRootPath() + File.separator + "causal_models";
				String filename = causalModelFilenames.get(selectedRow);

				try
				{
					proxy.eval("addpath " + causalModelDirPath + ";");
					proxy.eval("cd " + causalModelDirPath + ";");
					proxy.eval(String.format("dbseer_loaded_model = load('%s');", filename));
					proxy.eval("model = dbseer_loaded_model.model;");
					proxy.eval(String.format("model.cause = '%s';", newCause));
					proxy.eval(String.format("save('%s', 'model');", filename));
				}
				catch (Exception e)
				{
					DBSeerExceptionHandler.handleException(e);
				}
				updateCausalModelList();
			}
		}
	}

	@Override
	public void mouseClicked(MouseEvent event)
	{
		if (event.getClickCount() == 2)
		{
			int selectedRow = causalModelTable.getSelectedRow();
			if (selectedRow == -1)
			{
				return;
			}

			// get 'cause'
			String causalModelDirPath = DBSeerGUI.userSettings.getDBSeerRootPath() + File.separator + "causal_models";
			MatlabProxy proxy = DBSeerGUI.proxy;
			final String cause = (String)tableModel.getValueAt(selectedRow, 1);
			final String filename = (String)tableModel.getValueAt(selectedRow, 0);
			DBSeerCausalModel causalModel = null;

			try
			{
				proxy.eval("addpath " + causalModelDirPath + ";");
				proxy.eval(String.format("dbseer_causal_model = load('%s');", filename));
				Object[] predicates = (Object[]) proxy.getVariable("dbseer_causal_model.model.predicates");

				causalModel = new DBSeerCausalModel(cause, 0.0);

				int predicateRowCount = predicates.length / 5;
				for (int p = 0; p < predicateRowCount; ++p)
				{
					String predicateName = (String)predicates[p];
					double[] bounds = (double[])predicates[p+predicateRowCount*1];
					DBSeerPredicate predicate = new DBSeerPredicate(predicateName, bounds[0], bounds[1]);
					causalModel.getPredicates().add(predicate);
				}
			}
			catch (Exception e)
			{
				DBSeerExceptionHandler.handleException(e);
			}

			if (causalModel != null)
			{
				final DBSeerCausalModel model = causalModel;
				SwingUtilities.invokeLater(new Runnable()
				{
					@Override
					public void run()
					{
						DBSeerCausalModelFrame frame = new DBSeerCausalModelFrame(model,
								String.format("View Causal Model: %s - %s", cause, filename));
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
