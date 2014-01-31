package se.nextapp.task.full.notification;

import java.util.Calendar;

import se.nextapp.task.full.MainActivity;
import se.nextapp.task.full.R;
import se.nextapp.task.full.misc.App;
import se.nextapp.task.full.misc.Reminder;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

public class NotificationReceiver extends BroadcastReceiver {

	@SuppressLint("NewApi")
	public void onReceive(Context context, Intent intent) {
		int id = intent.getIntExtra(App.ID, -1);

		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

		String name = intent.getStringExtra(App.NAME);

		String timeString = getTimeString(intent, context);

		Toast.makeText(context, timeString, Toast.LENGTH_LONG).show();

		String title = (name != null) ? name : "There is something you have to do";
		String tickerText = title;
		String text = "";

		if (timeString != null) {
			tickerText += " ( " + context.getResources().getString(R.string.in) + " " + timeString + " ) ";
			text = App.capitalizeFirstWordInSentences(context.getResources().getString(R.string.in)) + " " + timeString;
		}

		PendingIntent p = PendingIntent.getActivity(context, 0, intent, 0);

		Notification n = null;
		if (Build.VERSION.SDK_INT >= 11) {
			// Starts the app when pressed
			Intent i = new Intent(context, MainActivity.class);
			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			i.putExtra(App.OPEN, id);
			PendingIntent pi = PendingIntent.getActivity(context, id, i, 0);

			Notification.Builder b = new Notification.Builder(context);
			b.setContentTitle(title);
			b.setContentText(text);
			b.setSmallIcon(R.drawable.ic_launcher);
			b.setAutoCancel(true);
			b.setContentIntent(pi);
			n = b.getNotification();
		} else {
			n = new Notification(R.drawable.ic_launcher, tickerText, System.currentTimeMillis());
			n.setLatestEventInfo(context, title, text, p);
		}

		notificationManager.notify(id, n);

		if (intent.hasExtra(Reminder.REMINDER_INFO)) {
			String reminderInfo = intent.getStringExtra(Reminder.REMINDER_INFO);

			long next = Reminder.getNext(reminderInfo, intent.getLongExtra(App.DUE_DATE, -1));
			if (next == -1) return;
			else next *= 1000;

			// Logs the next
			Calendar c = Calendar.getInstance();
			c.setTimeInMillis(next);
			Log.i("next : " + next, c.toString() + "");

			AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
			am.set(AlarmManager.RTC_WAKEUP, next, PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT));
		}
	}

	public static String getTimeString(Intent intent, Context context) {
		if (intent.hasExtra(App.DUE_DATE) && System.currentTimeMillis() / 1000 < intent.getExtras().getLong(App.DUE_DATE)) {
			return getTimeString(intent.getLongExtra(App.DUE_DATE, Long.valueOf(-1)), context);
		}
		return null;
	}

	public static String getTimeString(long dueDate, Context context) {
		if(dueDate == -1) return "";
		
		long dt = dueDate - System.currentTimeMillis() / 1000;

		int year = 3600 * 24 * 365;
		int month = 3600 * 24 * 30;
		int day = 3600 * 24;
		int hour = 3600;
		int minute = 60;

		if (dt >= year * 2) return getTimeString(context, (int) (dt / year + 0.5), R.string.years);
		else if (dt >= year + month) return getTimeString(context, 1, R.string.year, (int) ((dt % year) / month + 0.5), R.string.months);
		else if (dt >= year) return getTimeString(context, (int) (dt / year), R.string.year, 1, R.string.month);
		else if (dt >= month * 2) return getTimeString(context, (int) (dt / month + 0.5), R.string.months);
		else if (dt >= month + day) return getTimeString(context, 1, R.string.month, (int) ((dt % month) / day + 0.5), R.string.days);
		else if (dt >= month) return getTimeString(context, 1, R.string.month, 1, R.string.day);
		else if ((int) (dt / day) == 7) return getTimeString(context, 1, R.string.week);
		else if ((int) (dt / day) == 14) return getTimeString(context, 2, R.string.weeks);
		else if ((int) (dt / day) == 21) return getTimeString(context, 3, R.string.weeks);
		else if (dt >= day * 2) return getTimeString(context, (int) (dt / day + 0.5), R.string.days);
		else if (dt >= day + hour) return getTimeString(context, 1, R.string.day, (int) ((dt % day) / hour + 0.5), R.string.hours);
		else if (dt >= day) return getTimeString(context, 1, R.string.day, 1, R.string.hour);
		else if (dt >= hour * 2) return getTimeString(context, (int) (dt / hour + 0.5), R.string.hours);
		else if (dt >= hour + minute) return getTimeString(context, 1, R.string.hour, (int) ((dt % hour) / minute + 0.5), R.string.minutes);
		else if (dt >= hour) return getTimeString(context, 1, R.string.hour, 1, R.string.minute);
		else if (dt >= minute) return getTimeString(context, (int) (dt / minute + 0.5), R.string.minutes);
		else return context.getResources().getString(R.string.now);
	}

	private static String getTimeString(Context context, int va, int sa) {
		return va + " " + context.getResources().getString(sa);
	}

	private static String getTimeString(Context c, int va, int sa, int vb, int sb) {
		return getTimeString(c, va, sa) + " " + c.getResources().getString(R.string.and) + " " + getTimeString(c, vb, sb);
	}
}
