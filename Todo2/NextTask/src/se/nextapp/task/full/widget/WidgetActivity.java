package se.nextapp.task.full.widget;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

public class WidgetActivity extends Activity{

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		Toast.makeText(this, "asdasd", Toast.LENGTH_LONG).show();
	}
}
