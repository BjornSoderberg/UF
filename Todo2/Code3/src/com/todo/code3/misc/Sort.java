package com.todo.code3.misc;

import java.util.ArrayList;

import android.os.AsyncTask;

import com.todo.code3.item.ContentItem;
import com.todo.code3.item.TaskItem;

public class Sort {

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
		int i;
		boolean flag = true;
		ContentItem temp;

		while (flag) {
			flag = false;
			for (i = 0; i < list.size() - 1; i++) {
				if (list.get(i).getTimestampCreated() < list.get(i + 1).getTimestampCreated()) {
					temp = list.get(i);
					list.set(i, list.get(i + 1));
					list.set(i + 1, temp);

					flag = true;
				}
			}
		}

		for (ContentItem ia : list)
			ia.setTitle(ia.getTimestampCreated() + " : " + ia.getTitle());
	}
	
	public static void sortCompleted(ArrayList<ContentItem> oldList) {
		ArrayList<ContentItem> newList = new ArrayList<ContentItem>();
		
		for(ContentItem i : oldList) {
			if(i instanceof TaskItem) {
				if(((TaskItem)i).isCompleted()) newList.add(i);
			}
		}
		
		for(ContentItem i : oldList) {
			if(i instanceof TaskItem) {
				if(!((TaskItem)i).isCompleted()) newList.add(i);
			}
		}
		
		for(ContentItem i : oldList) {
			if(!(i instanceof TaskItem)) {
				newList.add(i);
			}
		}
		
		oldList.clear();
		oldList.addAll(newList);
	}
}
