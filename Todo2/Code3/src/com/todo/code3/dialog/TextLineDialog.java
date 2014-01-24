package com.todo.code3.dialog;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.widget.EditText;

import com.todo.code3.MainActivity;
import com.todo.code3.R;
import com.todo.code3.misc.App;

public class TextLineDialog extends Dialog {

	private EditText editText;

	public TextLineDialog(MainActivity activity, String title, String message, boolean hasVoiceRecognition) {
		super(activity, title, message, hasVoiceRecognition);
		
		init();
	}

	public TextLineDialog(MainActivity activity, String title, String message, boolean hasVoiceRecognition, String posButtonString, String negButtonString) {
		super(activity, title, message, hasVoiceRecognition, posButtonString, negButtonString);
		
		init();
	}

	protected void init() {
		super.init();

		editText = new EditText(activity);
		content.addView(editText);
	}

	protected String getResult() {
		return editText.getText().toString();
	}

	@SuppressLint("InlinedApi")
	public void onVoiceRecognitionResult(Bundle result) {
		if(Build.VERSION.SDK_INT < 8) return;
		
		ArrayList<String> l = result.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
		if(l.size() > 0) editText.setText(App.capitalizeFirstWordInSentences(l.get(0)));
		else editText.setText(activity.getResources().getString(R.string.sorry_did_not_catch_that));
	}
}
