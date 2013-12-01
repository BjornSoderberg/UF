package com.todo.code3.xml;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.todo.code3.MainActivity;
import com.todo.code3.misc.App;

public class Wrapper extends LinearLayout {

	private int x, y, startX, startY, lastX;

	private long startTime;

	private int dragStartLocation = -1;

	private static final int MENU_CLOSED = 0;
	private static final int MENU_OPEN = 1;

	private boolean isDragging = false;

	private ViewConfiguration viewConfig;

	private MainActivity activity;

	public Wrapper(Context context) {
		super(context);
		viewConfig = ViewConfiguration.get(context);
	}

	public Wrapper(Context context, AttributeSet attrs) {
		super(context, attrs);
		viewConfig = ViewConfiguration.get(context);
	}

	public boolean onTouchEvent(MotionEvent e) {
		if (activity == null) return true;

		if (e.getAction() == MotionEvent.ACTION_DOWN) {
			startX = x = lastX = (int) e.getRawX();
			startY = y = (int) e.getRawY();

			startTime = System.currentTimeMillis();

			// If the swipe started between 0 and
			// 30 dp from the screens right side,
			// the user started the swipe at
			// the correct location to be able to
			// open the menu
			if (x <= App.dpToPx(App.BEZEL_AREA_DP, getContext().getResources()) && x >= 0) dragStartLocation = MENU_CLOSED;
			if (activity.getMenuWidth() < e.getRawX()) dragStartLocation = MENU_OPEN;

		} else if (e.getAction() == MotionEvent.ACTION_MOVE) {
			x = (int) e.getRawX();
			y = (int) e.getRawY();

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
			if (isDragging) {
				// delta time
				double dt = (System.currentTimeMillis() - startTime) / 1000D;

				if (Math.abs(x - startX) / dt > viewConfig.getScaledMinimumFlingVelocity() * 4) {
					if (dragStartLocation == MENU_OPEN) activity.getFlyInMenu().hideMenu();
					else activity.getFlyInMenu().showMenu();
				} else {
					if (activity.getFlyInMenu().getContentOffset() > activity.getContentWidth() / 2) activity.getFlyInMenu().showMenu();
					else activity.getFlyInMenu().hideMenu();
				}
			}
			if (!isDragging && dragStartLocation == MENU_OPEN) activity.getFlyInMenu().hideMenu();

			isDragging = false;
			dragStartLocation = -1;
		}

		return true;
	}

	public boolean onInterceptTouchEvent(MotionEvent e) {
		Button b = activity.getDragButton();

		// The user will not be able to drag the menu if he tries
		// to touch where the drag button is (the button that opens
		// the menu. This prevents the buttons onClick method from being
		// overridden.
		if (b.getLeft() < e.getRawX() && e.getRawX() < b.getRight() && b.getTop() < e.getRawY() && e.getRawY() < b.getBottom()) return false;

		if (!isDragging && activity.getFlyInMenu().isMenuVisible()) {
			return true;
		}

		if (e.getRawX() > App.dpToPx(App.BEZEL_AREA_DP, getContext().getResources())) return false;

		return true;
	}

	public void setActivity(MainActivity a) {
		activity = a;
	}
}
