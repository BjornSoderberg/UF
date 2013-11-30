package com.todo.code3.misc;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;

public class App {
	
	public static final String PREFERENCES_NAME = "Code3SP";

	public static final String DATA = "data";
	public static final String NAME = "name";
	public static final String ID = "id";
	public static final String PARENT_ID = "parentId";
	public static final String CHILDREN_IDS = "childrenId";
	
	//public static final String NUM_CHILDREN = "numChildren";
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
}
