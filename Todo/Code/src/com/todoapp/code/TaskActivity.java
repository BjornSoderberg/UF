package com.todoapp.code;

import misc.Data;
import misc.SharedPreferencesEditor;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class TaskActivity extends Activity implements OnClickListener {

	private JSONObject data;

	private SharedPreferences prefs;
	private SharedPreferencesEditor editor;

	private EditText taskNameET;
	private Button save;
	private Button saveTaskName;
	private EditText descriptionET;

	private String taskName = "";
	private String description;

	private int folderId;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();
		String taskData = intent.getExtras().getString(Data.TASK_DATA, null);
		folderId = intent.getExtras().getInt(Data.FOLDER_ID, -1);

		if (taskData == null || folderId == -1) {
			onBackPressed();
			finish();
		} else try {
			data = new JSONObject(taskData);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		prefs = getSharedPreferences(Data.PREFERENCES_NAME, Context.MODE_PRIVATE);
		editor = new SharedPreferencesEditor(prefs);

		setContentView(R.layout.activity_task);

		initXMLElements();

		updateData();
	}

	private void initXMLElements() {
		taskNameET = (EditText) findViewById(R.id.taskName);
		taskNameET.setOnClickListener(this);
		
		descriptionET = (EditText) findViewById(R.id.description);
		
		save = (Button) findViewById(R.id.save);
		save.setOnClickListener(this);
		
		saveTaskName = (Button) findViewById(R.id.saveTaskName);
		saveTaskName.setOnClickListener(this);
	}

	private void updateData() {
		try {
			taskName = data.getString(Data.TASK_NAME);
			taskNameET.setText(taskName);
			
			Log.i("dasfassasa", data.toString());

			if (data.has(Data.DESCRIPTION)) {
				description = data.getString(Data.DESCRIPTION);
				if(description != "") descriptionET.setText(description);
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public void onClick(View v) {
		if (v.getId() == R.id.save) {
			save();
		}
		
		if (v.getId() == R.id.taskName) {
			taskNameET.setGravity(Gravity.LEFT);
			saveTaskName.setVisibility(View.VISIBLE);
			saveTaskName.setEnabled(true);
		}

		if (v.getId() == R.id.saveTaskName) {
			if (saveTaskName.isEnabled()) {
				taskNameET.setGravity(Gravity.CENTER);
				saveTaskName.setVisibility(View.INVISIBLE);
				saveTaskName.setEnabled(false);

				updateTaskName();
			}
		}
	}
	
	private void updateTaskName() {
		try {

			String s = taskNameET.getText().toString();
			if(s == null) return;

			data.put(Data.TASK_NAME, s);
			
			String ss = prefs.getString(Data.TASK_DATA, null);

			JSONObject o = new JSONObject(ss);
			
			JSONObject folder = new JSONObject(o.getString(Data.FOLDER + folderId));
			folder.put(Data.TASK + data.getInt(Data.TASK_ID), data.toString());
			
			o.put(Data.FOLDER + folderId, folder.toString());
			editor.put(Data.TASK_DATA, o.toString());

			updateData();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void save() {
		try {
			String s = descriptionET.getText().toString();
			// if nothing is written, return
			if (s == null) return;
			data.put(Data.DESCRIPTION, s);

			String ss = prefs.getString(Data.TASK_DATA, null);

			// This is the jsn object with all the task data
			JSONObject o = new JSONObject(ss);

			// this is the json object with the data for this folder
			JSONObject folder = new JSONObject(o.getString(Data.FOLDER + folderId));
			folder.put(Data.TASK + data.getInt(Data.TASK_ID), data.toString());

			o.put(Data.FOLDER + folderId, folder.toString());
			editor.put(Data.TASK_DATA, o.toString());

		} catch (JSONException e) {
			e.printStackTrace();
		}

		Toast.makeText(this, "The updates were successfully saved!", Toast.LENGTH_SHORT).show();
	}

}
