package com.todo.code3.view;

import java.util.Calendar;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.fourmob.datetimepicker.date.DatePickerDialog;
import com.fourmob.datetimepicker.date.DatePickerDialog.OnDateSetListener;
import com.sleepbot.datetimepicker.time.RadialPickerLayout;
import com.sleepbot.datetimepicker.time.TimePickerDialog;
import com.sleepbot.datetimepicker.time.TimePickerDialog.OnTimeSetListener;
import com.todo.code3.MainActivity;
import com.todo.code3.R;
import com.todo.code3.misc.App;
import com.todo.code3.notification.NotificationService;

public class TaskContentView extends ContentView {

	private TextView descTV;
	private EditText descET, focusDummy;
	private Button saveButton, setDueDateButton, setReminderButton;

	private JSONObject task;

	private int date[] = new int[3];
	private int time[] = new int[2];

	public TaskContentView(MainActivity activity, int parentId) {
		super(activity, parentId);
	}

	protected void init() {
		LayoutInflater.from(activity).inflate(R.layout.task_content_view, this, true);

		setLayoutParams(new LayoutParams(activity.getContentWidth(), LayoutParams.FILL_PARENT));

		descTV = (TextView) findViewById(R.id.descTV);
		descET = (EditText) findViewById(R.id.descET);
		focusDummy = (EditText) findViewById(R.id.focusDummy);
		saveButton = (Button) findViewById(R.id.saveButton);

		setDueDateButton = (Button) findViewById(R.id.dueButton);
		setReminderButton = (Button) findViewById(R.id.reminderButton);

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
				showDateAndTimePicker(App.REMINDER);
			}
		});
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
					setDueDateButton.setText("Due date:" + calendar.get(Calendar.DAY_OF_MONTH) + "-" + calendar.get(Calendar.MONTH) + "-" + calendar.get(Calendar.YEAR));
				}

				if (task.has(App.REMINDER) && task.getLong(App.REMINDER) != -1) {
					Calendar calendar = Calendar.getInstance();
					calendar.setTimeInMillis(task.getLong(App.REMINDER) * 1000);
					setReminderButton.setText("Reminder:" + calendar.get(Calendar.DAY_OF_MONTH) + "-" + calendar.get(Calendar.MONTH) + "-" + calendar.get(Calendar.YEAR));
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
		final OnTimeSetListener tsl = new OnTimeSetListener() {
			public void onTimeSet(RadialPickerLayout view, int hour, int minute) {
				time[0] = hour;
				time[1] = minute;

				setDate(type);
			}
		};

		final OnDateSetListener dsl = new OnDateSetListener() {
			public void onDateSet(DatePickerDialog dpd, int year, int month, int day) {
				if (year != -1 && month != -1 && day != -1) {
					date[0] = year;
					date[1] = month;
					date[2] = day;
					selectTime(tsl);
				}
			}
		};

		selectDate(dsl);
	}

	private void selectDate(OnDateSetListener dsl) {
		Calendar calendar = Calendar.getInstance();
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH);
		int day = calendar.get(Calendar.DAY_OF_MONTH);

		try {
			if (task.has(App.DUE_DATE) && task.getLong(App.DUE_DATE) != -1) {
				calendar.setTimeInMillis(task.getLong(App.DUE_DATE) * 1000);

				year = calendar.get(Calendar.YEAR);
				month = calendar.get(Calendar.MONTH);
				day = calendar.get(Calendar.DAY_OF_MONTH);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		DatePickerDialog dpd = DatePickerDialog.newInstance(dsl, year, month, day);
		dpd.setYearRange(year - 1, 2037);
		dpd.show(activity.getSupportFragmentManager(), "datepicker");
	}

	private void selectTime(OnTimeSetListener tsl) {
		Calendar calendar = Calendar.getInstance();
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE);

		try {
			if (task.has(App.DUE_DATE) && task.getInt(App.DUE_DATE) != -1) {
				calendar.setTimeInMillis(task.getInt(App.DUE_DATE));

				hour = calendar.get(Calendar.HOUR_OF_DAY);
				minute = calendar.get(Calendar.MINUTE);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		TimePickerDialog tpd = TimePickerDialog.newInstance(tsl, hour, minute, true);
		tpd.show(activity.getSupportFragmentManager(), "timepicker");
	}

	private void setDate(String type) {
		Calendar calendar = Calendar.getInstance();

		if (date[0] != -1 && date[1] != -1 && date[2] != -1) {
			if (time[0] != -1 && time[1] != -1) calendar.set(date[0], date[1], date[2], time[1], time[1], 0);
			else calendar.set(date[0], date[1], date[2], 12, 0, 0);

			long timestamp = calendar.getTimeInMillis() / 1000;

			activity.setProperty(type, timestamp, parentId);
		}

		// AlarmManager am = (AlarmManager)
		// activity.getSystemService(Context.ALARM_SERVICE);
		//
		// Intent i = new Intent();
		// i.setClass(activity, NotificationService.class);
		// i.putExtra("msg", "message?");
		// PendingIntent p = PendingIntent.getService(activity, 0, i, 0);
		//
		// am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 4000,
		// p);

		NotificationManager notificationManager = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);

		String tickerText = "ticker text";
		long when = System.currentTimeMillis() - 1907890000;
		String title = "title";
		String text = "text";
		Intent i = new Intent(activity, MainActivity.class);
		PendingIntent p = PendingIntent.getActivity(activity, 0, i, 0);
		Notification n = new Notification(R.drawable.ic_launcher, tickerText, when);
		n.setLatestEventInfo(activity, title + when, text + System.currentTimeMillis(), p);
		
		notificationManager.notify(123, n);
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