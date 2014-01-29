package com.todo.code3.view.settings;

import java.util.Arrays;

import org.json.JSONObject;

import android.content.res.Resources;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.ToggleButton;

import com.todo.code3.MainActivity;
import com.todo.code3.R;
import com.todo.code3.misc.App;
import com.todo.code3.misc.Sort;
import com.todo.code3.view.ContentView;

public class SettingsView extends ContentView {

	public static final int SELECT_VOICE_RECOGNITION = 0;
	public static final int SELECT_APP_LANGUAGE = 1;
	public static final int SEND_FEEDBACK = 2;

	public SettingsView(MainActivity activity) {
		super(activity, 0);
		init();
	}

	protected void init() {
		LayoutInflater.from(activity).inflate(R.layout.settings, this, true);

		setLayoutParams(new LayoutParams(activity.getContentWidth(), LayoutParams.FILL_PARENT));

		final ToggleButton b = (ToggleButton) findViewById(R.id.theme);
		b.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (b.isChecked()) activity.changeTheme(App.SETTINGS_THEME_LIGHT);
				else activity.changeTheme(App.SETTINGS_THEME_DARK);
			}
		});

		final ToggleButton bb = (ToggleButton) findViewById(R.id.clock);
		bb.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				activity.saveSetting(App.SETTINGS_24_HOUR_CLOCK, bb.isChecked());
			}
		});

		b.setChecked(!activity.isDarkTheme());
		bb.setChecked(activity.is24HourMode());

		Button asdf = (Button) findViewById(R.id.textLang);
		asdf.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				activity.openSettingsItem(SELECT_APP_LANGUAGE, getContext().getResources().getString(R.string.set_language));
			}
		});

		((Button) findViewById(R.id.feedback)).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				activity.openSettingsItem(SEND_FEEDBACK, getContext().getResources().getString(R.string.send_feedback));
			}
		});

		Resources r = getResources();
		final String[] paths = { r.getString(R.string.prioritized), //
				r.getString(R.string.created), //
				r.getString(R.string.completed), //
				r.getString(R.string.alphabetically),//
				r.getString(R.string.due_date), //
				r.getString(R.string.nothing) //
		};
		final int[] values = { Sort.SORT_PRIORITIZED,//
				Sort.SORT_TIMESTAMP_CREATED,//
				Sort.SORT_COMPLETED,//
				Sort.SORT_ALPHABETICALLY,//
				Sort.SORT_DUE_DATE, //
				Sort.SORT_NOTHING //
		};
		
		int i = 0;
		for(int j = 0; j < values.length; j++) 
			if(values[j] == activity.getSortType()) i = j;

		Log.i(Arrays.toString(values), i + " is selected ... " + activity.getSortType());
		
		final Spinner spinner = (Spinner) findViewById(R.id.sortSpinner);
		
		int layoutId = activity.isDarkTheme() ? R.layout.drop_down_item_dark : R.layout.drop_down_item_light;
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(activity, layoutId, R.id.item_text, paths);
		spinner.setAdapter(adapter);
		
		spinner.setSelection(i);
		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				int pos = spinner.getSelectedItemPosition();

				if (pos < values.length) activity.saveSetting(App.SETTINGS_SORT_TYPE, values[pos]);
				
				Log.i("set sort", values[pos] + "   " + activity.getSortType());
			}

			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});

		int colorId = activity.isDarkTheme() ? R.color.dark : R.color.white;
		if (Build.VERSION.SDK_INT < 11) spinner.setBackgroundColor(activity.getResources().getColor(colorId));

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
