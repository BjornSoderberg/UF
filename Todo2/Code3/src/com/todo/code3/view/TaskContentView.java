package com.todo.code3.view;

import java.util.Calendar;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.fourmob.datetimepicker.date.DatePickerDialog;
import com.fourmob.datetimepicker.date.DatePickerDialog.OnDateSetListener;
import com.sleepbot.datetimepicker.time.RadialPickerLayout;
import com.sleepbot.datetimepicker.time.TimePickerDialog;
import com.sleepbot.datetimepicker.time.TimePickerDialog.OnTimeSetListener;
import com.todo.code3.MainActivity;
import com.todo.code3.R;
import com.todo.code3.misc.App;
import com.todo.code3.misc.Reminder;
import com.todo.code3.notification.NotificationReceiver;

public class TaskContentView extends ContentView {

	private TextView descTV;
	private EditText descET, focusDummy;
	private Button saveButton;
	private Button setDueDateButton, setReminderButton, clearDueDateButton, clearReminderButton;
	private Button setWeeklyReminder, setEveryTwoWeeksReminder;
	private CheckBox[] weekDays;

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
				showDateAndTimePicker(App.REMINDER);
			}
		});

		clearDueDateButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				clearDateAndTime(App.DUE_DATE);
			}
		});
		clearReminderButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				clearDateAndTime(App.REMINDER);
			}
		});

		((Button) findViewById(R.id.repeatingMWF)).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// All days of the week at 8.20
				String info = Reminder.getReminderInfoString(Reminder.WEEKLY, 127, 8, 20);
				setRepeatingReminder(info);
			}
		});

		weekDays = new CheckBox[7];
		weekDays[0] = (CheckBox) findViewById(R.id.cbSu);
		weekDays[1] = (CheckBox) findViewById(R.id.cbSa);
		weekDays[2] = (CheckBox) findViewById(R.id.cbF);
		weekDays[3] = (CheckBox) findViewById(R.id.cbTh);
		weekDays[4] = (CheckBox) findViewById(R.id.cbW);
		weekDays[5] = (CheckBox) findViewById(R.id.cbTu);
		weekDays[6] = (CheckBox) findViewById(R.id.cbM);

		setWeeklyReminder = (Button) findViewById(R.id.setWeeklyReminder);
		setWeeklyReminder.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				OnTimeSetListener tsl = new OnTimeSetListener() {
					public void onTimeSet(RadialPickerLayout view, int hour, int minute) {
						int i = getWeekButtons();
						String reminderInfo = Reminder.getReminderInfoString(Reminder.WEEKLY, i, hour, minute);

						setRepeatingReminder(reminderInfo);

						Log.i("asdasd", reminderInfo);
					}
				};

				int hour = 12, minute = 0;

				TimePickerDialog tpd = TimePickerDialog.newInstance(tsl, hour, minute, true);
				tpd.show(activity.getSupportFragmentManager(), "datepicker");
			}
		});

		setEveryTwoWeeksReminder = (Button) findViewById(R.id.setEveryTwoWeeksReminder);
		setEveryTwoWeeksReminder.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				OnTimeSetListener tsl = new OnTimeSetListener() {
					public void onTimeSet(RadialPickerLayout view, int hour, int minute) {
						int i = getWeekButtons();
						int oe = (((CheckBox) findViewById(R.id.cbEven)).isChecked()) ? 0 : 1;
						String reminderInfo = Reminder.getReminderInfoString(Reminder.EVERY_TWO_WEEKS, i, hour, minute, oe);

						setRepeatingReminder(reminderInfo);

						Log.i("asdasd", reminderInfo);
					}
				};

				int hour = 12, minute = 0;

				TimePickerDialog tpd = TimePickerDialog.newInstance(tsl, hour, minute, true);
				tpd.show(activity.getSupportFragmentManager(), "datepicker");
			}
		});
		
		((Button) findViewById(R.id.monthlyReminder)).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				int dayOfMonth = Integer.parseInt(((EditText) findViewById(R.id.dayOfMonth)).getText().toString());
				if(0 > dayOfMonth || dayOfMonth > 32) {
					Toast.makeText(activity, "Between 1-31 pls", Toast.LENGTH_SHORT).show();
					return;
				}
				
				String reminderInfo = Reminder.getReminderInfoString(Reminder.MONTHLY, dayOfMonth, 12, 0);
				setRepeatingReminder(reminderInfo);
			}
		});
		
		((Button) findViewById(R.id.intervalReminder)).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				String type = "";
				int cid = ((RadioGroup) findViewById(R.id.intervalType)).getCheckedRadioButtonId();
				if(cid == R.id.weekInterval) type = Reminder.WEEK;
				else if(cid == R.id.dayInterval) type = Reminder.DAY;
				else if(cid == R.id.hourInterval) type = Reminder.HOUR;
				else if(cid == R.id.minuteInterval)type = Reminder.MINUTE;
				
				int intervalLength = Integer.parseInt(((EditText) findViewById(R.id.interval)).getText().toString());
				if(intervalLength < 0) {
					Toast.makeText(activity, "Larger than 0 pls", Toast.LENGTH_SHORT).show();
					return;
				}
				
				String reminderInfo = Reminder.getReminderInfoString(Reminder.SET_INTERVAL, type, intervalLength, System.currentTimeMillis() / 1000);
				setRepeatingReminder(reminderInfo);
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
					setDueDateButton.setText("Due date:" + calendar.get(Calendar.DAY_OF_MONTH) + "-" + calendar.get(Calendar.MONTH) + "-" + calendar.get(Calendar.YEAR) + "(at " + calendar.get(Calendar.HOUR_OF_DAY) + " : " + calendar.get(Calendar.MINUTE) + ")");
				} else setDueDateButton.setText("Set due date");

				if (task.has(App.REMINDER) && task.getLong(App.REMINDER) != -1) {
					Calendar calendar = Calendar.getInstance();
					calendar.setTimeInMillis(task.getLong(App.REMINDER) * 1000);
					setReminderButton.setText("Reminder:" + calendar.get(Calendar.DAY_OF_MONTH) + "-" + calendar.get(Calendar.MONTH) + "-" + calendar.get(Calendar.YEAR));
				} else setReminderButton.setText("Set reminder");
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
					selectTime(tsl, type);
				}
			}
		};

		selectDate(dsl, type);
	}

	private void selectDate(OnDateSetListener dsl, String type) {
		Calendar calendar = Calendar.getInstance();
		int year = calendar.get(Calendar.YEAR) - 1;
		int month = calendar.get(Calendar.MONTH);
		int day = calendar.get(Calendar.DAY_OF_MONTH);

		try {
			if (task.has(type) && task.getLong(type) != -1) {
				calendar.setTimeInMillis(task.getLong(type) * 1000);

				year = (year > calendar.get(Calendar.YEAR)) ? year : calendar.get(Calendar.YEAR);
				month = calendar.get(Calendar.MONTH);
				day = calendar.get(Calendar.DAY_OF_MONTH);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		DatePickerDialog dpd = null;
		try {
			if (type.equals(App.REMINDER) && task.has(App.DUE_DATE) && task.getLong(App.DUE_DATE) != -1) {
				int due[] = App.getDueDate(task.getLong(App.DUE_DATE));
				dpd = DatePickerDialog.newInstance(dsl, year, month, day, due[0], due[1], due[2]);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		if (dpd == null) dpd = DatePickerDialog.newInstance(dsl, year, month, day);

		dpd.setYearRange(year - 1, 2037);
		dpd.show(activity.getSupportFragmentManager(), "datepicker");
	}

	private void selectTime(OnTimeSetListener tsl, String type) {
		Calendar calendar = Calendar.getInstance();
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE);

		try {
			if (task.has(type) && task.getLong(type) != -1) {
				calendar.setTimeInMillis(task.getLong(type) * 1000);

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
			if (time[0] != -1 && time[1] != -1) calendar.set(date[0], date[1], date[2], time[0], time[1], 0);
			else calendar.set(date[0], date[1], date[2], 12, 0, 0);

			long timestamp = calendar.getTimeInMillis() / 1000;

			activity.setProperty(type, timestamp, parentId);

			if (type.equals(App.REMINDER)) setReminder(timestamp);
			// The user must also be able to remove a notification
			// The correct pending intent is gotten by using the parentId as
			// request code
		}

		for (int i : date)
			i = -1;
	}

	private void setReminder(long timestamp) {
		Intent i = new Intent(activity, NotificationReceiver.class);
		i.putExtra(App.ID, parentId);

		try {
			if (task.has(App.NAME)) i.putExtra(App.NAME, task.getString(App.NAME));
			if (task.has(App.DUE_DATE)) i.putExtra(App.DUE_DATE, task.getLong(App.DUE_DATE));
		} catch (JSONException e) {
			e.printStackTrace();
		}

		AlarmManager am = (AlarmManager) activity.getSystemService(Context.ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, timestamp * 1000, PendingIntent.getBroadcast(activity, parentId, i, PendingIntent.FLAG_UPDATE_CURRENT));
	}

	private void setRepeatingReminder(String reminderInfo) {
		activity.setProperty(Reminder.REMINDER_INFO, reminderInfo, parentId);

		// if (Reminder.getType(reminderInfo).equals(Reminder.WEEKLY)) {
		Intent i = new Intent(activity, NotificationReceiver.class);
		i.putExtra(App.ID, parentId);
		i.putExtra(Reminder.REMINDER_INFO, reminderInfo);

		try {
			if (task.has(App.NAME)) i.putExtra(App.NAME, task.getString(App.NAME));
			if (task.has(App.DUE_DATE)) i.putExtra(App.DUE_DATE, task.getLong(App.DUE_DATE));
		} catch (JSONException e) {
			e.printStackTrace();
		}

		long next = Reminder.getNext(reminderInfo);
		if (next == -1) return;
		else next *= 1000;

		// Logs the next
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(next);
		Log.i("next : " + next, c.toString() + "");

		AlarmManager am = (AlarmManager) activity.getSystemService(Context.ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, next, PendingIntent.getBroadcast(activity, parentId, i, PendingIntent.FLAG_UPDATE_CURRENT));
		// }
	}

	private int getWeekButtons() {
		int tot = 0;
		for (int i = 0; i < weekDays.length; i++) {
			if (weekDays[i].isChecked()) tot += Math.pow(2, i);
		}

		return tot;
	}

	private void clearDateAndTime(String type) {
		activity.removeProperty(type, parentId);

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