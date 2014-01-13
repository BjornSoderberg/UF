package com.todo.code3.misc;

import java.util.Calendar;
import java.util.Collection;

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
	public static final String REMINDER_INTERVAL = "d";
	// public static final String REMINDER_SINGLE_TIMESTAMPS = "e";
	public static final String REMINDER_RELATIVE_TO_DUE_DATE = "f";

	public static final int ONE_MONTH_RELATIVE_TO_DUE = 2592000;
	// seconds in month with 30 days (used for checking)

	public static final int INTERVAL_MINUTE = 60;
	public static final int INTERVAL_HOUR = 3600;
	public static final int INTERVAL_DAY = 3600 * 24;
	public static final int INTERVAL_WEEK = 3600 * 24 * 7;

	public static final int MONDAY = 64;
	public static final int TUESDAY = 32;
	public static final int WEDNESDAY = 16;
	public static final int THURSDAY = 8;
	public static final int FRIDAY = 4;
	public static final int SATURDAY = 2;
	public static final int SUNDAY = 1;

	public static final String DAYS_IN_WEEK = "daysInWeek";

	public static final String HOUR_OF_DAY = "hourOfDay";
	public static final String MINUTE_OF_HOUR = "minuteOfHour";

	public static final String REMINDER_INFO = "reminderInfo";

	public static long getNext(String reminderInfo, long dueDate) {
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
		} else if (getType(reminderInfo).equals(Reminder.REMINDER_INTERVAL)) {
			int type = -1;
			try {
				type = Integer.parseInt((String) getPart(reminderInfo, 1));
			} catch (NumberFormatException e) {
				return -1;
			}
			int intervalLength = Integer.parseInt(getPart(reminderInfo, 2));
			long start = Long.parseLong(getPart(reminderInfo, 3));

			int cycle = intervalLength;
			if(cycle == -1) return -1;
			
			if (type == Reminder.INTERVAL_WEEK) cycle *= 7 * 24 * 60 * 60;
			else if (type == Reminder.INTERVAL_DAY) cycle *= 24 * 60 * 60;
			else if (type == Reminder.INTERVAL_HOUR) cycle *= 60 * 60;
			else if (type == Reminder.INTERVAL_MINUTE) cycle *= 60;
			else return -1;

			c.setTimeInMillis(start * 1000);

			int count = 0;
			while (count < 100000) {
				// Return only if the time is in the future
				if (System.currentTimeMillis() < c.getTimeInMillis()) {
					return c.getTimeInMillis() / 1000;
				}

				count++;
				c.setTimeInMillis(c.getTimeInMillis() + cycle * 1000);
			}
		} else if (getType(reminderInfo).equals(Reminder.REMINDER_RELATIVE_TO_DUE_DATE)) {
			long now = System.currentTimeMillis() / 1000;
			long closest = -1;

			// 0 is type
			int part = 1;
			while (part < 1000) {
				if (hasPart(reminderInfo, part)) {
					try {
						long l = Long.parseLong(getPart(reminderInfo, part));
						long t = 0;
						// Exception for month (num seconds in month vary)
						if (l == Reminder.ONE_MONTH_RELATIVE_TO_DUE) {
							c.setTimeInMillis(dueDate * 1000);
							c.add(Calendar.MONTH, -1);
							t = c.getTimeInMillis() / 1000;
							// If t is larger than 10 years, it is a custom
							// timestamp (not relative to due date)
						} else if (l > 10 * 365 * 24 * 3600) {
							t = l;
						} else if (dueDate != -1) t = dueDate - l;

						if (t > now && (t < closest || closest == -1)) closest = t;
					} catch (NumberFormatException e) {

					}
				} else break;

				part++;
			}
			return closest;
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

	public static String getReminderInfoString(Object... items) {
		String s = "";
		for (Object item : items) {
			s += item.toString() + ",";
		}
		if (s.length() > 0 && s.charAt(s.length() - 1) == ',') s = s.substring(0, s.length() - 1);
		return s;
	}

	public static String getPart(String reminderInfo, int part) {
		String[] s = reminderInfo.split(",");
		if (s.length > part) return s[part];
		return "";
	}

	public static String setPart(String reminderInfo, int part, String newPart) {
		if (!hasPart(reminderInfo, part)) return reminderInfo;

		String[] ss = reminderInfo.split(",");
		String n = "";
		for (int i = 0; i < ss.length; i++) {
			if (i == part) n += newPart + ",";
			else n += ss[i] + ",";
		}
		return n;
	}

	public static String removePart(String reminderInfo, String part) {
		String[] parts = reminderInfo.split(",");
		String n = "";
		for (String s : parts) {
			if (!s.equals(part)) n += s + ",";
		}

		if (n.length() > 0 && n.charAt(n.length() - 1) == ',') n = n.substring(0, n.length() - 1);
		return n;
	}

	public static boolean hasPart(String reminderInfo, int part) {
		return reminderInfo.split(",").length > part;
	}

	public static String getType(String info) {
		return getPart(info, 0);
	}

	public static void startReminder(String reminderInfo, Context context, int id, JSONObject object) {
		Intent i = new Intent(context, NotificationReceiver.class);
		i.putExtra(App.ID, id);
		i.putExtra(Reminder.REMINDER_INFO, reminderInfo);

		long dueDate = -1;

		try {
			if (object != null) {
				if (object.has(App.NAME)) i.putExtra(App.NAME, object.getString(App.NAME));
				if (object.has(App.DUE_DATE)) {
					i.putExtra(App.DUE_DATE, object.getLong(App.DUE_DATE));

					dueDate = object.getLong(App.DUE_DATE);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		long next = Reminder.getNext(reminderInfo, dueDate);
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
