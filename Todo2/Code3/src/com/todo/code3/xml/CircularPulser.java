package com.todo.code3.xml;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;

import com.todo.code3.misc.App;

public class CircularPulser extends View {

	private float currDb = 0, lastDb = 0, drawDb = 0;
	private Paint paint;
	private int minSizeInDp = 40;
	
	private boolean updateable = true;

	int i = 0;

	public CircularPulser(Context context) {
		super(context);
		init();
	}

	public CircularPulser(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public CircularPulser(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		paint = new Paint();
	}

	public void onDraw(Canvas c) {
//		c.drawColor(0);

		int radius = getRadius();
		if (radius > getHeight() / 2) radius = getHeight() / 2;
		if (radius > getWidth() / 2) radius = getWidth() / 2;

		int x = getWidth() / 2;
		int y = getHeight() / 2;

		paint.setColor(0xffdddddd);
		paint.setAntiAlias(true);
		c.drawCircle(x, y, radius, paint);

		paint.setColor(0xffff8888);
		paint.setAntiAlias(true);
		c.drawCircle(x, y, getMinSizeInDp(), paint);
	}

	public void update(float db) {
		if(!updateable) return;
		
		lastDb = this.currDb;
		if (db < 0) db = 0;
		
		if(db > lastDb && db > lastDb + 6) db = lastDb + 6;
		if(db < lastDb && db < lastDb - 6) db = lastDb - 6;

		this.currDb = db;

		Animation a = new Animation() {
			protected void applyTransformation(float time, Transformation t) {
				float d = currDb - lastDb;
				d *= time;
				drawDb = lastDb + d;
				invalidate();
			}
		};
		a.setDuration(75);
		startAnimation(a);
		updateable = false;
		
		new Handler().postDelayed(new Runnable() {
			public void run() {
				updateable = true;
			}
		}, 75);
	}

	private int getRadius() {
		return App.dpToPx(minSizeInDp + 5/2 * drawDb, getContext().getResources());
	}

	public int getMinSizeInDp() {
		return minSizeInDp * 2;
	}
}
