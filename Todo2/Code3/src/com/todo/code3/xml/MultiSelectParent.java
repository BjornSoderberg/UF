package com.todo.code3.xml;

import java.util.ArrayList;
import java.util.Arrays;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;

import com.todo.code3.misc.Reminder;

public class MultiSelectParent extends LinearLayout {

	private ArrayList<String> strings;
	private ArrayList<Integer> values;
	private String reminderInfo;
	private String type;

	private boolean generated = false;

	public MultiSelectParent(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public MultiSelectParent(Context context) {
		super(context);
		init();
	}

	private void init() {
		strings = new ArrayList<String>();
		values = new ArrayList<Integer>();

		setOrientation(LinearLayout.VERTICAL);
		setVisibility(View.GONE);
	}

	public void setStrings(String... strings) {
		this.strings.clear();
		for (String s : strings)
			this.strings.add(s);
	}

	public void setReminderInfo(String s) {
		reminderInfo = s;
	}

	public void setType(String s) {
		type = s;
	}

	public void setValues(Integer... integers) {
		values.clear();
		for (int i : integers)
			values.add(i);
	}

	public void generate() {
		for (int i = 0; i < strings.size(); i++) {
			CheckBox c = new CheckBox(getContext());
			c.setText(strings.get(i));
			c.setId(values.get(i));
			c.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					changed();
				}
			});
			addView(c);

			if (reminderInfo != null) {
				String[] p = reminderInfo.split(",");
				for (String s : p)
					if (s.equals(values.get(i) + "")) c.setChecked(true);
			}
		}

		setVisibility(View.VISIBLE);

		generated = true;
	}

	public void update(String string) {
		generated = false;
		setReminderInfo(string);

		for (int i = 0; i < values.size(); i++) {
			CheckBox c = getCheckBoxChildById(values.get(i));
			if (c == null) continue;

			c.setChecked(false);
			if (reminderInfo != null) {
				String[] p = reminderInfo.split(",");
				for (String s : p)
					if (s.equals(values.get(i) + "") && Reminder.getType(reminderInfo).equals(type)) c.setChecked(true);
			}
		}
		
		generated = true;
	}

	private void changed() {
		String s = "";

		for (int i = 0; i < strings.size(); i++) {
			CheckBox c = getCheckBoxChildById(values.get(i));
			if (c == null) continue;
			if (c.isChecked()) s += c.getId() + ",";
		}
		if (s.length() > 0 && s.charAt(s.length() - 1) == ',') s = s.substring(0, s.length() - 1);

		if (generated) onChanged(type, s);
	}

	public void onChanged(String type, String changed) {

	}

	private CheckBox getCheckBoxChildById(int id) {
		for (int i = 0; i < getChildCount(); i++)
			if (getChildAt(i).getId() == id && getChildAt(i) instanceof CheckBox) return (CheckBox) getChildAt(i);

		return null;
	}
}
