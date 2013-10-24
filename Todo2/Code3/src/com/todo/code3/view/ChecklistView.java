package com.todo.code3.view;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.todo.code3.MainActivity;
import com.todo.code3.R;
import com.todo.code3.adapter.ChecklistAdapter;
import com.todo.code3.misc.App;
import com.todo.code3.xml.ChecklistItem;

public class ChecklistView extends ContentView {
	
	private ListView listView;
	
	protected int currentFolder = -1;
	
	protected ArrayList<ChecklistItem> checklistItems;
	
	public ChecklistView(MainActivity activity, int currentFolder) {
		super(activity);
		this.currentFolder = currentFolder;
	}

	protected void init() {
		View v = ((Activity) getContext()).getLayoutInflater().inflate(
				R.layout.checklist_view, null);
		LayoutParams params = new LayoutParams(activity.getContentWidth(),
				activity.getContentHeight());
		v.setLayoutParams(params);
		addView(v);

		checklistItems = new ArrayList<ChecklistItem>();

		listView = (ListView) v.findViewById(R.id.listview);
		ChecklistAdapter adapter = new ChecklistAdapter(activity, this);

		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				try {
					JSONObject folder = new JSONObject(activity.getData().getString(App.FOLDER + currentFolder));
					JSONObject checklist = new JSONObject(folder.getString(App.CHECKLIST + checklistItems.get(view.getId()).getId()));
					
					if(checklistItems.get(view.getId()).isEnabled()) activity.openChecklist(checklist);
				} catch(JSONException e) {
					e.printStackTrace();
				}
			}
		});
	}

	public void update(JSONObject data) {
		try {
			checklistItems.clear();
			
			JSONObject parent = new JSONObject(data.getString(App.FOLDER + currentFolder));
			
			for(int i = 0; i < parent.getInt(App.NUM_CHILDREN); i++) {
				if(parent.has(App.CHECKLIST + i)) {
					JSONObject checklist = new JSONObject(parent.getString(App.CHECKLIST + i));
					
					ChecklistItem ci = new ChecklistItem();
					ci.setTitle(checklist.getString(App.NAME));
					ci.setFolderId(currentFolder);
					ci.setId(checklist.getInt(App.ID));
					
					checklistItems.add(ci);
				}	
			}
			
			((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();
		} catch(JSONException e) {
			e.printStackTrace();
		}
	}
	
	public void setCurrentFolder(int folder) {
		currentFolder = folder;
	}
	
	public ArrayList<ChecklistItem> getChecklistItems() {
		return checklistItems;
	}
}
