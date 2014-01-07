package com.todo.code3.misc;

import java.util.Calendar;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.todo.code3.notification.NotificationReceiver;

public class Reminder {
	public static final String REMINDER_WEEKLY = "a";
	public static final String REMINDER_EVERY_TWO_WEEKS = "b";
	public static final String REMINDER_MONTHLY = "c";
	public static final String REMINDER_SET_INTERVAL = "d";
	public static final String REMINDER_SINGLE_TIMESTAMPS = "e";

	public static final int MONDAY = 64;
	public static final int TUESDAY = 32;
	public static final int WEDNESDAY = 16;
	public static final int THURSDAY = 8;
	public static final int FRIDAY = 4;
	public static final int SATURDAY = 2;
	public static final int SUNDAY = 1;

	public static final String WEEK = "week";
	public static final String DAY = "day";
	public static final String HOUR = "hour";
	public static final String MINUTE = "minute";

	public static final String DAYS_IN_WEEK = "daysInWeek";

	public static final String HOUR_OF_DAY = "hourOfDay";
	public static final String MINUTE_OF_HOUR = "minuteOfHour";

	public static final String REMINDER_INFO = "reminderInfo";

	public static long getNext(String reminderInfo) {
		Calendar c = Calendar.getInstance();

		if (getType(reminderInfo).equals(Reminder.REMINDER_WEEKLY) || getType(reminderInfo).equals(Reminder.REMINDER_EVERY_TWO_WEEKS)) {
			int gap = 1;
			if (getType(reminderInfo).equals(Reminder.REMINDER_WEEKLY)) gap = 1;
			else if (getType(reminderInfo).equals(Reminder.REMINDER_EVERY_TWO_WEEKS)) gap = 2;

			int days = Integer.parseInt(getPart(reminderInfo, 1));
			int hour = Integer.parseInt(getPart(reminderInfo, 2));
			int minute = Integer.parseInt(getPart(reminderInfo, 3));
			int startWeek = 1;
			if (hasPart(reminderInfo, 4)) startWeek = Integer.parseInt(getPart(reminderInfo, 4));

			int count = 0;
			while (count < 2000) {
				if (existsIn(c.get(Calendar.DAY_OF_WEEK), days)) {
					// if gap = 2, startWeek = 8: only 8, 10, 12, 14... will be
					// true
					if (startWeek % gap == c.get(Calendar.WEEK_OF_YEAR) % gap) {
						c.set(Calendar.HOUR_OF_DAY, hour);
						c.set(Calendar.MINUTE, minute);
						c.set(Calendar.SECOND, 0);

						// Return only if the time is in the future
						if (System.currentTimeMillis() < c.getTimeInMillis()) { //
							return c.getTimeInMillis() / 1000;
						}
					}
				}

				c.add(Calendar.DAY_OF_MONTH, 1);
				count++;
			}

			return -1;
		} else if (getType(reminderInfo).equals(Reminder.REMINDER_MONTHLY)) {
			int dayOfMonth = Integer.parseInt(getPart(reminderInfo, 1));
			int hour = Integer.parseInt(getPart(reminderInfo, 2));
			int minute = Integer.parseInt(getPart(reminderInfo, 3));

			int count = 0;
			while (count < 2000) {
				int d = dayOfMonth;
				if (c.getActualMaximum(Calendar.DAY_OF_MONTH) < d) d = c.getActualMaximum(Calendar.DAY_OF_MONTH);

				if (c.get(Calendar.DAY_OF_MONTH) == d) {
					c.set(Calendar.HOUR_OF_DAY, hour);
					c.set(Calendar.MINUTE, minute);
					c.set(Calendar.SECOND, 0);

					// Return only if the time is in the future
					if (System.currentTimeMillis() < c.getTimeInMillis()) { //
						return c.getTimeInMillis() / 1000;
					}
				}

				c.add(Calendar.DAY_OF_MONTH, 1);
				count++;
			}
		} else if (getType(reminderInfo).equals(Reminder.REMINDER_SET_INTERVAL)) {
			String type = getPart(reminderInfo, 1);
			int intervalLength = Integer.parseInt(getPart(reminderInfo, 2));
			long start = Long.parseLong(getPart(reminderInfo, 3));

			int cycle = intervalLength;
			if (type.equals(Reminder.WEEK)) cycle *= 7 * 24 * 60 * 60;
			else if (type.equals(Reminder.DAY)) cycle *= 24 * 60 * 60;
			else if (type.equals(Reminder.HOUR)) cycle *= 60 * 60;
			else if (type.equals(Reminder.MINUTE)) cycle *= 60;
			else return -1;

			c.setTimeInMillis(start * 1000);

			int count = 0;
			while (count < 5000) {
				// Return only if the time is in the future
				if (System.currentTimeMillis() < c.getTimeInMillis()) { //
					return c.getTimeInMillis() / 1000;
				}

				count++;
				c.setTimeInMillis(c.getTimeInMillis() + cycle * 1000);
			}
		} else if(getType(reminderInfo).equals(Reminder.REMINDER_SINGLE_TIMESTAMPS)) {
			long smallest = -1;
			
			String[] parts = reminderInfo.split(",");
			for(int i = 1; i < parts.length; i++) {
				try {
					long timestamp = Long.parseLong(parts[i]);
					if(timestamp < smallest || smallest == -1) {
						if(timestamp * 1000 > System.currentTimeMillis()) smallest = timestamp;
					}
				} catch(NumberFormatException e) {
					e.printStackTrace();
				}
			}
			
			return smallest;
		}

		return -1;
	}

	public static boolean existsIn(int day, int days) {
		day = convertDayToBinary(day);
		if ((days & day) != 0) return true;

		return false;
	}

	public static int convertDayToBinary(int day) {
		if (day == Calendar.MONDAY) return MONDAY;
		if (day == Calendar.TUESDAY) return TUESDAY;
		if (day == Calendar.WEDNESDAY) return WEDNESDAY;
		if (day == Calendar.THURSDAY) return THURSDAY;
		if (day == Calendar.FRIDAY) return FRIDAY;
		if (day == Calendar.SATURDAY) return SATURDAY;
		if (day == Calendar.SUNDAY) return SUNDAY;
		return 0;
	}
	
	public static String getReminderInfoString(Object type, Object asd) {
		String s = type.toString() + "," + asd.toString();
		return s;
	}

	public static String getReminderInfoString(Object type, Object days, Object hour, Object minute) {
		String s = type.toString() + "," + days.toString() + "," + hour.toString() + "," + minute.toString();
		return s;
	}

	public static String getReminderInfoString(Object type, Object days, Object hour, Object minute, Object startWeek) {
		String s = type.toString() + "," + days.toString() + "," + hour.toString() + "," + minute.toString() + "," + startWeek.toString();
		return s;
	}

	public static String getPart(String reminderInfo, int part) {
		String[] s = reminderInfo.split(",");
		if (s.length > part) return s[part];
		return "";
	}

	public static boolean hasPart(String reminderInfo, int part) {
		return reminderInfo.split(",").length > part;
	}

	public static String getType(String info) {
		return getPart(info, 0);
	}

	public static void startRepeatingReminder(String reminderInfo, Context context, int id, JSONObject object) {
		Intent i = new Intent(context, NotificationReceiver.class);
		i.putExtra(App.ID, id);
		i.putExtra(Reminder.REMINDER_INFO, reminderInfo);

		try {
			if (object != null) {
				if (object.has(App.NAME)) i.putExtra(App.NAME, object.getString(App.NAME));
				if (object.has(App.DUE_DATE)) i.putExtra(App.DUE_DATE, object.getLong(App.DUE_DATE));
			}
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

		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, next, PendingIntent.getBroadcast(context, id, i, PendingIntent.FLAG_UPDATE_CURRENT));
	}
}
