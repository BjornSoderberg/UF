package se.nextapp.task.full.view.settings.feedback;

import org.json.JSONObject;

import se.nextapp.task.full.MainActivity;
import se.nextapp.task.full.R;
import se.nextapp.task.full.misc.App;
import se.nextapp.task.full.view.ContentView;
import se.nextapp.task.full.view.settings.SettingsView;
import android.content.res.Resources;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class FeedbackView extends ContentView {

	private EditText message, mail;
	private Button send;
	private FeedbackSender feedbackSender;

	public FeedbackView(MainActivity activity) {
		super(activity, SettingsView.SEND_FEEDBACK);
		init();
	}

	protected void init() {
		super.init();
		
		LayoutInflater.from(activity).inflate(R.layout.feedback, this, true);
		setLayoutParams(new LayoutParams(activity.getContentWidth(), LayoutParams.FILL_PARENT));

		message = (EditText) findViewById(R.id.messageET);
		mail = (EditText) findViewById(R.id.mailET);
		send = (Button) findViewById(R.id.send);
		send.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				sendFeedback();
			}
		});

		setColors();
	}

	public void setFeedback(String msg, String mail) {
		if (msg != null) message.setText(msg);
		if (mail != null) this.mail.setText(mail);
	}

	public String getMessage() {
		return message.getText().toString();
	}

	public String getMail() {
		return mail.getText().toString();
	}

	private void sendFeedback() {
		if (!App.isNetworkAvailable(activity)) {
			Toast.makeText(activity, "No Internet connectio+++n", Toast.LENGTH_LONG).show();
			return;
		}
		
		send.setText(getResources().getString(R.string.sending) + "...");
		// Makes unpressable until result comes + sets other text color to let user know
		send.setEnabled(false);
		send.setTextColor(getResources().getColor(activity.isDarkTheme() ? R.color.text_color_checked_dark : R.color.text_color_checked_light));
		
		String msg = message.getText().toString();
		String mail = this.mail.getText().toString();
		if (msg.length() == 0) return;

		feedbackSender = new FeedbackSender(activity, this, msg, mail);
		
		new Handler().postDelayed(new Runnable() {
			public void run() {
				feedbackSender.cancel(false);
				feedbackSent(false);
			}
		}, 15000);
	}

	public void feedbackSent(boolean success) {
		if (success) {
			send.setText(getResources().getString(R.string.thank_you_for_your_feedback));
			message.setText("");
			mail.setText("");
		} else send.setText(getResources().getString(R.string.oops_something_went_wrong));
		
		// Enabling and setting original text color
		send.setEnabled(true);
		send.setTextColor(getResources().getColor(activity.isDarkTheme() ? R.color.text_color_dark : R.color.text_color_light));
	}

	public void setColors() {
		boolean dark = activity.isDarkTheme();
		Resources r = getResources();
		setBackgroundColor((dark) ? r.getColor(R.color.background_color_dark) : r.getColor(R.color.white));

		((TextView) findViewById(R.id.mailTV)).setTextColor((dark) ? r.getColor(R.color.text_color_dark) : r.getColor(R.color.text_color_light));
		((TextView) findViewById(R.id.messageTV)).setTextColor((dark) ? r.getColor(R.color.text_color_dark) : r.getColor(R.color.text_color_light));
		((TextView) findViewById(R.id.mailET)).setBackgroundColor((dark) ? r.getColor(R.color.dark) : r.getColor(R.color.light));
		((TextView) findViewById(R.id.mailET)).setTextColor((dark) ? r.getColor(R.color.text_color_dark) : r.getColor(R.color.text_color_light));
		((TextView) findViewById(R.id.messageET)).setBackgroundColor((dark) ? r.getColor(R.color.dark) : r.getColor(R.color.light));
		((TextView) findViewById(R.id.messageET)).setTextColor((dark) ? r.getColor(R.color.text_color_dark) : r.getColor(R.color.text_color_light));

		((Button) findViewById(R.id.send)).setTextColor((dark) ? r.getColor(R.color.text_color_dark) : r.getColor(R.color.text_color_light));
		((Button) findViewById(R.id.send)).setBackgroundDrawable((dark) ? r.getDrawable(R.drawable.item_selector_dark) : r.getDrawable(R.drawable.item_selector_light));
	}

	public void leave() {

	}

	public void update(JSONObject data) {
		setColors();
	}

	public void updateContentItemsOrder() {

	}
}