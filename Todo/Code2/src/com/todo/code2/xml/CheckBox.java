package com.todo.code2.xml;

import android.content.Context;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.RelativeLayout;

public class CheckBox extends Button {

	private boolean checked;

	public CheckBox(Context context, boolean checked) {
		super(context);
		this.checked = checked;

		setAttributes();
	}

	private void setAttributes() {
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
		setLayoutParams(params);

		if (checked) {
			setText(":)");
			setEnabled(false);
		} else setText("X");

		setBackgroundColor(0xffcccccc);
	}

}
