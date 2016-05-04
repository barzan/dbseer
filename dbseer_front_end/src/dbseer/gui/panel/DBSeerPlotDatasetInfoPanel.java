package dbseer.gui.panel;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by Dong Young Yoon on 4/29/16.
 */
public class DBSeerPlotDatasetInfoPanel extends JPanel implements ChangeListener
{
	private JPanel setTimePanel;

	private JLabel selectLabel;
	private JLabel startTimeLabel;
	private JLabel endTimeLabel;
	private JLabel durationLabel;

	private JLabel setStartTimeTextLabel;
	private JLabel setEndTimeTextLabel;
	private JLabel setStartTimeLabel;
	private JLabel setEndTimeLabel;
	private JLabel setDurationLabel;

	private JSlider startTimeSlider;
	private JSlider endTimeSlider;

	private int datasetDuration;
	private int increments;

	private long origStartTime;
	private long origEndTime;

	private long setStartTime;
	private long setEndTime;

	private long plotStartIndex;
	private long plotEndIndex;

	private final int MAX_DURATION = 10000; // in seconds;

	private boolean firstRun;
	private boolean ignoreChangeEvent;

	public DBSeerPlotDatasetInfoPanel()
	{
		setTimePanel = new JPanel();

		selectLabel = new JLabel();
		startTimeLabel = new JLabel();
		endTimeLabel = new JLabel();
		durationLabel = new JLabel();
		setStartTimeTextLabel = new JLabel();
		setEndTimeTextLabel = new JLabel();
		setStartTimeLabel = new JLabel();
		setEndTimeLabel = new JLabel();
		setDurationLabel = new JLabel();

		startTimeSlider = new JSlider(0, 100, 0);
		startTimeSlider.setEnabled(false);
		startTimeSlider.addChangeListener(this);
		endTimeSlider = new JSlider(0, 100, 100);
		endTimeSlider.setEnabled(false);
		endTimeSlider.addChangeListener(this);

		this.firstRun = true;
		this.ignoreChangeEvent = false;

		initializeGUI();
	}

	private void initializeGUI()
	{
		this.setLayout(new MigLayout("align 50% 50%"));
		setTimePanel.setLayout(new MigLayout());

		selectLabel.setText("Please select a dataset.");
		startTimeLabel.setText("Start Time: N/A");
		endTimeLabel.setText("End Time: N/A");
		durationLabel.setText("Total Duration: N/A");
		setStartTimeTextLabel.setText("Set Start Time");
		setEndTimeTextLabel.setText("Set End Time");
		setStartTimeLabel.setText("N/A");
		setEndTimeLabel.setText("N/A");
		setDurationLabel.setText("Duration: N/A");

		this.add(selectLabel);

		setTimePanel.add(setStartTimeTextLabel, "wrap");
		setTimePanel.add(startTimeSlider, "wrap");
		setTimePanel.add(setStartTimeLabel, "wrap 20px");
		setTimePanel.add(setEndTimeTextLabel, "wrap");
		setTimePanel.add(endTimeSlider, "wrap");
		setTimePanel.add(setEndTimeLabel, "wrap 10px");
		setTimePanel.add(setDurationLabel);

		setTimePanel.setBorder(BorderFactory.createTitledBorder("Set plot time/duration"));
	}

	public void setTime(long startTime, long endTime)
	{
		String startStr = "N/A";
		String endStr = "N/A";
		String durationStr = "N/A";

		SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");

		if (startTime != -1)
		{
			Date startDate = new Date(startTime * 1000);
			startStr = format.format(startDate);
		}
		if (endTime != -1)
		{
			Date endDate = new Date(endTime * 1000);
			endStr = format.format(endDate);
		}
		if (startTime != -1 && endTime != -1 && endTime >= startTime)
		{
			durationStr = getDuration(startTime, endTime);
		}

		origStartTime = startTime;
		origEndTime = endTime;
		startTimeLabel.setText(String.format("Start Time: %s", startStr));
		endTimeLabel.setText(String.format("End Time: %s", endStr));
		durationLabel.setText(String.format("Total Duration: %s", durationStr));
		setStartTimeLabel.setText(startStr);
		setEndTimeLabel.setText(endStr);
		setDurationLabel.setText(String.format("Duration: %s", durationStr));
		plotStartIndex = 0;
		plotEndIndex = endTime - startTime;

		if (datasetDuration > 0)
		{
			ignoreChangeEvent = true;
			increments = (int)Math.ceil((double)datasetDuration / (double)MAX_DURATION);
			if (datasetDuration > MAX_DURATION)
			{
				startTimeSlider.setMinimum(0);
				startTimeSlider.setValue(0);
				endTimeSlider.setMinimum(0);

				startTimeSlider.setMaximum(MAX_DURATION);
				endTimeSlider.setMaximum(MAX_DURATION);
				endTimeSlider.setValue(MAX_DURATION);
			}
			else
			{
				startTimeSlider.setMaximum(datasetDuration);
				endTimeSlider.setMaximum(datasetDuration);
				endTimeSlider.setValue(datasetDuration);
			}
			startTimeSlider.setEnabled(true);
			endTimeSlider.setEnabled(true);
			ignoreChangeEvent = false;
		}

		if (firstRun)
		{
			this.remove(selectLabel);
			this.setLayout(new MigLayout());
			this.add(startTimeLabel, "wrap");
			this.add(endTimeLabel, "wrap");
			this.add(durationLabel, "wrap 20px");
			this.add(setTimePanel, "wrap");
			firstRun = false;
		}
	}

	private String getDuration(long startTime, long endTime)
	{
		long duration = (endTime - startTime) * 1000;
		long hours = TimeUnit.MILLISECONDS.toHours(duration);
		long minutes = TimeUnit.MILLISECONDS.toMinutes(duration) % 60;
		long seconds = TimeUnit.MILLISECONDS.toSeconds(duration) % 60;

		datasetDuration = (int)(endTime - startTime);

		String str = "";
		if (hours > 0)
		{
			str += (hours + "h ");
		}
		if (minutes > 0)
		{
			str += (minutes + "m ");
		}
		str += (seconds + "s");

		return str;
	}

	@Override
	public void stateChanged(ChangeEvent e)
	{
		if (ignoreChangeEvent)
		{
			return;
		}

		if (e.getSource() == startTimeSlider || e.getSource() == endTimeSlider)
		{
			int val = startTimeSlider.getValue();
			setStartTime = origStartTime + (val * increments);
			if (setStartTime > origEndTime)
			{
				setStartTime = origEndTime;
			}
			setStartTimeLabel.setText(getDateString(setStartTime));
			plotStartIndex = setStartTime - origStartTime;

			val = endTimeSlider.getValue();
			setEndTime = origStartTime + (val * increments);
			if (setEndTime > origEndTime)
			{
				setEndTime = origEndTime;
			}
			setEndTimeLabel.setText(getDateString(setEndTime));
			plotEndIndex = setEndTime - origStartTime;
		}

		if (setStartTime < setEndTime)
		{
			setDurationLabel.setText(String.format("Duration: %s", getDuration(setStartTime, setEndTime)));

			setStartTimeTextLabel.setForeground(Color.black);
			setStartTimeLabel.setForeground(Color.black);
			setEndTimeTextLabel.setForeground(Color.black);
			setEndTimeLabel.setForeground(Color.black);
			setDurationLabel.setForeground(Color.black);
		}
		else
		{
			setDurationLabel.setText("Duration: N/A");

			setStartTimeTextLabel.setForeground(Color.red);
			setStartTimeLabel.setForeground(Color.red);
			setEndTimeTextLabel.setForeground(Color.red);
			setEndTimeLabel.setForeground(Color.red);
			setDurationLabel.setForeground(Color.red);
		}
	}

	private String getDateString(long timestamp)
	{
		SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
		Date date = new Date(timestamp * 1000);
		return format.format(date);
	}

	public long getPlotStartIndex()
	{
		return plotStartIndex;
	}

	public long getPlotEndIndex()
	{
		return plotEndIndex;
	}
}
