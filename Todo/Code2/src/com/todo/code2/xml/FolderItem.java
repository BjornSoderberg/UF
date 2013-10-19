package com.todo.code2.xml;

import android.content.Context;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class FolderItem extends LinearLayout {

	private String string = "Folder";

	public FolderItem(Context context, String string) {
		super(context);
		if (string != null && !string.equals("")) this.string = string;

		setAttributes();
	}

	private void setAttributes() {
		setOrientation(LinearLayout.VERTICAL);

		LinearLayout content = new LinearLayout(getContext());
		content.setOrientation(LinearLayout.HORIZONTAL);

		Button icon = new Button(getContext());
		icon.setText("Icon");
		icon.setBackgroundColor(0xff);
		//icon.setEnabled(false);

		content.addView(icon);

		TextView name = new TextView(getContext(), null, android.R.attr.textAppearanceLarge);
		name.setText(string);

		name.setPadding(30, 0, 0, 0);
		content.addView(name);
		addView(content);

		LinearLayout border = new LinearLayout(getContext());
		border.setBackgroundColor(0xff0c0c0c);
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, 2);
		border.setLayoutParams(params);
		addView(border);
	}

}
