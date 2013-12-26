package com.todo.code3.dialog;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.speech.RecognizerIntent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.todo.code3.MainActivity;
import com.todo.code3.misc.App;

public abstract class Dialog extends AlertDialog.Builder {

	protected String title, message;
	protected boolean hasVoiceRecognition;

	protected LinearLayout content;
	protected MainActivity activity;

	protected String posButtonString = null;
	protected String negButtonString = null;

	public Dialog(MainActivity activity, String title, String message, boolean hasVoiceRecognition) {
		super(activity);
		this.activity = activity;
		this.title = title;
		this.message = message;
		this.hasVoiceRecognition = hasVoiceRecognition;
	}

	public Dialog(MainActivity activity, String title, String message, boolean hasVoiceRecognition, String posButtonString, String negButtonString) {
		super(activity);
		this.activity = activity;
		this.title = title;
		this.message = message;
		this.hasVoiceRecognition = hasVoiceRecognition;
		this.posButtonString = posButtonString;
		this.negButtonString = negButtonString;
	}

	protected void init() {
		if (title != null) setTitle(title);
		if (message != null) setMessage(message);

		content = new LinearLayout(activity);
		content.setOrientation(LinearLayout.VERTICAL);

		if (hasVoiceRecognition) initVoiceRecognition();

		setView(content);

		if (posButtonString != null) setPositiveButton();
		if (negButtonString != null) setNegativeButton();
	}

	private void initVoiceRecognition() {
		Button b = new Button(activity);
		b.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));

		PackageManager pm = activity.getPackageManager();
		List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);

		// If can use voice recognition and has internet connection
		if (activities.size() != 0 && App.isNetworkAvailable(activity)) {
			b.setText("Press me to speak");
			b.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					activity.startVoiceRecognition(Dialog.this, title);
				}
			});

			content.addView(b);
		}
	}

	public abstract void onActivityResult(int requestCode, int resultCode, Intent data);
	
	protected abstract Object getResult();

	private void setPositiveButton() {
		setPositiveButton(posButtonString, new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				onResult(getResult(), true);
			}
		});
	}

	private void setNegativeButton() {
		setNegativeButton(negButtonString, new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				onResult(getResult(), false);
			}
		});
	}

	protected void onResult(Object result, boolean positive) {
	}
}
