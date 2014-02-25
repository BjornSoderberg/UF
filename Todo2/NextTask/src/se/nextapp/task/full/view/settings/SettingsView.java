package se.nextapp.task.full.view.settings;

import org.json.JSONObject;

import se.nextapp.task.full.MainActivity;
import se.nextapp.task.full.R;
import se.nextapp.task.full.misc.App;
import se.nextapp.task.full.misc.Sort;
import se.nextapp.task.full.view.ContentView;
import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

public class SettingsView extends ContentView {

	public static final int SETTINGS = -10;
	public static final int SELECT_APP_LANGUAGE = -11;
	public static final int SEND_FEEDBACK = -12;

	private ToggleButton theme, timeMode;

	private Spinner sortSpinner;
	private ArrayAdapter<String> sortAdapter;

	private String[] sortPaths = { getResources().getString(R.string.prioritized), //
			getResources().getString(R.string.created), //
			getResources().getString(R.string.completed), //
			getResources().getString(R.string.alphabetically),//
			getResources().getString(R.string.due_date), //
			getResources().getString(R.string.nothing) //
	};
	private int[] sortValues = { Sort.SORT_PRIORITIZED,//
			Sort.SORT_TIMESTAMP_CREATED,//
			Sort.SORT_COMPLETED,//
			Sort.SORT_ALPHABETICALLY,//
			Sort.SORT_DUE_DATE, //
			Sort.SORT_NOTHING //
	};

	public SettingsView(MainActivity activity) {
		super(activity, SETTINGS);
		init();
	}

	protected void init() {
		super.init();
		
		LayoutInflater.from(activity).inflate(R.layout.settings, this, true);
		setLayoutParams(new LayoutParams(activity.getContentWidth(), LayoutParams.FILL_PARENT));

		theme = (ToggleButton) findViewById(R.id.theme);
		theme.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (theme.isChecked()) activity.changeTheme(App.SETTINGS_THEME_LIGHT);
				else activity.changeTheme(App.SETTINGS_THEME_DARK);
			}
		});

		timeMode = (ToggleButton) findViewById(R.id.clock);
		timeMode.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				activity.saveSetting(App.SETTINGS_24_HOUR_CLOCK, timeMode.isChecked());
				update(null);
			}
		});

		theme.setChecked(!activity.isDarkTheme());
		theme.setText(null);
		theme.setTextOn(null);
		theme.setTextOff(null);

		timeMode.setChecked(activity.is24HourMode());
		timeMode.setText(null);
		timeMode.setTextOn(null);
		timeMode.setTextOff(null);

		LinearLayout textLang = (LinearLayout) findViewById(R.id.textLang);
		textLang.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				activity.openSettingsItem(SELECT_APP_LANGUAGE, getContext().getResources().getString(R.string.set_language));
			}
		});

		((LinearLayout) findViewById(R.id.feedback)).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				activity.openSettingsItem(SEND_FEEDBACK, getContext().getResources().getString(R.string.send_feedback));
			}
		});

		int i = 0;
		for (int j = 0; j < sortValues.length; j++)
			if (sortValues[j] == activity.getSortType()) i = j;

		sortSpinner = (Spinner) findViewById(R.id.sortSpinner);

		int layoutId = activity.isDarkTheme() ? R.layout.drop_down_item_dark : R.layout.drop_down_item_light;
		sortAdapter = new ArrayAdapter<String>(activity, layoutId, R.id.item_text, sortPaths);
		sortSpinner.setAdapter(sortAdapter);

		sortSpinner.setSelection(i);
		sortSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				int pos = sortSpinner.getSelectedItemPosition();

				if (pos < sortValues.length) activity.saveSetting(App.SETTINGS_SORT_TYPE, sortValues[pos]);
			}

			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
		
		((TextView) findViewById(R.id.help)).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				activity.startTutorial();
			}
		});
		
		Drawable d = getResources().getDrawable(R.drawable.ic_right_alt).mutate();
		d.setColorFilter(new PorterDuffColorFilter(getResources().getColor(R.color.icon_color), PorterDuff.Mode.MULTIPLY));
		((ImageView)findViewById(R.id.icon_expand1)).setBackgroundDrawable(d);
		((ImageView)findViewById(R.id.icon_expand2)).setBackgroundDrawable(d);
		
		setColors();
	}

	@SuppressLint("NewApi")
	public void setColors() {
		Resources r = activity.getResources();
		boolean dark = activity.isDarkTheme();
		setBackgroundColor((dark) ? r.getColor(R.color.background_color_dark) : r.getColor(R.color.white));

		sortSpinner.setBackgroundColor(activity.getResources().getColor(dark ? R.color.dark : R.color.light));

		theme.setBackgroundResource(activity.isDarkTheme() ? R.drawable.theme_dark : R.drawable.theme_light);
		timeMode.setBackgroundResource(activity.is24HourMode() ? R.drawable.time_24h : R.drawable.time_12h);

		((LinearLayout) findViewById(R.id.textLang)).setBackgroundDrawable(getResources().getDrawable(dark ? R.drawable.item_selector_dark2 : R.drawable.item_selector_white));
		((LinearLayout) findViewById(R.id.feedback)).setBackgroundDrawable(getResources().getDrawable(dark ? R.drawable.item_selector_dark2 : R.drawable.item_selector_white));
	
		((TextView) findViewById(R.id.tv1)).setTextColor(getResources().getColor(dark ? R.color.text_color_dark : R.color.text_color_light));
		((TextView) findViewById(R.id.tv2)).setTextColor(getResources().getColor(dark ? R.color.text_color_dark : R.color.text_color_light));
		((TextView) findViewById(R.id.tv3)).setTextColor(getResources().getColor(dark ? R.color.text_color_dark : R.color.text_color_light));
		((TextView) findViewById(R.id.tv4)).setTextColor(getResources().getColor(dark ? R.color.text_color_dark : R.color.text_color_light));
		((TextView) findViewById(R.id.tv5)).setTextColor(getResources().getColor(dark ? R.color.text_color_dark : R.color.text_color_light));
		
		((TextView) findViewById(R.id.help)).setTextColor(getResources().getColor(dark ? R.color.text_color_dark : R.color.text_color_light));
		((TextView) findViewById(R.id.help)).setBackgroundDrawable(getResources().getDrawable(dark ? R.drawable.item_selector_dark2 : R.drawable.item_selector_white));
	}

	public void leave() {

	}

	public void update(JSONObject data) {
		setColors();
	}

	public void updateContentItemsOrder() {

	}
}
