package se.nextapp.task.full.xml;

import java.util.Random;

import se.nextapp.task.full.MainActivity;
import se.nextapp.task.full.gesture.SimpleGestureFilter;
import se.nextapp.task.full.gesture.SimpleGestureFilter.SimpleGestureListener;
import se.nextapp.task.full.misc.App;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Scroller;

public class TutorialSlideView extends LinearLayout implements SimpleGestureListener {

	private int numItems = 10;
	
	private boolean isMoving = false;

	private MainActivity activity;

	private Scroller scroller;
	private Runnable scrollRunnable;
	private Handler scrollHandler;
	private int currentContentOffset;
	private long scrollFps = 1000 / 60;
	
	private SimpleGestureFilter detector;

	public TutorialSlideView(MainActivity activity) {
		super(activity);
		this.activity = activity;

		init();
	}

	private void init() {
		LayoutParams params = new LinearLayout.LayoutParams(activity.getContentWidth() * numItems, activity.getTotalHeight());
		setLayoutParams(params);

		scroller = new Scroller(activity, AnimationUtils.loadInterpolator(activity, android.R.anim.decelerate_interpolator));
		scrollRunnable = new AnimationRunnable();
		scrollHandler = new Handler();

		detector = new SimpleGestureFilter(getContext(), this);

		for (int i = 0; i < numItems; i++) {
			ImageView img = new ImageView(activity);
			img.setBackgroundColor(0xff000000 + new Random().nextInt(0xffffff));
			img.setLayoutParams(new LinearLayout.LayoutParams(activity.getContentWidth(), LayoutParams.FILL_PARENT));
			addView(img);
		}
		
		setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				
			}
		});
	}

	private void showNext() {
		if(isMoving) return;
		int pos = -currentContentOffset / activity.getContentWidth();
		if(pos >= numItems - 1) return;
		
		scroller.startScroll(currentContentOffset, 0, -activity.getContentWidth(), 0, App.ANIMATION_DURATION);
		scrollHandler.postDelayed(scrollRunnable, scrollFps);
	}

	private void showPrev() {
		if(isMoving) return;
		int pos = -currentContentOffset / activity.getContentWidth();
		if(pos <= 0) return;
		
		scroller.startScroll(currentContentOffset, 0, activity.getContentWidth(), 0, App.ANIMATION_DURATION);
		scrollHandler.postDelayed(scrollRunnable, scrollFps);
	}

	private void adjustContentPosition(boolean isAnimationOngoing) {
		int offset = scroller.getCurrX();

		for (int i = 0; i < getChildCount(); i++) {
			LayoutParams params;
			if (getChildAt(i).getLayoutParams() != null) params = (LayoutParams) getChildAt(i).getLayoutParams();
			else params = new LayoutParams(activity.getContentWidth(), LayoutParams.FILL_PARENT);

			params.setMargins(offset, 0, -offset, 0);
			getChildAt(i).setLayoutParams(params);
		}

		invalidate();

		if (isAnimationOngoing) scrollHandler.postDelayed(scrollRunnable, scrollFps);
		else {
			currentContentOffset = offset;
			isMoving = false;
		}
	}
	
	public boolean onTouchEvent(MotionEvent event) {
		return super.onTouchEvent(event);
	}

	public boolean dispatchTouchEvent(MotionEvent e) {
		detector.onTouchEvent(e);
		return super.dispatchTouchEvent(e);
	}
	
	protected class AnimationRunnable implements Runnable {
		public void run() {
			isMoving = true;
			boolean isAnimationOngoing = scroller.computeScrollOffset();

			adjustContentPosition(isAnimationOngoing);
		}
	}

	public void onSwipe(int direction) {
		if(isMoving) return;
		if (direction == SimpleGestureFilter.SWIPE_LEFT) {
			showNext();
		} else if (direction == SimpleGestureFilter.SWIPE_RIGHT) {
			showPrev();
		}
	}
}
