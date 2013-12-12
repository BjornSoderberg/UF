package com.todo.code3.view;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.view.animation.Animation.AnimationListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.todo.code3.MainActivity;
import com.todo.code3.R;
import com.todo.code3.adapter.ProjectAdapter;
import com.todo.code3.misc.App;
import com.todo.code3.xml.ChecklistItem;
import com.todo.code3.xml.DynamicListView;

public class ProjectView extends ContentView {

	private ListView listView;
	private LinearLayout stats;
	private TextView empty;

	private boolean hasDynamicListView;

	// the same as currentFolder
	protected int currentProject = -1;
	
	private int expandingItemId = -1;
	private int listViewItemHeight;

	protected ArrayList<ChecklistItem> checklistItems;

	public ProjectView(MainActivity activity, int currentProject) {
		// super(activity, currentProject);
		super(activity);
		this.currentProject = currentProject;
	}

	protected void init() {
		hasDynamicListView = activity.getSDKVersion() >= App.MIN_API_LEVEL_FOR_DRAGGABLE_LIST_VIEW_ITEMS;

		View v;

		if (hasDynamicListView) v = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.project_view_dynamic, null);
		else v = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.project_view, null);

		LayoutParams params = new LayoutParams(activity.getContentWidth(), activity.getContentHeight());
		v.setLayoutParams(params);
		addView(v);

		checklistItems = new ArrayList<ChecklistItem>();

		stats = (LinearLayout) v.findViewById(R.id.stats);

		listView = (ListView) v.findViewById(R.id.listview);
		ProjectAdapter adapter = new ProjectAdapter(activity, this);
		listView.setAdapter(adapter);

		if (hasDynamicListView) ((DynamicListView) listView).setContentView(this);

		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if(activity.isMoving()) return;
				
				try {
					JSONObject project = new JSONObject(activity.getData().getString(App.FOLDER + currentProject));
					JSONObject checklist = new JSONObject(project.getString(App.CHECKLIST + view.getId()));

					activity.openChecklist(checklist);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});

		LayoutParams l = (LayoutParams) stats.getLayoutParams();
		l.height = (int) (activity.getContentHeight() * 0.30);
		stats.setLayoutParams(l);
		LayoutParams s = (LayoutParams) listView.getLayoutParams();
		s.height = (int) (activity.getContentHeight() * 0.70);
		listView.setLayoutParams(s);

		empty = (TextView) findViewById(R.id.empty);
		empty.setText("This project is empty. Tap the + in the upper right corner to add a new checklist.");
		
		listViewItemHeight = (int) activity.getResources().getDimension(R.dimen.item_height);
	}

	public void update(JSONObject data) {
		Log.i("asdasd", "asdasd update project");

		try {
			checklistItems.clear();

			JSONObject project = new JSONObject(data.getString(App.FOLDER + currentProject));

			String childrenIds[] = project.getString(App.CHILDREN_IDS).split(",");

			for (int i = 0; i < childrenIds.length; i++) {
				String id = childrenIds[i];
				if (project.has(App.CHECKLIST + id)) {
					JSONObject checklist = new JSONObject(project.getString(App.CHECKLIST + id));

					ChecklistItem ci = new ChecklistItem();
					ci.setTitle(checklist.getString(App.NAME));
					ci.setFolderId(currentProject);
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
//			empty.setVisibility(View.VISIBLE);
			listView.setVisibility(View.GONE);
		} else {
//			empty.setVisibility(View.GONE);
			listView.setVisibility(View.VISIBLE);
		}
	}

	public void updateContentItemsOrder() {
		String order = "";
		for (int i = 0; i < checklistItems.size() - 1; i++) {
			order += checklistItems.get(i).getId() + ",";
		}
		order += checklistItems.get(checklistItems.size() - 1).getId();
		activity.updateChilrenOrder(order, -1, currentProject);
	}
	
	public void expandView(final View view) {
		Runnable expandRunnable = new Runnable() {
			public void run() {
				if(view.getLayoutParams() !=null)view.getLayoutParams().height = 1;
				
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
						if((int) (listViewItemHeight * time) != 0) {
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
