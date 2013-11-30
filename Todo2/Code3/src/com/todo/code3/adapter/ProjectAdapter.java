package com.todo.code3.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.todo.code3.MainActivity;
import com.todo.code3.R;
import com.todo.code3.view.ProjectView;
import com.todo.code3.xml.ChecklistItem;
import com.todo.code3.xml.ContentItem;

public class ProjectAdapter extends BaseAdapter {

	private LayoutInflater inflater;

	private MainActivity activity;
	private ProjectView projectView;

	public ProjectAdapter(MainActivity activity, ProjectView projectView) {
		inflater = LayoutInflater.from(activity);
		this.activity = activity;
		this.projectView = projectView;
	}

	public int getCount() {
		return projectView.getChecklistItems().size();
	}

	public ChecklistItem getItem(int position) {
		return projectView.getChecklistItems().get(position);
	}

	public long getItemId(int position) {
		if (position < 0 || position >= projectView.getChecklistItems().size()) {
			return -1;
		}
		ChecklistItem item = getItem(position);
		return item.getId();
	}

	public View getView(int position, View view, ViewGroup parent) {
		ContentItem item = projectView.getChecklistItems().get(position);

		if (view == null || view instanceof TextView) view = inflater.inflate(R.layout.checklist_item, null);

		final TextView text = (TextView) view.findViewById(R.id.rbm_item_text);

		text.setText(item.getTitle());
		view.setEnabled(item.isEnabled());
		Log.i("checklist adapter", item.getTitle() + " - " + item.getId());

		view.setId(item.getId());

		return view;
	}
}
