package misc;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.LinearLayout;

import com.todoapp.code.FolderActivity;
import com.todoapp.code.TaskActivity;

public class LinearLayout2 extends LinearLayout{
	
	private static Activity activity;

	public LinearLayout2(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public static void setActivity(Activity a) {
		activity = a;
		Log.i("dsafasfsa", "nsafsanfdsdsafdsafaasfcuck");
	}
	
	public boolean dispatchKeyEventPreIme(KeyEvent event) {
		
		Log.i("dsafasfsa", "nsafsanfasfcuck");
		
		if(activity instanceof FolderActivity) ((FolderActivity) activity).toggleHeader(!Data.ENABLE_EDITING);
		if(activity instanceof TaskActivity) ((TaskActivity) activity).toggleHeader(!Data.ENABLE_EDITING);
		
		return super.dispatchKeyEvent(event);
	}
	
	public static void recycle() {
		activity = null;
	}

}
