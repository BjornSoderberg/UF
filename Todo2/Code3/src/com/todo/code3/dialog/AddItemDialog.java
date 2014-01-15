package com.todo.code3.dialog;

import android.app.AlertDialog;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.todo.code3.MainActivity;
import com.todo.code3.R;
import com.todo.code3.misc.App;

public class AddItemDialog extends Dialog {

	private AlertDialog alert;

	public AddItemDialog(MainActivity activity, String title, String message) {
		super(activity, title, message, false);

		init();
	}

	public AddItemDialog(MainActivity activity, String title, String message, String posButtonString, String negButtonString) {
		super(activity, title, message, false, posButtonString, negButtonString);

		init();
	}

	protected void init() {
		super.init();

		LinearLayout l = new LinearLayout(activity);
		l.setOrientation(LinearLayout.HORIZONTAL);

		alert = create();

		Button task = new Button(activity);
		task.setText(activity.getString(R.string.task));
		task.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				askForName(App.TASK);
				alert.dismiss();
			}
		});

		Button note = new Button(activity);
		note.setText(activity.getString(R.string.note));
		note.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				askForName(App.NOTE);
				alert.dismiss();
			}
		});

		Button folder = new Button(activity);
		folder.setText(activity.getString(R.string.folder));
		folder.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				askForName(App.FOLDER);
				alert.dismiss();
			}
		});

		l.addView(task);
		l.addView(note);
		l.addView(folder);

		alert.setView(l);
	}

	public void askForName(final String type) {
		Resources r = activity.getResources();
		TextLineDialog d = new TextLineDialog(activity, r.getString(R.string.add_new) + " " + type, null, true, r.getString(R.string.add), r.getString(R.string.cancel)) {
			protected void onResult(Object result) {
				super.onResult(result);

				if (result instanceof String) AddItemDialog.this.onResult((String) result, type);
				else AddItemDialog.this.onResult("", type);
			}
		};

		d.show();
	}

	public void onVoiceRecognitionResult(Bundle result) {
	}

	public void onResult(String name, String type) {

	}

	public AlertDialog show() {
		alert.show();
		return alert;
	}

	protected Object getResult() {
		return null;
	}
}
