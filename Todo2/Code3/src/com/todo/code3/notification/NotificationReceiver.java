package com.todo.code3.notification;

import java.util.Arrays;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.todo.code3.R;
import com.todo.code3.misc.App;

public class NotificationReceiver extends BroadcastReceiver {

	public void onReceive(Context context, Intent intent) {
		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

		String difference = "";

		if (intent.hasExtra(App.DUE_DATE) && System.currentTimeMillis() / 1000 < intent.getExtras().getLong(App.DUE_DATE)) {
			int[] dt = App.getDifferenceBetweenTimestamps(System.currentTimeMillis() / 1000, intent.getExtras().getLong(App.DUE_DATE));
			if (dt != null) {				
				int years = dt[0];
				int months = dt[1];
				int days = dt[2];
				int hours = dt[3];
				int minutes = dt[4];

				if (years > 3) difference = years + " years";
				else if (years == 0) {
					if (months > 3) difference = months + " months";
					else if (months == 0) {
						if (days == 21) difference = "3 weeks";
						else if (days == 14) difference = "2 weeks";
						else if (days == 7) difference = "1 week";
						else if (days > 0) difference = days + " days";
						else if (days == 0) {
							if (hours > 0) difference = hours + " hours";
							else if (hours == 0) {
								if (minutes > 0) difference = minutes + " minutes";
								if (minutes == 0) difference = "now";
							}
						}
					}
				}
			}
		}
		
		String name = intent.getStringExtra(App.NAME);

		String tickerText = (name != null) ? name : "There is something you have to do";
		
		if(difference != "") {
			tickerText += " (in " + difference + ")";
		}

		String title = (name != null) ? name : "There is something you have to do";

		String text = intent.getExtras().getLong(App.DUE_DATE, -1) + "";

		PendingIntent p = PendingIntent.getActivity(context, 0, intent, 0);
		Notification n = new Notification(R.drawable.ic_launcher, tickerText, System.currentTimeMillis());
		n.setLatestEventInfo(context, title, text, p);

		notificationManager.notify(intent.getExtras().getInt(App.ID, -1), n);
	}
}
