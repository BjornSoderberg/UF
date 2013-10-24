package com.todo.code3.adapter;

import android.graphics.Paint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
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

	public Object getItem(int position) {
		return null;
	}

	public long getItemId(int position) {
		return -1;
	}

	public View getView(int position, View view, ViewGroup parent) {		
		final TaskItem item = taskView.getTaskItems().get(position);
//		final TaskItem item = new TaskItem();
//		item.setTitle("asd");
//		item.setId(1337);
//		item.isCompleted();

		if (view == null || view instanceof TextView) view = inflater.inflate(R.layout.task_item, null);

		Button button = (Button) view.findViewById(R.id.rbm_item_button);
		TextView text = (TextView) view.findViewById(R.id.rbm_item_text);
		text.setText(item.getTitle());

		if (item instanceof TaskItem) {
			button.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					activity.completeTask(((TaskItem) item).getId(), ((TaskItem) item).getChecklistId(), ((TaskItem) item).getFolderId());
				}
			});

			if (((TaskItem) item).isCompleted()) {
				text.setTextColor(0xff888888);
				text.setPaintFlags(text.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
			} else {
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
