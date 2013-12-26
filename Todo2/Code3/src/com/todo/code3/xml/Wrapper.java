package com.todo.code3.xml;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.todo.code3.MainActivity;
import com.todo.code3.gesture.SimpleGestureFilter;
import com.todo.code3.gesture.SimpleGestureFilter.SimpleGestureListener;
import com.todo.code3.misc.App;

public class Wrapper extends RelativeLayout implements SimpleGestureListener {

	private int x, y, startX, startY, lastX;

	private long startTime;

	private int dragStartLocation = -1;

	private static final int MENU_CLOSED = 0;
	private static final int MENU_OPEN = 1;

	private boolean isDragging = false;
	private boolean hasStarted = false;

	private ViewConfiguration viewConfig;
	private MainActivity activity;
	private SimpleGestureFilter detector;

	public Wrapper(Context context) {
		super(context);
		init();
	}

	public Wrapper(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		init();
	}
	
	private void init() {
		viewConfig = ViewConfiguration.get(getContext());
		detector = new SimpleGestureFilter(getContext(), this);
	}

	public boolean onTouchEvent(MotionEvent e) {
		if (activity == null) return true;

		if (e.getAction() == MotionEvent.ACTION_DOWN) {
			startX = x = lastX = (int) e.getRawX();
			startY = y = (int) e.getRawY() - App.getStatusBarHeight(activity.getResources());

			startTime = System.currentTimeMillis();
			hasStarted = true;

			// If the swipe started between 0 and
			// 30 dp from the screens right side,
			// the user started the swipe at
			// the correct location to be able to
			// open the menu
			if (x <= App.dpToPx(App.BEZEL_AREA_DP, getContext().getResources()) && x >= 0) dragStartLocation = MENU_CLOSED;
			if (activity.getMenuWidth() < e.getRawX()) dragStartLocation = MENU_OPEN;

		} else if (e.getAction() == MotionEvent.ACTION_MOVE) {
			if (!hasStarted) return true;

			x = (int) e.getRawX();
			y = (int) e.getRawY() - App.getStatusBarHeight(activity.getResources());

			if (dragStartLocation != -1) {
				// If the swipe is long enough to be considered a scroll
				if (Math.hypot(x - startX, y - startY) > viewConfig.getScaledTouchSlop()) isDragging = true;

				if (isDragging) {
					int dx = x - lastX;
					activity.getFlyInMenu().moveMenu(dx);
					lastX = x;
				}
			}
		} else if (e.getAction() == MotionEvent.ACTION_UP) {
			if (!hasStarted) return true;

			if (isDragging) {
				// delta time
				double dt = (System.currentTimeMillis() - startTime) / 1000D;

				if (Math.abs(x - startX) / dt > viewConfig.getScaledMinimumFlingVelocity() * 4) {
					if (dragStartLocation == MENU_OPEN) activity.hideMenu();
					else activity.showMenu();
				} else {
					if (activity.getFlyInMenu().getContentOffset() > activity.getContentWidth() / 2) activity.showMenu();
					else activity.hideMenu();
				}
			} else {
				FrameLayout b = activity.getDragButton();

				if (dragStartLocation == MENU_OPEN) activity.hideMenu();
				else if (b.getLeft() < startX && startX < b.getRight() //
						&& b.getTop() < startY && startY < b.getBottom()) activity.toggleMenu();

			}

			isDragging = hasStarted = false;
			dragStartLocation = -1;
		}

		return true;
	}

	public boolean onInterceptTouchEvent(MotionEvent e) {
		// If the menu is visible, the user is able to drag,
		// as long as he does not drag on the menu
		if (!isDragging && activity.getFlyInMenu().isMenuVisible()) {
			return true;
		}

		FrameLayout b = activity.getDragButton();
		// if the back button is visible and the user touches
		// the button, the menu should not open
		if (e.getRawX() < b.getRight() && e.getRawY() - App.getStatusBarHeight(activity.getResources()) < b.getBottom()) {
			if (activity.getPosInWrapper() != 0) return false;
			else return true;
		}

		// If the touch is inside the touch area for the drag, the user should
		// be able to drag
		if (e.getRawX() <= App.dpToPx(App.BEZEL_AREA_DP, getContext().getResources())) return true;

		return false;
	}

	public void setActivity(MainActivity a) {
		activity = a;
	}

	public boolean dispatchTouchEvent(MotionEvent e) {
		detector.onTouchEvent(e);
		return super.dispatchTouchEvent(e);
	}

	public void onSwipe(int direction) {
		if (!isDragging) if (direction == SimpleGestureFilter.SWIPE_RIGHT) activity.showMenu();
	}
}
