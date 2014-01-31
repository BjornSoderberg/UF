package se.nextapp.task.full.gesture;

import android.content.Context;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;

public class SimpleGestureFilter extends SimpleOnGestureListener {

	public static final int SWIPE_UP = 1;
	public static final int SWIPE_DOWN = 2;
	public static final int SWIPE_LEFT = 3;
	public static final int SWIPE_RIGHT = 4;
	
	private static final int MIN_SWIPE_DISTANCE = 16;
	private static final int MAX_SWIPE_DISTANCE = 1000;
	private static final int MIN_FLING_VELOCITY = 700;

	private Context context;
	private GestureDetector detector;
	private SimpleGestureListener listener;

	public SimpleGestureFilter(Context context, SimpleGestureListener l) {
		this.context = context;
		this.detector = new GestureDetector(context, this);
		this.listener = l;
	}

	public void onTouchEvent(MotionEvent e) {
		detector.onTouchEvent(e);
	}

	public boolean onFling(MotionEvent e1, MotionEvent e2, float vx, float vy) {
		float dx = Math.abs(e1.getX() - e2.getX());
		float dy = Math.abs(e1.getY() - e2.getY());

		if (dx > MAX_SWIPE_DISTANCE || dy > MAX_SWIPE_DISTANCE) return false;
		

		vx = Math.abs(vx);
		vy = Math.abs(vy);

		boolean result = false;
		int dir = -1;

		if (vx > MIN_FLING_VELOCITY && dx > MIN_SWIPE_DISTANCE) {
			if (e1.getX() > e2.getX()) dir = SWIPE_LEFT;
			else dir = SWIPE_RIGHT;
			
			result = true;
		} 
		if (vy > MIN_FLING_VELOCITY && dy > MIN_SWIPE_DISTANCE) {
			if (e1.getY() > e2.getY()) if(dy > dx) dir = SWIPE_UP;
			else if(dy > dx) dir = SWIPE_DOWN;
			
			result = true;
		}
		
		if(dir != -1) listener.onSwipe(dir);
		
		return result;
	}

	public static interface SimpleGestureListener {
		void onSwipe(int direction);
	}

}
