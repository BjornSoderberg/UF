package com.todo.code2.xml;

import android.content.Context;
import android.graphics.Paint;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TaskItem extends LinearLayout {

	protected int width, height;

	private CheckBox cb;

	private String string = "Checklist";
	private boolean checked;

	public TaskItem(Context context, String string, boolean checked) {
		super(context);
		if (string != null && !string.equals("")) this.string = string;
		this.checked = checked;
		
		setAttributes();
	}

	public CheckBox getCheckBox() {
		return cb;
	}

	private void setAttributes() {
		setOrientation(LinearLayout.VERTICAL);

		LinearLayout content = new LinearLayout(getContext());
		content.setOrientation(LinearLayout.HORIZONTAL);

		cb = new CheckBox(getContext(), checked);
		content.addView(cb);

		TextView name = new TextView(getContext(), null, android.R.attr.textAppearanceLarge);
		name.setText(string);

		name.setPadding(30, 0, 0, 0);
		
		if (checked) {
			name.setTextColor(0xff888888);
			name.setPaintFlags(name.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
		}

		content.addView(name);
		addView(content);

		LinearLayout border = new LinearLayout(getContext());
		border.setBackgroundColor(0xffaaaaaa);
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, 2);
		border.setLayoutParams(params);
		addView(border);

	}
}
