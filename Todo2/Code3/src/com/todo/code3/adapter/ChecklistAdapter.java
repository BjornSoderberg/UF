package com.todo.code3.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.todo.code3.MainActivity;
import com.todo.code3.R;
import com.todo.code3.item.ContentItem;
import com.todo.code3.item.FolderItem;
import com.todo.code3.item.TaskItem;
import com.todo.code3.view.ChecklistView;

public class ChecklistAdapter extends BaseAdapter {

	private LayoutInflater inflater;

	private MainActivity activity;
	private ChecklistView checklistView;
	
	public ChecklistAdapter(MainActivity activity, ChecklistView checklistView) {
		inflater = LayoutInflater.from(activity);
		this.activity = activity;
		this.checklistView = checklistView;
	}

	public int getCount() {
		return checklistView.getChecklistItems().size();
	}

	public FolderItem getItem(int position) {
		return checklistView.getChecklistItems().get(position);
	}

	public long getItemId(int position) {
        if (position < 0 || position >= checklistView.getChecklistItems().size()) {
            return -1;
        }
        FolderItem item = getItem(position);
        return item.getId();
    }

	public View getView(int position, View view, ViewGroup parent) {
		ContentItem item = checklistView.getChecklistItems().get(position);

		if (view == null || view instanceof TextView) view = inflater.inflate(R.layout.folder_item, null);

		final TextView text = (TextView) view.findViewById(R.id.item_text);

		text.setText(item.getTitle());
		view.setEnabled(item.isEnabled());
	
		if(item.getId() == checklistView.getExpandingItemId()) {
			checklistView.invalidateExpandingItemId();
			checklistView.expandView(view);
		}
		
		view.setId(item.getId());

		return view;
	}
}
