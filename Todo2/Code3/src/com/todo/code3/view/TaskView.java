package com.todo.code3.view;

import java.util.Calendar;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.fourmob.datetimepicker.date.DatePickerDialog;
import com.fourmob.datetimepicker.date.DatePickerDialog.OnDateSetListener;
import com.sleepbot.datetimepicker.time.RadialPickerLayout;
import com.sleepbot.datetimepicker.time.TimePickerDialog;
import com.sleepbot.datetimepicker.time.TimePickerDialog.OnTimeSetListener;
import com.todo.code3.MainActivity;
import com.todo.code3.R;
import com.todo.code3.dialog.DateAndTimeDialog;
import com.todo.code3.misc.App;
import com.todo.code3.misc.Reminder;
import com.todo.code3.xml.MultiSelectParent;

public class TaskView extends ContentView {

	private TextView descTV;
	private EditText descET, focusDummy;
	private Button saveButton;
	private Button setDueDateButton, setReminderButton, clearDueDateButton, clearReminderButton;
	private MultiSelectParent multiSelectParent;

	private JSONObject task;

	private int date[] = new int[3];
	private int time[] = new int[2];

	public TaskView(MainActivity activity, int parentId) {
		super(activity, parentId);
	}

	protected void init() {
		LayoutInflater.from(activity).inflate(R.layout.task_view, this, true);

		setLayoutParams(new LayoutParams(activity.getContentWidth(), LayoutParams.FILL_PARENT));

		descTV = (TextView) findViewById(R.id.descTV);
		descET = (EditText) findViewById(R.id.descET);
		focusDummy = (EditText) findViewById(R.id.focusDummy);
		saveButton = (Button) findViewById(R.id.saveButton);

		setDueDateButton = (Button) findViewById(R.id.dueButton);
		setReminderButton = (Button) findViewById(R.id.reminderButton);

		clearDueDateButton = (Button) findViewById(R.id.clearDue);
		clearReminderButton = (Button) findViewById(R.id.clearReminder);

		saveButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				endEditDescription(true);
			}
		});

		descTV.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startEditDescription();
			}
		});
		descET.setOnFocusChangeListener(new OnFocusChangeListener() {
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) App.showKeyboard(getContext());
				else App.hideKeyboard(getContext(), focusDummy);
			}
		});

		focusDummy.setOnFocusChangeListener(new OnFocusChangeListener() {
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) App.hideKeyboard(getContext(), focusDummy);
			}
		});

		setDueDateButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				showDateAndTimePicker(App.DUE_DATE);
			}
		});

		setReminderButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				showDateAndTimePicker(Reminder.REMINDER_INFO);
			}
		});

		clearDueDateButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				clearDueDate();
			}
		});
		clearReminderButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				clearReminderInfo();
			}
		});

		multiSelectParent = new MultiSelectParent(activity) {
			public void onChanged(String type, String changed) {
				String info = Reminder.getReminderInfoString(type, -1, changed);
				try {
					if (task.has(App.DUE_DATE) && task.getLong(App.DUE_DATE) != -1) info = Reminder.getReminderInfoString(type, task.getString(App.DUE_DATE), changed);
				} catch (JSONException e) {
					e.printStackTrace();
				}

				TaskView.this.setReminderInfo(info);
			}
		};

		multiSelectParent.setStrings("1 hour", "1 week", "1 month");
		multiSelectParent.setValues(3600, 3600 * 24 * 7, Reminder.ONE_MONTH_RELATIVE_TO_DUE);
		multiSelectParent.setType(Reminder.REMINDER_RELATIVE_TO_DUE_DATE);
		multiSelectParent.generate();
		multiSelectParent.setBackgroundColor(0xffff9999);

		((LinearLayout) ((ScrollView) findViewById(R.id.scrollView1)).getChildAt(0)).addView(multiSelectParent);
	}

	public void update(JSONObject data) {
		try {
			task = new JSONObject(data.getString(parentId + ""));
			if (task.getString(App.TYPE).equals(App.TASK)) {
				if (task.has(App.DESCRIPTION)) {
					descTV.setText(task.getString(App.DESCRIPTION));
				}

				if (task.has(App.DUE_DATE) && task.getLong(App.DUE_DATE) != -1) {
					Calendar calendar = Calendar.getInstance();
					calendar.setTimeInMillis(task.getLong(App.DUE_DATE) * 1000);
					setDueDateButton.setText("Due date:" + calendar.get(Calendar.DAY_OF_MONTH) + "-" + calendar.get(Calendar.MONTH) + "-" + calendar.get(Calendar.YEAR) + "(at " + calendar.get(Calendar.HOUR_OF_DAY) + " : " + calendar.get(Calendar.MINUTE) + ")");
				} else setDueDateButton.setText("Set due date");

				if (task.has(Reminder.REMINDER_INFO)) {
					setReminderButton.setText("Reminder: " + task.getString(Reminder.REMINDER_INFO));
					multiSelectParent.update(task.getString(Reminder.REMINDER_INFO));
				} else {
					setReminderButton.setText("Set reminder");
					multiSelectParent.update("");
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void startEditDescription() {
		descTV.setVisibility(View.GONE);
		descET.setVisibility(View.VISIBLE);
		saveButton.setVisibility(View.VISIBLE);

		descET.setText(descTV.getText());

		descET.requestFocus();
	}

	private void endEditDescription(boolean save) {

		descTV.setVisibility(View.VISIBLE);
		descET.setVisibility(View.GONE);
		saveButton.setVisibility(View.GONE);

		focusDummy.requestFocus();

		if (save) {
			saveDescription(descET.getText().toString());
			descTV.setText(descET.getText().toString());
		}
	}

	public void showDateAndTimePicker(final String type) {
		new DateAndTimeDialog(activity, task, type) {
			public void onResult(int year, int month, int day, int hour, int minute) {
				setDate(type, year, month, day, hour, minute);
			}
		};
	}

	private void setDate(String type, int year, int month, int day, int hour, int minute) {
		Calendar calendar = Calendar.getInstance();

		if (year != -1 && month != -1 && day != -1) {
			if (hour == -1) hour = 12;
			if (minute == -1) minute = 0;

			else calendar.set(year, month, day, hour, minute);
			long timestamp = calendar.getTimeInMillis() / 1000;

			if (type.equals(Reminder.REMINDER_INFO)) {
				String reminderInfo = Reminder.getReminderInfoString(Reminder.REMINDER_RELATIVE_TO_DUE_DATE, timestamp);
				setReminderInfo(reminderInfo);
			} else if(type.equals(App.DUE_DATE)) {
				activity.setProperty(type, timestamp, parentId);
			}
		}
	}

	private void setReminderInfo(String reminderInfo) {
		activity.setProperty(Reminder.REMINDER_INFO, reminderInfo, parentId);

		Log.i(reminderInfo, "");

		// Only starts reminders if the task is not checked
		try {
			if (!task.has(App.COMPLETED) || !task.getBoolean(App.COMPLETED)) Reminder.startReminder(reminderInfo, activity, parentId, task);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	// private int getWeekButtons() {
	// int tot = 0;
	// for (int i = 0; i < weekDays.length; i++) {
	// if (weekDays[i].isChecked()) tot += Math.pow(2, i);
	// }
	//
	// return tot;
	// }

	public String getReminderInfo() {
		if (task != null) {
			try {
				if (task.has(Reminder.REMINDER_INFO)) return task.getString(Reminder.REMINDER_INFO);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		return "";
	}

	private void clearDueDate() {
		activity.removeProperty(App.DUE_DATE, parentId);
		activity.cancelNotification(parentId);
	}

	private void clearReminderInfo() {
		activity.removeProperty(Reminder.REMINDER_INFO, parentId);
		activity.cancelNotification(parentId);
	}

	public void leave() {
		focusDummy.requestFocus();
		App.hideKeyboard(getContext(), focusDummy);
	}

	private void saveDescription(String desc) {
		App.hideKeyboard(getContext(), focusDummy);

		activity.setProperty(App.DESCRIPTION, desc, parentId);
	}

	// not used
	public void updateContentItemsOrder() {
	}
}