package com.todo.code3.view;

import java.util.ArrayList;
import java.util.Random;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.util.Log;
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
import com.todo.code3.xml.TaskItem;

public class TaskView extends ContentView {

	private ListView listView;
	private TextView empty;

	private ArrayList<TaskItem> taskItems;

	int currentFolder = -1;
	int currentChecklist = -1;

	public TaskView(MainActivity activity, int currentFolder, int currentChecklist) {
		super(activity);
		this.currentFolder = currentFolder;
		this.currentChecklist = currentChecklist;
	}

	protected void init() {
		View v = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.task_view, null);
		LayoutParams params = new LayoutParams(activity.getContentWidth(), activity.getContentHeight());
		v.setLayoutParams(params);
		addView(v);

		taskItems = new ArrayList<TaskItem>();

		listView = (ListView) v.findViewById(R.id.listview);
		TaskAdapter adapter = new TaskAdapter(activity, this);

		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				try {
					JSONObject folder = new JSONObject(activity.getData().getString(App.FOLDER + currentFolder));
					JSONObject task;
					if (currentChecklist != -1) {
						JSONObject checklist = new JSONObject(folder.getString(App.CHECKLIST + currentChecklist));
						task = new JSONObject(checklist.getString(App.TASK + taskItems.get(view.getId()).getId()));
					} else {
						task = new JSONObject(folder.getString(App.TASK + taskItems.get(view.getId()).getId()));
					}
					
					activity.openTask(task);
					// JSONObject task = new
					// JSONObject(checklist.getString(App.TASK +
					// currentChecklist));

					// vchecklistItems.get(view.getId()).getId())
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

			for (int i = 0; i < parent.getInt(App.NUM_CHILDREN); i++) {
				if (parent.has(App.TASK + i)) {
					JSONObject task = new JSONObject(parent.getString(App.TASK + i));

					TaskItem ti = new TaskItem();
					ti.setTitle(task.getString(App.NAME));
					ti.setEnabled(true);
					ti.setId(task.getInt(App.ID));
					ti.setFolderId(currentFolder);

					if (isChecklistChild) ti.setChecklistId(parent.getInt(App.ID));
					// redundant
					// else ti.isChecklistChild(isChecklistChild);

					if (task.has(App.COMPLETED) && task.getBoolean(App.COMPLETED)) ti.completed(true);
					else ti.completed(false);

					taskItems.add(ti);
				}
			}

			((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		if(taskItems.size() == 0) {
			empty.setVisibility(View.VISIBLE);
		} else {
			empty.setVisibility(View.GONE);
		}
	}

	public ArrayList<TaskItem> getTaskItems() {
		return taskItems;
	}

	public void leave() {
		
	}

//	public void setFolderAndChecklist(int folder, int checklist) {
//		currentFolder = folder;
//		currentChecklist = checklist;
//	}

}

/*
 * FOR CHECKLIST VIEW public void onItemClick(AdapterView<?> parent, View view,
 * int position, long id) { if (folderContentType.equals(App.CHECKLIST)) { try {
 * JSONObject folder = new JSONObject(data.getString(App.FOLDER +
 * currentFolder)); JSONObject checklist = new
 * JSONObject(folder.getString(App.CHECKLIST +
 * contentItems.get(view.getId()).getId())); if
 * (contentItems.get(view.getId()).isEnabled()) openChecklist(checklist);
 * 
 * } catch (JSONException e) { e.printStackTrace(); } } }
 */
