package com.todo.code3.view;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.Transformation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.todo.code3.MainActivity;
import com.todo.code3.R;
import com.todo.code3.adapter.AllAdapter;
import com.todo.code3.misc.App;
import com.todo.code3.xml.ContentItem;
import com.todo.code3.xml.DynamicListView;
import com.todo.code3.xml.FolderItem;
import com.todo.code3.xml.TaskItem;

public class AllView extends ContentView {

	private DynamicListView listView;
	private TextView empty;

	private ArrayList<ContentItem> contentItems;

	private AllAdapter adapter;

	private int itemHeight;
	private int expandingItemId = -1;

	public AllView(MainActivity activity, int parentId) {
		super(activity, parentId);
	}

	protected void init() {
		View v = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.all_item_view, null);

		LayoutParams params = new LayoutParams(activity.getContentWidth(), activity.getContentHeight());
		v.setLayoutParams(params);
		addView(v);

		contentItems = new ArrayList<ContentItem>();

		listView = (DynamicListView) v.findViewById(R.id.listview);
		listView.setContentView(this);

		adapter = new AllAdapter(activity, this);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (activity.isMoving()) return;

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
		if(view.getLayoutParams() != null) view.getLayoutParams().height = 1;
		else view.setLayoutParams(new ListView.LayoutParams(LayoutParams.FILL_PARENT, 1));
		
		new Handler().postDelayed(new Runnable() {
			public void run() {
				AnimationListener al = new AnimationListener() {
					public void onAnimationEnd(Animation a) {
						activity.isMoving(false);
					}

					public void onAnimationRepeat(Animation a) {
					}
					
					public void onAnimationStart(Animation a) {
						activity.isMoving(true);
					}
				};
				
				Animation animation = new Animation() {
					protected void applyTransformation(float time, Transformation t) {
						if((int) (itemHeight * time) != 0) view.getLayoutParams().height = (int) (itemHeight * time);
						else view.getLayoutParams().height = 1;
						
						view.requestLayout();
					}
				};
				
				animation.setAnimationListener(al);
				animation.setDuration(App.EXPAND_ANIMATION_DURATION);
				view.startAnimation(animation);
			}
		}, 0);
	}

	public void updateContentItemsOrder() {
		String order = "";
		for(int i = 0; i< contentItems.size(); i++) {
			order += contentItems.get(i).getId() + ",";
		}
		// Removes the last ',' from the string
		order = order.substring(0, order.length() - 1);
		activity.updateChildrenOrder(order, parentId);
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
}
