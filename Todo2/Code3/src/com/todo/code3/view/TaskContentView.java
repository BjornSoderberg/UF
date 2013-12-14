package com.todo.code3.view;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.todo.code3.MainActivity;
import com.todo.code3.R;
import com.todo.code3.misc.App;

public class TaskContentView extends ContentView {

	private TextView descTV;
	private EditText descET, focusDummy;
	private Button saveButton;
	
	private int id;

	public TaskContentView(MainActivity activity, int id) {
		super(activity);
		this.id = id;
	}

	protected void init() {
		View v = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.task_content_view, null);
		LayoutParams params = new LayoutParams(activity.getContentWidth(), activity.getContentHeight());
		v.setLayoutParams(params);
		addView(v);

		descTV = (TextView) v.findViewById(R.id.descTV);
		descET = (EditText) v.findViewById(R.id.descET);
		focusDummy = (EditText) v.findViewById(R.id.focusDummy);
		saveButton = (Button) v.findViewById(R.id.saveButton);

		saveButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				descTV.setVisibility(View.VISIBLE);
				descET.setVisibility(View.GONE);
				saveButton.setVisibility(View.GONE);

				focusDummy.requestFocus();
				
				saveDescription(descET.getText().toString());
				descTV.setText(descET.getText().toString());
			}
		});

		descTV.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				descTV.setVisibility(View.GONE);
				descET.setVisibility(View.VISIBLE);
				saveButton.setVisibility(View.VISIBLE);
				
				descET.setText(descTV.getText());

				descET.requestFocus();
			}
		});
		descET.setOnFocusChangeListener(new OnFocusChangeListener() {
			public void onFocusChange(View v, boolean hasFocus) {
				// This has not been tested on a real device
				if (hasFocus) showKeyboard();
				else hideKeyboard();
			}
		});

		focusDummy.setOnFocusChangeListener(new OnFocusChangeListener() {
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) hideKeyboard();
			}
		});
	}

	public void update(JSONObject data) {
		try {
			JSONObject task = new JSONObject(data.getString(App.TASK + id));

			if (task.has(App.DESCRIPTION)) {
				descTV.setText(task.getString(App.DESCRIPTION));
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}

	}

	private void showKeyboard() {
		((InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE)).toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
	}

	private void hideKeyboard() {
		((InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(focusDummy.getWindowToken(), 0);
	}

	public void leave() {
		focusDummy.requestFocus();
		hideKeyboard();
	}

	private void saveDescription(String desc) {
		hideKeyboard();
		
		activity.setTaskDescription(desc, id);
	}

	// not used
	public void updateContentItemsOrder() {		
	}

}
