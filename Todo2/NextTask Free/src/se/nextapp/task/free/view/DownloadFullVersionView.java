package se.nextapp.task.free.view;

import org.json.JSONObject;

import se.nextapp.task.free.MainActivity;
import se.nextapp.task.free.R;
import android.view.LayoutInflater;

public class DownloadFullVersionView extends ContentView {

	public static final int SELECT_VOICE_RECOGNITION = 0;
	public static final int SELECT_APP_LANGUAGE = 1;
	public static final int SEND_FEEDBACK = 2;


	public DownloadFullVersionView(MainActivity activity) {
		super(activity, 0);
		init();
	}

	protected void init() {
		LayoutInflater.from(activity).inflate(R.layout.download_full_version_view, this, true);
		setLayoutParams(new LayoutParams(activity.getContentWidth(), LayoutParams.FILL_PARENT));
	}

	public void leave() {

	}

	public void update(JSONObject data) {
		setColors();
	}

	public void updateContentItemsOrder() {

	}

	public void setColors() {
		
	}
}
