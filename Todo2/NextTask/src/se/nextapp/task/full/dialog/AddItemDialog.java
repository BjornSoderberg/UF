package se.nextapp.task.full.dialog;

import se.nextapp.task.full.MainActivity;
import se.nextapp.task.full.R;
import se.nextapp.task.full.misc.App;
import android.app.AlertDialog;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class AddItemDialog extends Dialog {

	private AlertDialog alert;

	public AddItemDialog(MainActivity activity, String title, String message) {
		super(activity, title, message, false);

		init();
	}

	public AddItemDialog(MainActivity activity, String title, String message, String posButtonString, String negButtonString) {
		super(activity, title, message, false, posButtonString, negButtonString);

		init();
	}

	protected void init() {
		super.init();

		RelativeLayout r = new RelativeLayout(activity);
//		l.setWeightSum(3);

		alert = create();

		FrameLayout task = new FrameLayout(activity);
		ImageView taskImg = new ImageView(activity);
		task.addView(taskImg);
		task.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		((RelativeLayout.LayoutParams)task.getLayoutParams()).addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		task.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				askForName(App.TASK);
				alert.dismiss();
			}
		});

		FrameLayout note = new FrameLayout(activity);
		ImageView noteImg = new ImageView(activity);
		note.addView(noteImg);
		note.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		((RelativeLayout.LayoutParams) note.getLayoutParams()).addRule(RelativeLayout.CENTER_HORIZONTAL);
		note.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				askForName(App.NOTE);
				alert.dismiss();
			}
		});

		FrameLayout folder = new FrameLayout(activity);
		ImageView folderImg = new ImageView(activity);
		folder.addView(folderImg);
		folder.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		((RelativeLayout.LayoutParams)folder.getLayoutParams()).addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		folder.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				askForName(App.FOLDER);
				alert.dismiss();
			}
		});

		r.addView(task);
		r.addView(note);
		r.addView(folder);

		alert.setView(r);
		
		DisplayMetrics dm = new DisplayMetrics();
		alert.getWindow().getWindowManager().getDefaultDisplay().getMetrics(dm);
		int s =(int)( dm.widthPixels * 1 / 3);
		if(s > dm.heightPixels * 1/3) s = (int)(dm.heightPixels * 1/3);
		note.getLayoutParams().height = s;
		note.getLayoutParams().width = s;
		task.getLayoutParams().height = s;
		task.getLayoutParams().width = s;
		folder.getLayoutParams().height = s;
		folder.getLayoutParams().width = s;
		
		int ss = s * 2/3;
		int p = (s-ss) / 2;

		taskImg.setLayoutParams(new FrameLayout.LayoutParams(ss, ss));
		noteImg.setLayoutParams(new FrameLayout.LayoutParams(ss, ss));
		folderImg.setLayoutParams(new FrameLayout.LayoutParams(ss, ss));

		task.setPadding(p, p, p, p);
		note.setPadding(p, p, p, p);
		folder.setPadding(p, p, p, p);
		
		Drawable d = alert.getContext().getResources().getDrawable(R.drawable.ic_folder_big).mutate();
		d.setColorFilter(new PorterDuffColorFilter(alert.getContext().getResources().getColor(R.color.aqua_blue), PorterDuff.Mode.MULTIPLY));
		folderImg.setBackgroundDrawable(d);
		
		d = alert.getContext().getResources().getDrawable(R.drawable.ic_note_big).mutate();
		d.setColorFilter(new PorterDuffColorFilter(alert.getContext().getResources().getColor(R.color.aqua_blue), PorterDuff.Mode.MULTIPLY));
		noteImg.setBackgroundDrawable(d);
		
		d = alert.getContext().getResources().getDrawable(R.drawable.ic_task_big).mutate();
		d.setColorFilter(new PorterDuffColorFilter(alert.getContext().getResources().getColor(R.color.aqua_blue), PorterDuff.Mode.MULTIPLY));
		taskImg.setBackgroundDrawable(d);
	}

	public void askForName(final String type) {
		Resources r = activity.getResources();
		String t = "";
		if (type.equals(App.TASK)) t = activity.getResources().getString(R.string.task);
		else if (type.equals(App.NOTE)) t = activity.getResources().getString(R.string.note);
		else if (type.equals(App.FOLDER)) t = activity.getResources().getString(R.string.folder);
		TextLineDialog d = new TextLineDialog(activity, r.getString(R.string.add_new) + " " + t, null, true, r.getString(R.string.add), r.getString(R.string.cancel)) {
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

	public void onResult(String name, String type) {

	}

	public AlertDialog show() {
		alert.show();
		return alert;
	}

	protected Object getResult() {
		return null;
	}
}
