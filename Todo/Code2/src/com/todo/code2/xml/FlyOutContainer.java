package com.todo.code2.xml;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Scroller;

public class FlyOutContainer extends LinearLayout {

	private LinearLayout menu;
	private LinearLayout content;

	// number of pixels of the content which are visible when the menu is shown
	// (set this to a % value later
	protected static final int menuMargin = 150;
	private static final int menuAnimationDuration = 400;
	private static final int menuAnimationPollingInterval = 16;
	
	private boolean ready = false;

	public enum MenuState {
		CLOSED, OPEN, CLOSING, OPENING, MOVING
	};

	protected int currentContentOffset = 0;
	protected MenuState menuCurrentState = MenuState.CLOSED;

	protected Scroller menuAnimationScroller = new Scroller(this.getContext(), new SmoothInterpolator());
	protected Runnable menuAnimationRunnable = new AnimationRunnable();
	protected Handler menuAnimationHandler = new Handler();

	// CONSTRUCTORS
	public FlyOutContainer(Context context) {
		super(context);
	}

	public FlyOutContainer(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		ready = true;

		menu = (LinearLayout) getChildAt(0);
		content = (LinearLayout) getChildAt(1);

		menu.setVisibility(View.GONE);
	}

	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);

		if (changed) calculateChildDimensions();

		menu.layout(-getWidth() + menuMargin, top, 0, bottom);

		content.layout(left + currentContentOffset, top, right + currentContentOffset, bottom);

		content.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_UP) {
					if (menuCurrentState == MenuState.OPEN) {
						FlyOutContainer.this.toggleMenu();
					}
				}

				return true;
			}
		});
	}

	public void toggleMenu() {
		if(!ready) return;
		switch (this.menuCurrentState) {
		case CLOSED:
			menuCurrentState = MenuState.OPENING;
			menu.setVisibility(View.VISIBLE);
			menuAnimationScroller.startScroll(currentContentOffset, 0, getMenuWidth() - currentContentOffset, 0, menuAnimationDuration);
			break;
		case OPEN:
			menuCurrentState = MenuState.CLOSING;
			menuAnimationScroller.startScroll(currentContentOffset, 0, -currentContentOffset, 0, menuAnimationDuration);
			break;
		default:
			return;
		}

		menuAnimationHandler.postDelayed(menuAnimationRunnable, menuAnimationPollingInterval);
	}

	private int getMenuWidth() {
		return menu.getLayoutParams().width;
	}

	private void calculateChildDimensions() {
		content.getLayoutParams().height = getHeight();
		content.getLayoutParams().width = getWidth();

		menu.getLayoutParams().height = getHeight();
		menu.getLayoutParams().width = getWidth() - menuMargin;
	}

	private void adjustContentPosition(boolean isAnimationOngoing) {
		if(!ready) return;
		int scrollerOffset = menuAnimationScroller.getCurrX();

		content.offsetLeftAndRight(scrollerOffset - currentContentOffset);
		menu.offsetLeftAndRight(scrollerOffset - currentContentOffset);

		currentContentOffset = scrollerOffset;

		//invalidate();

		if (isAnimationOngoing) menuAnimationHandler.postDelayed(menuAnimationRunnable, menuAnimationPollingInterval);
		else onMenuTransitionComplete();
	}

	private void onMenuTransitionComplete() {
		switch (menuCurrentState) {
		case OPENING:
			menuCurrentState = MenuState.OPEN;
			break;
		case CLOSING:
			menuCurrentState = MenuState.CLOSED;
			menu.setVisibility(View.GONE);
			break;
		default:
			return;
		}
	}

	public void move(int dx) {
		if(!ready) return;
		if (menuCurrentState == MenuState.OPENING || menuCurrentState == MenuState.CLOSING) return;

		if (menuCurrentState != MenuState.MOVING) {
			menuCurrentState = MenuState.MOVING;

			menu.setVisibility(View.VISIBLE);
		}

		int nx = currentContentOffset + dx;

		if (nx < 0) nx = 0;
		if (nx > this.getWidth() - menuMargin) nx = this.getWidth() - menuMargin;

		content.offsetLeftAndRight(nx - currentContentOffset);
		menu.offsetLeftAndRight(nx - currentContentOffset);

		if (menu.getRight() < currentContentOffset) menu.offsetLeftAndRight(currentContentOffset - menu.getRight());

		if (menu.getRight() >= content.getLeft()) menu.offsetLeftAndRight(content.getLeft() - menu.getRight());

		currentContentOffset = nx;

		//invalidate();

	}

	public void released() {
		if (currentContentOffset >= (this.getWidth() / 2)) menuCurrentState = MenuState.CLOSED;
		else menuCurrentState = MenuState.OPEN;
		toggleMenu();
	}

	public void clicked() {
		if (currentContentOffset < (this.getWidth() / 2)) menuCurrentState = MenuState.CLOSED;
		else menuCurrentState = MenuState.OPEN;
		toggleMenu();
	}

	protected class SmoothInterpolator implements Interpolator {
		public float getInterpolation(float t) {
			return (float) Math.pow(t - 1, 5) + 1;
		}
	}

	protected class AnimationRunnable implements Runnable {
		public void run() {
			boolean isAnimationOngoing = FlyOutContainer.this.menuAnimationScroller.computeScrollOffset();

			FlyOutContainer.this.adjustContentPosition(isAnimationOngoing);
		}
	}
}
