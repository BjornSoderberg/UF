package com.todo.code3.misc;

import java.util.Calendar;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public class App {

	public static final String PREFERENCES_NAME = "Code3SP";

	public static final String DATA = "data";
	public static final String NAME = "name";

	public static final String ID = "id";
	public static final String PARENT_ID = "parentId";
	public static final String CHILDREN_IDS = "childrenIds";
	public static final String NUM_IDS = "numIds";

	public static final String REMOVED = "removed";
	public static final String COMPLETED = "completed";
	public static final String TYPE = "type";

	public static final String FOLDER = "folder";
	public static final String TASK = "task";
	public static final String NOTE = "note";

	public static final String TASK_VIEW = "taskView";
	public static final String CHECKLIST_VIEW = "checklistView";

	public static final String TIMESTAMP_CREATED = "timestampCreated";
	public static final String TIMESTAMP_COMPLETED = "timestampCompleted";
	public static final String TIMESTAMP_LAST_UPDATED = "timestampLastUpdated";

	public static final String DESCRIPTION = "description";
	public static final String PRIORITIZED = "prioritized";

	public static final String OPEN_OBJECT_ID = "openObjectId";
	public static final String OPEN = "open";

	public static final String DUE_DATE = "dueDate";
//	public static final String REMINDER = "reminder";
	public static final String REPEAT = "repeat";

	public static final int BEZEL_AREA_DP = 16;
	public static final int ANIMATION_DURATION = 300;

	// Ids for the options menu
	public static final int OPTIONS_REMOVE = 0;
	public static final int OPTIONS_GROUP_ITEMS = 1;
	public static final int OPTIONS_SELECT_ALL = 2;
	public static final int OPTIONS_MOVE = 3;

	// converting dp to pixels and vice versa
	public static int dpToPx(float dp, Resources r) {
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

	public static JSONObject add(String name, String type, int parentId, JSONObject data) {
		try {
			JSONObject object = new JSONObject();
			object.put(App.NAME, name);
			object.put(App.ID, data.getInt(App.NUM_IDS));
			object.put(App.TYPE, type);
			object.put(App.TIMESTAMP_CREATED, (int) (System.currentTimeMillis() / 1000));

			if (parentId != -1) object.put(App.PARENT_ID, parentId);
			if (type.equals(App.FOLDER)) object.put(App.CHILDREN_IDS, "");

			data.put(App.NUM_IDS, data.getInt(App.NUM_IDS) + 1);

			if (parentId != -1) {
				JSONObject parent = new JSONObject(data.getString(parentId + ""));

				String children = addToChildrenString(parent, object.getInt(App.ID));
				parent.put(App.CHILDREN_IDS, children);
				data.put(parentId + "", parent.toString());
			} else {
				String children = addToChildrenString(data, object.getInt(App.ID));
				data.put(App.CHILDREN_IDS, children);
			}

			data.put(object.getInt(App.ID) + "", object.toString());

		} catch (JSONException e) {
			e.printStackTrace();
		}

		return data;
	}

	public static JSONObject remove(int id, JSONObject data) {
		try {
			if (!data.has(id + "")) return data;

			JSONObject object = new JSONObject(data.getString(id + ""));
			if (object.has(App.PARENT_ID)) {
				JSONObject parent = new JSONObject(data.getString(object.getInt(App.PARENT_ID) + ""));
				String children = removeFromChildrenString(parent, object.getInt(App.ID));
				parent.put(App.CHILDREN_IDS, children);
				data.put(parent.getInt(App.ID) + "", parent.toString());
			} else {
				String children = removeFromChildrenString(data, object.getInt(App.ID));
				data.put(App.CHILDREN_IDS, children);
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}

		data.remove(id + "");

		return data;
	}

	public static JSONObject move(int id, int parentId, JSONObject data) {
		try {
			Log.i("moving " + id, "to" + parentId);

			if (!data.has(id + "")) return data;

			JSONObject object = new JSONObject(data.getString(id + ""));

			if (object.has(App.PARENT_ID)) {
				JSONObject oldParent = new JSONObject(data.getString(object.getInt(App.PARENT_ID) + ""));
				String children = removeFromChildrenString(oldParent, object.getInt(App.ID));
				oldParent.put(App.CHILDREN_IDS, children);
				data.put(oldParent.getInt(App.ID) + "", oldParent.toString());
			} else {
				String children = removeFromChildrenString(data, object.getInt(App.ID));
				data.put(App.CHILDREN_IDS, children);
			}

			object.put(App.PARENT_ID, parentId);
			data.put(object.getInt(App.ID) + "", object.toString());

			if (parentId != -1) {
				JSONObject newParent = new JSONObject(data.getString(object.getInt(App.PARENT_ID) + ""));
				String children = addToChildrenString(newParent, id);
				newParent.put(App.CHILDREN_IDS, children);
				data.put(newParent.getInt(App.ID) + "", newParent.toString());
			} else {
				String children = addToChildrenString(data, id);
				data.put(App.CHILDREN_IDS, children);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return data;
	}

	public static JSONObject checkTask(int taskId, boolean isChecked, JSONObject data) {
		try {

			JSONObject task = new JSONObject(data.getString(taskId + ""));
			if (!task.getString(App.TYPE).equals(App.TASK)) return data;

			task.put(App.COMPLETED, isChecked);

			if (isChecked) task.put(App.TIMESTAMP_COMPLETED, (int) (System.currentTimeMillis() / 1000));
			else task.put(App.TIMESTAMP_COMPLETED, -1);

			data.put(taskId + "", task.toString());
			JSONObject parent = new JSONObject(data.getString(task.getInt(App.PARENT_ID) + ""));

			data.put(task.getInt(App.PARENT_ID) + "", parent.toString());

		} catch (JSONException e) {
			e.printStackTrace();
		}

		return data;
	}

	public static JSONObject setProperty(String key, Object value, int id, JSONObject data) {
		try {
			JSONObject object = new JSONObject(data.getString(id + ""));

			object.put(key, value);

			data.put(id + "", object.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return data;
	}

	public static JSONObject removeProperty(String key, int id, JSONObject data) {
		try {
			JSONObject object = new JSONObject(data.getString(id + ""));
			object.remove(key);
			data.put(id + "", object.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return data;
	}

	public static JSONObject updateChildrenOrder(String children, int parentId, JSONObject data) {
		try {
			children = children.replaceAll(" ", "");

			if (parentId != -1) {
				JSONObject parent = new JSONObject(data.getString(parentId + ""));
				parent.put(App.CHILDREN_IDS, children);
				data.put(parentId + "", parent.toString());
			} else {
				data.put(App.CHILDREN_IDS, children);
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

	public static String removeFromChildrenString(JSONObject parent, int id) {
		String[] s;

		try {
			if (parent.has(App.CHILDREN_IDS)) s = parent.getString(App.CHILDREN_IDS).split(",");
			else s = new String[0];

			String children = "";
			for (String string : s) {
				if (!string.equals(id + "")) children += string + ",";
			}

			// Removes the last "," from the string
			if (children.length() > 0) children = children.substring(0, children.length() - 1);

			return children;
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return "";
	}

	public static String removeFromChildrenString(JSONObject parent, int[] ids) {
		String[] s;

		try {
			if (parent.has(App.CHILDREN_IDS)) s = parent.getString(App.CHILDREN_IDS).split(",");
			else s = new String[0];

			String children = "";
			for (String string : s) {
				boolean existed = false;
				for (int id : ids) {
					if (string.equals(id + "")) existed = true;
				}

				if (!existed) children += string + ",";
			}

			children = children.substring(0, children.length() - 1);
			return children;
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return "";
	}

	public static boolean isParentOf(int childId, int parentId, JSONObject data) {
		if (childId == parentId) return true;

		try {
			if (!data.has(childId + "")) return false;
			JSONObject child = new JSONObject(data.getString(childId + ""));

			if (!child.has(App.PARENT_ID)) return false;

			if (child.getInt(App.PARENT_ID) == parentId) return true;
			else return isParentOf(child.getInt(App.PARENT_ID), parentId, data);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return false;
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

	public static int[] getDifferenceBetweenTimestamps(long t1, long t2) {
		if (t1 > t2) return null;

		// years, months, days, hours, minutes
		int[] time = new int[5];

		Calendar c1 = Calendar.getInstance();
		Calendar c2 = Calendar.getInstance();

		c1.setTimeInMillis(t1 * 1000);
		c2.setTimeInMillis(t2 * 1000);

		int years = c2.get(Calendar.YEAR) - c1.get(Calendar.YEAR);
		int months = c2.get(Calendar.MONTH) - c1.get(Calendar.MONTH);
		int days = c2.get(Calendar.DAY_OF_YEAR) - c1.get(Calendar.DAY_OF_YEAR);
		int hours = c2.get(Calendar.HOUR_OF_DAY) - c1.get(Calendar.HOUR_OF_DAY);
		int minutes = c2.get(Calendar.MINUTE) - c1.get(Calendar.MINUTE);

		time[0] = years;
		time[1] = months;
		time[2] = days;
		time[3] = hours;
		time[4] = minutes;

		return time;
	}

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
					if (isOverDue(parent.getLong(App.DUE_DATE))) return 1;

				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return numTasksOverDue;
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

	public static boolean isOverDue(long timestamp) {
		return timestamp < System.currentTimeMillis() / 1000 && timestamp != -1;
	}

	public static int[] getDueDate(long timestamp) {
		int[] i = new int[3];

		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(timestamp * 1000);

		i[0] = c.get(Calendar.YEAR);
		i[1] = c.get(Calendar.MONTH);
		i[2] = c.get(Calendar.DAY_OF_MONTH);

		return i;
	}

	public static String getParentHierarchyString(int id, JSONObject data) {
		String s = "";

		try {
			JSONObject object = new JSONObject(data.getString(id + ""));

			if (object.has(App.PARENT_ID) && object.getInt(App.PARENT_ID) != -1) {
				s = getParentHierarchyString(object.getInt(App.PARENT_ID), data);
			}

			return s + object.getInt(App.ID) + ",";
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return "";
	}

	public static void showKeyboard(Context c) {
		((InputMethodManager) c.getSystemService(Context.INPUT_METHOD_SERVICE)).toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
	}

	public static void hideKeyboard(Context c, View v) {
		((InputMethodManager) c.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(v.getWindowToken(), 0);
	}
}
