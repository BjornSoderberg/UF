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
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
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
		String taskData = intent.getStringExtra(Data.TASK_DATA);
		folderId = intent.getExtras().getInt(Data.FOLDER_ID, -1);

		if (taskData == null || folderId == -1) {
			onBackPressed();
			finish();
		} else
			try {
				data = new JSONObject(taskData);
			} catch (JSONException e) {
				e.printStackTrace();
			}

		prefs = getSharedPreferences(Data.PREFERENCES_NAME,
				Context.MODE_PRIVATE);
		editor = new SharedPreferencesEditor(prefs);

		setContentView(R.layout.activity_task);

		initXMLElements();

		updateData();
	}

	private void initXMLElements() {
		taskNameET = (EditText) findViewById(R.id.taskName);
		taskNameET.setOnClickListener(this);
		// When the edit text has focus, the button becomes visible
		// If not, nothing is seen
		taskNameET.setOnFocusChangeListener(new OnFocusChangeListener() {
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					taskNameET.setGravity(Gravity.LEFT);
					saveTaskName.setVisibility(View.VISIBLE);
				} else {
					taskNameET.setGravity(Gravity.CENTER);
					saveTaskName.setVisibility(View.INVISIBLE);
					taskNameET.setText(taskName);
				}
			}
		});
		// When the enter button is pressed, the keyboard
		// is hidden, the save button becomes invisible
		// and the text of the edit text is set to what it
		// was before it was changed
		taskNameET.setOnKeyListener(new OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_DOWN) {
					switch (keyCode) {
					case KeyEvent.KEYCODE_DPAD_CENTER:
					case KeyEvent.KEYCODE_ENTER:
						taskNameET.setGravity(Gravity.CENTER);
						saveTaskName.setVisibility(View.INVISIBLE);
						taskNameET.setText(taskName);
						hideKeyboard(v);
						return true;
					}
				}
				return true;
			}
		});

		descriptionET = (EditText) findViewById(R.id.description);
		descriptionET.setOnClickListener(this);
		// When enter is pressed, the keyboard is hidden
		// This will probably need to be changed since
		// users might want to have line breaks in their
		// task descriptions
		descriptionET.setOnKeyListener(new OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_DOWN) {
					switch (keyCode) {
					case KeyEvent.KEYCODE_DPAD_CENTER:
					case KeyEvent.KEYCODE_ENTER:
						hideKeyboard(v);
						return true;
					}
				}
				return true;
			}

		});

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
				if (description != "")
					descriptionET.setText(description);
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void hideKeyboard(View v) {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
	}

	public void onClick(View v) {

		if (v.getId() == R.id.save) {
			save();
			hideKeyboard(v);
		}

		// If the task name edit text is pressed,
		// the button becomes visible
		// This is just to insure that the
		// button always is visible when the user
		// should be able to edit the task name
		if (v.getId() == R.id.taskName) {
			taskNameET.setGravity(Gravity.LEFT);
			saveTaskName.setVisibility(View.VISIBLE);
		}

		if (v.getId() == R.id.saveTaskName) {
			taskNameET.setGravity(Gravity.CENTER);
			saveTaskName.setVisibility(View.INVISIBLE);
			hideKeyboard(v);

			updateTaskName();
		}
	}

	private void updateTaskName() {
		try {
			String s = taskNameET.getText().toString();
			if (s == null)
				return;

			data.put(Data.TASK_NAME, s);

			String ss = prefs.getString(Data.TASK_DATA, null);

			JSONObject o = new JSONObject(ss);

			JSONObject folder = new JSONObject(o.getString(Data.FOLDER
					+ folderId));
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
			if (s == null)
				return;
			data.put(Data.DESCRIPTION, s);

			String ss = prefs.getString(Data.TASK_DATA, null);

			// This is the jsn object with all the task data
			JSONObject o = new JSONObject(ss);

			// this is the json object with the data for this folder
			JSONObject folder = new JSONObject(o.getString(Data.FOLDER
					+ folderId));
			folder.put(Data.TASK + data.getInt(Data.TASK_ID), data.toString());

			o.put(Data.FOLDER + folderId, folder.toString());
			editor.put(Data.TASK_DATA, o.toString());

		} catch (JSONException e) {
			e.printStackTrace();
		}

		Toast.makeText(this, "The updates were successfully saved!",
				Toast.LENGTH_SHORT).show();
	}

}
