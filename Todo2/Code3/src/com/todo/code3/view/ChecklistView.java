package com.todo.code3.view;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.view.animation.Animation.AnimationListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.todo.code3.MainActivity;
import com.todo.code3.R;
import com.todo.code3.adapter.ChecklistAdapter;
import com.todo.code3.misc.App;
import com.todo.code3.xml.ChecklistItem;
import com.todo.code3.xml.DynamicListView;

public class ChecklistView extends ContentView {

	private ListView listView;
	private TextView empty;

	private boolean hasDynamicListView;

	private int currentFolder = -1;

	private int expandingItemId = -1;
	private int listViewItemHeight;

	protected ArrayList<ChecklistItem> checklistItems;

	public ChecklistView(MainActivity activity, int currentFolder) {
		super(activity);
		this.currentFolder = currentFolder;
	}

	protected void init() {
		hasDynamicListView = activity.getSDKVersion() >= App.MIN_API_LEVEL_FOR_DRAGGABLE_LIST_VIEW_ITEMS;

		View v;
		if (hasDynamicListView) v = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.checklist_view_dynamic, null);
		else v = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.checklist_view, null);

		LayoutParams params = new LayoutParams(activity.getContentWidth(), activity.getContentHeight());
		v.setLayoutParams(params);
		addView(v);

		checklistItems = new ArrayList<ChecklistItem>();

		listView = (ListView) v.findViewById(R.id.listview);
		ChecklistAdapter adapter = new ChecklistAdapter(activity, this);

		if (hasDynamicListView) ((DynamicListView) listView).setContentView(this);

		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if(activity.isMoving()) return;
				
				try {
					JSONObject folder = new JSONObject(activity.getData().getString(App.FOLDER + currentFolder));
					JSONObject checklist = new JSONObject(folder.getString(App.CHECKLIST + view.getId()));

					activity.openChecklist(checklist);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});

		empty = (TextView) findViewById(R.id.empty);
		empty.setText("This folder is empty. Tap the + in the upper right corner to add a new checklist");

		listViewItemHeight = (int) activity.getResources().getDimension(R.dimen.item_height);
	}

	public void update(JSONObject data) {
		try {
			checklistItems.clear();

			JSONObject folder = new JSONObject(data.getString(App.FOLDER + currentFolder));

			String childrenIds[] = folder.getString(App.CHILDREN_IDS).split(",");

			for (int i = 0; i < childrenIds.length; i++) {
				String id = childrenIds[i];
				if (folder.has(App.CHECKLIST + id)) {
					JSONObject checklist = new JSONObject(folder.getString(App.CHECKLIST + id));

					ChecklistItem ci = new ChecklistItem();
					ci.setTitle(checklist.getString(App.NAME));
					ci.setFolderId(currentFolder);
					ci.setId(checklist.getInt(App.ID));

					checklistItems.add(ci);
				}
			}

			((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();

			if (hasDynamicListView) ((DynamicListView) listView).setChecklistItems(checklistItems);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		if (checklistItems.size() == 0) {
			empty.setVisibility(View.VISIBLE);
		} else {
			empty.setVisibility(View.GONE);
		}
	}

	public void updateContentItemsOrder() {
		String order = "";
		for (int i = 0; i < checklistItems.size() - 1; i++) {
			order += checklistItems.get(i).getId() + ",";
		}
		order += checklistItems.get(checklistItems.size() - 1).getId();
		activity.updateChilrenOrder(order, -1, currentFolder);
	}

	public void expandView(final View view) {
		Runnable expandRunnable = new Runnable() {
			public void run() {
				if (view.getLayoutParams() != null) view.getLayoutParams().height = 1;

				AnimationListener al = new AnimationListener() {
					public void onAnimationEnd(Animation animation) {
						activity.isMoving(false);
					}

					public void onAnimationRepeat(Animation animation) {
					}

					public void onAnimationStart(Animation animation) {
						activity.isMoving(true);
					}
				};
				
				Animation animation = new Animation() {
					protected void applyTransformation(float time, Transformation t) {
						if ((int) (listViewItemHeight * time) != 0) {
							view.getLayoutParams().height = (int) (listViewItemHeight * time);
						} else {
							view.getLayoutParams().height = 1;
						}
						
						view.requestLayout();
					}
				};
				
				animation.setAnimationListener(al);
				animation.setDuration(App.EXPAND_ANIMATION_DURATION);
				view.startAnimation(animation);
			}
		};
		
		activity.runOnUiThread(expandRunnable);
	}

	public ArrayList<ChecklistItem> getChecklistItems() {
		return checklistItems;
	}

	public void setExpandingItemId(int id) {
		expandingItemId = id;
	}

	public int getExpandingItemId() {
		return expandingItemId;
	}

	public void invalidateExpandingItemId() {
		expandingItemId = -1;
	}

	public void leave() {

	}
}
