package se.nextapp.task.free.xml.multi_select_parent;

import java.util.ArrayList;

import android.content.Context;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

public class ScrollSelector extends ScrollView {

	private ArrayList<String> items;
	private ArrayList<Integer> values;

	private LinearLayout container;

	public ScrollSelector(Context context) {
		super(context);

		init();
	}

	private void init() {
		container = new LinearLayout(getContext());
		container.setOrientation(LinearLayout.VERTICAL);
		addView(container);

		items = new ArrayList<String>();
		values = new ArrayList<Integer>();
	}

	public void setSelectItems(String... items) {
		this.items.clear();
		for (String item : items)
			this.items.add(item);
	}

	public void setValues(int... values) {
		this.values.clear();
		for (int value : values)
			this.values.add(value);
	}

	public void generate() {
		container.removeAllViews();

		for (int asd = 0; asd < 2; asd++) {
			for (int i = 0; i < items.size(); i++) {
				TextView t = new TextView(getContext());
				t.setLayoutParams(new LinearLayout.LayoutParams(-2, -1));
				t.setPadding(20, 20, 20, 20);
				t.setText(items.get(i));
				container.addView(t);
			}
		}
		
		setLayoutParams(new LinearLayout.LayoutParams(-1, 100));
	}
}
