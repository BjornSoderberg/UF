package com.todo.code2.misc;

import java.util.Date;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;

import com.todo.code2.xml.FlyOutContainer;

public class OnTouchHandler implements OnTouchListener {

	private FlyOutContainer root;
	private Button button;

	private float x = -1, y = -1;

	private boolean dragged = false;
	
	int i = 0;

	public OnTouchHandler(FlyOutContainer f, Button b) {
		root = f;
		button = b;
	}

	public boolean onTouch(View v, MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			x = event.getX();
			y = event.getY();
			i++;
			button.setBackgroundColor(0xff6ABD89);
		}
		if (event.getAction() == MotionEvent.ACTION_MOVE) {
//			if (x == -1 || y == -1)
//				return false;

			int dx = (int) (event.getX() - x);
			int dy = (int) (event.getY() - y);
			
			root.move(dx);
			
			//Log.i("On touch - root", flyOutContainer + "");

			if (Math.abs(dx) > 2 && Math.abs(dy) > 2)
				dragged = true;
		}
		if (event.getAction() == MotionEvent.ACTION_UP) {
			x = -1;
			y = -1;

			if (!dragged)
				root.clicked();
			else
				root.released();
			
			dragged = false;

			button.setBackgroundColor(0xff8ADDA9);

		}

		return true;
	}

}
