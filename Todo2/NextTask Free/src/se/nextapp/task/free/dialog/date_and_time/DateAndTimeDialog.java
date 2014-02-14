package se.nextapp.task.free.dialog.date_and_time;

import java.util.Arrays;
import java.util.Calendar;

import org.json.JSONException;
import org.json.JSONObject;

import se.nextapp.task.free.MainActivity;
import se.nextapp.task.free.misc.App;
import se.nextapp.task.free.misc.Reminder;

import android.util.Log;

import com.fourmob.datetimepicker.date.DatePickerDialog;
import com.fourmob.datetimepicker.date.DatePickerDialog.OnDateSetListener;
import com.sleepbot.datetimepicker.time.RadialPickerLayout;
import com.sleepbot.datetimepicker.time.TimePickerDialog;
import com.sleepbot.datetimepicker.time.TimePickerDialog.OnTimeSetListener;

public class DateAndTimeDialog {

	private int year = -1;
	private int month = -1;
	private int day = -1;
	private int hour = 12;
	private int minute = 0;

	private int showYear, showMonth, showDay, showHour, showMinute;

	private long timestamp;

	private String type;
	private JSONObject object;

	private OnDateSetListener dsl;
	private OnTimeSetListener tsl;

	private Calendar calendar = Calendar.getInstance();
	private MainActivity activity;

	public DateAndTimeDialog(MainActivity activity, JSONObject object, String type) {
		this.type = type;
		this.object = object;
		this.activity = activity;

		initStandardTimeAndDateFromJSONObject();

		init();
	}

	public DateAndTimeDialog(MainActivity activity, long timestamp, JSONObject object, String type) {
		this.type = type;
		this.timestamp = timestamp;
		this.activity = activity;
		this.object = object;

		if (timestamp == -1) initStandardTimeAndDateFromJSONObject();
		else initStandardTimeAndDateFromTimestamp();

		init();
	}

	private void init() {
		dsl = new OnDateSetListener() {
			public void onDateSet(DatePickerDialog datePickerDialog, int y, int m, int d) {
				year = y;
				month = m;
				day = d;

				if (year != -1 && month != -1 && day != -1) showTimePicker();
				else onResult(-1, -1, -1, -1, -1);
			}
		};

		tsl = new OnTimeSetListener() {
			public void onTimeSet(RadialPickerLayout view, int h, int m) {
				hour = h;
				minute = m;

				onResult(year, month, day, hour, minute);
			}
		};

		showDatePicker();
	}

	private void initStandardTimeAndDateFromJSONObject() {
		showYear = calendar.get(Calendar.YEAR);
		showMonth = calendar.get(Calendar.MONTH);
		showDay = calendar.get(Calendar.DAY_OF_MONTH);
		showHour = calendar.get(Calendar.HOUR_OF_DAY);
		showMinute = calendar.get(Calendar.MINUTE);

		try {
			if (type.equals(App.DUE_DATE)) {
				if (object.has(type) && object.getLong(type) != -1) {
					calendar.setTimeInMillis(object.getLong(type) * 1000);

					showYear = (showYear > calendar.get(Calendar.YEAR)) ? showYear : calendar.get(Calendar.YEAR);
					showMonth = calendar.get(Calendar.MONTH);
					showDay = calendar.get(Calendar.DAY_OF_MONTH);
					showHour = calendar.get(Calendar.HOUR_OF_DAY);
					showMinute = calendar.get(Calendar.MINUTE);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void initStandardTimeAndDateFromTimestamp() {
		calendar.setTimeInMillis(timestamp * 1000);

		showYear = calendar.get(Calendar.YEAR);
		showMonth = calendar.get(Calendar.MONTH);
		showDay = calendar.get(Calendar.DAY_OF_MONTH);
		showHour = calendar.get(Calendar.HOUR_OF_DAY);
		showMinute = calendar.get(Calendar.MINUTE);
	}

	private void showDatePicker() {		
		DatePickerDialog d = null;

		try {
			int due[] = App.getDueDate(object.getLong(App.DUE_DATE));
			d = DatePickerDialog.newInstance(dsl, showYear, showMonth, showDay, due[0], due[1], due[2], activity.isDarkTheme());
		} catch (JSONException e) {
		}

		if (d == null) d = DatePickerDialog.newInstance(dsl, showYear, showMonth, showDay, activity.isDarkTheme());

		d.setYearRange(Calendar.getInstance().get(Calendar.YEAR) - 1, 2037);
		d.show(activity.getSupportFragmentManager(), "datepicker");
	}

	private void showTimePicker() {
		TimePickerDialog t = TimePickerDialog.newInstance(tsl, showHour, showMinute, activity.is24HourMode(), activity.isDarkTheme());
		t.show(activity.getSupportFragmentManager(), "timepicker");
	}

	public void onResult(int year, int month, int day, int hour, int minute) {

	}
}
