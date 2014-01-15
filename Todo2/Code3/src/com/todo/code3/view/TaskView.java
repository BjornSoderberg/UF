package com.todo.code3.view;

import java.util.ArrayList;
import java.util.Calendar;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.todo.code3.MainActivity;
import com.todo.code3.R;
import com.todo.code3.adapter.CustomReminderListAdapter;
import com.todo.code3.dialog.date_and_time.DateAndTimeDialog;
import com.todo.code3.dialog.date_and_time.TimeDialog;
import com.todo.code3.misc.App;
import com.todo.code3.misc.Reminder;
import com.todo.code3.xml.multi_select_parent.MultiSelectParent;
import com.todo.code3.xml.multi_select_parent.SelectDaysInWeek;
import com.todo.code3.xml.multi_select_parent.Selector;

public class TaskView extends ContentView {

	private TextView descTV;
	private EditText descET, focusDummy;
	private Button saveButton;
	private Button setDueDateButton, setReminderButton, clearDueDateButton, clearReminderButton;
	private ListView customReminderList;

	private MultiSelectParent multiSelectParentRelativeToDue;
	private SelectDaysInWeek selectDaysInWeek;
	private Selector intervalSelector;

	private JSONObject task;

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
				showDateAndTimePicker(Reminder.REMINDER_RELATIVE_TO_DUE_DATE);
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

		multiSelectParentRelativeToDue = new MultiSelectParent(activity) {
			public void onChanged(String type, ArrayList<Integer> selected) {
				String currInfo = getReminderInfo();
				String newInfo = null;

				String changed = "";
				for (int i : selected)
					changed += i + ",";

				// If curr info is same type, set changed and type + all
				// timestamps in curr info that are over 10 years from now
				// (custom timestamps)
				if (Reminder.getType(currInfo).equals(type)) {
					String[] parts = currInfo.split(",");
					newInfo = Reminder.getReminderInfoString(type, changed);
					for (int i = 1; i < parts.length; i++) {
						try {
							Long l = Long.parseLong(parts[i]);
							if (l > 10 * 365 * 24 * 3600) newInfo += "," + l;
						} catch (NumberFormatException e) {

						}
					}
				} else {
					newInfo = Reminder.getReminderInfoString(type, changed);
				}

				if (newInfo != null) TaskView.this.setReminderInfo(newInfo);
			}
		};

		Resources r = activity.getResources();
		multiSelectParentRelativeToDue.setStrings(r.getString(R.string.one_hour), r.getString(R.string.one_week), r.getString(R.string.one_month));
		multiSelectParentRelativeToDue.setValues(3600, 3600 * 24 * 7, Reminder.ONE_MONTH_RELATIVE_TO_DUE);
		multiSelectParentRelativeToDue.setType(Reminder.REMINDER_RELATIVE_TO_DUE_DATE);
		multiSelectParentRelativeToDue.generate();
		multiSelectParentRelativeToDue.setBackgroundColor(0xffff9999);
		((LinearLayout) findViewById(R.id.relativeToDueContainer)).addView(multiSelectParentRelativeToDue);

		selectDaysInWeek = new SelectDaysInWeek(activity) {
			public void onChanged(final String type, ArrayList<Integer> selected, int hour, int minute) {
				int days = 0;

				for (int i : selected)
					days += i;

				String reminderInfo = Reminder.getReminderInfoString(type, days, hour, minute);
				setReminderInfo(reminderInfo);
			}
		};

		selectDaysInWeek.setStrings(r.getString(R.string.monday), //
				r.getString(R.string.tuesday), //
				r.getString(R.string.wednesday), //
				r.getString(R.string.thursday), //
				r.getString(R.string.friday),//
				r.getString(R.string.saturday), //
				r.getString(R.string.sunday));//
		selectDaysInWeek.setValues(Reminder.MONDAY, Reminder.TUESDAY, Reminder.WEDNESDAY, Reminder.THURSDAY, Reminder.FRIDAY, Reminder.SATURDAY, Reminder.SUNDAY);
		selectDaysInWeek.setType(Reminder.REMINDER_WEEKLY);
		selectDaysInWeek.setBackgroundColor(0xff9999ff);
		selectDaysInWeek.generate();
		((LinearLayout) findViewById(R.id.daysOfWeekContainer)).addView(selectDaysInWeek);

		customReminderList = (ListView) findViewById(R.id.customReminderList);
		customReminderList.setAdapter(new CustomReminderListAdapter(this));
		customReminderList.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
				editCustomReminder(view.getId());
			}
		});

		((Button) findViewById(R.id.monthlyReminder)).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				try {
					final int dayOfMonth = Integer.parseInt(((EditText) findViewById(R.id.dayOfMonth)).getText().toString());
					if (dayOfMonth > 0 && dayOfMonth < 32) {
						new TimeDialog(activity) {
							public void onResult(int hour, int minute) {
								if (hour == -1) hour = 12;
								if (minute == -1) minute = 0;
								String reminderInfo = Reminder.getReminderInfoString(Reminder.REMINDER_MONTHLY, dayOfMonth, hour, minute);
								setReminderInfo(reminderInfo);
							}
						};
					}
				} catch (NumberFormatException e) {
				}
			}
		});

		intervalSelector = new Selector(activity) {
			public void onChanged(int type, int size) {
				if (type == -1) return;
				if (size == -1) size = 1;

				String reminderInfo = Reminder.getReminderInfoString(Reminder.REMINDER_INTERVAL, type, size, System.currentTimeMillis() / 1000);
				setReminderInfo(reminderInfo);
			}
		};
		intervalSelector.setStrings(r.getString(R.string.minutes), r.getString(R.string.hours), r.getString(R.string.days), r.getString(R.string.weeks));
		intervalSelector.setValues(Reminder.INTERVAL_MINUTE, Reminder.INTERVAL_HOUR, Reminder.INTERVAL_DAY, Reminder.INTERVAL_WEEK);
		intervalSelector.setBackgroundColor(0xff77ff77);
		intervalSelector.generate();
		((LinearLayout) findViewById(R.id.intervalType)).addView(intervalSelector);

		setColors();
	}

	public void setColors() {
		Resources r = activity.getResources();
		boolean dark = activity.isDarkTheme();
		setBackgroundColor((dark) ? r.getColor(R.color.background_color_dark) : r.getColor(R.color.white));
		descTV.setBackgroundColor((dark) ? r.getColor(R.color.background_color_dark) : r.getColor(R.color.white));
		descTV.setTextColor((dark) ? r.getColor(R.color.text_color_dark) : r.getColor(R.color.text_color_light));
		descET.setBackgroundColor((dark) ? r.getColor(R.color.selected_dark) : r.getColor(R.color.selected_light));
		descET.setTextColor((dark) ? r.getColor(R.color.text_color_dark) : r.getColor(R.color.text_color_light));
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
					setDueDateButton.setText(activity.getResources().getString(R.string.due_date) + " : " + calendar.get(Calendar.DAY_OF_MONTH) + "-" + calendar.get(Calendar.MONTH) + "-" + calendar.get(Calendar.YEAR) + "(" + calendar.get(Calendar.HOUR_OF_DAY) + " : " + calendar.get(Calendar.MINUTE) + ")");
				} else setDueDateButton.setText(activity.getResources().getString(R.string.set_due_date));

				if (task.has(Reminder.REMINDER_INFO)) {
					setReminderButton.setText("Reminder: " + task.getString(Reminder.REMINDER_INFO));
				} else {
					setReminderButton.setText(activity.getResources().getString(R.string.set_reminder));
				}

				multiSelectParentRelativeToDue.update(getReminderInfo());
				selectDaysInWeek.update(getReminderInfo());
				intervalSelector.update(getReminderInfo());
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		((BaseAdapter) customReminderList.getAdapter()).notifyDataSetChanged();
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

			calendar.set(year, month, day, hour, minute, 0);
			long timestamp = calendar.getTimeInMillis() / 1000;

			if (type.equals(Reminder.REMINDER_RELATIVE_TO_DUE_DATE)) {
				String reminderInfo = "";

				// If there is no reminder info or the reminder info is of
				// another type, the type is set and the due
				// date is set to -1
				if (getReminderInfo() == "" || !Reminder.getType(getReminderInfo()).equals(type)) reminderInfo = Reminder.getReminderInfoString(type, timestamp);
				else reminderInfo = Reminder.getReminderInfoString(getReminderInfo(), timestamp);

				if (getReminderInfo() != "") {
					// If the timestamp already exists, it is not added
					String[] ss = getReminderInfo().split(",");
					for (String s : ss)
						if (s.equals(timestamp + "")) {
							reminderInfo = getReminderInfo();
							break;
						}
				}
				setReminderInfo(reminderInfo);
			} else if (type.equals(App.DUE_DATE)) {
				activity.setProperty(type, timestamp, parentId);
				// Sets the reminder info (to update the due date relative
				// reminders)
				try {
					task.put(App.DUE_DATE, timestamp); // Just in case
				} catch (JSONException e) {
					e.printStackTrace();
				}
				setReminderInfo(getReminderInfo());
			}
		}
	}

	private void setReminderInfo(String reminderInfo) {
		activity.setProperty(Reminder.REMINDER_INFO, reminderInfo, parentId);

		// Only starts reminders if the task is not checked
		try {
			if (!task.has(App.COMPLETED) || !task.getBoolean(App.COMPLETED)) Reminder.startReminder(reminderInfo, activity, parentId, task);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void editCustomReminder(final long timestamp) {
		final String info = getReminderInfo();
		if (!Reminder.getType(info).equals(Reminder.REMINDER_RELATIVE_TO_DUE_DATE)) return;

		new DateAndTimeDialog(activity, timestamp, task, Reminder.REMINDER_RELATIVE_TO_DUE_DATE) {
			public void onResult(int year, int month, int day, int hour, int minute) {
				if (year == -1 && month == -1 && day == -1) return;
				if (hour == -1) hour = 12;
				if (minute == -1) minute = 0;

				Calendar calendar = Calendar.getInstance();
				calendar.set(year, month, day, hour, minute, 0);
				long newTimestamp = calendar.getTimeInMillis() / 1000;
				String s = "";
				String[] parts = info.split(",");
				// Starts at 1 since 0 is the type
				for (int i = 1; i < parts.length; i++) {
					if (parts[i].equals(timestamp + "")) {
						s = Reminder.setPart(info, i, newTimestamp + "");
						setReminderInfo(s);
						break;
					}
				}
			}
		};
	}

	public void removeCustomReminder(long timestamp) {
		String info = getReminderInfo();
		if (!Reminder.getType(info).equals(Reminder.REMINDER_RELATIVE_TO_DUE_DATE)) return;

		info = Reminder.removePart(info, timestamp + "");
		setReminderInfo(info);
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