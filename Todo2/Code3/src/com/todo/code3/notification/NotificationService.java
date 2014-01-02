package com.todo.code3.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import com.todo.code3.MainActivity;
import com.todo.code3.R;

public class NotificationService extends Service{

	public IBinder onBind(Intent arg0) {
		return null;
	}
}
