package com.todo.code3.adapter;

import android.graphics.Paint;
import android.os.Handler;
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
import com.todo.code3.misc.App;
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

	public View getView(final int position, View convertView, ViewGroup parent) {
		final TaskItem item = taskView.getTaskItems().get(position);
		final View view;

		if (convertView == null || convertView instanceof TextView) view = inflater.inflate(R.layout.task_item, null);
		else view = convertView;

		if (view.getLayoutParams() != null) view.getLayoutParams().height = taskView.getListViewItemHeight();

		ImageView button = (ImageView) view.findViewById(R.id.rbm_item_checkbox);

		final TextView text = (TextView) view.findViewById(R.id.rbm_item_text);
		text.setText(item.getTitle());

		if (item.getId() == taskView.getExpandingItemId()) {
			taskView.invalidateExpandingItemId();
			taskView.expandView(view);
		}
		if (item instanceof TaskItem) {
			button.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					if (taskView.getActivity().isMoving()) return;

					final boolean shouldCheck = !item.isCompleted();
					boolean shouldCollapse = true;

					if (shouldCheck) {
						if (taskView.getTaskItems().size() > position + 1) {
							if (taskView.getTaskItems().get(position + 1).isCompleted()) {
								shouldCollapse = false;
							}
						} else shouldCollapse = false;
					} else {
						if (position - 1 >= 0) {
							if (!taskView.getTaskItems().get(position - 1).isCompleted()) {
								shouldCollapse = false;
							}
						} else shouldCollapse = false;
					}
					
					if(shouldCollapse) {
						taskView.collapseView(view, item.getId());
						
						// This runs the code inside with a delay
						new Handler().postDelayed(new Runnable() {
							public void run() {
								activity.checkTask(item.getId(), item.getChecklistId(), item.getFolderId(), shouldCheck);
							}
						}, App.COLLAPSE_ANIMATION_DURATION);
					} else {
						activity.checkTask(item.getId(),  item.getChecklistId(), item.getFolderId(), shouldCheck);
					}

				}
			});

			if (((TaskItem) item).isCompleted()) {
				 button.setImageResource(R.drawable.checked);
				text.setTextColor(0xff888888);
				text.setPaintFlags(text.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
			} else {
				button.setImageResource(R.drawable.box);
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
