package se.nextapp.task.full.misc;

import se.nextapp.task.full.MainActivity;
import se.nextapp.task.full.R;
import se.nextapp.task.full.dialog.AddItemDialog;
import se.nextapp.task.full.dialog.ConfirmDialog;
import se.nextapp.task.full.tutorial.TutorialState;
import android.content.res.Resources;
import android.hardware.SensorListener;
import android.hardware.SensorManager;

public class ShakeListener implements SensorListener {

	private static final int SHAKE_THRESHOLD = 2000;

	private long lastUpdate;
	private float x, y, z;
	private float lastX, lastY, lastZ;

	private MainActivity activity;

	public ShakeListener(MainActivity activity) {
		this.activity = activity;
	}

	public void onAccuracyChanged(int arg0, int arg1) {

	}

	public void onSensorChanged(int sensor, float[] values) {
		if (sensor == SensorManager.SENSOR_ACCELEROMETER) {
			long curTime = System.currentTimeMillis();
			// only allow one update every 100ms.
			if ((curTime - lastUpdate) > 100) {
				long diffTime = (curTime - lastUpdate);
				lastUpdate = curTime;

				x = values[SensorManager.DATA_X];
				y = values[SensorManager.DATA_Y];
				z = values[SensorManager.DATA_Z];

				float speed = Math.abs(x + y + z - lastX - lastY - lastZ) / diffTime * 10000;

				lastX = x;
				lastY = y;
				lastZ = z;

				if (speed > SHAKE_THRESHOLD) {
					Resources r = activity.getResources();
					if (!ConfirmDialog.isVisible && activity.getTutorialState() == TutorialState.END) {
						ConfirmDialog cd = new ConfirmDialog(activity, r.getString(R.string.start_tutorial), r.getString(R.string.do_you_want_to_start_the_tutorial), r.getString(R.string.yes), r.getString(R.string.no)){
							protected void onResult(Object result) {
								super.onResult(result);
								if(result instanceof Boolean && (Boolean) result) activity.startTutorial();
							}
						};
						
						cd.show();
					}
				}
			}
		}
	}

}
