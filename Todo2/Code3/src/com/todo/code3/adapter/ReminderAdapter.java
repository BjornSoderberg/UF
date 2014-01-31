package com.todo.code3.adapter;

import java.util.ArrayList;
import java.util.Calendar;

import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;

import com.todo.code3.R;
import com.todo.code3.animation.CollapseAnimation;
import com.todo.code3.dialog.date_and_time.DateAndTimeDialog;
import com.todo.code3.misc.App;
import com.todo.code3.misc.Reminder;
import com.todo.code3.view.TaskView;

public class ReminderAdapter extends BaseAdapter {

	private LayoutInflater inflater;
	private TaskView taskView;

	private ArrayList<String> strings;
	private ArrayList<Long> values;
	private ArrayList<Long> list;
	private ArrayList<Long> selected;

	private long created;

	public ReminderAdapter(TaskView tv) {
		inflater = LayoutInflater.from(tv.getActivity());
		taskView = tv;

		list = new ArrayList<Long>();
		values = new ArrayList<Long>();
		selected = new ArrayList<Long>();
		strings = new ArrayList<String>();

		Resources r = taskView.getActivity().getResources();
		strings.add(r.getString(R.string.add_new));
		strings.add(r.getString(R.string.set_custom_reminder));
		strings.add("1 " + r.getString(R.string.hour));
		strings.add("2 " + r.getString(R.string.hours));
		strings.add("1 " + r.getString(R.string.day));
		strings.add("2 " + r.getString(R.string.days));
		strings.add("1 " + r.getString(R.string.week));
		strings.add("2 " + r.getString(R.string.weeks));
		strings.add("1 " + r.getString(R.string.month));
		values.add(-1L);
		values.add(-2L);
		values.add(3600 * 1L);
		values.add(3600 * 2L);
		values.add(3600 * 24 * 1L);
		values.add(3600 * 24 * 2L);
		values.add(3600 * 24 * 7 * 1L);
		values.add(3600 * 24 * 7 * 2L);
		values.add(3600 * 24 * 30 * 1L);

		created = System.currentTimeMillis();

		init();
	}

	private void init() {
		for (String s : taskView.getReminderInfo().split(",")) {
			try {
				Long l = Long.parseLong(s);
				if (l < 3600 * 24 * 365) selected.add(l);
			} catch (NumberFormatException e) {

			}
		}
	}

	public int getCount() {
		// +1 because an extra item should be added (to add more reminders)
		return taskView.getReminderCount() + 1;
	}

	public String getItem(int position) {
		if (taskView.getReminderInfo().split(",").length > position) return taskView.getReminderInfo().split(",")[position];
		else return taskView.getActivity().getResources().getString(R.string.no_name);
	}

	public long getItemId(int position) {
		return values.get(position);
	}

	public int getPosition(int pos) {
		try {
			if (taskView.getReminderInfo().split(",").length <= pos) return 0;

			// +1 because first is nothing
			int value = Integer.parseInt(taskView.getReminderInfo().split(",")[pos]);
			for (int i = 0; i < values.size(); i++)
				if (values.get(i) == value) return i;
		} catch (NumberFormatException e) {

		}
		return 0;
	}

	public View getView(final int position, View convertView, ViewGroup parent) {
		final View view = inflater.inflate(R.layout.reminder_item, null);

		final Spinner spinner = (Spinner) view.findViewById(R.id.spinner);

		final ArrayList<String> s = new ArrayList<String>();
		s.addAll(strings);
		final ArrayList<Long> v = new ArrayList<Long>();
		v.addAll(values);

		int open = 0;

		// Prevents the already selected items from being shown again
		for (int i = 0; i < selected.size(); i++) {
			if (v.contains(selected.get(i))) {
				s.remove(v.indexOf(selected.get(i)));
				v.remove(selected.get(i));
			}
		}

		if (position != getCount() - 1) {
			s.remove(v.indexOf(Long.valueOf(-1)));
			v.remove(Long.valueOf(-1));

			try {
				long selected = Long.parseLong(taskView.getReminderInfo().split(",")[position]);
				String str = selected + "";
				if (selected > 3600 * 24 * 365) {
					str = App.getFormattedDateString(selected, taskView.getActivity().is24HourMode(), taskView.getActivity().getLocaleString());
				} else str = getTimeString(selected);
				if (!s.contains(str)) s.add(str);
				if (!v.contains(selected)) v.add(selected);

				open = v.indexOf(selected);
			} catch (NumberFormatException e) {
			}
		}

		int layoutId = taskView.getActivity().isDarkTheme() ? R.layout.drop_down_item_dark : R.layout.drop_down_item_light;
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(taskView.getActivity(), layoutId, R.id.item_text, s);
		spinner.setAdapter(adapter);
		spinner.setSelection(open);

		// If the version is under 11 the background of the spinner is set
		// The holo theme did not exist and spinners had a gradient background
		int colorId = taskView.getActivity().isDarkTheme() ? R.color.dark : R.color.light;
		if (Build.VERSION.SDK_INT < 11) spinner.setBackgroundColor(taskView.getActivity().getResources().getColor(colorId));

		if (list.size() > position) list.set(position, v.get(open));
		else list.add(v.get(open));

		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				if (System.currentTimeMillis() - created < 1000) return;

				int pos = spinner.getSelectedItemPosition();
				if (v.get(pos) == -2) {
					updateTime(-1);
					return;
				}

				setNewTime(v.get(pos), position);
			}

			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});

		// Only looks for on touch events if the value is a custom value
		if (!values.contains(v.get(open))) {
			spinner.setOnTouchListener(new OnTouchListener() {
				int x, y;
				ViewConfiguration vc = ViewConfiguration.get(taskView.getActivity());

				public boolean onTouch(View v, MotionEvent e) {
					if (e.getAction() == MotionEvent.ACTION_DOWN) {
						x = (int) e.getX();
						y = (int) e.getY();
					} else if (e.getAction() == MotionEvent.ACTION_MOVE) {
					} else if (e.getAction() == MotionEvent.ACTION_UP) {

						if (Math.hypot(x - e.getX(), y - e.getY()) < vc.getScaledTouchSlop()) {
							updateTime(position);
						}
					}
					return true;
				}
			});
		}

		// Assures that all the views have the same height
		if (view.getLayoutParams() != null) view.getLayoutParams().height = (int) taskView.getActivity().getResources().getDimension(R.dimen.item_height);
		else view.setLayoutParams(new ListView.LayoutParams(LayoutParams.FILL_PARENT, (int) taskView.getActivity().getResources().getDimension(R.dimen.item_height)));

		if (position != getCount() - 1) {
			Drawable d = ((ImageView) view.findViewById(R.id.icon)).getDrawable().mutate();
			d.setColorFilter(new PorterDuffColorFilter(taskView.getActivity().getResources().getColor(R.color.icon_color), PorterDuff.Mode.MULTIPLY));

			((FrameLayout) view.findViewById(R.id.item_remove)).setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					// Collapses the view when removing it
					new CollapseAnimation(view, App.ANIMATION_DURATION, (int) taskView.getActivity().getResources().getDimension(R.dimen.item_height)).animate();
					new Handler().postDelayed(new Runnable() {
						public void run() {
							list.remove(position);
							updateList();
						}
					}, App.ANIMATION_DURATION);
				}
			});
		} else {
			((ImageView) view.findViewById(R.id.icon)).setVisibility(View.GONE);
		}

		view.setBackgroundColor(taskView.getActivity().getResources().getColor(colorId));

		return view;
	}

	private void setNewTime(long value, int oldPos) {
		if (oldPos == -1) {
			if (!list.contains(value)) list.add(value);
		} else {
			if (!list.contains(value)) list.set(oldPos, value);
		}

		updateList();
	}

	private void updateList() {
		taskView.updateReminder(list);
	}

	private void updateTime(final int oldPos) {
		new DateAndTimeDialog(taskView.getActivity(), (oldPos == -1) ? -1 : list.get(oldPos), taskView.getTask(), Reminder.REMINDER_CUSTOM) {
			public void onResult(int year, int month, int day, int hour, int minute) {
				if (year != -1 && month != -1 && day != -1) {
					if (hour == -1) hour = 12;
					if (minute == -1) minute = 0;

					Calendar c = Calendar.getInstance();
					c.set(year, month, day, hour, minute);
					long timestamp = c.getTimeInMillis() / 1000;

					setNewTime(timestamp, oldPos);
				} else updateList();
			}
		};
	}

	private String getTimeString(long time) {
		int index = values.indexOf(time);
		if (index != -1) return strings.get(index);
		else return "";
	}
}
