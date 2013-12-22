package com.todo.code3.view;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.Transformation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.todo.code3.MainActivity;
import com.todo.code3.R;
import com.todo.code3.adapter.ChecklistAdapter;
import com.todo.code3.misc.App;
import com.todo.code3.xml.FolderItem;
import com.todo.code3.xml.DynamicListView;

public class ChecklistView extends ContentView {

	private ListView listView;
	private TextView empty;

	private boolean hasDynamicListView;

	private int expandingItemId = -1;
	private int listViewItemHeight;

	protected ArrayList<FolderItem> checklistItems;

	public ChecklistView(MainActivity activity, int parentId) {
		super(activity, parentId);
	}

	protected void init() {
//		hasDynamicListView = activity.getSDKVersion() >= App.MIN_API_LEVEL_FOR_DRAGGABLE_LIST_VIEW_ITEMS;

		View v;
		if (hasDynamicListView) v = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.checklist_view_dynamic, null);
		else v = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.checklist_view, null);

		LayoutParams params = new LayoutParams(activity.getContentWidth(), activity.getContentHeight());
		v.setLayoutParams(params);
		addView(v);

		checklistItems = new ArrayList<FolderItem>();

		listView = (ListView) v.findViewById(R.id.listview);
		ChecklistAdapter adapter = new ChecklistAdapter(activity, this);

//		if (hasDynamicListView) ((DynamicListView) listView).setContentView(this);

		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if(activity.isMoving()) return;
				
				try {
					JSONObject checklist = new JSONObject(activity.getData().getString(view.getId() + ""));
					if(checklist.getString(App.TYPE).equals(App.FOLDER))
					activity.open(checklist.getInt(App.ID));
					
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

			JSONObject folder = new JSONObject(data.getString(parentId + ""));

			String childrenIds[] = folder.getString(App.CHILDREN_IDS).split(",");

			for (int i = 0; i < childrenIds.length; i++) {
				String id = childrenIds[i];
				if (data.has(id)) {
					JSONObject checklist = new JSONObject(data.getString(id));
					if(!checklist.getString(App.TYPE).equals(App.FOLDER)) continue;

					FolderItem ci = new FolderItem();
					ci.setTitle(checklist.getString(App.NAME));
					ci.setParentId(parentId);
					ci.setId(checklist.getInt(App.ID));
					if (checklist.has(App.TIMESTAMP_CREATED)) ci.setTimestampCreated(checklist.getInt(App.TIMESTAMP_CREATED));

					checklistItems.add(ci);
				}
			}

			((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();

//			if (hasDynamicListView) ((DynamicListView) listView).setChecklistItems(checklistItems);
		} catch (JSONException e) {
			e.printStackTrace();
		}
//		if (checklistItems.size() == 0) {
//			empty.setVisibility(View.VISIBLE);
//		} else {
//			empty.setVisibility(View.GONE);
//		}
	}

	public void updateContentItemsOrder() {
		String order = "";
		for (int i = 0; i < checklistItems.size() - 1; i++) {
			order += checklistItems.get(i).getId() + ",";
		}
		order += checklistItems.get(checklistItems.size() - 1).getId();
		activity.updateChildrenOrder(order, parentId);
	}

	public void expandView(final View view) {
		if(view.getLayoutParams() != null) view.getLayoutParams().height = 1;
		else view.setLayoutParams(new ListView.LayoutParams(ListView.LayoutParams.MATCH_PARENT, 1));
		
		new Handler().postDelayed(new Runnable() {
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
				animation.setDuration(App.ANIMATION_DURATION);
				view.startAnimation(animation);
			}
		}, 0);
	}

	public ArrayList<FolderItem> getChecklistItems() {
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
