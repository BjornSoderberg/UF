package se.nextapp.task.full.xml;

import java.io.InputStream;

import se.nextapp.task.full.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.view.View;
import android.widget.RelativeLayout;

public class GIFView extends View {

	private Movie movie;
	private InputStream is;
	private long movieStart;
	private int res;

	private boolean crash = false;

	public GIFView(Context context, int res) {
		super(context);
		this.res = res;
		init();
	}

	private void init() {
		try {
			is = getContext().getResources().openRawResource(res);
			movie = Movie.decodeStream(is);

			setLayoutParams(new RelativeLayout.LayoutParams(movie.width(), movie.height()));
		} catch (Exception e) {
			crash = true;
			setLayoutParams(new RelativeLayout.LayoutParams(200, 200));
		}
	}

	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		if (!crash) {
			long now = System.currentTimeMillis();
			if (movieStart == 0) movieStart = now;

			int relTime = (int) ((now - movieStart) % movie.duration());
			movie.setTime(relTime);
			movie.draw(canvas, 0, 0);

			invalidate();
		} 
//		else {
			// getContext().getResources().getDrawable(R.drawable.ic_check).draw(canvas);
//			Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_mic_big);
//			canvas.drawBitmap(bitmap, 0, 0, null);
//			setLayoutParams(new RelativeLayout.LayoutParams(bitmap.getWidth(), bitmap.getHeight()));
//		}
	}
}
