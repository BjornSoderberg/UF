package misc;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class SharedPreferencesEditor {

	private SharedPreferences prefs;
	private Editor editor;
	
	public SharedPreferencesEditor(SharedPreferences prefs) {
		this.prefs = prefs;
		editor = prefs.edit();
	}
	
	public void put(String key, String value) {
		editor.putString(key, value);
		editor.commit();
	}
	
	public void put(String key, int value) {
		editor.putInt(key, value);
		editor.commit();
	}
	
}
