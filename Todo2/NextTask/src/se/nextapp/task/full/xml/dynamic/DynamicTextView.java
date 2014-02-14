package se.nextapp.task.full.xml.dynamic;

import se.nextapp.task.full.misc.App;
import android.content.Context;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View.MeasureSpec;
import android.widget.TextView;

public class DynamicTextView extends TextView {

	private float startTextSize;
	private float minTextSize;

	public DynamicTextView(Context context) {
		super(context);
		init();
	}

	public DynamicTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		startTextSize = getTextSize();
		minTextSize = App.dpToPx(10, getResources());

		mTestPaint = new Paint();
		mTestPaint.set(this.getPaint());
		// max size defaults to the initially specified text size unless it is
		// too small
	}

	/*
	 * Re size the font so the specified text fits in the text box assuming the
	 * text box is the specified width.
	 */
	private void refitText(String text, int textWidth) {
		if (textWidth <= 0) return;
		int targetWidth = textWidth - this.getPaddingLeft() - this.getPaddingRight();
		float hi = startTextSize;
		float lo = minTextSize;
		final float threshold = 0.5f; // How close we have to be

		mTestPaint.set(this.getPaint());

		while ((hi - lo) > threshold) {
			float size = (hi + lo) / 2;
			if (size < minTextSize) {
				lo = minTextSize;
				break;
			}
			mTestPaint.setTextSize(size);
			// to big:
			if (mTestPaint.measureText(text) >= targetWidth) hi = size;
			else lo = size; // too small
		}
		// Use lo so that we undershoot rather than overshoot
		this.setTextSize(TypedValue.COMPLEX_UNIT_PX, lo);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
		int height = getMeasuredHeight();
		refitText(this.getText().toString(), parentWidth);
		this.setMeasuredDimension(parentWidth, height);
	}

	@Override
	protected void onTextChanged(final CharSequence text, final int start, final int before, final int after) {
		refitText(text.toString(), this.getWidth());
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		if (w != oldw) {
			refitText(this.getText().toString(), w);
		}
	}

	// Attributes
	private Paint mTestPaint;
}
