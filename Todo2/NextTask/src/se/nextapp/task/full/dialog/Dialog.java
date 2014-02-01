package se.nextapp.task.full.dialog;

import java.util.List;

import se.nextapp.task.full.MainActivity;
import se.nextapp.task.full.R;
import se.nextapp.task.full.misc.App;
import se.nextapp.task.full.xml.CircularPulser;
import android.annotation.SuppressLint;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public abstract class Dialog extends AlertDialog.Builder {

	protected String title, message;
	protected boolean hasVoiceRecognition;
	protected boolean isListening = false;

	protected LinearLayout content;
	protected MainActivity activity;
	protected Button voiceRecognitionButton;

	protected SpeechRecognizer speechRecognizer;

	protected String posButtonString = null;
	protected String negButtonString = null;

	private CircularPulser cp;

	@SuppressLint("NewApi")
	public Dialog(MainActivity activity, String title, String message, boolean hasVoiceRecognition) {
		super(activity, (Build.VERSION.SDK_INT < 11) ? 0 : activity.isDarkTheme() ? AlertDialog.THEME_HOLO_DARK : AlertDialog.THEME_HOLO_LIGHT);
		this.activity = activity;
		this.title = title;
		this.message = message;
		this.hasVoiceRecognition = hasVoiceRecognition;
	}

	@SuppressLint("NewApi")
	public Dialog(MainActivity activity, String title, String message, boolean hasVoiceRecognition, String posButtonString, String negButtonString) {
		super(activity, (Build.VERSION.SDK_INT < 11) ? 0 : activity.isDarkTheme() ? AlertDialog.THEME_HOLO_DARK : AlertDialog.THEME_HOLO_LIGHT);
		this.activity = activity;
		this.title = title;
		this.message = message;
		this.hasVoiceRecognition = hasVoiceRecognition;
		this.posButtonString = posButtonString;
		this.negButtonString = negButtonString;
	}

	protected void init() {
		if (message != null) setMessage(message);
		if (title != null) {
			TextView t = new TextView(activity);
			t.setText(title);
			t.setPadding(30, 30, 0, 30);
			t.setBackgroundColor(activity.getResources().getColor(R.color.aqua_blue));
			t.setTextSize(App.pxToDp(activity.getResources().getDimension(R.dimen.text_extra_large), activity.getResources()));
			t.setTextColor(activity.getResources().getColor(R.color.white));
			setCustomTitle(t);
		}

		content = new LinearLayout(activity);
		content.setOrientation(LinearLayout.VERTICAL);

		if (hasVoiceRecognition) initVoiceRecognition();

		setView(content);

		if (posButtonString != null) setPositiveButton();
		if (negButtonString != null) setNegativeButton();
	}

	@SuppressLint("NewApi")
	private void initVoiceRecognition() {
		if (Build.VERSION.SDK_INT < App.MIN_API_FOR_VOICE_RECOGNITION) return;

		View view = LayoutInflater.from(activity).inflate(R.layout.dialog_voice_recognition, null);
		speechRecognizer = SpeechRecognizer.createSpeechRecognizer(activity);

		voiceRecognitionButton = (Button) view.findViewById(R.id.button1);
		voiceRecognitionButton.setBackgroundDrawable(activity.getResources().getDrawable(R.drawable.ic_mic_big));
		voiceRecognitionButton.getLayoutParams().height = App.dpToPx(80, activity.getResources());
		voiceRecognitionButton.getLayoutParams().width = App.dpToPx(80, activity.getResources());

		PackageManager pm = activity.getPackageManager();
		List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);

		// If can use voice recognition and has internet connection
		if (activities.size() != 0 && App.isNetworkAvailable(activity)) {
			voiceRecognitionButton.setText("");
			voiceRecognitionButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					if (!isListening) startVoiceRecognition();
					else endVoiceRecognition();

				}
			});

			cp = (CircularPulser) view.findViewById(R.id.circularPulsar1);
			cp.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, App.dpToPx(150, activity.getResources())));
			content.addView(view);
		}
	}

	@SuppressLint("NewApi")
	private void startVoiceRecognition() {
		if (Build.VERSION.SDK_INT < App.MIN_API_FOR_VOICE_RECOGNITION) return;

		speechRecognizer.setRecognitionListener(new RecognitionListener() {
			public void onBeginningOfSpeech() {
			}

			public void onBufferReceived(byte[] buffer) {
			}

			public void onEndOfSpeech() {
			}

			public void onError(int error) {
				if (cp != null) cp.disable();
			}

			public void onEvent(int eventType, Bundle params) {
			}

			public void onPartialResults(Bundle partialResults) {
				if (cp != null) cp.disable();
			}

			public void onReadyForSpeech(Bundle params) {
			}

			public void onResults(Bundle results) {
				endVoiceRecognition();
				onVoiceRecognitionResult(results);
				if (cp != null) cp.disable();
			}

			public void onRmsChanged(float rmsdB) {
				cp.update(rmsdB);
			}
		});

		if (!SpeechRecognizer.isRecognitionAvailable(getContext())) return;

		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, activity.getApplication().getPackageName());

		if (activity.getLocaleString() != "") intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, activity.getLocaleString());
		// http://stackoverflow.com/questions/7973023/what-is-the-list-of-supported-languages-locales-on-android

		cp.enable();
		isListening = true;

		speechRecognizer.startListening(intent);
	}

	@SuppressLint("NewApi")
	private void endVoiceRecognition() {
		if (speechRecognizer != null && Build.VERSION.SDK_INT >= App.MIN_API_FOR_VOICE_RECOGNITION) {
			speechRecognizer.stopListening();
			isListening = false;
		}

		if (cp != null) cp.disable();
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
