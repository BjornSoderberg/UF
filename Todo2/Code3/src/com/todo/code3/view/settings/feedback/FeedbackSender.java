package com.todo.code3.view.settings.feedback;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import com.todo.code3.internet.JSONParser;
import com.todo.code3.internet.Tags;
import com.todo.code3.internet.URL;

public class FeedbackSender extends AsyncTask<Void, Void, Void> {

	private JSONParser parser;
	private JSONObject result;
	private Context context;
	private FeedbackView feedbackView;
	private String msg, mail = null;

	private boolean success = false;

	public FeedbackSender(Context context, FeedbackView feedbackView, String msg) {
		this.context = context;
		this.feedbackView = feedbackView;
		this.msg = msg;
		parser = new JSONParser();

		execute();
	}
	
	public FeedbackSender(Context context, FeedbackView feedbackView, String msg, String mail) {
		this.context = context;
		this.feedbackView = feedbackView;
		this.msg = msg;
		this.mail = mail;
		parser = new JSONParser();
		
		execute();
	}

	protected void onPreExecute() {
		super.onPreExecute();
	}

	protected Void doInBackground(Void... nothing) {
		try {
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair(Tags.MESSAGE, msg));
			params.add(new BasicNameValuePair(Tags.API_LEVEL, Build.VERSION.SDK_INT + ""));
			try {
				params.add(new BasicNameValuePair(Tags.APP_VERSION, context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName));
			} catch (NameNotFoundException e) {
			}
			params.add(new BasicNameValuePair(Tags.DEVICE, Build.MODEL));
			if(mail != null) params.add(new BasicNameValuePair(Tags.MAIL, mail));
			
			Log.i("SENDING FEEDBACK", msg);

			result = parser.makeHttpRequest(URL.FEEDBACK_URL, "POST", params);
			success = result.getInt(Tags.SUCCESS) == 1;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	protected void onPostExecute(Void result) {
		super.onPostExecute(result);

		feedbackView.feedbackSent(success);
	}

}
