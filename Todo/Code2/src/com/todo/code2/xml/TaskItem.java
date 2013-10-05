package com.todo.code2.xml;

import android.R;
import android.content.Context;
import android.text.style.TextAppearanceSpan;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TaskItem extends LinearLayout {
	
	protected int width, height;
	
	private CheckBox cb;
	
	private String string = "Checklist";
	
	public TaskItem(Context context, String string) {
		super(context);
		if(string != null && string != "") this.string = string;
		setAttributes();
	}

	public TaskItem(Context context) {
		super(context);
		setAttributes();
	}

	public TaskItem(Context context, AttributeSet attrs) {
		super(context, attrs);
		setAttributes();
	}
	
	public CheckBox getCheckBox() {
		return cb;
	}

	private void setAttributes() {
		setOrientation(LinearLayout.VERTICAL);
		
		LinearLayout linear = new LinearLayout(getContext());
		linear.setOrientation(LinearLayout.HORIZONTAL);
		
		cb = new CheckBox(getContext());
		linear.addView(cb);
		
		TextView tv = new TextView(getContext(), null, android.R.attr.textAppearanceLarge);
		tv.setText(string);
		tv.setPadding(30, 0, 0, 0);
		linear.addView(tv);
		
		addView(linear);
		
		LinearLayout linear2 = new LinearLayout(getContext());
		linear2.setBackgroundColor(0xffaaaaaa);
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, 2);
		linear2.setLayoutParams(params);
		addView(linear2);
		
		}
}
