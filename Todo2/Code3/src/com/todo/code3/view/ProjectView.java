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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.todo.code3.MainActivity;
import com.todo.code3.R;
import com.todo.code3.adapter.ProjectAdapter;
import com.todo.code3.misc.App;
import com.todo.code3.xml.ChecklistItem;

public class ProjectView extends ContentView {
	
	private ListView listView;
	private LinearLayout stats;
	private TextView empty;
	
	// the same as currentFolder
	protected int currentProject = -1;
	
	protected ArrayList<ChecklistItem> checklistItems;

	public ProjectView(MainActivity activity, int currentProject) {
		//super(activity, currentProject);
		super(activity);
		this.currentProject = currentProject;
	}

	protected void init() {
		View view = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.project_view, null);
		LayoutParams params = new LayoutParams(activity.getContentWidth(), activity.getContentHeight());
		view.setLayoutParams(params);
		addView(view);
		
		checklistItems = new ArrayList<ChecklistItem>();
		
		stats = (LinearLayout) view.findViewById(R.id.stats);
		
		listView = (ListView) view.findViewById(R.id.listview);
		ProjectAdapter adapter = new ProjectAdapter(activity,this);
		
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				try {
					JSONObject project = new JSONObject(activity.getData().getString(App.FOLDER + currentProject));
					JSONObject checklist = new JSONObject(project.getString(App.CHECKLIST + checklistItems.get(view.getId()).getId()));
				
					if(checklistItems.get(view.getId()).isEnabled()) activity.openChecklist(checklist);
				} catch(JSONException e) {
					e.printStackTrace();
				}
			}
		});
		
		LayoutParams l = (LayoutParams) stats.getLayoutParams();
		l.height = (int) (activity.getContentHeight() * 0.30);
		stats.setLayoutParams(l);
		LayoutParams s = (LayoutParams) listView.getLayoutParams();
		s.height = (int) (activity.getContentHeight() * 0.70);
		listView.setLayoutParams(s);
		
		
		empty = (TextView) findViewById(R.id.empty);
		empty.setText("This project is empty. Tap the + in the upper right corner to add a new checklist.");
	}
	
	public void update(JSONObject data) {
		Log.i("asdasd", "asdasd update project");
		
		try {
			checklistItems.clear();
			
			JSONObject project = new JSONObject(data.getString(App.FOLDER + currentProject));
			
			for(int i = 0; i < project.getInt(App.NUM_CHILDREN); i++) {
				if(project.has(App.CHECKLIST + i)) {
					JSONObject checklist = new JSONObject(project.getString(App.CHECKLIST + i));
					
					ChecklistItem ci = new ChecklistItem();
					ci.setTitle(checklist.getString(App.NAME));
					ci.setFolderId(currentProject);
					ci.setId(checklist.getInt(App.ID));
					
					checklistItems.add(ci);
				}
			}
			
			((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();
		} catch(JSONException e) {
			e.printStackTrace();
		}
		
		if(checklistItems.size() == 0) {
			empty.setVisibility(View.VISIBLE);
			listView.setVisibility(View.GONE);
		} else {
			empty.setVisibility(View.GONE);
			listView.setVisibility(View.VISIBLE);
		}
	}
	
	public ArrayList<ChecklistItem> getChecklistItems() {
		return checklistItems;
	}

	public void leave() {
		
	}
}
