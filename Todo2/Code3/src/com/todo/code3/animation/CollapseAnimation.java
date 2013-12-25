package com.todo.code3.animation;

import java.security.PublicKey;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

public class CollapseAnimation extends Animation {

	private View view;
	private int duration;
	private int itemHeight;
	private AnimationListener al;

	public CollapseAnimation(View view, int duration, int itemHeight) {
		this.view = view;
		this.duration = duration;
		this.itemHeight = itemHeight;
	}

	public CollapseAnimation(View view, int duration, int itemHeight, AnimationListener al) {
		this.view = view;
		this.duration = duration;
		this.itemHeight = itemHeight;
		this.al = al;
	}

	protected void applyTransformation(float time, Transformation t) {
		if (itemHeight - (int) (itemHeight * time) == 0) view.getLayoutParams().height = 1;
		else view.getLayoutParams().height = itemHeight - (int) (itemHeight * time);
		
		view.requestLayout();
	}

	public void animate() {
		if(al != null) setAnimationListener(al);
		setDuration(duration);
		view.startAnimation(this);
	}
}
