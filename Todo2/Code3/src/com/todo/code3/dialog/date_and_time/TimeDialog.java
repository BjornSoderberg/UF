package com.todo.code3.dialog.date_and_time;

import java.util.Calendar;

import com.sleepbot.datetimepicker.time.RadialPickerLayout;
import com.sleepbot.datetimepicker.time.TimePickerDialog;
import com.sleepbot.datetimepicker.time.TimePickerDialog.OnTimeSetListener;
import com.todo.code3.MainActivity;

public class TimeDialog {

	private OnTimeSetListener tsl;
	private MainActivity activity;

	public TimeDialog(MainActivity activity) {
		this.activity = activity;

		init();
	}

	private void init() {
		tsl = new OnTimeSetListener() {
			public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute) {
				TimeDialog.this.onResult(hourOfDay, minute);
			}
		};
		
		Calendar c = Calendar.getInstance();
		TimePickerDialog t = TimePickerDialog.newInstance(tsl,  c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), activity.is24HourMode());
		t.show(activity.getSupportFragmentManager(), "timepicker");
	}

	public void onResult(int hour, int minute) {
	}
}
