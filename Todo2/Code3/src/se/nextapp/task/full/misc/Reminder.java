package se.nextapp.task.full.misc;

import java.util.Calendar;
import java.util.Collection;

import org.json.JSONException;
import org.json.JSONObject;

import se.nextapp.task.full.notification.NotificationReceiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


public class Reminder {
	public static final String REMINDER_WEEKLY = "a";
	// public static final String REMINDER_EVERY_TWO_WEEKS = "b";
	public static final String REMINDER_MONTHLY = "c";
	public static final String REMINDER_INTERVAL = "d";
	// public static final String REMINDER_SINGLE_TIMESTAMPS = "e";
	public static final String REMINDER_RELATIVE_TO_DUE_DATE = "f";
	public static final String REMINDER_CUSTOM = "g";

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

	public static final int PART_DUE_DATE = 0;
	public static final int PART_REMINDER = 1;
	public static final int PART_REPEAT = 2;

	public static final String DAYS_IN_WEEK = "daysInWeek";

	public static final String HOUR_OF_DAY = "hourOfDay";
	public static final String MINUTE_OF_HOUR = "minuteOfHour";

	public static final String REMINDER_INFO = "reminderInfo";
	public static final String REPEAT_INFO = "repeatInfo";

	public static long getNext(String reminderInfo, long dueDate) {
		Calendar c = Calendar.getInstance();
		// Makes the reminder only go off once
		c.set(Calendar.SECOND, 0);
		c.add(Calendar.MINUTE, 1);

		long next = -1;
		String[] info = reminderInfo.split(",");

		for (String i : info) {
			try {
				long l = Long.parseLong(i);
				if (l < 3600 * 24 * 365) l = dueDate - l;

				if (l < c.getTimeInMillis() / 1000) continue;

				if (next > l || next == -1) next = l;
			} catch (NumberFormatException e) {
			}
		}

		// Sets the next reminder to due date if it is closer than the closest
		// reminder
		if (dueDate < next) if (dueDate > c.getTimeInMillis() / 1000) next = dueDate;

		return next;
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
