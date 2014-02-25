package se.nextapp.task.full.xml;

import se.nextapp.task.full.MainActivity;
import se.nextapp.task.full.R;
import se.nextapp.task.full.gesture.SimpleGestureFilter;
import se.nextapp.task.full.gesture.SimpleGestureFilter.SimpleGestureListener;
import se.nextapp.task.full.misc.App;
import se.nextapp.task.full.tutorial.TutorialState;
import se.nextapp.task.full.view.NoteView;
import se.nextapp.task.full.view.TaskView;
import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class Wrapper extends RelativeLayout implements SimpleGestureListener {

	private int x, y, startX, startY, lastX;

	private long startTime;

	private int dragStartLocation = -1;

	private static final int MENU_CLOSED = 0;
	private static final int MENU_OPEN = 1;

	private boolean canDrag = false; // solves the swipe bug
	private boolean isDragging = false;
	private boolean hasStarted = false;

	private boolean addTouch = false;

	private ViewConfiguration viewConfig;
	private MainActivity activity;
	private SimpleGestureFilter detector;

	private ImageView touchImageView;

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

		// initTouchImageView();
	}

	private void initTouchImageView() {
		int numButtons = 3;
		double v[] = { Math.toRadians(15), Math.toRadians(45), Math.toRadians(75) };
		int w = App.dpToPx(50, getContext().getResources()); // width
		// distance from upper right corner
		int d = App.dpToPx(150, getContext().getResources());

		touchImageView = new ImageView(getContext());
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(d, d);
		params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		touchImageView.setLayoutParams(params);
		addView(touchImageView);
	}

	public boolean onTouchEvent(MotionEvent e) {
		if (activity == null) return false;
		if (!activity.canRun()) return false;
		if (!canDrag) return false;

		if (addTouch) return onAddTouchEvent(e);

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
					moveMenu(dx);
					lastX = x;
				}
			}
		} else if (e.getAction() == MotionEvent.ACTION_UP) {
			if (!hasStarted) return true;

			if (isDragging) {
				// delta time
				double dt = (System.currentTimeMillis() - startTime) / 1000D;

				if (Math.abs(x - startX) / dt > viewConfig.getScaledMinimumFlingVelocity()) {
					if (dragStartLocation == MENU_OPEN) activity.hideMenu();
					else activity.showMenu();
				} else {
					if (activity.getFlyInMenu().getContentOffset() > activity.getContentWidth() / 2) showMenu();
					else hideMenu();
				}
			} else {
				FrameLayout b = activity.getMenuButton();

				if (dragStartLocation == MENU_OPEN) activity.hideMenu();
				else if (b.getLeft() < startX && startX < b.getRight() //
						&& b.getTop() < startY && startY < b.getBottom()) activity.toggleMenu();

			}

			isDragging = hasStarted = canDrag = false;
			dragStartLocation = -1;
		}

		return true;
	}

	private boolean onAddTouchEvent(MotionEvent e) {
		if (e.getAction() == MotionEvent.ACTION_DOWN) {
			showAddOptions();
			startX = x = (int) e.getRawX();
			startY = y = (int) e.getRawY() - App.getStatusBarHeight(activity.getResources());
		} else if (e.getAction() == MotionEvent.ACTION_MOVE) {
			x = (int) e.getRawX();
			y = (int) e.getRawY() - App.getStatusBarHeight(activity.getResources());

			if (y != startY && x != startX) {
				double v = Math.atan((y - startY) * 1.0 / (x - startX) * 1.0);
				v = Math.toDegrees(v);

				int selected = R.drawable.select_none;

				if (Math.hypot(x - startX, y - startY) > viewConfig.getScaledTouchSlop()) {
					if ((-90 <= v && v < -60) || (60 <= v && v <= 90)) selected = R.drawable.select_folder;
					else if (-60 <= v && v < -30) selected = R.drawable.select_note;
					else if ((-30 <= v && v < 0) || (0 <= v && v <= 30)) selected = R.drawable.select_task;
				}

				touchImageView.setBackgroundDrawable(getContext().getResources().getDrawable(selected));

			}
		} else if (e.getAction() == MotionEvent.ACTION_UP) {
			addTouch = false;
			hideAddOptions();

			if (Math.hypot(x - startX, y - startY) < viewConfig.getScaledTouchSlop()) activity.getAddButton().performClick();
			else if (y != startY && x != startX) {
				double v = Math.atan((y - startY) * 1.0 / (x - startX) * 1.0);
				v = Math.toDegrees(v);

				if ((-90 <= v && v < -60) || (60 <= v && v <= 90)) activity.addDialog(App.FOLDER);
				else if (-60 <= v && v < -30) activity.addDialog(App.NOTE);
				else if ((-30 <= v && v < 0) || (0 <= v && v <= 30)) activity.addDialog(App.TASK);
			}
		}

		return true;
	}

	public boolean onInterceptTouchEvent(MotionEvent e) {
		if (!activity.canRun()) return false;
		canDrag = false;

		int x = (int) e.getRawX();
		int y = (int) e.getRawY() - App.getStatusBarHeight(activity.getResources());
		if (activity.isInMasterView()) x -= activity.getMenuWidth();

		// If the menu is visible, the user is able to drag,
		// as long as he does not drag on the menu
		if (!isDragging && activity.getFlyInMenu().isVisible()) {
			canDrag = true;
			return true;
		}

		FrameLayout b = activity.getMenuButton();
		// if the back button is visible and the user touches
		// the button, the menu should not open
		if (x < b.getRight() && y < b.getBottom()) {
			if (activity.getPosInWrapper() == 0 && !activity.isInOptions()) {
				canDrag = true;
				return true;
			}
		}

		// If the touch is inside the touch area for the drag, the user should
		// be able to drag
		if (x <= App.dpToPx(App.BEZEL_AREA_DP, getContext().getResources())) {
			canDrag = true;
			return true;
		}

		FrameLayout add = activity.getAddButton();
		if (add.getLeft() < x && x < add.getRight()) {
			if (add.getTop() < y && y < add.getBottom()) {
				if (e.getAction() == MotionEvent.ACTION_DOWN) {
					if (!activity.isInSettings() && !activity.isInOptions() && //
							!(activity.getOpenContentView() instanceof TaskView || //
							activity.getOpenContentView() instanceof NoteView)//
							&& !activity.isEditingTitle()) {
						addTouch = true;
						canDrag = true;
						return true;
					}
				}
			}
		}

		return false;
	}

	public void setActivity(MainActivity a) {
		activity = a;
	}

	public boolean dispatchTouchEvent(MotionEvent e) {
		detector.onTouchEvent(e);
		return super.dispatchTouchEvent(e);
	}
	
	private void moveMenu(int dx) {
		if(activity.getTutorialState() != TutorialState.END && activity.getTutorialState() != TutorialState.SHOW_MENU) return;
		
		activity.getFlyInMenu().moveMenu(dx);
	}
	
	private void showMenu() {
		if(activity.getTutorialState() != TutorialState.END && activity.getTutorialState() != TutorialState.SHOW_MENU) return;
		
		activity.getFlyInMenu().showMenu();
	}
	
	private void hideMenu() {
		activity.getFlyInMenu().hideMenu();
	}

	public void onSwipe(int direction) {
		if (!isDragging && !activity.isMoving()) {
			if (direction == SimpleGestureFilter.SWIPE_RIGHT) {
				int posBefore = activity.getPosInWrapper();
				if (activity.getPosInWrapper() == 0) {
					activity.showMenu();
					return;
				} else activity.goBack();
				// Should solve bug with nothing happening on swipe
				if (posBefore == activity.getPosInWrapper() && !activity.getFlyInMenu().isVisible()) showMenu();
			}
		}
	}

	private void showAddOptions() {
		if (touchImageView == null) initTouchImageView();

		touchImageView.setVisibility(View.VISIBLE);
		touchImageView.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.select_none));
		touchImageView.bringToFront();
		touchImageView.requestLayout();

		Animation animation = new AlphaAnimation(0, 1);
		animation.setDuration(App.ANIMATION_DURATION);
		touchImageView.startAnimation(animation);
	}

	private void hideAddOptions() {
		Animation animation = new AlphaAnimation(1, 0);
		animation.setDuration(App.ANIMATION_DURATION);
		touchImageView.startAnimation(animation);

		new Handler().postDelayed(new Runnable() {
			public void run() {
				touchImageView.setVisibility(View.GONE);
			}
		}, App.ANIMATION_DURATION);
	}
}
