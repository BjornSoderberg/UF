package com.todo.code3.xml;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.todo.code3.R;
import com.todo.code3.animation.CollapseAnimation;
import com.todo.code3.animation.ExpandAnimation;
import com.todo.code3.item.ContentItem;
import com.todo.code3.misc.App;

public class HierarchyParent extends ScrollView {

	private JSONObject data;

	private LinearLayout contentHolder;

	private ArrayList<HierarchyChild> children;
	private ArrayList<ContentItem> selectedItems;

	private Button moveToMenuButton;

	private int selectedItem = -2;
	private int itemHeight;

	public HierarchyParent(Context context, JSONObject data, ArrayList<ContentItem> selectedItems) {
		super(context);
		this.data = data;
		this.selectedItems = selectedItems;

		init();
	}

	private void init() {
		contentHolder = new LinearLayout(getContext());
		contentHolder.setOrientation(LinearLayout.VERTICAL);
		contentHolder.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		addView(contentHolder);

		children = new ArrayList<HierarchyChild>();

		try {
			if (data.has(App.CHILDREN_IDS)) {
				String[] childrenIds = data.getString(App.CHILDREN_IDS).split(",");

				addChildren(childrenIds);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		// only the menu items are visible
		for (HierarchyChild i : children) {
			contentHolder.addView(i);
			if (i.getItemLevel() != 0) {
				i.setVisibility(View.GONE);
				i.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, 1));
			}
		}

		moveToMenuButton = new Button(getContext());
		moveToMenuButton.setBackgroundColor(0xffffffff);
		moveToMenuButton.setText("+   " + getContext().getResources().getString(R.string.add_to_menu));
		moveToMenuButton.setOnClickListener(new OnClickListener() {
			boolean selected = false;

			public void onClick(View v) {
				if (getSelectedItem() != -1) selected = false;

				if (!selected) {
					selectItem(-1);
					selected = true;
				} else {
					selectItem(-2);
					selected = false;
				}
			}
		});

		if (!onlyContainsFolders()) {
			moveToMenuButton.setEnabled(false);
			moveToMenuButton.setText(getContext().getResources().getString(R.string.you_cannot_move_tasks_and_notes_to_the_menu));
		}

		contentHolder.addView(moveToMenuButton);

		itemHeight = (int) getContext().getResources().getDimension(R.dimen.item_height);
	}

	// Same as the other function, but lists the items reversed.
	// This one is used for the first items (menu items) and are therefore reversed
	private void addChildren(String[] childrenIds) {
		int level = 0;

		for (int i = childrenIds.length - 1; i >= 0; i--) {
			String id = childrenIds[i];

			if (!data.has(id)) continue;

			try {
				JSONObject object = new JSONObject(data.getString(id));
				if (!object.getString(App.TYPE).equals(App.FOLDER)) continue;

				HierarchyChild child = new HierarchyChild(getContext(), object, level, this);

				// If the item is not selected, it is added
				// (Folders cannot be moved into themselves)
				if (!isSelected(object.getInt(App.ID))) children.add(child);

				if (object.has(App.CHILDREN_IDS)) addChildren(object.getString(App.CHILDREN_IDS).split(","), level + 1);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	private void addChildren(String[] childrenIds, int level) {
		for (String id : childrenIds) {
			if (!data.has(id)) continue;

			try {
				JSONObject object = new JSONObject(data.getString(id));
				if (!object.getString(App.TYPE).equals(App.FOLDER)) continue;

				HierarchyChild child = new HierarchyChild(getContext(), object, level, this);

				// If the item is not selected, it is added
				// (Folders cannot be moved into themselves)
				if (!isSelected(object.getInt(App.ID))) children.add(child);

				if (object.has(App.CHILDREN_IDS)) addChildren(object.getString(App.CHILDREN_IDS).split(","), level + 1);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	public void expandItem(int id) {
		for (HierarchyChild i : children) {
			if (i.getItemParentId() == id) {
				i.setVisibility(View.VISIBLE);

				// If the view is expanded from earlier, it should be expanded
				// again
				if (i.isExpanded()) expandItem(i.getId());

				expandView(i);
			}
		}
	}

	private void expandView(final View view) {
		new ExpandAnimation(view, App.ANIMATION_DURATION, itemHeight).animate();
	}

	public void collapseItem(int id) {
		for (final HierarchyChild i : children) {
			if (i.getItemParentId() == id) {
				collapseItem(i.getId());

				collapseView(i);
			}
		}
	}

	private void collapseView(final View view) {
		AnimationListener al = new AnimationListener() {
			public void onAnimationEnd(Animation animation) {
				view.setVisibility(View.GONE);
			}

			public void onAnimationRepeat(Animation animation) {
			}

			public void onAnimationStart(Animation animation) {
			}

		};

		new CollapseAnimation(view, App.ANIMATION_DURATION, itemHeight, al).animate();
	}

	public void selectItem(int id) {
		selectedItem = id;

		for (HierarchyChild i : children) {
			if (i.getId() == id) i.getChildAt(0).setBackgroundColor(0xffddddff);
			else {
				i.unselect();
				i.getChildAt(0).setBackgroundColor(0xffffffff);
			}
		}

		if (id == -1) moveToMenuButton.setBackgroundColor(0xffddddff);
		else moveToMenuButton.setBackgroundColor(0xffffffff);

		// -2 because -1 is the id for the menu
		onItemSelected(id, id != -2);
	}

	private boolean onlyContainsFolders() {
		for (ContentItem i : selectedItems) {
			if (!i.getType().equals(App.FOLDER)) return false;
		}

		return true;
	}

	public boolean isSelected(int id) {
		for (ContentItem i : selectedItems) {
			if (i.getId() == id) return true;
		}

		return false;
	}

	public ArrayList<ContentItem> getSelectedItems() {
		return selectedItems;
	}

	public JSONObject getData() {
		return data;
	}

	public int getSelectedItem() {
		return selectedItem;
	}

	public void onItemSelected(int selectedId, boolean selected) {
	}
}
