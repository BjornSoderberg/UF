package com.todoapp.code;

import misc.Data;
import misc.LinearLayout2;
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
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class TaskActivity extends Activity implements OnClickListener {

	private JSONObject data;

	private SharedPreferences prefs;
	private SharedPreferencesEditor editor;

	private TextView nameTV;
	private Button save;
	private Button saveName;
	private EditText descriptionET;
	private EditText editName;
	private EditText focusDummy;

	private String taskName = "";
	private String description;

	private int folderId;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();
		String taskData = intent.getStringExtra(Data.TASK_DATA);
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

	private void getData() {
		String taskData = prefs.getString(Data.TASK_DATA, null);

		try {
			JSONObject o = new JSONObject(taskData);
			JSONObject folder = new JSONObject(o.getString(Data.FOLDER + folderId));
			data = new JSONObject(folder.getString(Data.TASK + data.getString(Data.TASK_ID)));
			updateData();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void initXMLElements() {
		LinearLayout2.setActivity(this);

		nameTV = (TextView) findViewById(R.id.taskNameTV);
		nameTV.setOnClickListener(this);

		descriptionET = (EditText) findViewById(R.id.description);
		descriptionET.setOnClickListener(this);

		save = (Button) findViewById(R.id.save);
		save.setOnClickListener(this);

		saveName = (Button) findViewById(R.id.saveName);
		saveName.setOnClickListener(this);

		editName = (EditText) findViewById(R.id.editName);
		editName.setOnClickListener(this);
		editName.setOnFocusChangeListener(new OnFocusChangeListener() {
			public void onFocusChange(View v, boolean hasFocus) {
				if(!hasFocus) toggleHeader(!Data.ENABLE_EDITING);
			}
		});
		
		focusDummy = (EditText) findViewById(R.id.focusDummy);
	}

	private void updateData() {
		try {
			taskName = data.getString(Data.TASK_NAME);
			nameTV.setText(taskName);

			Log.i("dasfassasa", data.toString());

			if (data.has(Data.DESCRIPTION)) {
				description = data.getString(Data.DESCRIPTION);
				if (description != "") descriptionET.setText(description);
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public void onClick(View v) {

		if (v.getId() == R.id.save) {
			save();
			hideKeyboard();
			focusDummy.requestFocus();
		}
		
		if (v.getId() == R.id.taskNameTV) {
			toggleHeader(Data.ENABLE_EDITING);
		}

		if (v.getId() == R.id.saveName) {
			String name = editName.getText().toString();
			updateTaskName(name);
			
			toggleHeader(!Data.ENABLE_EDITING);
			
			hideKeyboard();
		}
	}

	private void updateTaskName(String name) {
		try {
			if (name == null) return;

			data.put(Data.TASK_NAME, name);

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

	public void toggleHeader(boolean b) {
		if (b) {
			nameTV.setVisibility(View.GONE);
			saveName.setVisibility(View.VISIBLE);

			editName.setText(taskName);
			editName.setVisibility(View.VISIBLE);
			editName.requestFocus();
			
			hideKeyboard();
			showKeyboard();
		} else {
			nameTV.setText(taskName);
			nameTV.setVisibility(View.VISIBLE);

			saveName.setVisibility(View.GONE);
			editName.setVisibility(View.GONE);
			
			focusDummy.requestFocus();
		}
	}

	public void onBackPressed() {
		super.onBackPressed();
		toggleHeader(!Data.ENABLE_EDITING);

		Log.i("FOLDER ACTIVITY", "BACK PRESSED");
	}

	private void showKeyboard() {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.toggleSoftInput(InputMethodManager.RESULT_SHOWN, 0);
	}

	private void hideKeyboard() {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(editName.getWindowToken(), 0);
	}

	protected void onPause() {
		super.onPause();
		toggleHeader(!Data.ENABLE_EDITING);
	}

	protected void onRestart() {
		super.onRestart();
		getData();
	}

	protected void onDestroy() {
		super.onDestroy();

		LinearLayout2.recycle();
	}

}
