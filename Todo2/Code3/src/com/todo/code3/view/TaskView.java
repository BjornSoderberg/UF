package com.todo.code3.view;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.todo.code3.MainActivity;
import com.todo.code3.R;
import com.todo.code3.adapter.TaskAdapter;
import com.todo.code3.misc.App;
import com.todo.code3.xml.ContentItem;
import com.todo.code3.xml.DynamicListView;
import com.todo.code3.xml.TaskItem;

public class TaskView extends ContentView {

	private ListView listView;
	private TextView empty;

	private boolean hasDynamicListView;

	private ArrayList<TaskItem> taskItems;

	int currentFolder = -1;
	int currentChecklist = -1;

	public TaskView(MainActivity activity, int currentFolder, int currentChecklist) {
		super(activity);
		this.currentFolder = currentFolder;
		this.currentChecklist = currentChecklist;
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

		TaskAdapter adapter = new TaskAdapter(activity, this);
		listView.setAdapter(adapter);

		if (hasDynamicListView) ((DynamicListView) listView).setContentView(this);

		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				try {
					JSONObject folder = new JSONObject(activity.getData().getString(App.FOLDER + currentFolder));
					JSONObject task;
					if (currentChecklist != -1) {
						JSONObject checklist = new JSONObject(folder.getString(App.CHECKLIST + currentChecklist));

						task = new JSONObject(checklist.getString(App.TASK + view.getId()));
					} else {
						task = new JSONObject(folder.getString(App.TASK + view.getId()));
					}

					activity.openTask(task);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});

		empty = (TextView) findViewById(R.id.empty);
		empty.setText("This checklist is empty. Tap the + in the upper right corner to add a new task");
	}

	public void update(JSONObject data) {
		try {
			taskItems.clear();

			boolean isChecklistChild = false;

			JSONObject parent = new JSONObject(data.getString(App.FOLDER + currentFolder));
			if (parent.getString(App.CONTENT_TYPE).equals(App.CHECKLIST) && currentChecklist != -1) {
				parent = new JSONObject(parent.getString(App.CHECKLIST + currentChecklist));
				isChecklistChild = true;
			}

			String childrenIds[] = parent.getString(App.CHILDREN_IDS).split(",");

			for (int i = 0; i < childrenIds.length; i++) {
				String id = childrenIds[i];
				if (parent.has(App.TASK + id)) {
					JSONObject task = new JSONObject(parent.getString(App.TASK + id));

					TaskItem ti = new TaskItem();
					ti.setTitle(task.getString(App.NAME));
					ti.setEnabled(true);
					ti.setId(task.getInt(App.ID));
					ti.setFolderId(currentFolder);

					if (isChecklistChild) ti.setChecklistId(parent.getInt(App.ID));

					if (task.has(App.COMPLETED) && task.getBoolean(App.COMPLETED)) ti.completed(true);
					else ti.completed(false);

					taskItems.add(ti);
				}
			}

			sortTaskItems();

			((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();

			if (hasDynamicListView) ((DynamicListView) listView).setTaskItems(taskItems);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		if (taskItems.size() == 0) {
			empty.setVisibility(View.VISIBLE);
		} else {
			empty.setVisibility(View.GONE);
		}
	}

	public void updateContentItemsOrder() {
		String order = "";
		for (int i = 0; i < taskItems.size() - 1; i++) {
			order += taskItems.get(i).getId() + ",";
		}
		order += taskItems.get(taskItems.size() - 1).getId();
		activity.updateChilrenOrder(order, currentChecklist, currentFolder);
	}

	private void sortTaskItems() {
		ArrayList<TaskItem> checked = new ArrayList<TaskItem>();
		ArrayList<TaskItem> unchecked = new ArrayList<TaskItem>();

		for (TaskItem i : taskItems) {
			if (i.isCompleted()) checked.add(i);
			else unchecked.add(i);
		}

		taskItems.clear();
		taskItems.addAll(unchecked);
		taskItems.addAll(checked);
	}

	public ArrayList<TaskItem> getTaskItems() {
		return taskItems;
	}

	public void leave() {

	}

}