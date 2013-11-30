package com.todo.code3.adapter;

import android.graphics.Paint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.todo.code3.MainActivity;
import com.todo.code3.R;
import com.todo.code3.view.TaskView;
import com.todo.code3.xml.TaskItem;

public class TaskAdapter extends BaseAdapter {

	private LayoutInflater inflater;

	private MainActivity activity;
	private TaskView taskView;

	public TaskAdapter(MainActivity activity, TaskView taskView) {
		inflater = LayoutInflater.from(activity);
		this.activity = activity;
		this.taskView = taskView;
	}

	public int getCount() {
		return taskView.getTaskItems().size();
	}

	public TaskItem getItem(int position) {
		return taskView.getTaskItems().get(position);
	}

	public long getItemId(int position) {
        if (position < 0 || position >= taskView.getTaskItems().size()) {
            return -1;
        }
        TaskItem item = getItem(position);
        return item.getId();
    }

	public View getView(int position, View view, ViewGroup parent) {
		final TaskItem item = taskView.getTaskItems().get(position);

		if (view == null || view instanceof TextView) view = inflater.inflate(R.layout.task_item, null);

		ImageView button = (ImageView) view.findViewById(R.id.rbm_item_checkbox);
		TextView text = (TextView) view.findViewById(R.id.rbm_item_text);
		text.setText(item.getTitle());

		if (item instanceof TaskItem) {

			button.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					activity.checkTask(((TaskItem) item).getId(), ((TaskItem) item).getChecklistId(), ((TaskItem) item).getFolderId(), true);
					Log.i("complete task task adapter", ((TaskItem) item).getId() + ", " + ((TaskItem) item).getChecklistId() + ", " + ((TaskItem) item).getFolderId());
				}
			});

			if (((TaskItem) item).isCompleted()) {
				button.setBackgroundColor(0xff585858);
				text.setTextColor(0xff888888);
				text.setPaintFlags(text.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
			} else {
				button.setBackgroundColor(0xff888888);
				// These need to be set, otherwise attributes from the last
				// task will be inherited
				text.setTextColor(0xff585858);
				text.setPaintFlags(257);
			}
		}

		view.setId(item.getId());

		return view;
	}
}
