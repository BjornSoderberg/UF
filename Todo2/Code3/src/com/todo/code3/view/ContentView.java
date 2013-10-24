package com.todo.code3.view;

import org.json.JSONObject;

import android.app.Activity;
import android.widget.LinearLayout;

import com.todo.code3.MainActivity;

public abstract class ContentView extends LinearLayout {
	
	protected MainActivity activity;
	
	public ContentView(MainActivity activity) {
		super(activity);
		this.activity = activity;
		
		init();
	}

	protected abstract void init();
	
	public abstract void update(JSONObject data);
}
