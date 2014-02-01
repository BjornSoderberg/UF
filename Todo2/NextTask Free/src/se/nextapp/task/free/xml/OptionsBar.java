package se.nextapp.task.free.xml;

import java.util.ArrayList;

import se.nextapp.task.free.MainActivity;
import se.nextapp.task.free.R;
import se.nextapp.task.free.misc.App;
import se.nextapp.task.free.view.ItemView;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class OptionsBar extends LinearLayout {

	private MainActivity activity;

	// Holds ids for different options items
	private ArrayList<Integer> items;

	private int selectedCount = 0;
	private boolean allSelected = false;

	public OptionsBar(Context context) {
		super(context);

		init();
	}

	public OptionsBar(Context context, AttributeSet attrs) {
		super(context, attrs);

		init();
	}

	private void init() {
		items = new ArrayList<Integer>();

		setOrientation(LinearLayout.HORIZONTAL);
	}

	public void setMainActivity(MainActivity a) {
		activity = a;
	}

	private void updateItems() {
		removeAllViews();
		if (items.size() == 0) return;

		// Dividing the size - the size of the separating lines (2 px thick)
		// 1 is added so that the whole width of the options bar is used
		int itemWidth = (activity.getContentWidth() - (items.size() - 1) * 2) / items.size() + 1;

		for (final int id : items) {
			LinearLayout b = new LinearLayout(getContext());
			b.setLayoutParams(new LayoutParams(itemWidth, LayoutParams.MATCH_PARENT));
			b.setBackgroundColor(0);
			b.setGravity(Gravity.CENTER);
			b.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					if (activity.isMoving()) return;

					if (activity.getOpenContentView() instanceof ItemView) {
						ItemView i = (ItemView) activity.getOpenContentView();
						if (i.isInOptionsMode()) i.performActionOnSelectedItems(id);
					}
				}
			});

			int imgRes = 0;
			int mult = 0xffffffff; // white
			if (id == App.OPTIONS_MOVE) imgRes = R.drawable.ic_move;
			else if (id == App.OPTIONS_GROUP_ITEMS) imgRes = R.drawable.ic_group;
			else if (id == App.OPTIONS_REMOVE) imgRes = R.drawable.ic_trash;
			else if (id == App.OPTIONS_SELECT_ALL) {
				if (allSelected) imgRes = R.drawable.ic_unselect;
				else imgRes = R.drawable.ic_select;
			}
			
			// if no items are selected, the icon is semi transparent
			if(id == App.OPTIONS_MOVE || id == App.OPTIONS_GROUP_ITEMS || id == App.OPTIONS_REMOVE) {
				if(selectedCount <= 0) mult = 0x4cffffff; // semi transparent
			}

			ImageView i = new ImageView(activity);
			i.setLayoutParams(new LinearLayout.LayoutParams((int)getResources().getDimension(R.dimen.item_image_height), (int)getResources().getDimension(R.dimen.item_image_height)));
			i.setBackgroundDrawable(activity.getResources().getDrawable(imgRes));
			i.getBackground().setColorFilter(new PorterDuffColorFilter(mult, PorterDuff.Mode.MULTIPLY));
			b.addView(i);

			addView(b);

			// The border between the items
			if (items.indexOf(id) != items.size() - 1) {
				LinearLayout l = new LinearLayout(getContext());
				l.setLayoutParams(new LayoutParams(2, LayoutParams.MATCH_PARENT));
				l.setBackgroundColor(0xff730592);
				// addView(l);
			}
		}
	}

	public void clearOptionsItems() {
		items.clear();

		updateItems();
	}

	public void addOptionsItem(int id) {
		if (!items.contains(id)) items.add(id);

		updateItems();
	}

	public void removeOptionsItem(int id) {
		if (items.contains(id)) items.remove(id);

		updateItems();
	}

	public void updateSelectedCount(int count, boolean allSelected) {
		selectedCount = count;
		this.allSelected = allSelected;
		updateItems();
	}
}
