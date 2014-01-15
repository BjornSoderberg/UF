package com.todo.code3.dialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.todo.code3.MainActivity;
import com.todo.code3.R;
import com.todo.code3.misc.App;

public abstract class Dialog extends AlertDialog.Builder {

	protected String title, message;
	protected boolean hasVoiceRecognition;

	protected LinearLayout content;
	protected MainActivity activity;
	protected Button voiceRecognitionButton;

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
		if (Build.VERSION.SDK_INT < App.MIN_API_FOR_VOICE_RECOGNITION) return;

		voiceRecognitionButton = new Button(activity);
		voiceRecognitionButton.setBackgroundColor(0xffcccccc);
		voiceRecognitionButton.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));

		PackageManager pm = activity.getPackageManager();
		List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);

		// If can use voice recognition and has internet connection
		if (activities.size() != 0 && App.isNetworkAvailable(activity)) {
			voiceRecognitionButton.setText(activity.getResources().getString(R.string.press_me_to_speak));
			voiceRecognitionButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					startVoiceRecognition();
				}
			});

			content.addView(voiceRecognitionButton);
		}
	}

	@SuppressLint("NewApi")
	private void startVoiceRecognition() {
		if (Build.VERSION.SDK_INT < App.MIN_API_FOR_VOICE_RECOGNITION) return;

		SpeechRecognizer r = SpeechRecognizer.createSpeechRecognizer(getContext());
		r.setRecognitionListener(new RecognitionListener() {
			public void onBeginningOfSpeech() {
			}

			public void onBufferReceived(byte[] buffer) {
			}

			public void onEndOfSpeech() {
				voiceRecognitionButton.setBackgroundColor(0xffcccccc);
			}

			public void onError(int error) {
			}

			public void onEvent(int eventType, Bundle params) {
			}

			public void onPartialResults(Bundle partialResults) {
			}

			public void onReadyForSpeech(Bundle params) {
			}

			public void onResults(Bundle results) {
				onVoiceRecognitionResult(results);
			}

			public void onRmsChanged(float rmsdB) {
			}
		});

		if (!SpeechRecognizer.isRecognitionAvailable(getContext())) return;

		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, activity.getApplication().getPackageName());
		// intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,
		// Locale.ENGLISH.toString());
		if (!activity.getSetting(App.SETTINGS_VOICE_RECOGNITION_LANGUAGE).equals("")) intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, activity.getSetting(App.SETTINGS_VOICE_RECOGNITION_LANGUAGE));
		// http://stackoverflow.com/questions/7973023/what-is-the-list-of-supported-languages-locales-on-android

		voiceRecognitionButton.setBackgroundColor(0xff999999);
		r.startListening(intent);
	}

	public abstract void onVoiceRecognitionResult(Bundle result);

	protected abstract Object getResult();

	private void setPositiveButton() {
		setPositiveButton(posButtonString, new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				onResult(getResult());
			}
		});
	}

	private void setNegativeButton() {
		setNegativeButton(negButtonString, new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				onCancel();
			}
		});
	}

	protected void onResult(Object result) {
	}

	protected void onCancel() {
	}
}
