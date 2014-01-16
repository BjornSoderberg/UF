package com.todo.code3.view.settings;

import org.json.JSONObject;

import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.todo.code3.MainActivity;
import com.todo.code3.R;
import com.todo.code3.misc.App;
import com.todo.code3.view.ContentView;

public class SelectLanguage extends ContentView {

	private ListView listView;
	private Adapter adapter;

	private String[] languages;
	private String[] values;
	
	private String type;

	public SelectLanguage(MainActivity activity, String type) {
		super(activity, 0);
		this.type = type;
		init();
	}

	protected void init() {
		LayoutInflater.from(getContext()).inflate(R.layout.select_language_view, this, true);

		setLayoutParams(new LayoutParams(activity.getContentWidth(), LayoutParams.FILL_PARENT));
		initLanguages();

		adapter = new Adapter();

		listView = (ListView) findViewById(R.id.listview);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if(type.equals(App.SETTINGS_APP_LANGUAGE)) activity.setLocale(values[position]);
				else activity.saveSetting(type, values[position]);
			}
		});

		setColors();
	}
	
	private void initLanguages() {
		if(type.equals(App.SETTINGS_VOICE_RECOGNITION_LANGUAGE)) {
			languages = new String[2];
			languages[0] = "Svenska";
			languages[1] = "English";

			values = new String[2];
			values[0] = "sv_SE";
			values[1] = "en_US";
		} else if(type.equals(App.SETTINGS_APP_LANGUAGE)) {
			languages = new String[2];
			languages[0] = "Svenska";
			languages[1] = "English";

			values = new String[2];
			values[0] = "sv";
			values[1] = "en";
		}
	}

	public void setColors() {
		Resources r = activity.getResources();
		boolean dark = activity.isDarkTheme();
		setBackgroundColor((dark) ? r.getColor(R.color.background_color_dark) : r.getColor(R.color.white));

		listView.setBackgroundColor(0xffff9999);
	}

	public void leave() {

	}

	public void update(JSONObject data) {

	}

	public void updateContentItemsOrder() {

	}

	class Adapter extends BaseAdapter {
		LayoutInflater inflater = LayoutInflater.from(getContext());

		public int getCount() {
			return languages.length;
		}

		public String getItem(int pos) {
			return languages[pos];
		}

		public long getItemId(int pos) {
			return 0;
		}

		public View getView(int pos, View convertView, ViewGroup parent) {
			View view = inflater.inflate(R.layout.list_view_item, null);

			TextView t = (TextView) view.findViewById(R.id.item_text);
			t.setText(languages[pos]);
			t.setTextColor((activity.isDarkTheme()) ? activity.getResources().getColor(R.color.text_color_dark) : activity.getResources().getColor(R.color.text_color_light));

			if (activity.isDarkTheme()) view.setBackgroundDrawable(activity.getResources().getDrawable(R.drawable.item_selector_dark));
			else view.setBackgroundDrawable(activity.getResources().getDrawable(R.drawable.item_selector_light));

			return view;
		}
	}
}
