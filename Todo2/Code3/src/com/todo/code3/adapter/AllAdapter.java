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
import com.todo.code3.view.AllView;
import com.todo.code3.xml.FolderItem;
import com.todo.code3.xml.ContentItem;
import com.todo.code3.xml.TaskItem;

public class AllAdapter extends BaseAdapter {

	private LayoutInflater inflater;
	private AllView contentView;
	private MainActivity activity;

	private int movingId = -1;

	public AllAdapter(MainActivity a, AllView c) {
		inflater = LayoutInflater.from(a);
		activity = a;
		contentView = c;
	}

	public int getCount() {
		return contentView.getContentItems().size();
	}

	public ContentItem getItem(int position) {
		return contentView.getContentItems().get(position);
	}

	public long getItemId(int position) {
		if (position < 0 || position >= contentView.getContentItems().size()) {
			return -1;
		}
		ContentItem item = getItem(position);
		return item.getId();
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		final ContentItem item = contentView.getContentItems().get(position);

		View view = null;		
		if (item instanceof TaskItem) view = getTaskView(position, (TaskItem) item);
		if (item instanceof FolderItem) view = getFolderView(position, (FolderItem) item);
		if(view == null) return null;
		
		if (item.getId() == movingId) {
			view.setVisibility(View.INVISIBLE);
			movingId = -1;
		}
		
		if(item.getId() == contentView.getExpandingItemId()) {
			contentView.invalidateExpandingItemId();
			contentView.expandView(view);
		}
		
		return view;
	}

	private View getTaskView(int position, final TaskItem item) {
		final View view = inflater.inflate(R.layout.task_item, null);

		if (view.getLayoutParams() != null) view.getLayoutParams().height = contentView.getItemHeight();

		ImageView button = (ImageView) view.findViewById(R.id.rbm_item_checkbox);
		TextView text = (TextView) view.findViewById(R.id.rbm_item_text);
		text.setText(item.getTitle());

		// if(item.getId() == contentView.getExpandingItemId()) {
		// contentView.invalidateExpandingItemId();
		// contentView.expandView(view);
		// }

		button.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (contentView.getActivity().isMoving()) return;

				boolean shouldCheck = !item.isCompleted();
				activity.checkTask(item.getId(), item.getParentId(), shouldCheck);
			}
		});

		if (item.isCompleted()) {
			button.setImageResource(R.drawable.checked);
			text.setTextColor(0xff888888);
			text.setPaintFlags(text.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
		} else {
			button.setImageResource(R.drawable.box);
			text.setTextColor(0xff585858);
			text.setPaintFlags(257);
		}

		view.setId(item.getId());

		return view;
	}

	private View getFolderView(int position, FolderItem item) {
		View view = inflater.inflate(R.layout.folder_item, null);

		TextView text = (TextView) view.findViewById(R.id.rbm_item_text);
		text.setText(item.getTitle());

		// if(item.getId() == contentView.getExpandingItemId()) {
		// contentView.invalidateExpandingItemId();
		// contentView.expandView(view);
		// }

		view.setId(item.getId());

		return view;
	}

	public void setMovingItemId(int i) {
		movingId = i;
	}
}
