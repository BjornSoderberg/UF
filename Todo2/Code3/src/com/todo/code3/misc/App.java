package com.todo.code3.misc;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.DisplayMetrics;
import android.util.Log;

public class App {

	public static final String PREFERENCES_NAME = "Code3SP";

	public static final String DATA = "data";
	public static final String NAME = "name";
	public static final String ID = "id";
	public static final String PARENT_ID = "parentId";
	public static final String CHILDREN_IDS = "childrenId";

	// public static final String NUM_CHILDREN = "numChildren";
	public static final String NUM_TASKS = "numTasks";
	public static final String NUM_CHECKLISTS = "numChecklists";
	public static final String NUM_FOLDERS = "numFolders";

	public static final String REMOVABLE = "removable";
	public static final String REMOVED = "removed";
	public static final String COMPLETED = "completed";

	public static final boolean CHECKED = true;
	public static final boolean UNCHECKED = false;

	public static final String TYPE = "type";
	public static final String CONTENT_TYPE = "contentType";
	public static final String PARENT_CONTENT_TYPE = "parentContentType";

	public static final String FOLDER = "folder";
	public static final String PROJECT = "project";
	public static final String CHECKLIST = "checklist";
	public static final String TASK = "task";

	public static final int TASK_VIEW = 0;
	public static final int CHECKLIST_VIEW = 1;

	public static final String DESCRIPTION = "description";

	public static final int MIN_API_LEVEL_FOR_DRAGGABLE_LIST_VIEW_ITEMS = 11;
	public static final int BEZEL_AREA_DP = 16;

	public static final int COLLAPSE_ANIMATION_DURATION = 300;
	public static final int EXPAND_ANIMATION_DURATION = 300;

	public static final int VOICE_RECOGNITION_REQUEST_CODE = 1337;

	// converting dp to pixels and vice versa
	public static int dpToPx(int dp, Resources r) {
		DisplayMetrics displayMetrics = r.getDisplayMetrics();
		int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
		return px;
	}

	public static int pxToDp(int px, Resources r) {
		DisplayMetrics displayMetrics = r.getDisplayMetrics();
		int dp = Math.round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
		return dp;
	}

	public static int getStatusBarHeight(Resources r) {
		int resourceId = r.getIdentifier("status_bar_height", "dimen", "android");
		if (resourceId > 0) return r.getDimensionPixelSize(resourceId);

		return 0;

	}

	// static methods originally from the MainActivity
	public static JSONObject addTask(String name, int currentChecklist, int currentFolder, JSONObject data) {
		try {
			JSONObject task = new JSONObject();
			task.put(App.NAME, name);
			// It needs to be checked if the number of tasks is correct
			task.put(App.ID, data.getInt(App.NUM_TASKS));

			JSONObject folder = new JSONObject(data.getString(App.FOLDER + currentFolder));

			if (folder.getString(App.CONTENT_TYPE).equals(App.CHECKLIST)) {
				JSONObject checklist = new JSONObject(folder.getString(App.CHECKLIST + currentChecklist));

				task.put(App.PARENT_CONTENT_TYPE, App.CHECKLIST);
				task.put(App.PARENT_ID, currentChecklist);

				checklist.put(App.TASK + task.getInt(App.ID), task.toString());

				String children = addToChildrenString(checklist, task.getInt(App.ID));
				checklist.put(App.CHILDREN_IDS, children);

				folder.put(App.CHECKLIST + currentChecklist, checklist.toString());
			} else if (folder.getString(App.CONTENT_TYPE).equals(App.TASK)) {
				task.put(App.PARENT_CONTENT_TYPE, App.FOLDER);
				task.put(App.PARENT_ID, currentFolder);

				String children = addToChildrenString(folder, task.getInt(App.ID));
				folder.put(App.CHILDREN_IDS, children);

				folder.put(App.TASK + task.getInt(App.ID), task.toString());
			}

			data.put(App.NUM_TASKS, data.getInt(App.NUM_TASKS) + 1);
			data.put(App.FOLDER + folder.getInt(App.ID), folder.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return data;
	}

	public static JSONObject addChecklist(String name, int currentFolder, JSONObject data) {
		try {
			JSONObject checklist = new JSONObject();
			checklist.put(App.NAME, name);
			checklist.put(App.PARENT_ID, currentFolder);
			checklist.put(App.PARENT_CONTENT_TYPE, App.FOLDER);

			JSONObject folder = new JSONObject(data.getString(App.FOLDER + currentFolder));

			if (folder.getString(App.CONTENT_TYPE).equals(App.CHECKLIST)) {
				// It needs to be checked if the number of checklists is correct

				checklist.put(App.ID, data.getInt(App.NUM_CHECKLISTS));

				folder.put(App.CHECKLIST + checklist.getInt(App.ID), checklist.toString());

				String children = addToChildrenString(folder, checklist.getInt(App.ID));
				folder.put(App.CHILDREN_IDS, children);

				data.put(App.NUM_CHECKLISTS, data.getInt(App.NUM_CHECKLISTS) + 1);
				data.put(App.FOLDER + folder.getInt(App.ID), folder.toString());
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return data;
	}

	public static JSONObject addFolder(String name, String contentType, String type, boolean removable, JSONObject data) {
		try {
			JSONObject folder = new JSONObject();
			folder.put(App.NAME, name);
			folder.put(App.CONTENT_TYPE, contentType);
			folder.put(App.ID, data.getInt(App.NUM_FOLDERS));
			folder.put(App.REMOVABLE, removable);
			folder.put(App.TYPE, type);

			folder.put(App.CHILDREN_IDS, "");

			// This makes the project non-visible
			data.put(App.FOLDER + data.getInt(App.NUM_FOLDERS), folder.toString());
			data.put(App.NUM_FOLDERS, data.getInt(App.NUM_FOLDERS) + 1);

			String children = addToChildrenString(data, folder.getInt(App.ID), true);
			Log.i("asdsadsadsads c", children);
			data.put(App.CHILDREN_IDS, children);

			Log.i("Main Activity", "Added folder " + name);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return data;
	}

	public static JSONObject checkTask(int taskId, int checklistId, int folderId, boolean isChecked, JSONObject data) {
		try {
			JSONObject folder = new JSONObject(data.getString(App.FOLDER + folderId));

			if (checklistId != -1 /* && folder.has(App.CHECKLIST + checklistId) */) {
				JSONObject checklist = new JSONObject(folder.getString(App.CHECKLIST + checklistId));
				JSONObject task = new JSONObject(checklist.getString(App.TASK + taskId));

				task.put(App.COMPLETED, isChecked);

				checklist.put(App.TASK + taskId, task.toString());

				String childrenOrder = putTaskBeforeCheckedTasks(taskId, checklist);
				checklist.put(App.CHILDREN_IDS, childrenOrder);

				folder.put(App.CHECKLIST + checklistId, checklist.toString());
				data.put(App.FOLDER + folderId, folder.toString());
			} else if (folder.has(App.TASK + taskId)) {
				JSONObject task = new JSONObject(folder.getString(App.TASK + taskId));

				task.put(App.COMPLETED, isChecked);

				folder.put(App.TASK + taskId, task.toString());

				String childrenOrder = putTaskBeforeCheckedTasks(taskId, folder);
				folder.put(App.CHILDREN_IDS, childrenOrder);

				data.put(App.FOLDER + folderId, folder.toString());
			}

			Log.i("Compelted Task", "Completed Task");
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return data;
	}

	public static JSONObject setTaskDescription(String desc, int taskId, int checklistId, int folderId, JSONObject data) {
		try {
			JSONObject folder = new JSONObject(data.getString(App.FOLDER + folderId));

			if (checklistId != -1) {
				JSONObject checklist = new JSONObject(folder.getString(App.CHECKLIST + checklistId));
				JSONObject task = new JSONObject(checklist.getString(App.TASK + taskId));

				task.put(App.DESCRIPTION, desc);

				checklist.put(App.TASK + taskId, task.toString());
				folder.put(App.CHECKLIST + checklistId, checklist.toString());
				data.put(App.FOLDER + folderId, folder.toString());
			} else if (folder.has(App.TASK + taskId)) {
				JSONObject task = new JSONObject(folder.getString(App.TASK + taskId));

				task.put(App.DESCRIPTION, desc);

				folder.put(App.TASK + taskId, task.toString());
				data.put(App.FOLDER + folderId, folder.toString());
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return data;
	}

	public static JSONObject updateChildrenOrder(String children, int checklistId, int folderId, JSONObject data) {
		try {
			JSONObject folder, checklist;

			children = children.replaceAll(" ", "");

			if (folderId != -1) {
				if (data.has(App.FOLDER + folderId)) {
					folder = new JSONObject(data.getString(App.FOLDER + folderId));

					if (checklistId == -1) {
						folder.put(App.CHILDREN_IDS, children);
					} else if (folder.getString(App.CONTENT_TYPE).equals(App.CHECKLIST)) {
						if (folder.has(App.CHECKLIST + checklistId)) {
							checklist = new JSONObject(folder.getString(App.CHECKLIST + checklistId));
							checklist.put(App.CHILDREN_IDS, children);

							folder.put(App.CHECKLIST + checklistId, checklist.toString());
						}
					}

					data.put(App.FOLDER + folderId, folder.toString());
				}
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}

		return data;
	}

	public static String addToChildrenString(JSONObject parent, int newChildId) {
		return addToChildrenString(parent, newChildId, false);
	}

	public static String addToChildrenString(JSONObject parent, int newChildId, boolean atBeginning) {
		String[] s;
		Log.i("Adding to child string", newChildId + "");

		try {
			if (parent.has(App.CHILDREN_IDS)) s = parent.getString(App.CHILDREN_IDS).split(",");
			else s = new String[0];

			String children = "";
			if (atBeginning) {
				for (String string : s)
					children += string + ",";
				children += newChildId;
			} else {
				children += newChildId;
				for (String string : s)
					children += "," + string;
			}
			return children;
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return "";
	}

	public static String putTaskBeforeCheckedTasks(int taskId, JSONObject parent) {
		try {
			String order = "";
			boolean hasUsedTaskId = false;

			String[] childrenIds;
			if (parent.has(App.CHILDREN_IDS)) childrenIds = parent.getString(App.CHILDREN_IDS).split(",");
			else childrenIds = new String[0];

			if (childrenIds.length == 0) return taskId + "";

			for (int i = 0; i < childrenIds.length; i++) {
				if (!childrenIds[i].equals(taskId + "")) {

					JSONObject task = new JSONObject(parent.getString(App.TASK + childrenIds[i]));

					if (task.has(App.COMPLETED) && task.getBoolean(App.COMPLETED) && !hasUsedTaskId) {
						order += taskId + ",";
						hasUsedTaskId = true;
					}

					order += task.getInt(App.ID) + ",";
				}
			}

			if (!hasUsedTaskId) order += taskId + "";

			if (order.charAt(order.length() - 1) == ',' && order.length() > 1) return order.substring(0, order.length() - 1);
			else return order;
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return taskId + "";
	}

	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();
		return ni != null && ni.isConnected();
	}

	public static String capitalizeFirstWordInSentences(String phrase) {
		int pos = 0;
		boolean capitalize = true;
		StringBuilder sb = new StringBuilder(phrase);
		while (pos < sb.length()) {
			if (sb.charAt(pos) == '.' || sb.charAt(pos) == '!' || sb.charAt(pos) == '?') {
				capitalize = true;
			} else if (capitalize && !Character.isWhitespace(sb.charAt(pos))) {
				sb.setCharAt(pos, Character.toUpperCase(sb.charAt(pos)));
				capitalize = false;
			}
			pos++;
		}

		return sb.toString();
	}
}
