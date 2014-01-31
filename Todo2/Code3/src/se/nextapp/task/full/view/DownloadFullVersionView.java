package se.nextapp.task.full.view;

import org.json.JSONObject;

import se.nextapp.task.full.MainActivity;
import se.nextapp.task.full.R;
import se.nextapp.task.full.misc.App;
import se.nextapp.task.full.misc.Sort;
import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

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
