package com.todo.code2.misc;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class SPEditor {
	
	private Editor editor;
	
	public SPEditor(SharedPreferences prefs) {
		editor = prefs.edit();
//		editor.clear();
//		editor.commit();
	}
	
	public void put(String key, String value) {
		editor.putString(key, value);
		editor.commit();
	}



}
