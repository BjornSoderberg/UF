package com.todo.code3.view;

import org.json.JSONException;
import org.json.JSONObject;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.todo.code3.MainActivity;
import com.todo.code3.R;
import com.todo.code3.misc.App;

public class NoteView extends ContentView {

	private TextView descTV;
	private EditText descET, focusDummy;
	private Button saveButton;
	
	private JSONObject note;

	public NoteView(MainActivity activity, int parentId) {
		super(activity, parentId);
	}

	protected void init() {
		LayoutInflater.from(activity).inflate(R.layout.note_view, this, true);

		setLayoutParams(new LayoutParams(activity.getContentWidth(), LayoutParams.FILL_PARENT));

		descTV = (TextView) findViewById(R.id.descTV);
		descET = (EditText) findViewById(R.id.descET);
		focusDummy = (EditText) findViewById(R.id.focusDummy);
		saveButton = (Button) findViewById(R.id.saveButton);

		saveButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				endEditDescription(true);
			}
		});

		descTV.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startEditDescription();
			}
		});

		descET.setOnFocusChangeListener(new OnFocusChangeListener() {
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) App.showKeyboard(getContext());
				else App.hideKeyboard(getContext(), focusDummy);
			}
		});

		focusDummy.setOnFocusChangeListener(new OnFocusChangeListener() {
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) App.hideKeyboard(getContext(), focusDummy);
			}
		});
	}

	public void update(JSONObject data) {
		try {
			note = new JSONObject(data.getString(parentId + ""));
			if(note.getString(App.TYPE).equals(App.NOTE)) {
				if(note.has(App.DESCRIPTION)) {
					descTV.setText(note.getString(App.DESCRIPTION));
				}
			}
		} catch(JSONException e) {
			e.printStackTrace();
		}
	}

	private void startEditDescription() {
		descTV.setVisibility(View.GONE);
		descET.setVisibility(View.VISIBLE);
		saveButton.setVisibility(View.VISIBLE);

		descET.setText(descTV.getText());

		descET.requestFocus();
	}

	private void endEditDescription(boolean save) {

		descTV.setVisibility(View.VISIBLE);
		descET.setVisibility(View.GONE);
		saveButton.setVisibility(View.GONE);

		focusDummy.requestFocus();

		if (save) {
			saveDescription(descET.getText().toString());
			descTV.setText(descET.getText().toString());
		}
	}

	private void saveDescription(String desc) {
		App.hideKeyboard(getContext(), focusDummy);

		activity.setProperty(App.DESCRIPTION, desc, parentId);
	}

	public void leave() {
		focusDummy.requestFocus();
		App.hideKeyboard(getContext(), focusDummy);
	}

	public void updateContentItemsOrder() {

	}
}
