package com.todo.code3.adapter;

import android.graphics.Paint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.todo.code3.MainActivity;
import com.todo.code3.R;
import com.todo.code3.view.ItemView;
import com.todo.code3.xml.ContentItem;
import com.todo.code3.xml.FolderItem;
import com.todo.code3.xml.TaskItem;

public class ItemAdapter extends BaseAdapter {

	private LayoutInflater inflater;
	private ItemView itemView;
	private MainActivity activity;

	private int movingId = -1;

	public ItemAdapter(MainActivity a, ItemView c) {
		inflater = LayoutInflater.from(a);
		activity = a;
		itemView = c;
	}

	public int getCount() {
		return itemView.getContentItems().size();
	}

	public ContentItem getItem(int position) {
		return itemView.getContentItems().get(position);
	}

	public long getItemId(int position) {
		if (position < 0 || position >= itemView.getContentItems().size()) {
			return -1;
		}
		ContentItem item = getItem(position);
		return item.getId();
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		final ContentItem item = itemView.getContentItems().get(position);

		View view = null;
		if (itemView.isInOptionsMode()) view = getOptionsView(position, item);
		else if (item instanceof TaskItem) view = getTaskView(position, (TaskItem) item);
		else if (item instanceof FolderItem) view = getFolderView(position, (FolderItem) item);
		if (view == null) return null;

		// Assures that all the views have the same height
		if (view.getLayoutParams() != null) view.getLayoutParams().height = itemView.getItemHeight();
		else view.setLayoutParams(new ListView.LayoutParams(LayoutParams.FILL_PARENT, itemView.getItemHeight()));

		if (item.getId() == movingId && itemView.getListView().isDragging()) {
			view.setVisibility(View.INVISIBLE);
			movingId = -1;
		}

		if (item.getId() == itemView.getExpandingItemId()) {
			itemView.invalidateExpandingItemId();
			itemView.expandView(view);
		}

		view.setId(item.getId());

		return view;
	}

	private View getOptionsView(int position, final ContentItem item) {
		final View view = inflater.inflate(R.layout.options_item, null);

		final ImageView iv = (ImageView) view.findViewById(R.id.item_checkbox);
		if (itemView.isSelected(item.getId())) iv.setImageResource(R.drawable.checked);
		else iv.setImageResource(R.drawable.box);
		
		TextView text = (TextView) view.findViewById(R.id.item_text);
		text.setText(item.getTitle() + " (edit)");

		iv.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (itemView.getActivity().isMoving()) return;

				itemView.toggleItem(item.getId());
			}
		});

		return view;
	}

	private View getTaskView(int position, final TaskItem item) {
		final View view = inflater.inflate(R.layout.task_item, null);

		ImageView iv = (ImageView) view.findViewById(R.id.item_checkbox);
		TextView text = (TextView) view.findViewById(R.id.item_text);
		text.setText(item.getTitle());

		iv.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (itemView.getActivity().isMoving()) return;

				boolean shouldCheck = !item.isCompleted();
				activity.checkTask(item.getId(), item.getParentId(), shouldCheck);
			}
		});

		if (item.isCompleted()) {
			iv.setImageResource(R.drawable.checked);
			text.setTextColor(0xff888888);
			text.setPaintFlags(text.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
		} else {
			iv.setImageResource(R.drawable.box);
			text.setTextColor(0xff585858);
			text.setPaintFlags(257);
		}

		return view;
	}

	private View getFolderView(int position, FolderItem item) {
		View view = inflater.inflate(R.layout.folder_item, null);

		TextView text = (TextView) view.findViewById(R.id.item_text);
		text.setText(item.getTitle());

		return view;
	}

	public void setMovingItemId(int i) {
		movingId = i;
	}
}
