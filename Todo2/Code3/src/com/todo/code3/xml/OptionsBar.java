package com.todo.code3.xml;

import java.util.ArrayList;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.todo.code3.MainActivity;
import com.todo.code3.view.ItemView;

public class OptionsBar extends LinearLayout {

	private MainActivity activity;

	// Holds ids for different options items
	private ArrayList<Integer> items;

	public OptionsBar(Context context) {
		super(context);

		init();
	}

	public OptionsBar(Context context, AttributeSet attrs) {
		super(context, attrs);

		init();
	}

	private void init() {
		items = new ArrayList<Integer>();

		setOrientation(LinearLayout.HORIZONTAL);
	}

	public void setMainActivity(MainActivity a) {
		activity = a;
	}

	private void updateItems() {
		removeAllViews();
		if(items.size() == 0) return;

		// Dividing the size - the size of the separating lines (2 px thick)
		// 1 is added so that the whole width of the options bar is used
		int itemWidth = (activity.getContentWidth() - (items.size() - 1) * 2) / items.size() + 1;

		for (final int id : items) {
			Button b = new Button(getContext());
			b.setLayoutParams(new LayoutParams(itemWidth, LayoutParams.MATCH_PARENT));
			b.setBackgroundColor(0xffffff00);
			b.setText(id + "");
			b.setGravity(Gravity.CENTER);
			b.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					if(activity.isMoving()) return;
					
					if(activity.getOpenContentView() instanceof ItemView) {
						ItemView i = (ItemView) activity.getOpenContentView();
						if(i.isInOptionsMode()) i.performActionOnSelectedItems(id);
					}
				}
			});
			addView(b);

			// The border between the items
			if (items.indexOf(id) != items.size() - 1) {
				LinearLayout l = new LinearLayout(getContext());
				l.setLayoutParams(new LayoutParams(2, LayoutParams.MATCH_PARENT));
				l.setBackgroundColor(0xff730592);
				addView(l);
			}
		}
	}
	
	public void clearOptionsItems() {
		items.clear();
		
		updateItems();
	}

	public void addOptionsItem(int id) {
		if (!items.contains(id)) items.add(id);

		updateItems();
	}

	public void removeOptionsItem(int id) {
		if (items.contains(id)) items.remove(id);

		updateItems();
	}
}
