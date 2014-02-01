package se.nextapp.task.free.xml.multi_select_parent;

import java.util.ArrayList;

import se.nextapp.task.free.misc.Reminder;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;


public class MultiSelectParent extends LinearLayout {

	protected ArrayList<String> strings;
	protected ArrayList<Integer> values;
	protected String reminderInfo;
	protected String type;

	private boolean generated = false;

	public MultiSelectParent(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public MultiSelectParent(Context context) {
		super(context);
		init();
	}

	protected void init() {
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
		if(!Reminder.getType(string).equals(type)) return;
		
		generated = false;
		reminderInfo = string;

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

	protected void changed() {
		ArrayList<Integer> selected = new ArrayList<Integer>();

		for (int i = 0; i < strings.size(); i++) {
			CheckBox c = getCheckBoxChildById(values.get(i));
			if (c == null) continue;
			if (c.isChecked()) selected.add(c.getId());
		}

		if (generated) onChanged(type, selected);
	}

	public void onChanged(String type, ArrayList<Integer> selected) {

	}

	private CheckBox getCheckBoxChildById(int id) {
		for (int i = 0; i < getChildCount(); i++)
			if (getChildAt(i).getId() == id && getChildAt(i) instanceof CheckBox) return (CheckBox) getChildAt(i);

		return null;
	}
}
