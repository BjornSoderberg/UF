package com.todo.code3.xml.multi_select_parent;

import java.util.ArrayList;

import android.content.Context;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.todo.code3.misc.Reminder;

public class Selector extends LinearLayout {

	protected ArrayList<String> strings;
	protected ArrayList<Integer> values;

	private RadioGroup radioGroup;
	private EditText sizeET;

	private int size = -1;
	private int type = -1;

	private boolean updated = false;

	public Selector(Context context) {
		super(context);
		init();
	}

	private void init() {
		strings = new ArrayList<String>();
		values = new ArrayList<Integer>();

		setOrientation(LinearLayout.VERTICAL);

		sizeET = new EditText(getContext());
		sizeET.setRawInputType(InputType.TYPE_CLASS_NUMBER);
		sizeET.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {
			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (updated) changed();
			}
		});

		radioGroup = new RadioGroup(getContext());
		radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if (updated) changed();
			}
		});

		addView(sizeET);
		addView(radioGroup);
	}

	public void setStrings(String... strings) {
		this.strings.clear();
		for (String s : strings)
			this.strings.add(s);
	}

	public void setValues(int... values) {
		this.values.clear();
		for (int i : values)
			this.values.add(i);
	}

	public void generate() {
		radioGroup.removeAllViews();

		for (int i = 0; i < strings.size(); i++) {
			RadioButton b = new RadioButton(getContext());
			b.setText(strings.get(i));
			b.setId(values.get(i));
			radioGroup.addView(b);
		}
	}

	public void update(String reminderInfo) {
		if (!Reminder.getType(reminderInfo).equals(type)) return;

		updated = false;

		try {
			int checkedValue = Integer.parseInt(Reminder.getPart(reminderInfo, 1));
			radioGroup.check(checkedValue);

			String size = Reminder.getPart(reminderInfo, 2);
			sizeET.setText(size);
		} catch (NumberFormatException e) {

		}
		updated = true;
	}

	private void changed() {
		type = radioGroup.getCheckedRadioButtonId();
		try {
			size = Integer.parseInt(sizeET.getText().toString());
		} catch (NumberFormatException e) {
			size = -1;
		}

		onChanged(type, size);
	}

	public void onChanged(int type, int size) {

	}
}
