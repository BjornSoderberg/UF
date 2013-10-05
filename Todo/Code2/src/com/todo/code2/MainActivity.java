package com.todo.code2;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.todo.code2.misc.App;
import com.todo.code2.misc.OnTouchHandler;
import com.todo.code2.misc.SPEditor;
import com.todo.code2.xml.FlyOutContainer;
import com.todo.code2.xml.TaskItem;

public class MainActivity extends Activity implements OnClickListener {

	private FlyOutContainer root;
	private LinearLayout menu, content, list;
	private TextView nameTV;
	private Button add;

	private SharedPreferences prefs;
	private SPEditor editor;

	private JSONObject data;

	private String currentFolderName = "Inbox";
	private String currentChecklistName = "";
	private String contentFolderType = App.TASK;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		root = (FlyOutContainer) getLayoutInflater().inflate(R.layout.main_activity, null);

		setContentView(root);

		Button b = (Button) findViewById(R.id.dragButton);
		OnTouchHandler touch = new OnTouchHandler(root, b);
		b.setOnTouchListener(touch);

		initXML();

		prefs = getSharedPreferences(App.PREFERENCES_NAME, Context.MODE_PRIVATE);
		editor = new SPEditor(prefs);

		getData();
	}

	private void initXML() {
		menu = (LinearLayout) findViewById(R.id.menu);
		content = (LinearLayout) findViewById(R.id.content);
		list = (LinearLayout) findViewById(R.id.list);

		nameTV = (TextView) findViewById(R.id.name);

		add = (Button) findViewById(R.id.add);
		add.setOnClickListener(this);
	}

	private void getData() {
		try {
			String d = prefs.getString(App.DATA, null);

			if (d == null) {
				data = new JSONObject();
				data.put(App.NUM_FOLDERS, 0);

				addFolder("Inbox", App.TASK, false);

			} else {
				data = new JSONObject(d);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		updateData();
	}

	private void updateData() {
		Log.i("Updating data...", "" + data.toString());

		list.removeAllViews();

		try {
			JSONObject folder = null;
			for (int i = 0; i < data.getInt(App.NUM_FOLDERS); i++) {
				JSONObject o = new JSONObject(data.getString(App.FOLDER + i));

				if (o.getString(App.NAME).equalsIgnoreCase(currentFolderName)) folder = new JSONObject(data.getString(App.FOLDER + i));
			}

			Log.i("Folder data", folder.toString());

			nameTV.setText(folder.getString(App.NAME));

			/*
			 * if (folder.getString(App.CONTENT_TYPE).equals(App.CHECKLIST)) {
			 * for (int i = folder.getInt(App.NUM_CHECKLISTS) - 0; i >= 0; i--)
			 * { if (folder.has(App.CHECKLIST + i)) { location = App.CHECKLIST +
			 * i;} } } else
			 */if (folder.getString(App.CONTENT_TYPE).equals(App.TASK)) {
				for (int i = folder.getInt(App.NUM_TASKS) - 0; i >= 0; i--) {
					if (folder.has(App.TASK + i)) {
						JSONObject itemData = new JSONObject(folder.getString(App.TASK + i));
						if (!itemData.has(App.COMPLETED) || !itemData.getBoolean(App.COMPLETED)) list
								.addView(getTaskItem(itemData.getString(App.NAME), itemData.getInt(App.ID), folder.getInt(App.ID)));
					}
				}
				for (int i = folder.getInt(App.NUM_TASKS) - 0; i >= 0; i--) {
					if (folder.has(App.TASK + i)) {
						JSONObject itemData = new JSONObject(folder.getString(App.TASK + i));
						if (itemData.has(App.COMPLETED)) if (itemData.getBoolean(App.COMPLETED)) list
								.addView(getTaskItem(itemData.getString(App.NAME) + " - done", itemData.getInt(App.ID), folder.getInt(App.ID)));
					}
				}
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}

		if (list.getChildCount() == 0) {
			TextView tv = new TextView(this);
			tv.setText(currentFolderName + " is empty. Click on the button in the upper right corner to add a new " + contentFolderType + ".");

			list.addView(tv);
		}
	}

	private View getChecklist() {
		return null;
	}

	private TaskItem getTaskItem(String name, final int taskId, final int parentId) {
		return getTaskItem(name, taskId, parentId, -1);
	}

	private TaskItem getTaskItem(String name, final int taskId, final int parentId, final int grandParentId) {
		TaskItem ti = new TaskItem(this, name);
		ti.getCheckBox().setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				completeTask(taskId, parentId, grandParentId);
			}
		});
		return ti;
	}

	// public void completeTask(int taskId, int parentId) {
	// completeTask(taskId, parentId, -1);
	// }

	public void completeTask(int taskId, int parentId, int grandParentId) {
		try {
			if (grandParentId != -1) {
				JSONObject grandParent = new JSONObject(data.getString(App.FOLDER + grandParentId));
				JSONObject parent = new JSONObject(grandParent.getString(App.CHECKLIST + parentId));
				JSONObject task = new JSONObject(parent.getString(App.TASK + taskId));
				task.put(App.COMPLETED, true);
				parent.put(App.TASK + taskId, task.toString());
				grandParent.put(App.CHECKLIST + parentId, parent.toString());
				data.put(App.FOLDER + grandParentId, grandParent.toString());
			} else {
				Log.i("Completed task's data: ", "taskId: " + taskId + ", parentId: " + parentId + ", grandParentId: " + grandParentId);
				JSONObject parent = new JSONObject(data.getString(App.FOLDER + parentId));
				JSONObject task = new JSONObject(parent.getString(App.TASK + taskId));
				task.put(App.COMPLETED, true);
				parent.put(App.TASK + taskId, task.toString());
				data.put(App.FOLDER + parentId, parent.toString());
			}

		} catch (JSONException e) {
			e.printStackTrace();
		} finally {
			editor.put(App.DATA, data.toString());
			updateData();
		}
	}

	private void addFolder(String name) {
		addFolder(name, App.CHECKLIST, true);
	}

	private void addFolder(String name, String content, boolean removable) {
		try {
			JSONObject folderData = new JSONObject();
			folderData.put(App.NAME, name);
			folderData.put(App.CONTENT_TYPE, content);
			folderData.put(App.ID, data.getInt(App.NUM_FOLDERS));
			folderData.put(App.REMOVABLE, removable);

			if (content == App.CHECKLIST) folderData.put(App.NUM_CHECKLISTS, 0);
			if (content == App.TASK) folderData.put(App.NUM_TASKS, 0);

			data.put(App.FOLDER + data.getInt(App.NUM_FOLDERS), folderData.toString());
			data.put(App.NUM_FOLDERS, data.getInt(App.NUM_FOLDERS) + 1);

			editor.put(App.DATA, data.toString());

			updateData();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void addTask(String name) {
		try {
			JSONObject taskData = new JSONObject();
			taskData.put(App.NAME, name);

			JSONObject folder = null;

			for (int i = 0; i < data.getInt(App.NUM_FOLDERS); i++) {
				JSONObject f = new JSONObject(data.getString(App.FOLDER + i));

				if (f.getString(App.NAME).equalsIgnoreCase(currentFolderName)) folder = new JSONObject(data.getString(App.FOLDER + i));
			}

			// This will not be used in the beginning
			if (folder.getString(App.CONTENT_TYPE).equals(App.CHECKLIST)) {
				for (int i = 0; i < folder.getInt(App.NUM_CHECKLISTS); i++) {
					JSONObject cl = new JSONObject(folder.getString(App.CHECKLIST + i));

					if (cl.getString(App.NAME).equalsIgnoreCase(currentChecklistName)) {
						taskData.put(App.ID, cl.getInt(App.NUM_TASKS));

						cl.put(App.TASK + cl.getInt(App.NUM_TASKS), taskData.toString());
						cl.put(App.NUM_TASKS, cl.getInt(App.NUM_TASKS) + 1);

						folder.put(App.CHECKLIST + cl.getInt(App.ID), cl.toString());
					}
				}
			} else if (folder.getString(App.CONTENT_TYPE).equals(App.TASK)) {
				taskData.put(App.ID, folder.getInt(App.NUM_TASKS));

				folder.put(App.TASK + folder.getInt(App.NUM_TASKS), taskData.toString());
				folder.put(App.NUM_TASKS, folder.getInt(App.NUM_TASKS) + 1);
			}

			data.put(App.FOLDER + folder.getInt(App.ID), folder.toString());
			editor.put(App.DATA, data.toString());

			updateData();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public void toggleMenu(View v) {
		root.toggleMenu();
	}

	public void onClick(View v) {
		if (v.getId() == R.id.add) {
			AlertDialog.Builder alert = new AlertDialog.Builder(this);
			alert.setTitle("Add new task");
			alert.setMessage("What to you have to do");

			// Set edit text to get user input
			final EditText input = new EditText(this);
			alert.setView(input);
			alert.setPositiveButton("Add", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					String name = input.getText().toString();
					addTask(name);
				}
			});

			alert.setNegativeButton("Cancel", null);

			alert.show();
		}
	}

}
