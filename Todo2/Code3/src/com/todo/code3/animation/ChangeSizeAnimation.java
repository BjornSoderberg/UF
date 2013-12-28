package com.todo.code3.animation;

import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.LinearLayout;

public class ChangeSizeAnimation extends Animation {

	private View view;
	private int duration;
	private int startHeight;
	private int change;
	private AnimationListener al;

	public ChangeSizeAnimation(View view, int duration, int startHeight, int change) {
		this.view = view;
		this.duration = duration;
		this.startHeight = startHeight;
		this.change = change;

		init();
	}

	public ChangeSizeAnimation(View view, int duration, int startHeight, int change, AnimationListener al) {
		this.view = view;
		this.duration = duration;
		this.startHeight = startHeight;
		this.change = change;
		this.al = al;

		init();
	}

	private void init() {

	}

	protected void applyTransformation(float time, Transformation t) {
		int newHeight = startHeight + (int) (change*time);
		
		if(newHeight <= 0) newHeight = 1;
		view.getLayoutParams().height = newHeight;
		
		view.requestLayout();
	}
	
	public void animate() {
		if(al != null) setAnimationListener(al);
		setDuration(duration);
		view.startAnimation(this);
	}
}
