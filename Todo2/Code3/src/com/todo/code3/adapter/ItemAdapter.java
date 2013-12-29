package com.todo.code3.adapter;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.todo.code3.R;
import com.todo.code3.item.ContentItem;
import com.todo.code3.item.FolderItem;
import com.todo.code3.item.TaskItem;
import com.todo.code3.view.ItemView;

public class ItemAdapter extends BaseAdapter {

	private LayoutInflater inflater;
	private ItemView itemView;

	private int movingId = -1;

	public ItemAdapter(Context c, ItemView i) {
		inflater = LayoutInflater.from(c);
		itemView = i;
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
		View view = inflater.inflate(R.layout.options_item, null);

		FrameLayout fl = (FrameLayout) view.findViewById(R.id.item_checkbox);
		ImageView iv = (ImageView) fl.findViewById(R.id.checkbox);

		if (itemView.isSelected(item.getId())) iv.setImageResource(R.drawable.checked);
		else iv.setImageResource(R.drawable.box);

		TextView text = (TextView) view.findViewById(R.id.item_text);
		text.setText(item.getTitle() + " (edit)");

		// All touches that are not consumed are interpreted as actions for
		// dragging the item. There is an area to the right of this view which
		// is "empty". By holding there, the user can drag and reorder the
		// items. This on long click listener both consumes the touch event and
		// toggles the options
		text.setOnLongClickListener(new OnLongClickListener() {
			public boolean onLongClick(View v) {
				itemView.getActivity().toggleOptions();
				return true;
			}
		});

		fl.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (itemView.getActivity().isMoving()) return;

				itemView.toggleItem(item.getId());
			}
		});

		return view;
	}

	private View getTaskView(int position, final TaskItem item) {
		View view = inflater.inflate(R.layout.task_item, null);

		// Set text
		TextView text = (TextView) view.findViewById(R.id.item_text);
		text.setText(item.getTitle());
		
		// Is prio
		FrameLayout prio = (FrameLayout) view.findViewById(R.id.item_prio);
		ImageView i = (ImageView) view.findViewById(R.id.icon);
		
		prio.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if(itemView.getActivity().isMoving()) return;
				
				boolean shouldPrio = !item.isPrioritized();
				itemView.getActivity().prioritize(item.getId(), shouldPrio);
			}
		});
		
		if(item.isPrioritized()) i.setImageResource(R.drawable.checked);
		else i.setImageResource(R.drawable.box);

		// Checkbox and image
		FrameLayout fl = (FrameLayout) view.findViewById(R.id.item_checkbox);
		ImageView iv = (ImageView) view.findViewById(R.id.checkbox);

		fl.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (itemView.getActivity().isMoving()) return;

				boolean shouldCheck = !item.isCompleted();
				itemView.getActivity().checkTask(item.getId(), item.getParentId(), shouldCheck);
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

	private View getFolderView(int position, final FolderItem item) {
		View view = inflater.inflate(R.layout.folder_item, null);
		
		// Init prio
		FrameLayout prio = (FrameLayout) view.findViewById(R.id.item_prio);
		ImageView i = (ImageView) view.findViewById(R.id.icon);
		
		prio.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if(itemView.getActivity().isMoving()) return;
				
				boolean shouldPrio = !item.isPrioritized();
				itemView.getActivity().prioritize(item.getId(), shouldPrio);
			}
		});
		
		if(item.isPrioritized()) i.setImageResource(R.drawable.checked);
		else i.setImageResource(R.drawable.box);
		
		// Set text
		TextView text = (TextView) view.findViewById(R.id.item_text);
		text.setText(item.getTitle());

		return view;
	}

	public void setMovingItemId(int i) {
		movingId = i;
	}
}
