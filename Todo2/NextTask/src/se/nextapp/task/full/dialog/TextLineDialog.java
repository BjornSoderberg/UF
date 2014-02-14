package se.nextapp.task.full.dialog;

import java.util.ArrayList;

import se.nextapp.task.full.MainActivity;
import se.nextapp.task.full.R;
import se.nextapp.task.full.misc.App;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Build;
import android.os.Bundle;
import android.speech.SpeechRecognizer;
import android.view.Gravity;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;

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

	public TextLineDialog(MainActivity activity, String title, String message, boolean hasVoiceRecognition, boolean cancelable) {
		super(activity, title, message, hasVoiceRecognition);
		setCancelable(cancelable);

		init();
	}

	public TextLineDialog(MainActivity activity, String title, String message, boolean hasVoiceRecognition, String posButtonString, String negButtonString, boolean cancelable) {
		super(activity, title, message, hasVoiceRecognition, posButtonString, negButtonString);
		setCancelable(cancelable);

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
		if (Build.VERSION.SDK_INT < 8) return;

		ArrayList<String> l = result.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
		if (l.size() > 0) editText.setText(App.capitalizeFirstWordInSentences(l.get(0)));
		else editText.setText(activity.getResources().getString(R.string.sorry_did_not_catch_that));
	}
	
	public AlertDialog show() {
		AlertDialog alert = create();
		alert.show();
		
		// The move button is disabled as default
		if(!isCancelable()) alert.getButton(android.app.Dialog.BUTTON2).setEnabled(false);
		
		return alert;
	}
}
