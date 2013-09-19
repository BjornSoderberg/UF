package com.todoapp.code;

import misc.Data;
import misc.SharedPreferencesEditor;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class FolderActivity extends Activity implements OnClickListener {

	private TextView folderNameTV;
	private Button addTask;
	private LinearLayout taskList;

	private JSONObject data;
	private String folderName = "";
	
	private SharedPreferences prefs;
	private SharedPreferencesEditor editor;

	private int numberOfTasks = 0;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();
		String folderData = intent.getExtras().getString(Data.FOLDER_DATA, null);

		if (folderData == null) {
			Intent i = new Intent(this, MainActivity.class);
			startActivity(i);
			finish();
		} else try {
			data = new JSONObject(folderData);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		prefs = getSharedPreferences(Data.PREFERNCES_NAME, Context.MODE_PRIVATE);
		editor = new SharedPreferencesEditor(prefs);
		
		setContentView(R.layout.activity_folder);

		initXMLElements();

		updateData();

	}

	private void initXMLElements() {
		folderNameTV = (TextView) findViewById(R.id.folderName);
		taskList = (LinearLayout) findViewById(R.id.taskList);

		addTask = (Button) findViewById(R.id.addTask);
		addTask.setOnClickListener(this);
	}

	private void updateData() {
		try {
			folderName = data.getString(Data.FOLDER_NAME);
			folderNameTV.setText(folderName);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		updateTaskButtons();
	}

	private void updateTaskButtons() {
		try {
			numberOfTasks = Integer.parseInt(data.getString(Data.NUMBER_OF_TASKS));

			if (numberOfTasks != 0) taskList.removeAllViews();

			for (int i = 0; i < numberOfTasks; i++) {
				Button b = new Button(this);

				final JSONObject taskData = new JSONObject(data.getString(Data.TASK + i));

				b.setText(taskData.getString(Data.TASK_NAME));

				// add on click listener

				taskList.addView(b);
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public void onClick(View v) {
		if (v.getId() == R.id.addTask) {
			AlertDialog.Builder alert = new AlertDialog.Builder(this);
			alert.setTitle("Add a new task");
			alert.setMessage("What do you have to do?");

			// Set edit text view to get user input
			final EditText input = new EditText(this);
			alert.setView(input);
			alert.setPositiveButton("Add", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					String name = input.getText().toString();
					addNewTask(name);
				}
			});

			alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					// Does nothing
				}
			});

			alert.show();
		}
	}

	private void addNewTask(String name) {
		try {
			JSONObject taskData = new JSONObject();
			taskData.put(Data.TASK_NAME, name);
			
			data.put(Data.NUMBER_OF_TASKS, numberOfTasks + 1);
			data.put(Data.TASK+numberOfTasks, taskData.toString());
			
			String s = prefs.getString(Data.TASK_DATA, null);
			
			// This is the json object containing all the data
			JSONObject o = new JSONObject(s);
			
			o.put(Data.FOLDER + data.getString(Data.FOLDER_ID), data.toString());
			editor.put(Data.TASK_DATA, o.toString());
			
			updateData();
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
