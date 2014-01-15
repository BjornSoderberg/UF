package com.todo.code3.view;

import org.json.JSONObject;

import android.content.res.Resources;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ToggleButton;

import com.todo.code3.MainActivity;
import com.todo.code3.R;
import com.todo.code3.misc.App;

public class SettingsView extends ContentView {

	public SettingsView(MainActivity activity) {
		super(activity, 0);
	}

	protected void init() {
		LayoutInflater.from(activity).inflate(R.layout.settings, this, true);

		setLayoutParams(new LayoutParams(activity.getContentWidth(), LayoutParams.FILL_PARENT));

		final ToggleButton b = (ToggleButton) findViewById(R.id.theme);
		b.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if(b.isChecked()) activity.changeTheme(App.SETTINGS_THEME_LIGHT);
				else activity.changeTheme(App.SETTINGS_THEME_DARK);
			}
		});
		
		if(activity.isDarkTheme()) b.setChecked(false);
		else b.setChecked(true);
		
		setColors();
	}
	
	public void setColors() {
		Resources r = activity.getResources();
		boolean dark = activity.isDarkTheme();
		setBackgroundColor((dark) ? r.getColor(R.color.background_color_dark) : r.getColor(R.color.white));
	}

	public void leave() {

	}

	public void update(JSONObject data) {

	}

	public void updateContentItemsOrder() {

	}
}
