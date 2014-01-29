package com.espian.flyin.library;

import org.json.JSONException;
import org.json.JSONObject;

public class App {
	public static final String TYPE = "type";

	public static final String FOLDER = "folder";
	public static final String TASK = "task";
	public static final String NOTE = "note";

	public static final String CHILDREN_IDS = "childrenIds";
	public static final String DUE_DATE = "dueDate";
	public static final String COMPLETED = "completed";

	public static int getNumberOfTasksOverDue(int parentId, JSONObject data) {
		int numTasksOverDue = 0;

		try {
			if (!data.has(parentId + "")) return 0;

			JSONObject parent = new JSONObject(data.getString(parentId + ""));

			if (parent.getString(App.TYPE).equals(App.FOLDER)) {
				if (parent.has(App.CHILDREN_IDS) && parent.getString(App.CHILDREN_IDS) != "") {

					String[] childrenIds = parent.getString(App.CHILDREN_IDS).split(",");

					for (String id : childrenIds) {
						if (id == "") continue;
						try {
							numTasksOverDue += getNumberOfTasksOverDue(Integer.parseInt(id), data);
						} catch (NumberFormatException e) {
							e.printStackTrace();
						}
					}
				}
			} else if (parent.getString(App.TYPE).equals(App.TASK)) {
				if (parent.has(App.DUE_DATE) && parent.getLong(App.DUE_DATE) != -1) {
					// If the item is over due and not completed
					if (isOverDue(parent.getLong(App.DUE_DATE)) && (!parent.has(App.COMPLETED) || !parent.getBoolean(App.COMPLETED))) return 1;

				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return numTasksOverDue;
	}

	public static boolean isOverDue(long timestamp) {
		return timestamp < System.currentTimeMillis() / 1000 && timestamp != -1;
	}

	public static int getNumberOfTasksCompleted(int parentId, boolean isCompleted, JSONObject data) {
		int numTasksCompleted = 0;

		try {
			if (!data.has(parentId + "")) return 0;

			JSONObject parent = new JSONObject(data.getString(parentId + ""));

			if (parent.getString(App.TYPE).equals(App.FOLDER)) {
				if (parent.has(App.CHILDREN_IDS) && parent.getString(App.CHILDREN_IDS) != "") {

					String[] childrenIds = parent.getString(App.CHILDREN_IDS).split(",");

					for (String id : childrenIds) {
						if (id == "") continue;
						try {
							numTasksCompleted += getNumberOfTasksCompleted(Integer.parseInt(id), isCompleted, data);
						} catch (NumberFormatException e) {
							e.printStackTrace();
						}
					}
				}
			} else if (parent.getString(App.TYPE).equals(App.TASK)) {
				if (parent.has(App.COMPLETED) && parent.getBoolean(App.COMPLETED) == isCompleted) {
					return 1;
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return numTasksCompleted;
	}
}
