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
	
	public static final String NUM_CHILDREN = "numChildren";

	public static final String REMOVABLE = "removable";
	public static final String REMOVED = "removed";
	public static final String COMPLETED = "completed";
	
	public static final boolean CHECKED = true;
	public static final boolean UNCHECKED = false;
	
	public static final String CONTENT_TYPE = "contentType";
	public static final String PARENT_CONTENT_TYPE = "parentContentType";

	public static final String FOLDER = "folder";
	
	public static final String CHECKLIST = "checklist";

	public static final String TASK = "task";
	
	public static final String DESCRIPTION = "description";
	
	public static float dpToPx(float dp, Context context){
	    Resources resources = context.getResources();
	    DisplayMetrics metrics = resources.getDisplayMetrics();
	    float px = dp * (metrics.densityDpi / 160f);
	    return px;
	}

}
