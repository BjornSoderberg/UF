package se.nextapp.task.full.xml.hierarchy;

import org.json.JSONException;
import org.json.JSONObject;

import se.nextapp.task.full.*;
import se.nextapp.task.full.misc.App;

import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.todo.code3.R;

public class HierarchyChild extends RelativeLayout {

	private HierarchyParent parent;

	private FrameLayout expandButton;
	private TextView text;

	private JSONObject object;
	private MainActivity activity;

	private int level;

	private boolean expanded = false;
	private boolean selected = false;

	public HierarchyChild(MainActivity activity, JSONObject object, int level, HierarchyParent parent) {
		super(activity);
		this.activity = activity;
		this.object = object;
		this.level = level;
		this.parent = parent;

		init();
	}

	private void init() {
		LayoutInflater.from(getContext()).inflate(R.layout.hierarchy_item, this, true);

		try {
			setId(object.getInt(App.ID));
		} catch (JSONException e) {
			e.printStackTrace();
		}

		expandButton = (FrameLayout) findViewById(R.id.expand);
		text = (TextView) findViewById(R.id.item_text);

		try {
			String s = "";
			for (int i = 0; i < level; i++)
				s += "   ";
			text.setText(s + object.getString(App.NAME));
		} catch (JSONException e) {
			e.printStackTrace();
		}

		expandButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (!expanded) {
					expanded = true;
					parent.expandItem(getId());
				} else {
					expanded = false;
					parent.collapseItem(getId());
				}
			}
		});

		setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (!isSelectable()) return;

				// -2 because -1 is the id for the menu
				if (selected) {
					parent.selectItem(-2);
					selected = false;
				} else {
					parent.selectItem(getId());
					selected = true;
				}
			}
		});

		if (!isExpandable()) expandButton.setVisibility(View.GONE);
		if (!isSelectable()) text.setText(text.getText() + " (" + getContext().getResources().getString(R.string.current) + ")");

		((ImageView) findViewById(R.id.expandIcon)).getBackground().setColorFilter(new PorterDuffColorFilter(getResources().getColor(R.color.icon_color), PorterDuff.Mode.MULTIPLY));

		setColors();
	}

	private void setColors() {
		int colorId = activity.isDarkTheme() ? R.color.text_color_dark : R.color.text_color_checked_light;
		text.setTextColor(activity.getResources().getColor(colorId));
	}

	public int getItemParentId() {
		try {
			if (object.has(App.PARENT_ID)) return object.getInt(App.PARENT_ID);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return -1;
	}

	public int getItemLevel() {
		return level;
	}

	public boolean isExpanded() {
		return expanded;
	}

	public void unselect() {
		selected = false;
	}

	// Checks if the item has folders has children and if the child is selected
	// If the item has an unselected folder child, it can be expanded
	public boolean isExpandable() {
		try {
			String[] childrenIds = object.getString(App.CHILDREN_IDS).split(",");

			for (String id : childrenIds) {
				if (!parent.getData().has(id)) continue;

				if (new JSONObject(parent.getData().getString(id)).getString(App.TYPE).equals(App.FOLDER)) {
					if (!parent.isSelected(Integer.parseInt(id))) return true;
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return false;
	}

	// If the selected items' parent is this folder, it is not selectable
	public boolean isSelectable() {
		if (parent.getSelectedItems().get(0).getParentId() == getId()) return false;
		return true;
	}
}
