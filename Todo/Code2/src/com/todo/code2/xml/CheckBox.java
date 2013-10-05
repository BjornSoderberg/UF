package com.todo.code2.xml;

import android.content.Context;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.RelativeLayout;

public class CheckBox extends Button {
	
	public CheckBox(Context context) {
		super(context);
		setAttributes();
	}
	
	private void setAttributes() {
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
		setLayoutParams(params);
		
		setText("X");
		setBackgroundColor(0xffcccccc);
	}

}
