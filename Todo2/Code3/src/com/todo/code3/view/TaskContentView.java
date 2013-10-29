package com.todo.code3.view;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.todo.code3.MainActivity;
import com.todo.code3.R;
import com.todo.code3.misc.App;

public class TaskContentView extends ContentView {

	private TextView descTV;
	private EditText descET, focusDummy;

	private int currentFolder = -1;
	private int currentChecklist = -1;
	private int currentTask = -1;

	public TaskContentView(MainActivity activity, int currentFolder, int currentChecklist, int currentTask) {
		super(activity);
		this.currentFolder = currentFolder;
		this.currentChecklist = currentChecklist;
		this.currentTask = currentTask;
	}

	protected void init() {
		View v = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.task_content_view, null);
		LayoutParams params = new LayoutParams(activity.getContentWidth(), activity.getContentHeight());
		v.setLayoutParams(params);
		addView(v);

		descTV = (TextView) v.findViewById(R.id.descTV);
		descET = (EditText) v.findViewById(R.id.descET);
		focusDummy = (EditText) v.findViewById(R.id.focusDummy);

		descTV.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				descTV.setVisibility(View.GONE);
				descET.setVisibility(View.VISIBLE);
				descET.requestFocus();
			}
		});
		descET.setOnFocusChangeListener(new OnFocusChangeListener() {
			public void onFocusChange(View v, boolean hasFocus) {
				// This has not been tested on a real device
				activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
			}
		});
	}

	public void update(JSONObject data) {
		try {
			JSONObject parent = new JSONObject(data.getString(App.FOLDER + currentFolder));
			if (parent.getString(App.CONTENT_TYPE).equals(App.CHECKLIST) && currentChecklist != -1) {
				parent = new JSONObject(parent.getString(App.CHECKLIST + currentChecklist));
			}
			JSONObject task = new JSONObject(parent.getString(App.TASK + currentTask));

			if (task.has(App.DESCRIPTION)) {
				descTV.setText(task.getString(App.DESCRIPTION));
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}

	}

	public void setFolderAndChecklistAndTask(int folder, int checklist, int task) {

	}

}
