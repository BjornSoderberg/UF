package com.todo.code3.misc;

import java.util.ArrayList;

import android.os.AsyncTask;

import com.todo.code3.item.ContentItem;
import com.todo.code3.item.TaskItem;

public class Sort {
	
	public static final int SORT_PRIORITIZED = 0;
	public static final int SORT_TIMESTAMP_CREATED = 1;
	public static final int SORT_COMPLETED = 2;
	
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
		sortTimestampCreated(list, 0, list.size() - 1);

		for (ContentItem ia : list)
			ia.setTitle(ia.getTimestampCreated() + " : " + ia.getTitle());
	}

	public static void sortTimestampCreated(ArrayList<ContentItem> list, int start, int end) {
		int index = partition(list, start, end);
		
		if(start < index-1) sortTimestampCreated(list, start, index-1);
		if(index < end) sortTimestampCreated(list, index, end);
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

	// Used in the quicksort (sort timestamp created)
	private static int partition(ArrayList<ContentItem> list, int start, int end) {
		ContentItem temp;
		long pivot = list.get((start + end) / 2).getTimestampCreated();
		
		while(start <= end) {
			while(list.get(start).getTimestampCreated() < pivot)
				start++;
			while(list.get(end).getTimestampCreated() > pivot)
				end--;
			if(start <= end) {
				temp = list.get(start);
				list.set(start, list.get(end));
				list.set(end, temp);
			}
		}
		
		return start;
	}
}
