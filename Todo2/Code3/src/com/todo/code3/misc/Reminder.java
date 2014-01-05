package com.todo.code3.misc;

import java.util.Calendar;

public class Reminder {
	public static final String WEEKLY = "a";
//	public static final String EVERY_TWO_WEEKS = "b";
	
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

	public static long getNext(String reminderInfo) {
		Calendar c = Calendar.getInstance();

		if (getType(reminderInfo).equals(Reminder.WEEKLY)) {
			int days = Integer.parseInt(getPart(reminderInfo, 1));
			int hour = Integer.parseInt(getPart(reminderInfo, 2));
			int minute = Integer.parseInt(getPart(reminderInfo, 3));

			int count = 0;
			while (count < 2000) {
				if (existsIn(c.get(Calendar.DAY_OF_WEEK), days)) {
					c.set(Calendar.HOUR_OF_DAY, hour);
					c.set(Calendar.MINUTE, minute);
					c.set(Calendar.SECOND, 0);

					// Return only if the time is in the future
					if (System.currentTimeMillis() < c.getTimeInMillis()) //
					return c.getTimeInMillis() / 1000;
				}

				c.add(Calendar.DAY_OF_MONTH, 1);
				count++;
			}
			
			return -1;
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

	public static String getReminderInfoString(String type, int days, int hour, int minute) {
		if (type.equals(Reminder.WEEKLY)) {
			String s = type + "," + days + "," + hour + "," + minute;
			return s;
		}

		return "";
	}

	public static String getPart(String reminderInfo, int part) {
		String[] s = reminderInfo.split(",");
		if (s.length > part) return s[part];
		return "";
	}

	public static String getType(String info) {
		return info.split(",")[0];
	}
}
