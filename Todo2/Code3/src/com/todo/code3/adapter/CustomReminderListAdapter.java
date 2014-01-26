package com.todo.code3.adapter;

import java.util.Calendar;

import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.todo.code3.R;
import com.todo.code3.misc.Reminder;
import com.todo.code3.view.TaskView;

public class CustomReminderListAdapter extends BaseAdapter {

	private Calendar calendar = Calendar.getInstance();
	private TaskView taskView;
	private LayoutInflater inflater;

	public CustomReminderListAdapter(TaskView t) {
		taskView = t;
		inflater = LayoutInflater.from(taskView.getActivity());
	}

	public int getCount() {
		String reminderInfo = taskView.getReminderInfo();
		if (reminderInfo == "") return 0;

		String[] parts = reminderInfo.split(",");
		int num = 0;

		// Only lists timestamps if it is the right type
		if (!Reminder.getType(reminderInfo).equals(Reminder.REMINDER_RELATIVE_TO_DUE_DATE)) return 0;
		for (int i = 0; i < parts.length; i++) {
			try {
				long l = Long.parseLong(parts[i]);
				if (l > 365 * 24 * 3600 * 10) num++;
			} catch (NumberFormatException e) {
				continue;
			}
		}

		return num;
	}

	public Object getItem(int position) {
		return null;
	}

	public long getItemId(int position) {
		return 0;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View view = inflater.inflate(R.layout.custom_reminder_item, null);

		TextView t = (TextView) view.findViewById(R.id.item_text);
		final long timestamp = getTimestamp(position);
		t.setText(timestamp + "");
		t.setTextColor((taskView.getActivity().isDarkTheme()) ? taskView.getActivity().getResources().getColor(R.color.text_color_dark) : taskView.getActivity().getResources().getColor(R.color.text_color_light));

		calendar.setTimeInMillis(timestamp * 1000);

		view.setId((int) timestamp);

		FrameLayout f = (FrameLayout) view.findViewById(R.id.item_remove);
		f.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				taskView.removeCustomReminder(timestamp);
			}
		});
		((ImageView) view.findViewById(R.id.icon)).getDrawable().setColorFilter(new PorterDuffColorFilter(taskView.getActivity().getResources().getColor(R.color.icon_color), PorterDuff.Mode.MULTIPLY));

		if (taskView.getActivity().isDarkTheme()) view.setBackgroundDrawable(taskView.getActivity().getResources().getDrawable(R.drawable.item_selector_dark));
		else view.setBackgroundDrawable(taskView.getActivity().getResources().getDrawable(R.drawable.item_selector_light));

		return view;
	}

	private long getTimestamp(int position) {
		String reminderInfo = taskView.getReminderInfo();
		if (reminderInfo == "") return -1;

		String[] parts = reminderInfo.split(",");
		// Start value is -1 since everything is zero based. When num is ++ the
		// first time, the first match has been found
		int num = -1;

		for (int i = 0; i < parts.length; i++) {
			try {
				long l = Long.parseLong(parts[i]);
				if (l > 365 * 24 * 3600 * 10) num++;

				if (num == position) return l;
			} catch (NumberFormatException e) {
				continue;
			}
		}

		return -1;
	}

	// Update icons
	// White/dark theme for custom reminder items (set colors)
}
