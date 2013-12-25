package com.todo.code3.view;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.Transformation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.todo.code3.MainActivity;
import com.todo.code3.R;
import com.todo.code3.adapter.ItemAdapter;
import com.todo.code3.item.ContentItem;
import com.todo.code3.item.FolderItem;
import com.todo.code3.item.TaskItem;
import com.todo.code3.misc.App;
import com.todo.code3.xml.DynamicListView;
import com.todo.code3.xml.HierarchyParent;

public class ItemView extends ContentView {

	private DynamicListView listView;
	private TextView empty;

	private ArrayList<ContentItem> contentItems;
	private ArrayList<ContentItem> selectedItems;

	private ItemAdapter adapter;

	private int itemHeight;
	private int expandingItemId = -1;

	private boolean optionsMode = false;

	public ItemView(MainActivity activity, int parentId) {
		super(activity, parentId);
	}

	protected void init() {
		LayoutInflater.from(activity).inflate(R.layout.item_view, this, true);

		LayoutParams params = new LayoutParams(activity.getContentWidth(), activity.getContentHeight());
		setLayoutParams(params);

		contentItems = new ArrayList<ContentItem>();
		selectedItems = new ArrayList<ContentItem>();

		listView = (DynamicListView) findViewById(R.id.listview);
		listView.setItemView(this);

		adapter = new ItemAdapter(activity, this);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (activity.isMoving()) return;
				if (isInOptionsMode()) return;

				try {
					JSONObject object = new JSONObject(activity.getData().getString(view.getId() + ""));
					activity.open(object.getInt(App.ID));
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});

		empty = (TextView) findViewById(R.id.empty);
		empty.setText("Empty");

		itemHeight = (int) activity.getResources().getDimension(R.dimen.item_height);
	}

	public void update(JSONObject data) {
		try {
			contentItems.clear();

			if (!data.has(parentId + "")) return;
			JSONObject parent = new JSONObject(data.getString(parentId + ""));

			String childrenIds[];

			if (parent.has(App.CHILDREN_IDS)) childrenIds = parent.getString(App.CHILDREN_IDS).split(",");
			else childrenIds = new String[0];

			for (String id : childrenIds) {
				if (!data.has(id)) continue;

				JSONObject object = new JSONObject(data.getString(id));

				if (object.getString(App.TYPE).equals(App.TASK)) {
					TaskItem item = new TaskItem();
					item.setTitle(object.getString(App.NAME));
					item.setId(object.getInt(App.ID));
					item.setType(App.TASK);
					item.setParentId(parentId);
					if (object.has(App.TIMESTAMP_CREATED)) item.setTimestampChecked(object.getInt(App.TIMESTAMP_CREATED));
					if (object.has(App.TIMESTAMP_COMPLETED)) item.setTimestampChecked(object.getInt(App.TIMESTAMP_COMPLETED));
					if (object.has(App.COMPLETED) && object.getBoolean(App.COMPLETED)) item.completed(true);
					else item.completed(false);

					contentItems.add(item);
				} else if (object.getString(App.TYPE).equals(App.FOLDER)) {
					FolderItem item = new FolderItem();
					item.setTitle(object.getString(App.NAME));
					item.setParentId(parentId);
					item.setId(object.getInt(App.ID));
					item.setType(App.FOLDER);
					if (object.has(App.TIMESTAMP_CREATED)) item.setTimestampCreated(object.getInt(App.TIMESTAMP_CREATED));

					contentItems.add(item);
				}
			}

			adapter.notifyDataSetChanged();

			listView.setContentItems(contentItems);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public void expandView(final View view) {
		if (view.getLayoutParams() != null) view.getLayoutParams().height = 1;
		else view.setLayoutParams(new ListView.LayoutParams(LayoutParams.FILL_PARENT, 1));

		Animation animation = new Animation() {
			protected void applyTransformation(float time, Transformation t) {
				if ((int) (itemHeight * time) != 0) view.getLayoutParams().height = (int) (itemHeight * time);
				else view.getLayoutParams().height = 1;

				view.requestLayout();
			}
		};

		animation.setDuration(App.ANIMATION_DURATION);
		view.startAnimation(animation);
	}

	public void collapseView(final View view) {
		AnimationListener al = new AnimationListener() {
			public void onAnimationEnd(Animation a) {
				view.setVisibility(View.GONE);
			}

			public void onAnimationRepeat(Animation a) {
			}

			public void onAnimationStart(Animation a) {
			}
		};

		Animation animation = new Animation() {
			protected void applyTransformation(float time, Transformation t) {
				view.getLayoutParams().height = itemHeight - (int) (itemHeight * time);

				view.requestLayout();
			}
		};

		animation.setAnimationListener(al);
		animation.setDuration(App.ANIMATION_DURATION);
		view.startAnimation(animation);
	}

	public void updateContentItemsOrder() {
		String order = "";
		for (int i = 0; i < contentItems.size(); i++) {
			order += contentItems.get(i).getId() + ",";
		}
		// Removes the last ',' from the string
		order = order.substring(0, order.length() - 1);
		activity.updateChildrenOrder(order, parentId);
	}

	public void toggleItem(int id) {
		// If the selected items contains the id it is removed
		for (ContentItem i : selectedItems) {
			if (i.getId() == id) {
				selectedItems.remove(i);
				updateIcon(id);
				return;
			}
		}

		// If it does not exist, it is added to the selected items
		for (ContentItem i : contentItems) {
			if (i.getId() == id && !selectedItems.contains(i)) {
				selectedItems.add(i);
				updateIcon(id);
				return;
			}
		}
	}

	private void updateIcon(int id) {
		if ((ImageView) getViewById(id).findViewById(R.id.checkbox) == null) return;

		if (isSelected(id)) ((ImageView) getViewById(id).findViewById(R.id.checkbox)).setImageResource(R.drawable.checked);
		else ((ImageView) getViewById(id).findViewById(R.id.checkbox)).setImageResource(R.drawable.box);
	}

	public void performActionOnSelectedItems(int id) {
		if (id == App.OPTIONS_REMOVE) removeSelectedItems();
		if (id == App.OPTIONS_GROUP_ITEMS) groupSelectedItems();
		if (id == App.OPTIONS_SELECT_ALL) toggleSelection();
		if (id == App.OPTIONS_MOVE) moveSelectedItems();
	}

	private void removeSelectedItems() {
		if (selectedItems.size() == 0) return;

		for (ContentItem i : selectedItems) {
			View v = getViewById(i.getId());
			if (v != null) collapseView(v);
		}

		// Since the items should not be removed until they have collapsed
		// the removing of the items is delayed by the animation duration.
		// The selected items array is then emptied.
		new Handler().postDelayed(new Runnable() {
			public void run() {
				for (ContentItem i : selectedItems) {
					activity.removeWithChildren(i.getId());
				}

				selectedItems.clear();
			}
		}, App.ANIMATION_DURATION);
	}

	private void groupSelectedItems() {
		if (selectedItems.size() == 0) return;

		AlertDialog.Builder alert = new AlertDialog.Builder(activity);
		alert.setTitle("Name the new folder");

		final EditText et = new EditText(activity);
		alert.setView(et);
		alert.setPositiveButton("Add", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				final String name = et.getText().toString();
				final int[] selectedItemIds = new int[selectedItems.size()];

				for (int i = 0; i < selectedItems.size(); i++) {
					selectedItemIds[i] = selectedItems.get(i).getId();

					collapseView(getViewById(selectedItems.get(i).getId()));
				}

				selectedItems.clear();

				new Handler().postDelayed(new Runnable() {
					public void run() {
						activity.groupItemsInNewFolder(name, selectedItemIds);
					}
				}, App.ANIMATION_DURATION);
			}
		});

		alert.setNegativeButton("Cancel", null);

		alert.show();

	}

	private void toggleSelection() {
		if (selectedItems.size() != contentItems.size()) {
			selectedItems.clear();
			selectedItems.addAll(contentItems);
		} else selectedItems.clear();

		for (ContentItem i : contentItems)
			updateIcon(i.getId());
	}

	private void moveSelectedItems() {
		if (selectedItems.size() == 0) return;

		AlertDialog.Builder builder = new AlertDialog.Builder(activity);

		builder.setTitle("Select a folder to move to");

		// The actions when pressing "move" are defined later
		// (set onclick for BUTTON1)
		builder.setPositiveButton("Move", null);
		builder.setNegativeButton("Cancel", null);

		final AlertDialog alert = builder.create();

		final HierarchyParent p = new HierarchyParent(activity, activity.getData(), selectedItems) {
			public void onItemSelected(int id, boolean selected) {
				alert.getButton(Dialog.BUTTON1).setEnabled(selected);
			}
		};

		p.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, 1000));
		alert.setView(p);

		alert.show();
		// This sets the position and prevents the alert dialog from
		// "lagging" when its children are animated
		alert.getWindow().setLayout(-1, activity.getContentHeight());

		alert.getButton(Dialog.BUTTON1).setEnabled(false);
		alert.getButton(Dialog.BUTTON1).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				alert.dismiss();

				for (ContentItem i : selectedItems)
					collapseView(getViewById(i.getId()));

				new Handler().postDelayed(new Runnable() {
					public void run() {
						for (ContentItem i : selectedItems) {
							// Checks that the parent id is not the same
							if (i.getParentId() != p.getSelectedItem()) activity.move(i.getId(), p.getSelectedItem());
						}

						selectedItems.clear();
					}
				}, App.ANIMATION_DURATION);
			}
		});
	}

	private View getViewById(int id) {
		for (int i = 0; i < listView.getChildCount(); i++) {
			View v = listView.getChildAt(i);
			if (v.getId() == id) return v;
		}

		return null;
	}

	public boolean isSelected(int id) {
		for (ContentItem i : selectedItems)
			if (i.getId() == id) return true;

		return false;
	}

	public void enterOptionsMode() {
		optionsMode = true;
		// Clears the selected items every time the options mode is entered
		selectedItems.clear();
	}

	public void exitOptionsMode() {
		optionsMode = false;
	}

	public boolean isInOptionsMode() {
		return optionsMode;
	}

	public void leave() {

	}

	public void setExpandingItemId(int id) {
		expandingItemId = id;
	}

	public int getExpandingItemId() {
		return expandingItemId;
	}

	public void invalidateExpandingItemId() {
		expandingItemId = -1;
	}

	public ArrayList<ContentItem> getContentItems() {
		return contentItems;
	}

	public int getItemHeight() {
		return itemHeight;
	}

	public DynamicListView getListView() {
		return listView;
	}
}