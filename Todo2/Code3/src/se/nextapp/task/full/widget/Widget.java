package se.nextapp.task.full.widget;

import se.nextapp.task.full.R;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.Toast;

public class Widget extends AppWidgetProvider{
	
	public static final String asd = "asd";

	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		
		ComponentName widget = new ComponentName(context, Widget.class);
		int[] allWidgetIds = appWidgetManager.getAppWidgetIds(widget);
		for(int id : allWidgetIds) {			
			RemoteViews v = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
			
			Intent intent = new Intent(context, Widget.class);
			intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
			intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
			
			PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
			v.setOnClickPendingIntent(R.id.button, pendingIntent);
			appWidgetManager.updateAppWidget(id, v);
		}
	}
	
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
		
		if(intent.getAction().equals(asd)) {
			Toast.makeText(context, "Pressed the button", Toast.LENGTH_LONG).show();
		}
	}
}
