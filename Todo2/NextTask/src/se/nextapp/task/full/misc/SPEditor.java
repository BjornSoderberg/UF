package se.nextapp.task.full.misc;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class SPEditor {

	private Editor editor;

	public SPEditor(SharedPreferences prefs) {
		editor = prefs.edit();
//		editor.clear();
//		editor.commit();
	}

	public void put(String key, boolean value) {
		editor.putBoolean(key, value);
		editor.commit();
	}

	public void put(String key, String value) {
		editor.putString(key, value);
		editor.commit();
	}

	public void put(String key, long value) {
		editor.putLong(key, value);
		editor.commit();
	}

	public void put(String key, int value) {
		editor.putInt(key, value);
		editor.commit();
	}
}
