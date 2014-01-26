package com.todo.code3.view.settings.feedback;

import org.json.JSONObject;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.todo.code3.MainActivity;
import com.todo.code3.R;
import com.todo.code3.misc.App;
import com.todo.code3.view.ContentView;

public class FeedbackView extends ContentView {

	private EditText editText, mail;
	private Button send;
	private FeedbackSender feedbackSender;

	public FeedbackView(MainActivity activity) {
		super(activity, 0);
		init();
	}

	protected void init() {
		LayoutInflater.from(activity).inflate(R.layout.feedback, this, true);

		setLayoutParams(new LayoutParams(activity.getContentWidth(), LayoutParams.FILL_PARENT));

		editText = (EditText) findViewById(R.id.message);
		mail = (EditText) findViewById(R.id.mail);
		send = (Button) findViewById(R.id.send);
		send.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (App.isNetworkAvailable(activity)) sendFeedback();
			}
		});
	}

	private void sendFeedback() {
		if (!App.isNetworkAvailable(activity)) {
			Toast.makeText(activity, "No Internet connection", Toast.LENGTH_LONG).show();
			return;
		}

		String msg = editText.getText().toString();
		String mail = this.mail.getText().toString();
		if (msg.length() == 0) return;

		feedbackSender = new FeedbackSender(activity, this, msg, mail);
	}

	public void feedbackSent(boolean success) {
		if (success) send.setText("ty for feedback");
		else send.setText("error");
	}

	public void leave() {

	}

	public void update(JSONObject data) {

	}

	public void updateContentItemsOrder() {

	}

	public void setColors() {

	}

}
