package se.nextapp.task.free.view;

import org.json.JSONObject;

import se.nextapp.task.free.MainActivity;

import android.app.Activity;
import android.util.Log;
import android.widget.LinearLayout;


public abstract class ContentView extends LinearLayout {
	
	protected MainActivity activity;
	protected int parentId;
	
	public ContentView(MainActivity activity, int parentId) {
		super(activity);
		this.activity = activity;
		this.parentId = parentId;
	}

	protected abstract void init();
	
	public abstract void leave();
	
	public abstract void update(JSONObject data);
	
	public abstract void updateContentItemsOrder();
	
	public abstract void setColors();
	
	public int getParentId() {
		return parentId;
	}
	
	public MainActivity getActivity() {
		return activity;
	}
}
