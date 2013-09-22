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

public class MainActivity extends Activity implements OnClickListener {

	private LinearLayout folderList;
	private Button addFolder;

	private SharedPreferences prefs;
	private SharedPreferencesEditor editor;

	private JSONObject data;

	private int numberOfFolders = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		initXMLElements();

		prefs = getSharedPreferences(Data.PREFERENCES_NAME, Context.MODE_PRIVATE);
		editor = new SharedPreferencesEditor(prefs);

		getData();

	}
	
	private void getData() {
		String taskData = prefs.getString(Data.TASK_DATA, null);

		if (data == null) {
			// If no data is received, the number of folders is set to 0
			try {
				data = new JSONObject();
				data.put(Data.NUMBER_OF_FOLDERS, 0);
				editor.put(Data.TASK_DATA, data.toString());
			} catch (JSONException e) {
			}
		} else {
			try {
				data = new JSONObject(taskData);
				updateData();
			} catch (JSONException e) {
			}
		}
	}
	
	private void initXMLElements() {
		folderList = (LinearLayout) findViewById(R.id.folderList);

		addFolder = (Button) findViewById(R.id.addFolder);
		addFolder.setOnClickListener(this);
	}
	
	private void updateData() {
		updateFolderButtons();
	}

	private void updateFolderButtons() {
		try {
			numberOfFolders = Integer.parseInt(data.getString(Data.NUMBER_OF_FOLDERS));

			if (numberOfFolders != 0) folderList.removeAllViews();

			for (int i = 0; i < numberOfFolders; i++) {
				Button b = new Button(this);

				final JSONObject folderData = new JSONObject(data.getString(Data.FOLDER + i));

				b.setText(folderData.getString(Data.FOLDER_NAME));

				b.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						Intent i = new Intent(MainActivity.this, FolderActivity.class);
						i.putExtra(Data.FOLDER_DATA, folderData.toString());
						startActivity(i);
					}
				});

				folderList.addView(b);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public void onClick(View v) {
		if (v.getId() == R.id.addFolder) {
			AlertDialog.Builder alert = new AlertDialog.Builder(this);
			alert.setTitle("Add a new folder");
			alert.setMessage("Name the folder!");

			// Set edit text view to get user input
			final EditText input = new EditText(this);
			alert.setView(input);
			alert.setPositiveButton("Add", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					String name = input.getText().toString();
					addNewFolder(name);
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

	private void addNewFolder(String name) {
		try {
			JSONObject folderData = new JSONObject();
			folderData.put(Data.FOLDER_NAME, name);
			folderData.put(Data.NUMBER_OF_TASKS, 0);
			folderData.put(Data.FOLDER_ID, numberOfFolders);

			data.put(Data.NUMBER_OF_FOLDERS, numberOfFolders + 1);
			data.put(Data.FOLDER + numberOfFolders, folderData.toString());
			editor.put(Data.TASK_DATA, data.toString());

			updateData();

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	protected void onRestart() {
		super.onRestart();
		getData();
	}
}
