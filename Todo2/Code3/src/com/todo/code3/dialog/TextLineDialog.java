package com.todo.code3.dialog;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.widget.EditText;

import com.todo.code3.MainActivity;
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

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == App.VOICE_RECOGNITION_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
			ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

			editText.setText(App.capitalizeFirstWordInSentences(results.get(0)));
		}
	}
}
