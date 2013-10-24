package com.todo.code3.view;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.todo.code3.MainActivity;
import com.todo.code3.R;
import com.todo.code3.adapter.TaskAdapter;
import com.todo.code3.misc.App;
import com.todo.code3.xml.TaskItem;

public class TaskView extends ContentView {

	private ListView listView;

	private ArrayList<TaskItem> taskItems;

	int currentFolder = -1;
	int currentChecklist = -1;

	public TaskView(MainActivity activity, int currentFolder,
			int currentChecklist) {
		super(activity);
		this.currentFolder = currentFolder;
		this.currentChecklist = currentChecklist;
	}

	protected void init() {
		View v = ((Activity) getContext()).getLayoutInflater().inflate(
				R.layout.task_view, null);
		LayoutParams params = new LayoutParams(activity.getContentWidth(),
				activity.getContentHeight());
		v.setLayoutParams(params);
		addView(v);

		taskItems = new ArrayList<TaskItem>();

		listView = (ListView) v.findViewById(R.id.listview);
		TaskAdapter adapter = new TaskAdapter(activity, this);

		listView.setAdapter(adapter);
	}

	public void update(JSONObject data) {
		try {
			taskItems.clear();
			
			JSONObject parent = new JSONObject(data.getString(App.FOLDER
					+ currentFolder));
			if (parent.getString(App.CONTENT_TYPE).equals(App.CHECKLIST) && currentChecklist != -1) {
				parent = new JSONObject(parent.getString(App.CHECKLIST
						+ currentChecklist));
			}

			for (int i = 0; i < parent.getInt(App.NUM_CHILDREN); i++) {
				if (parent.has(App.TASK + i)) {
					JSONObject task = new JSONObject(parent.getString(App.TASK
							+ i));

					TaskItem ti = new TaskItem();
					ti.setTitle(task.getString(App.NAME));
					ti.setEnabled(true);
					ti.setId(task.getInt(App.ID));
					ti.isChecklistChild(false);

					if (task.has(App.COMPLETED)
							&& task.getBoolean(App.COMPLETED))
						ti.completed(true);
					else {
						ti.completed(false);
					}

					taskItems.add(ti);
				}
			}
			
			((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public ArrayList<TaskItem> getTaskItems() {
		return taskItems;
	}
	
	public void setCurrentFolderAndChecklist(int folder, int checklist)  {
		currentFolder = folder;
		currentChecklist = checklist;
	}

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
