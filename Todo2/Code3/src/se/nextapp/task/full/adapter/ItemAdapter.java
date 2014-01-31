package se.nextapp.task.full.adapter;

import se.nextapp.task.full.item.ContentItem;
import se.nextapp.task.full.item.FolderItem;
import se.nextapp.task.full.item.NoteItem;
import se.nextapp.task.full.item.TaskItem;
import se.nextapp.task.full.misc.App;
import se.nextapp.task.full.view.ItemView;
import android.content.Context;
import android.graphics.Paint;
import android.graphics.PorterDuffColorFilter;
import android.os.Handler;
import android.util.Log;
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
		else if (item instanceof NoteItem) view = getNoteView(position, (NoteItem) item);
		if (view == null) return null;

		// Assures that all the views have the same height
		if (view.getLayoutParams() != null) view.getLayoutParams().height = itemView.getItemHeight();
		else view.setLayoutParams(new ListView.LayoutParams(LayoutParams.FILL_PARENT, itemView.getItemHeight()));

		if (item.getId() == movingId && itemView.getListView().isDragging()) {
			view.setVisibility(View.INVISIBLE);
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

		TextView text = (TextView) view.findViewById(R.id.item_text);
		text.setText(item.getTitle() + " (" + itemView.getActivity().getResources().getString(R.string.edit) + ")");
		text.setTextColor((itemView.getActivity().isDarkTheme()) ? itemView.getActivity().getResources().getColor(R.color.text_color_dark) : itemView.getActivity().getResources().getColor(R.color.text_color_light));

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

		text.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (itemView.getActivity().isMoving()) return;

				itemView.toggleItem(item.getId());
			}
		});

		((ImageView) view.findViewById(R.id.drag)).getDrawable().setColorFilter(new PorterDuffColorFilter(itemView.getActivity().getResources().getColor(R.color.icon_color), android.graphics.PorterDuff.Mode.MULTIPLY));

		// Sets the background (which is dependent on its state (pressed,
		// focused etc.))
		if (itemView.isSelected(item.getId())) {
			view.setBackgroundDrawable(itemView.getActivity().getResources().getDrawable(itemView.getActivity().isDarkTheme() ? R.drawable.blue_item_selector_dark : R.drawable.blue_item_selector_light));
		} else {
			if (itemView.getActivity().isDarkTheme()) view.setBackgroundDrawable(itemView.getActivity().getResources().getDrawable(R.drawable.item_selector_dark));
			else view.setBackgroundDrawable(itemView.getActivity().getResources().getDrawable(R.drawable.item_selector_white));
		}

		return view;
	}

	private View getTaskView(int position, final TaskItem item) {
		View view = inflater.inflate(R.layout.task_item, null);

		// Set text
		TextView text = (TextView) view.findViewById(R.id.item_text);

		if (App.isOverDue(item.getDueDate()) && !item.isCompleted()) text.setText(item.getTitle() + " - " + itemView.getActivity().getResources().getString(R.string.overdue));
		else text.setText(item.getTitle());

		// Is prio
		FrameLayout prio = (FrameLayout) view.findViewById(R.id.item_prio);
		final ImageView i = (ImageView) view.findViewById(R.id.star);

		prio.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				final boolean shouldPrio = !item.isPrioritized();

				new Handler().postDelayed(new Runnable() {
					public void run() {
						itemView.getActivity().setProperty(App.PRIORITIZED, shouldPrio, item.getId());
					}
				}, App.ANIMATION_DURATION);

				if (shouldPrio) i.setImageResource(R.drawable.ic_star);
				else i.setImageResource(R.drawable.ic_unstar);
			}
		});

		if (item.isPrioritized()) i.setImageResource(R.drawable.ic_star);
		else i.setImageResource(R.drawable.ic_unstar);
		i.getDrawable().setColorFilter(new PorterDuffColorFilter(itemView.getActivity().getResources().getColor(R.color.star_color), android.graphics.PorterDuff.Mode.MULTIPLY));

		// Checkbox and image
		FrameLayout fl = (FrameLayout) view.findViewById(R.id.item_checkbox);
		ImageView iv = (ImageView) view.findViewById(R.id.checkbox);

		fl.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (itemView.getActivity().isMoving()) return;

				boolean shouldCheck = !item.isCompleted();
				itemView.getActivity().checkTask(item.getId(), shouldCheck);
			}
		});

		if (item.isCompleted()) {
			iv.setImageResource(R.drawable.ic_checked);
			text.setTextColor((itemView.getActivity().isDarkTheme()) ? itemView.getActivity().getResources().getColor(R.color.text_color_checked_dark) : itemView.getActivity().getResources().getColor(R.color.text_color_checked_dark));
			text.setPaintFlags(text.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
		} else {
			iv.setImageResource(R.drawable.ic_unchecked);
			text.setTextColor((itemView.getActivity().isDarkTheme()) ? itemView.getActivity().getResources().getColor(R.color.text_color_dark) : itemView.getActivity().getResources().getColor(R.color.text_color_light));
			text.setPaintFlags(257);
		}

		// Sets the background (which is dependent on its state (pressed,
		// focused etc.))
		if (App.isOverDue(item.getDueDate()) && !item.isCompleted()) view.setBackgroundDrawable(itemView.getActivity().getResources().getDrawable(itemView.getActivity().isDarkTheme() ? R.drawable.red_item_selector_dark : R.drawable.red_item_selector_light));
		else if (itemView.getActivity().isDarkTheme()) view.setBackgroundDrawable(itemView.getActivity().getResources().getDrawable(R.drawable.item_selector_dark));
		else view.setBackgroundDrawable(itemView.getActivity().getResources().getDrawable(R.drawable.item_selector_white));

		return view;
	}

	private View getFolderView(int position, final FolderItem item) {
		View view = inflater.inflate(R.layout.folder_item, null);

		// Init prio
		FrameLayout prio = (FrameLayout) view.findViewById(R.id.item_prio);
		final ImageView i = (ImageView) view.findViewById(R.id.star);

		prio.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (itemView.getActivity().isMoving()) return;

				final boolean shouldPrio = !item.isPrioritized();
				new Handler().postDelayed(new Runnable() {
					public void run() {
						itemView.getActivity().setProperty(App.PRIORITIZED, shouldPrio, item.getId());
					}
				}, App.ANIMATION_DURATION);

				if (shouldPrio) i.setImageResource(R.drawable.ic_star);
				else i.setImageResource(R.drawable.ic_unstar);
			}
		});

		if (item.isPrioritized()) i.setImageResource(R.drawable.ic_star);
		else i.setImageResource(R.drawable.ic_unstar);
		i.getDrawable().setColorFilter(new PorterDuffColorFilter(itemView.getActivity().getResources().getColor(R.color.star_color), android.graphics.PorterDuff.Mode.MULTIPLY));

		// If there are items over due
		if(App.getNumberOfTasksOverDue(item.getId(), itemView.getActivity().getData()) > 0) ((ImageView) view.findViewById(R.id.icon)).setImageResource(R.drawable.ic_folder_due);
		else if(App.getNumberOfTasksCompleted(item.getId(), false, itemView.getActivity().getData()) == 0 && App.getNumberOfTasksCompleted(item.getId(), true, itemView.getActivity().getData()) > 0)((ImageView) view.findViewById(R.id.icon)).setImageResource(R.drawable.ic_folder_check);
		// Set correct color on icon (through filter)
		((ImageView) view.findViewById(R.id.icon)).getDrawable().setColorFilter(new PorterDuffColorFilter(itemView.getActivity().getResources().getColor(R.color.icon_color), android.graphics.PorterDuff.Mode.MULTIPLY));
		
		Log.i(App.getNumberOfTasksCompleted(item.getId(), false, itemView.getActivity().getData())+ " ", App.getNumberOfTasksCompleted(item.getId(), true, itemView.getActivity().getData()) + "");
		
		// Set text
		TextView text = (TextView) view.findViewById(R.id.item_text);
		text.setText(item.getTitle());
		text.setTextColor((itemView.getActivity().isDarkTheme()) ? itemView.getActivity().getResources().getColor(R.color.text_color_dark) : itemView.getActivity().getResources().getColor(R.color.text_color_light));

		// Sets the background (which is dependent on its state (pressed,
		// focused etc.))
		if (itemView.getActivity().isDarkTheme()) view.setBackgroundDrawable(itemView.getActivity().getResources().getDrawable(R.drawable.item_selector_dark));
		else view.setBackgroundDrawable(itemView.getActivity().getResources().getDrawable(R.drawable.item_selector_white));

		return view;
	}

	private View getNoteView(int position, final NoteItem item) {
		View view = inflater.inflate(R.layout.note_item, null);

		// Init prio
		FrameLayout prio = (FrameLayout) view.findViewById(R.id.item_prio);
		final ImageView i = (ImageView) view.findViewById(R.id.star);

		prio.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (itemView.getActivity().isMoving()) return;

				final boolean shouldPrio = !item.isPrioritized();
				new Handler().postDelayed(new Runnable() {
					public void run() {
						itemView.getActivity().setProperty(App.PRIORITIZED, shouldPrio, item.getId());
					}
				}, App.ANIMATION_DURATION);

				if (shouldPrio) i.setImageResource(R.drawable.ic_star);
				else i.setImageResource(R.drawable.ic_unstar);
			}
		});

		if (item.isPrioritized()) i.setImageResource(R.drawable.ic_star);
		else i.setImageResource(R.drawable.ic_unstar);
		i.getDrawable().setColorFilter(new PorterDuffColorFilter(itemView.getActivity().getResources().getColor(R.color.star_color), android.graphics.PorterDuff.Mode.MULTIPLY));

		// Set correct color on icon (through filter)
		((ImageView) view.findViewById(R.id.icon)).getDrawable().setColorFilter(new PorterDuffColorFilter(itemView.getActivity().getResources().getColor(R.color.icon_color), android.graphics.PorterDuff.Mode.MULTIPLY));

		// Set text
		TextView text = (TextView) view.findViewById(R.id.item_text);
		text.setText(item.getTitle());
		text.setTextColor((itemView.getActivity().isDarkTheme()) ? itemView.getActivity().getResources().getColor(R.color.text_color_dark) : itemView.getActivity().getResources().getColor(R.color.text_color_light));

		// Sets the background (which is dependent on its state (pressed,
		// focused etc.))
		if (itemView.getActivity().isDarkTheme()) view.setBackgroundDrawable(itemView.getActivity().getResources().getDrawable(R.drawable.item_selector_dark));
		else view.setBackgroundDrawable(itemView.getActivity().getResources().getDrawable(R.drawable.item_selector_white));

		return view;
	}

	public void setMovingItemId(int i) {
		movingId = i;
	}
}
