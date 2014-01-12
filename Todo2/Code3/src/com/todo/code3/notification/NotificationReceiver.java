package com.todo.code3.notification;

import java.util.Calendar;

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

import com.todo.code3.MainActivity;
import com.todo.code3.R;
import com.todo.code3.misc.App;
import com.todo.code3.misc.Reminder;

public class NotificationReceiver extends BroadcastReceiver {

	@SuppressLint("NewApi")
	public void onReceive(Context context, Intent intent) {
		int id = intent.getIntExtra(App.ID, -1);

		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

		String name = intent.getStringExtra(App.NAME);

		String title = (name != null) ? name : "There is something you have to do";
		String text = "This is some further information.";

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
			n = new Notification(R.drawable.ic_launcher, title, System.currentTimeMillis());
			n.setLatestEventInfo(context, title, text, p);
		}
		
		notificationManager.notify(id, n);

		if (intent.hasExtra(Reminder.REMINDER_INFO)) {
			Toast.makeText(context, "set new reminder", Toast.LENGTH_SHORT).show();

			String reminderInfo = intent.getStringExtra(Reminder.REMINDER_INFO);

			long next = Reminder.getNext(reminderInfo, intent.getLongExtra(App.DUE_DATE, -1));
			if(next == -1) return;
			else next *= 1000;

			// Logs the next
			Calendar c = Calendar.getInstance();
			c.setTimeInMillis(next);
			Log.i("next : " + next, c.toString() + "");

			AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
			am.set(AlarmManager.RTC_WAKEUP, next, PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT));
		}
	}
	
	

	// if (intent.hasExtra(App.DUE_DATE) && System.currentTimeMillis() / 1000 <
	// intent.getExtras().getLong(App.DUE_DATE)) {
	// int[] dt = App.getDifferenceBetweenTimestamps(System.currentTimeMillis()
	// / 1000, intent.getExtras().getLong(App.DUE_DATE));
	// if (dt != null) {
	// int years = dt[0];
	// int months = dt[1];
	// int days = dt[2];
	// int hours = dt[3];
	// int minutes = dt[4];
	//
	// if (years > 3) difference = years + " years";
	// else if (years == 0) {
	// if (months > 3) difference = months + " months";
	// else if (months == 0) {
	// if (days == 21) difference = "3 weeks";
	// else if (days == 14) difference = "2 weeks";
	// else if (days == 7) difference = "1 week";
	// else if (days > 0) difference = days + " days";
	// else if (days == 0) {
	// if (hours > 0) difference = hours + " hours";
	// else if (hours == 0) {
	// if (minutes > 0) difference = minutes + " minutes";
	// if (minutes == 0) difference = "now";
	// }
	// }
	// }
	// }
	// }
	// }
}
