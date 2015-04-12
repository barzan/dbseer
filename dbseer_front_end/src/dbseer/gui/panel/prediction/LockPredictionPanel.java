package dbseer.gui.panel.prediction;

import dbseer.gui.DBSeerConstants;
import dbseer.gui.actions.ManuallyChangeFieldAction;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

/**
 * Created by dyoon on 2014. 6. 21..
 */
public class LockPredictionPanel extends JPanel
{
	private JLabel lockConfLabel;
	private JTextField lockConfTextField;
	private JLabel learnLockLabel;
	private JComboBox learnLockComboBox;
	private JLabel lockTypeLabel;
	private JComboBox lockTypeComboBox;

	private JButton changeLockConfButton;

	public LockPredictionPanel()
	{
		initializeGUI();
	}

	private void initializeGUI()
	{
		this.setLayout(new MigLayout("fill"));
//		this.add(new JLabel("Prediction: LockPrediction"), "wrap");

		lockConfLabel = new JLabel("Lock Configuration");
		lockConfTextField = new JTextField();

		learnLockLabel = new JLabel("Learn Lock: ");
		learnLockComboBox = new JComboBox(DBSeerConstants.LEARN_LOCK);

		lockTypeLabel = new JLabel("Lock Type: ");
		lockTypeComboBox = new JComboBox(DBSeerConstants.LOCK_TYPES);

		changeLockConfButton = new JButton(new ManuallyChangeFieldAction(lockConfTextField));

		this.add(lockConfLabel, "wrap");
		this.add(lockConfTextField, "spanx 2, growx");
		this.add(changeLockConfButton, "wrap");
		this.add(learnLockLabel);
		this.add(learnLockComboBox, "growx, wrap");
		this.add(lockTypeLabel);
		this.add(lockTypeComboBox, "growx, wrap");
	}

	public String getLockConf() { return lockConfTextField.getText(); }

	public void setLockConf(String conf)
	{
		lockConfTextField.setText(conf);
		if (!conf.isEmpty())
		{
			lockConfTextField.setEnabled(false);
		}
	}

	public boolean getLearnLock()
	{
		return learnLockComboBox.getSelectedIndex() == 0;
	}

	public int getLockType()
	{
		return lockTypeComboBox.getSelectedIndex();
	}
}
