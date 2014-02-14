package se.nextapp.task.full.dialog;

import android.os.Bundle;
import se.nextapp.task.full.MainActivity;

public class ConfirmDialog extends Dialog {
	
	public static boolean isVisible = false;

	public ConfirmDialog(MainActivity activity, String title, String message, String posButtonString, String negButtonString) {
		super(activity, title, message, false, posButtonString, negButtonString);
		isVisible = true;
		init();
	}

	public void onVoiceRecognitionResult(Bundle result) {
	}

	protected Boolean getResult() {
		return true;
	}
	
	protected void onResult(Object result) {
		super.onResult(result);
		isVisible = false;
	}
	
	protected void onCancel() {
		super.onCancel();
		isVisible = false;
	}
}
