package com.todo.code3.xml;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.todo.code3.misc.App;

public class FolderHierarchyViewer extends LinearLayout {

	private JSONObject data;
	private ArrayList<FolderHierarchyViewer> items;
	private int id = -1;
	private int level = 0;

	public FolderHierarchyViewer(Context context, AttributeSet attrs, JSONObject data, int id, int level) {
		super(context, attrs);
		this.data = data;
		this.id = id;
		this.level = level;

		init();
	}

	public FolderHierarchyViewer(Context context, JSONObject data, int id, int level) {
		super(context);
		this.data = data;
		this.id = id;
		this.level = level;

		init();
	}

	private void init() {
		items = new ArrayList<FolderHierarchyViewer>();

		setOrientation(LinearLayout.VERTICAL);

		try {
			TextView t = new TextView(getContext());
			String s = "";
			for (int i = 0; i < level; i++)
				s += "     ";
			if (data.has(id + "")) {
				t.setText(s + new JSONObject(data.getString(id + "")).getString(App.NAME) + " is here");
				t.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, 50));
				addView(t);
			}

			String[] childrenIds;
			if (id == -1) childrenIds = data.getString(App.CHILDREN_IDS).split(",");
			else {
				if (!new JSONObject(data.getString(id + "")).has(App.CHILDREN_IDS)) return;
				childrenIds = new JSONObject(data.getString(id + "")).getString(App.CHILDREN_IDS).split(",");
			}

			for (final String id : childrenIds) {
				if (!data.has(id)) continue;

				final JSONObject object = new JSONObject(data.getString(id));
				if (object.getString(App.TYPE).equals(App.FOLDER)) {
					final FolderHierarchyViewer f = new FolderHierarchyViewer(getContext(), data, Integer.parseInt(id), level + 1);
					f.setBackgroundColor(0x222222 * level + 0xff000000);
					items.add(f);
				}
			}

			for (FolderHierarchyViewer f : items) {
				addView(f);
			}

			if (id != -1) {
				for (FolderHierarchyViewer f : items) {
					f.setVisibility(View.GONE);
				}
				
				setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						if(items.size() > 0) {
							
							int visibility = 0;
							if(items.get(0).getVisibility() == View.VISIBLE) visibility = View.GONE;
							else visibility = View.VISIBLE;
							
							for(FolderHierarchyViewer f : items) {
								f.setVisibility(visibility);
							}
						}
					}
				});
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
