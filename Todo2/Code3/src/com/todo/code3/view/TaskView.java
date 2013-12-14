package com.todo.code3.view;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.Transformation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.todo.code3.MainActivity;
import com.todo.code3.R;
import com.todo.code3.adapter.TaskAdapter;
import com.todo.code3.misc.App;
import com.todo.code3.xml.DynamicListView;
import com.todo.code3.xml.TaskItem;

public class TaskView extends ContentView {

	private ListView listView;
	private TextView empty;

	private boolean hasDynamicListView;

	private ArrayList<TaskItem> taskItems;

	private TaskAdapter adapter;

	private int listViewItemHeight;
	private int expandingItemId = -1;

	public TaskView(MainActivity activity, int parentId, String parentType) {
		super(activity);
		this.parentId = parentId;
		this.parentType = parentType;
	}

	protected void init() {
		hasDynamicListView = activity.getSDKVersion() >= App.MIN_API_LEVEL_FOR_DRAGGABLE_LIST_VIEW_ITEMS;

		View v;
		if (hasDynamicListView) v = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.task_view_dynamic, null);
		else v = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.task_view, null);

		LayoutParams params = new LayoutParams(activity.getContentWidth(), activity.getContentHeight());
		v.setLayoutParams(params);
		addView(v);

		taskItems = new ArrayList<TaskItem>();

		listView = (ListView) v.findViewById(R.id.listview);

		adapter = new TaskAdapter(activity, this);
		listView.setAdapter(adapter);

		if (hasDynamicListView) ((DynamicListView) listView).setContentView(this);

		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (activity.isMoving()) return;

				try {
					JSONObject task = new JSONObject(activity.getData().getString(App.TASK + view.getId()));

					activity.open(task.getInt(App.ID), App.TASK);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});

		empty = (TextView) findViewById(R.id.empty);
		empty.setText("This checklist is empty. Tap the + in the upper right corner to add a new task");

		listViewItemHeight = (int) activity.getResources().getDimension(R.dimen.item_height);
	}

	public void update(JSONObject data) {
		try {
			taskItems.clear();

			JSONObject parent = new JSONObject(data.getString(parentType + parentId));

			String childrenIds[];

			if (parent.has(App.CHILDREN_IDS)) childrenIds = parent.getString(App.CHILDREN_IDS).split(",");
			else childrenIds = new String[0];

			for (int i = 0; i < childrenIds.length; i++) {
				String id = childrenIds[i];
				if (data.has(App.TASK + id)) {
					JSONObject task = new JSONObject(data.getString(App.TASK + id));

					TaskItem ti = new TaskItem();
					ti.setTitle(task.getString(App.NAME));
					ti.setEnabled(true);
					ti.setId(task.getInt(App.ID));
					ti.setParentId(parentId);
					ti.setParentType(parentType);
					if (task.has(App.TIMESTAMP_CREATED)) ti.setTimestampCreated(task.getInt(App.TIMESTAMP_CREATED));
					if (task.has(App.TIMESTAMP_COMPLETED)) ti.setTimestampChecked(task.getInt(App.TIMESTAMP_COMPLETED));

					if (task.has(App.COMPLETED) && task.getBoolean(App.COMPLETED)) ti.completed(true);
					else ti.completed(false);

					taskItems.add(ti);
				}
			}

			sortTaskItems();

			adapter.notifyDataSetChanged();

			if (hasDynamicListView) ((DynamicListView) listView).setTaskItems(taskItems);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		// if (taskItems.size() == 0) {
		// empty.setVisibility(View.VISIBLE);
		// } else {
		// empty.setVisibility(View.GONE);
		// }
	}

	public void updateContentItemsOrder() {
		String order = "";
		for (int i = 0; i < taskItems.size() - 1; i++) {
			order += taskItems.get(i).getId() + ",";
		}
		order += taskItems.get(taskItems.size() - 1).getId();
		activity.updateChilrenOrder(order, parentId, parentType);
	}

	private void sortTaskItems() {
//		ArrayList<TaskItem> checked = new ArrayList<TaskItem>();
//		ArrayList<TaskItem> unchecked = new ArrayList<TaskItem>();
//
//		for (TaskItem i : taskItems) {
//			if (i.isCompleted()) checked.add(i);
//			else unchecked.add(i);
//		}
//
//		taskItems.clear();
//		taskItems.addAll(unchecked);
//		taskItems.addAll(checked);
	}

	public void collapseView(final View view, final int id) {
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
				if (time == 0) {
					view.getLayoutParams().height = listViewItemHeight;
				} else {
					view.getLayoutParams().height = listViewItemHeight - (int) (listViewItemHeight * time);
					view.requestLayout();
				}
			}

			public boolean willChangeBounds() {
				return true;
			}
		};

		animation.setAnimationListener(al);
		animation.setDuration(App.COLLAPSE_ANIMATION_DURATION);
		view.startAnimation(animation);

		expandingItemId = id;
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
				animation.setDuration(App.EXPAND_ANIMATION_DURATION);
				view.startAnimation(animation);
			}
		}, 0);
	}

	public ArrayList<TaskItem> getTaskItems() {
		return taskItems;
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

	public int getListViewItemHeight() {
		return listViewItemHeight;
	}

	public void leave() {
	}
}