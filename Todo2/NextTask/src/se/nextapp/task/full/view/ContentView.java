package se.nextapp.task.full.view;

import org.json.JSONObject;

import se.nextapp.task.full.MainActivity;

import android.app.Activity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;

public abstract class ContentView extends LinearLayout {

	protected MainActivity activity;
	protected int parentId;

	public ContentView(MainActivity activity, int parentId) {
		super(activity);
		this.activity = activity;
		this.parentId = parentId;
	}

	protected void init() {
		// Probably solves the swipe bug
		setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
			}
		});
	}

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
