package se.nextapp.task.full.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

import org.json.JSONException;
import org.json.JSONObject;

import se.nextapp.task.full.MainActivity;
import se.nextapp.task.full.R;
import se.nextapp.task.full.adapter.ReminderAdapter;
import se.nextapp.task.full.dialog.date_and_time.DateAndTimeDialog;
import se.nextapp.task.full.misc.App;
import se.nextapp.task.full.misc.Reminder;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class TaskView extends ContentView {

	private EditText descET, focusDummy;
	private TextView descTV, dueTV;
	private FrameLayout dueRemove;
	private ListView reminderListView;

	private JSONObject task;

	public TaskView(MainActivity activity, int parentId) {
		super(activity, parentId);
		init();
	}

	protected void init() {
		LayoutInflater.from(activity).inflate(R.layout.task_view, this, true);
		setLayoutParams(new LayoutParams(activity.getContentWidth(), LayoutParams.FILL_PARENT));

		descET = (EditText) findViewById(R.id.etDescription);
		descTV = (TextView) findViewById(R.id.tvDescription);
		focusDummy = (EditText) findViewById(R.id.focusDummy);

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

		dueTV = (TextView) findViewById(R.id.tvDue);
		dueTV.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				showDateAndTimePicker(App.DUE_DATE);
			}
		});

		dueRemove = (FrameLayout) findViewById(R.id.remove_due);
		((ImageView)findViewById(R.id.dueRemove)).getDrawable().mutate().setColorFilter(new PorterDuffColorFilter(getResources().getColor(R.color.icon_color), PorterDuff.Mode.MULTIPLY));
		dueRemove.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				clearDueDate();
			}
		});

		reminderListView = (ListView) findViewById(R.id.lvReminder);
		reminderListView.setAdapter(new ReminderAdapter(this));

		reminderListView.getLayoutParams().height = (int) (((BaseAdapter) reminderListView.getAdapter()).getCount() * activity.getResources().getDimension(R.dimen.item_height));
		
		((TextView) findViewById(R.id.tvDescriptionHeader)).setText(((TextView) findViewById(R.id.tvDescriptionHeader)).getText().toString().toUpperCase());
		((TextView) findViewById(R.id.tvDueHeader)).setText(((TextView) findViewById(R.id.tvDueHeader)).getText().toString().toUpperCase());
		((TextView) findViewById(R.id.tvReminderHeader)).setText(((TextView) findViewById(R.id.tvReminderHeader)).getText().toString().toUpperCase());
		
		setColors();
	}

	public void setColors() {
		Resources r = activity.getResources();
		boolean dark = activity.isDarkTheme();
		setBackgroundColor((dark) ? r.getColor(R.color.background_color_dark) : r.getColor(R.color.white));

		descTV.setBackgroundColor((dark) ? r.getColor(R.color.dark) : r.getColor(R.color.light));
		descTV.setTextColor((dark) ? r.getColor(R.color.text_color_dark) : r.getColor(R.color.text_color_light));
		descET.setBackgroundColor((dark) ? r.getColor(R.color.dark) : r.getColor(R.color.light));
		descET.setTextColor((dark) ? r.getColor(R.color.text_color_dark) : r.getColor(R.color.text_color_light));
		
		dueTV.setBackgroundColor((dark) ? r.getColor(R.color.dark) : r.getColor(R.color.light));
		dueTV.setTextColor((dark) ? r.getColor(R.color.text_color_dark) : r.getColor(R.color.text_color_light));
		
		dueRemove.setBackgroundColor((dark) ? r.getColor(R.color.dark) : r.getColor(R.color.light));
	}

	public void update(JSONObject data) {
		try {
			task = new JSONObject(data.getString(parentId + ""));
			if (task.getString(App.TYPE).equals(App.TASK)) {
				if (task.has(App.DESCRIPTION)) descTV.setText(task.getString(App.DESCRIPTION));

				if (task.has(App.DUE_DATE) && task.getLong(App.DUE_DATE) != -1) {
					Calendar calendar = Calendar.getInstance();
					calendar.setTimeInMillis(task.getLong(App.DUE_DATE) * 1000);
					
					dueTV.setText(App.getFormattedDateString(task.getLong(App.DUE_DATE), activity.is24HourMode(), activity.getLocaleString()));
					dueRemove.setVisibility(View.VISIBLE);
				} else {
					dueTV.setText(activity.getResources().getString(R.string.set_due_date));
					dueRemove.setVisibility(View.GONE);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		reminderListView.setAdapter(new ReminderAdapter(this));
		reminderListView.getLayoutParams().height = (int) (((BaseAdapter) reminderListView.getAdapter()).getCount() * activity.getResources().getDimension(R.dimen.item_height));
	}

	private void startEditDescription() {
		descTV.setVisibility(View.GONE);
		descET.setVisibility(View.VISIBLE);
		// saveButton.setVisibility(View.VISIBLE);
		activity.enableCheck();

		descET.setText(descTV.getText());

		descET.requestFocus();
	}

	public void endEditDescription(boolean save) {
		descTV.setVisibility(View.VISIBLE);
		descET.setVisibility(View.GONE);
		activity.disableCheck();

		focusDummy.requestFocus();

		if (save) {
			saveDescription(descET.getText().toString());
			descTV.setText(descET.getText().toString());
		}
	}

	public void showDateAndTimePicker(final String type) {
		endEditDescription(true);
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

			if (type.equals(App.DUE_DATE)) {
				activity.setProperty(type, timestamp, parentId);
				try {
					task.put(App.DUE_DATE, timestamp); // Just in case
				} catch (JSONException e) {
					e.printStackTrace();
				}

				// Sets the reminder info (to update the due date relative
				// reminders)
				setReminderInfo(getReminderInfo());
			} else if (type.equals(Reminder.REMINDER_CUSTOM)) {
				boolean old = false;
				for (String s : getReminderInfo().split(","))
					if (s.equals(timestamp + "")) old = true;

				if (!old) setReminderInfo(getReminderInfo() + "," + timestamp);
			}
		}
	}

	private void setReminderInfo(String reminderInfo) {
		long[] l = new long[reminderInfo.split(",").length];
		for (int i = 0; i < reminderInfo.split(",").length; i++) {
			try {
				l[i] = Long.parseLong(reminderInfo.split(",")[i]);
			} catch (NumberFormatException e) {

			}
		}
		// Sorts the array to place the smalles values at the top
		Arrays.sort(l);
		String s = "";
		for (long ll : l) {
			if (ll != 0) s += ll + ",";
		}
		reminderInfo = s;
		activity.setProperty(Reminder.REMINDER_INFO, reminderInfo, parentId);

		// Only starts reminders if the task is not checked
		try {
			if (!task.has(App.COMPLETED) || !task.getBoolean(App.COMPLETED)) Reminder.startReminder(reminderInfo, activity, parentId, task);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		Log.i("Set Reminder Info", reminderInfo);
	}

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

	public int getReminderCount() {
		if (getReminderInfo().length() == 0) return 0;
		return getReminderInfo().split(",").length;
	}

	public boolean isChecked() {
		try {
			if (task.has(App.COMPLETED) && task.getBoolean(App.COMPLETED)) return true;
		} catch (JSONException e) {
		}
		return false;
	}

	public JSONObject getTask() {
		return task;
	}

	// not used
	public void updateContentItemsOrder() {
	}

	public void updateReminder(ArrayList<Long> list) {
		String info = "";

		// Removes items that are the same
		boolean redo = true;
		while (redo) {
			redo = false;
			big: for (int i = 0; i < list.size(); i++)
				for (int j = 0; j < list.size(); j++)
					if (list.get(i) == list.get(j) && i != j) {
						list.remove(i);
						redo = true;
						break big;
					}
		}

		for (Long l : list) {
			if (l != -1) info += l + ",";
		}
		if (info.length() > 0 && info.charAt(info.length() - 1) == ',') info = info.substring(0, info.length() - 1);

		setReminderInfo(info);
	}
}