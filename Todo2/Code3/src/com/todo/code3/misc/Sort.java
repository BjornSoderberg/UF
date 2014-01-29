package com.todo.code3.misc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.content.Context;

import com.todo.code3.item.ContentItem;
import com.todo.code3.item.TaskItem;
import com.todo.code3.notification.NotificationReceiver;

public class Sort {

	public static final int SORT_PRIORITIZED = 0;
	public static final int SORT_TIMESTAMP_CREATED = 1;
	public static final int SORT_COMPLETED = 2;
	public static final int SORT_ALPHABETICALLY = 3;
	public static final int SORT_DUE_DATE = 4;
	public static final int SORT_NOTHING = 5;

	public static void sortPrioritized(ArrayList<ContentItem> oldList, boolean invert) {
		ArrayList<ContentItem> newList = new ArrayList<ContentItem>();

		for (ContentItem i : oldList) {
			if (i.isPrioritized() == !invert) newList.add(i);
		}

		for (ContentItem i : oldList) {
			if (i.isPrioritized() == invert) newList.add(i);
		}

		oldList.clear();
		oldList.addAll(newList);
	}

	public static void sortTimestampCreated(ArrayList<ContentItem> list) {
		// Highest at top
		Comparator<ContentItem> c = new Comparator<ContentItem>() {
			public int compare(ContentItem lhs, ContentItem rhs) {
				if (lhs.getTimestampCreated() > rhs.getTimestampCreated()) return -1;
				return 1;
			}
		};

		Collections.sort(list, c);

		for (ContentItem ia : list)
			ia.setTitle(ia.getTitle() + " (" + App.getSimpleFormattedDateString(ia.getTimestampCreated()) + ")");
	}

	public static void sortCompleted(ArrayList<ContentItem> oldList) {
		ArrayList<ContentItem> newList = new ArrayList<ContentItem>();

		for (ContentItem i : oldList) {
			if (i instanceof TaskItem) {
				if (((TaskItem) i).isCompleted()) newList.add(i);
			}
		}

		for (ContentItem i : oldList) {
			if (i instanceof TaskItem) {
				if (!((TaskItem) i).isCompleted()) newList.add(i);
			}
		}

		for (ContentItem i : oldList) {
			if (!(i instanceof TaskItem)) {
				newList.add(i);
			}
		}

		oldList.clear();
		oldList.addAll(newList);
	}

	public static void sortAlphabetically(ArrayList<ContentItem> list) {
		Comparator<ContentItem> comparator = new Comparator<ContentItem>() {
			public int compare(ContentItem lhs, ContentItem rhs) {
				String title1 = lhs.getTitle().toString().toLowerCase();
				String title2 = rhs.getTitle().toString().toLowerCase();

				return title1.compareTo(title2);
			}
		};

		Collections.sort(list, comparator);
	}

	public static void sortDueDate(ArrayList<ContentItem> list, Context context) {
		ArrayList<ContentItem> sorted = new ArrayList<ContentItem>();

		for (ContentItem i : list)
			if (i instanceof TaskItem && !((TaskItem) i).isCompleted()) sorted.add(i);

		// Next due date at top
		Comparator<ContentItem> c = new Comparator<ContentItem>() {
			public int compare(ContentItem lhs, ContentItem rhs) {
				long l = ((TaskItem) lhs).getDueDate();
				long r = ((TaskItem) rhs).getDueDate();

				if (l == -1 && r != -1) return 1;
				else if (r == -1 && l != -1) return -1;
				else if (r == -1 && l == -1) return 0;

				if (l < r) return -1;
				return 1;
			}
		};

		Collections.sort(sorted, c);

		for (ContentItem i : list) {
			if (!sorted.contains(i)) sorted.add(i);
		}

		list.clear();
		list.addAll(sorted);

		for (ContentItem ia : list) {
			if (ia instanceof TaskItem) {
				if (((TaskItem) ia).getDueDate() != -1) ia.setTitle(ia.getTitle() + " (" + NotificationReceiver.getTimeString(((TaskItem) ia).getDueDate(), context) + ") ");
			}
		}
	}
}
