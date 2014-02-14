package se.nextapp.task.full.dialog;

import se.nextapp.task.full.MainActivity;
import se.nextapp.task.full.R;
import se.nextapp.task.full.misc.App;
import se.nextapp.task.full.tutorial.TutorialState;
import android.app.AlertDialog;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class AddItemDialog extends Dialog {

	private AlertDialog alert;

	private String highlight = "";

	public AddItemDialog(MainActivity activity, String title, String message) {
		super(activity, title, message, false);

		init();
	}

	public AddItemDialog(MainActivity activity, String title, String message, String highlight) {
		super(activity, title, message, false);
		this.highlight = highlight;

		init();
	}

	public AddItemDialog(MainActivity activity, String title, String message, String posButtonString, String negButtonString) {
		super(activity, title, message, false, posButtonString, negButtonString);

		init();
	}

	public AddItemDialog(MainActivity activity, String title, String message, String posButtonString, String negButtonString, String highlight) {
		super(activity, title, message, false, posButtonString, negButtonString);
		this.highlight = highlight;

		init();
	}

	protected void init() {
		super.init();

		RelativeLayout r = new RelativeLayout(activity);

		alert = create();

		LayoutInflater inflater = LayoutInflater.from(activity);
		Drawable d;

		DisplayMetrics dm = new DisplayMetrics();
		alert.getWindow().getWindowManager().getDefaultDisplay().getMetrics(dm);

		int textHeight = (int) activity.getResources().getDimension(R.dimen.text_normal);
		int size = (int) (dm.widthPixels * 1 / 3);
		if (size > dm.heightPixels * 1 / 3) size = (int) (dm.heightPixels * 1 / 3);
		size -= textHeight;

		int imgSize = size * 2 / 3;
		int padding = (size - imgSize) / 2;

		int[] imgRes = { R.drawable.ic_task_big, R.drawable.ic_note_big, R.drawable.ic_folder_big };
		int[] stringRes = { R.string.Task, R.string.Note, R.string.Folder };
		int[] viewRules = { RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.ALIGN_PARENT_RIGHT };
//		int[] anchors = { R.drawable.ic_note, -1, R.drawable.ic_note };
		final String[] types = { App.TASK, App.NOTE, App.FOLDER };
		
		// Just set correct layout size
		r.setLayoutParams(new FrameLayout.LayoutParams(200, LayoutParams.WRAP_CONTENT));

		for (int j = 0; j < 3; j++) {
			/////////// FIX THIS
			// ALIGN CENTER ITEM INSTEAD OF FLOATING
			// ADD ITEM DIALOG SHOUld not have avbryt & klar as options
			int i = j;
			if(j == 0) i = 1;
			if(j == 1) i = 0;
			final String type = types[i];

			boolean highlighted = type.equals(highlight);
			final boolean clickable = highlighted || highlight == "";

			View v = inflater.inflate(R.layout.add_dialog_item, null);
			v.setLayoutParams(new RelativeLayout.LayoutParams(size, LayoutParams.WRAP_CONTENT));
			v.setPadding(0, textHeight, 0, textHeight);
			v.setPadding(padding, 0, padding, 0);
			if(type.equals(App.TASK)) v.setId(R.drawable.ic_note);

			d = activity.getResources().getDrawable(imgRes[i]).mutate();
			d.setColorFilter(new PorterDuffColorFilter(alert.getContext().getResources().getColor(highlighted ? R.color.highlight_color : R.color.aqua_blue), PorterDuff.Mode.MULTIPLY));

			((ImageView) v.findViewById(R.id.item_image)).setBackgroundDrawable(d);
			((ImageView) v.findViewById(R.id.item_image)).setLayoutParams(new FrameLayout.LayoutParams(imgSize, imgSize));

			((FrameLayout) v.findViewById(R.id.item_frame)).setLayoutParams(new LinearLayout.LayoutParams(imgSize, imgSize));

			((TextView) v.findViewById(R.id.item_text)).setText(activity.getResources().getString(stringRes[i]));
//			if (anchors[i] != -1) ((RelativeLayout.LayoutParams) v.getLayoutParams()).addRule(viewRules[i], anchors[i]);
			((RelativeLayout.LayoutParams) v.getLayoutParams()).addRule(viewRules[i]);

			v.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					if (clickable) {
						askForName(type);
						alert.dismiss();
					}
				}
			});

			r.addView(v);
		}

		if (Build.VERSION.SDK_INT < 11) r.setBackgroundColor(activity.isDarkTheme() ? 0xff000000 : 0xffffffff);

		alert.setView(r);
	}

	public void askForName(final String type) {
		Resources r = activity.getResources();
		String t = "";
		if (type.equals(App.TASK)) t = activity.getResources().getString(R.string.task);
		else if (type.equals(App.NOTE)) t = activity.getResources().getString(R.string.note);
		else if (type.equals(App.FOLDER)) t = activity.getResources().getString(R.string.folder);
		
		boolean cancelable = activity.getTutorialState() != TutorialState.ADD_TASK;
		
		TextLineDialog d = new TextLineDialog(activity, r.getString(R.string.add_new) + " " + t, null, true, r.getString(R.string.add), r.getString(R.string.cancel), cancelable) {
			protected void onResult(Object result) {
				super.onResult(result);

				if (result instanceof String) AddItemDialog.this.onResult((String) result, type);
				else AddItemDialog.this.onResult("", type);
			}
		};

		d.show();
	}

	public void onVoiceRecognitionResult(Bundle result) {
	}

	protected void onResult(String name, String type) {

	}

	public AlertDialog show() {
		alert.show();
		return alert;
	}

	protected Object getResult() {
		return null;
	}
}
