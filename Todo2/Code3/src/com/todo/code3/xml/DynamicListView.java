package com.todo.code3.xml;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.TypeEvaluator;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.animation.ValueAnimator.AnimatorUpdateListener;
import com.todo.code3.R;
import com.todo.code3.adapter.ItemAdapter;
import com.todo.code3.item.ContentItem;
import com.todo.code3.view.ItemView;

public class DynamicListView extends ListView {

	private final int SMOOTH_SCROLL_AMOUNT_AT_EDGE = 45;
	private final int MOVE_DURATION = 150;
	private final int LINE_THICKNESS = 5;

	private ArrayList<ContentItem> contentItems;

	private int mLastEventY = -1;

	private int mDownY = -1;
	private int mDownX = -1;

	private int mTotalOffset = 0;

	private boolean mCellIsMobile = false;
	private boolean mIsMobileScrolling = false;
	private int mSmoothScrollAmountAtEdge = 0;

	private final int INVALID_ID = -1;
	private long mAboveItemId = INVALID_ID;
	private long mMobileItemId = INVALID_ID;
	private long mBelowItemId = INVALID_ID;

	private BitmapDrawable mHoverCell;
	private Rect mHoverCellCurrentBounds;
	private Rect mHoverCellOriginalBounds;

	private final int INVALID_POINTER_ID = -1;
	private int mActivePointerId = INVALID_POINTER_ID;

	private boolean mIsWaitingForScrollFinish = false;
	private int mScrollState = OnScrollListener.SCROLL_STATE_IDLE;

	private ItemView itemView;

	public DynamicListView(Context context) {
		super(context);
		init(context);
	}

	public DynamicListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public DynamicListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public void init(Context context) {
		setOnItemLongClickListener(mOnItemLongClickListener);
		setOnScrollListener(mScrollListener);
		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		mSmoothScrollAmountAtEdge = (int) (SMOOTH_SCROLL_AMOUNT_AT_EDGE / metrics.density);
	}

	private AdapterView.OnItemLongClickListener mOnItemLongClickListener = new AdapterView.OnItemLongClickListener() {
		public boolean onItemLongClick(AdapterView<?> parent, View view, int pos, long id) {
			itemView.getActivity().toggleOptions();
			// Selects the pressed item
			itemView.toggleItem(view.getId());

			return true;
		}
	};

	public void startDragging() {
		if (isDragging() || mMobileItemId != INVALID_ID) return;

		// Checks if the touch location in x is greater than the width - the
		// width of the "reorder button"
		if (itemView.getActivity().getContentWidth() - getContext().getResources().getDimension(R.dimen.item_height) > mDownX) return;

		mTotalOffset = 0;

		int position = pointToPosition(mDownX, mDownY);
		int itemNum = position - getFirstVisiblePosition();

		View selectedView = getChildAt(itemNum);

		// Returns if no view is retrieved from the position
		// (the touch was not on an item)
		if (selectedView == null) return;

		mMobileItemId = getAdapter().getItemId(position);
		mHoverCell = getAndAddHoverView(selectedView);
		selectedView.setVisibility(INVISIBLE);

		mCellIsMobile = true;

		updateNeighborViewsForID(mMobileItemId);
		((ItemAdapter) getAdapter()).setMovingItemId((int) mMobileItemId);
	}

	/**
	 * Creates the hover cell with the appropriate bitmap and of appropriate
	 * size. The hover cell's BitmapDrawable is drawn on top of the bitmap every
	 * single time an invalidate call is made.
	 */
	private BitmapDrawable getAndAddHoverView(View v) {
		int w = v.getWidth();
		int h = v.getHeight();
		int top = v.getTop();
		int left = v.getLeft();

		Bitmap b = getBitmapWithBorder(v);

		BitmapDrawable drawable = new BitmapDrawable(getResources(), b);

		mHoverCellOriginalBounds = new Rect(left, top, left + w, top + h);
		mHoverCellCurrentBounds = new Rect(mHoverCellOriginalBounds);

		drawable.setBounds(mHoverCellCurrentBounds);

		return drawable;
	}

	/** Draws a black border over the screenshot of the view passed in. */
	private Bitmap getBitmapWithBorder(View v) {
		Bitmap bitmap = getBitmapFromView(v);
		Canvas can = new Canvas(bitmap);

		Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

		Paint paint = new Paint();
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(LINE_THICKNESS);
		paint.setColor(0xff38adcf);

		can.drawBitmap(bitmap, 0, 0, null);
		can.drawRect(rect, paint);

		return bitmap;
	}

	/** Returns a bitmap showing a screenshot of the view passed in. */
	private Bitmap getBitmapFromView(View v) {
		Bitmap bitmap = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		v.draw(canvas);
		return bitmap;
	}

	/**
	 * Stores a reference to the views above and below the item currently
	 * corresponding to the hover cell. It is important to note that if this
	 * item is either at the top or bottom of the list, mAboveItemId or
	 * mBelowItemId may be invalid.
	 */
	private void updateNeighborViewsForID(long itemID) {
		int position = getPositionForID(itemID);
		BaseAdapter adapter = (BaseAdapter) getAdapter();
		mAboveItemId = adapter.getItemId(position - 1);
		mBelowItemId = adapter.getItemId(position + 1);
	}

	/** Retrieves the view in the list corresponding to itemID */
	public View getViewForID(long itemID) {
		int firstVisiblePosition = getFirstVisiblePosition();
		BaseAdapter adapter = ((BaseAdapter) getAdapter());
		for (int i = 0; i < getChildCount(); i++) {
			View v = getChildAt(i);
			int position = firstVisiblePosition + i;
			long id = adapter.getItemId(position);
			if (id == itemID) {
				return v;
			}
		}
		return null;
	}

	/** Retrieves the position in the list corresponding to itemID */
	public int getPositionForID(long itemID) {
		View v = getViewForID(itemID);
		if (v == null) {
			return -1;
		} else {
			return getPositionForView(v);
		}
	}

	/**
	 * dispatchDraw gets invoked when all the child views are about to be drawn.
	 * By overriding this method, the hover cell (BitmapDrawable) can be drawn
	 * over the listview's items whenever the listview is redrawn.
	 */
	@Override
	protected void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);
		if (mHoverCell != null) {
			mHoverCell.draw(canvas);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent e) {
		switch (e.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
			mDownX = (int) e.getX();
			mDownY = (int) e.getY();
			mActivePointerId = e.getPointerId(0);

			if (itemView.isInOptionsMode() && !isDragging()) {
				startDragging();
			}
			break;
		case MotionEvent.ACTION_MOVE:
			if (mActivePointerId == INVALID_POINTER_ID) {
				break;
			}

			int pointerIndex = e.findPointerIndex(mActivePointerId);

			mLastEventY = (int) e.getY(pointerIndex);
			int deltaY = mLastEventY - mDownY;

			if (mCellIsMobile) {
				mHoverCellCurrentBounds.offsetTo(mHoverCellOriginalBounds.left, mHoverCellOriginalBounds.top + deltaY + mTotalOffset);
				mHoverCell.setBounds(mHoverCellCurrentBounds);
				invalidate();

				handleCellSwitch();

				mIsMobileScrolling = false;
				handleMobileCellScroll();

				return false;
			}
			break;
		case MotionEvent.ACTION_UP:
			touchEventsEnded();
			break;
		case MotionEvent.ACTION_CANCEL:
			touchEventsEnded();
			// touchEventsCancelled();
			break;
		case MotionEvent.ACTION_POINTER_UP:
			/*
			 * If a multitouch event took place and the original touch dictating
			 * the movement of the hover cell has ended, then the dragging event
			 * ends and the hover cell is animated to its corresponding position
			 * in the listview.
			 */
			pointerIndex = (e.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
			final int pointerId = e.getPointerId(pointerIndex);
			if (pointerId == mActivePointerId) {
				touchEventsEnded();
			}
			break;
		default:
			break;
		}

		return super.onTouchEvent(e);
	}

	/**
	 * This method determines whether the hover cell has been shifted far enough
	 * to invoke a cell swap. If so, then the respective cell swap candidate is
	 * determined and the data set is changed. Upon posting a notification of
	 * the data set change, a layout is invoked to place the cells in the right
	 * place. Using a ViewTreeObserver and a corresponding OnPreDrawListener, we
	 * can offset the cell being swapped to where it previously was and then
	 * animate it to its new position.
	 */
	private void handleCellSwitch() {

		final int deltaY = mLastEventY - mDownY;
		int deltaYTotal = mHoverCellOriginalBounds.top + mTotalOffset + deltaY;

		View belowView = getViewForID(mBelowItemId);
		View mobileView = getViewForID(mMobileItemId);
		View aboveView = getViewForID(mAboveItemId);

		boolean isBelow = (belowView != null) && (deltaYTotal > belowView.getTop());
		boolean isAbove = (aboveView != null) && (deltaYTotal < aboveView.getTop());

		if (isBelow || isAbove) {

			final View switchView = isBelow ? belowView : aboveView;
			final int originalItem = getPositionForView(mobileView);

			if (switchView == null) {
				updateNeighborViewsForID(mMobileItemId);
				return;
			}

			swapElements(originalItem, getPositionForView(switchView));

			int delta = (int) getContext().getResources().getDimension(R.dimen.item_height);
			if (isBelow) delta = -delta;

			ObjectAnimator a = ObjectAnimator.ofFloat(switchView, "translationY", delta);
			a.setDuration(MOVE_DURATION);
			a.start();

			new Handler().postDelayed(new Runnable() {
				public void run() {
					((ItemAdapter) getAdapter()).notifyDataSetChanged();
				}
			}, MOVE_DURATION);

			mDownY = mLastEventY;

			updateNeighborViewsForID(mMobileItemId);

			final ViewTreeObserver observer = getViewTreeObserver();
			observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
				public boolean onPreDraw() {
					observer.removeOnPreDrawListener(this);

					mTotalOffset += deltaY;

					return true;
				}
			});
		}
	}

	private void swapElements(int indexOne, int indexTwo) {
		ContentItem temp = contentItems.get(indexOne);
		contentItems.set(indexOne, contentItems.get(indexTwo));
		contentItems.set(indexTwo, temp);
	}

	/**
	 * Resets all the appropriate fields to a default state while also animating
	 * the hover cell back to its correct location.
	 */
	private void touchEventsEnded() {
		final View mobileView = getViewForID(mMobileItemId);
		if (mCellIsMobile || mIsWaitingForScrollFinish) {
			mIsWaitingForScrollFinish = false;
			mIsMobileScrolling = false;
			mCellIsMobile = false;
			mActivePointerId = INVALID_POINTER_ID;

			// If the autoscroller has not completed scrolling, we need to wait
			// for it to
			// finish in order to determine the final location of where the
			// hover cell
			// should be animated to.
			if (mScrollState != OnScrollListener.SCROLL_STATE_IDLE) {
				mIsWaitingForScrollFinish = true;
				return;
			}

			if (mobileView != null) {
				mHoverCellCurrentBounds.offsetTo(mHoverCellOriginalBounds.left, mobileView.getTop());

				ObjectAnimator a = ObjectAnimator.ofObject(mHoverCell, "bounds", sBoundEvaluator, mHoverCellCurrentBounds);
				a.addUpdateListener(new AnimatorUpdateListener() {
					public void onAnimationUpdate(ValueAnimator v) {
						invalidate();
					}
				});
				a.setDuration(MOVE_DURATION);
				a.start();
			}

			new Handler().postDelayed(new Runnable() {
				public void run() {
					((ItemAdapter) getAdapter()).setMovingItemId(-1);
					((BaseAdapter) getAdapter()).notifyDataSetChanged();
					mAboveItemId = INVALID_ID;
					mMobileItemId = INVALID_ID;
					mBelowItemId = INVALID_ID;
					if (mobileView != null) mobileView.setVisibility(VISIBLE);
					mHoverCell = null;
					setEnabled(true);
					invalidate();

//					itemView.updateContentItemsOrder();
				}
			}, MOVE_DURATION);

		} else {
			touchEventsCancelled();
		}
	}

	/**
	 * Resets all the appropriate fields to a default state.
	 */
	private void touchEventsCancelled() {
		View mobileView = getViewForID(mMobileItemId);
		if (mCellIsMobile) {
			mAboveItemId = INVALID_ID;
			mMobileItemId = INVALID_ID;
			mBelowItemId = INVALID_ID;
			mHoverCell = null;
			if (mobileView != null) mobileView.setVisibility(VISIBLE);
			invalidate();
		}
		mCellIsMobile = false;
		mIsMobileScrolling = false;
		mActivePointerId = INVALID_POINTER_ID;
	}

	/**
	 * This TypeEvaluator is used to animate the BitmapDrawable back to its
	 * final location when the user lifts his finger by modifying the
	 * BitmapDrawable's bounds.
	 */
	private static TypeEvaluator<Rect> sBoundEvaluator = new TypeEvaluator<Rect>() {
		public Rect evaluate(float fraction, Rect startValue, Rect endValue) {
			return new Rect(interpolate(startValue.left, endValue.left, fraction), interpolate(startValue.top, endValue.top, fraction), interpolate(startValue.right, endValue.right, fraction), interpolate(startValue.bottom, endValue.bottom, fraction));
		}

		public int interpolate(int start, int end, float fraction) {
			return (int) (start + fraction * (end - start));
		}
	};;

	/**
	 * Determines whether this listview is in a scrolling state invoked by the
	 * fact that the hover cell is out of the bounds of the listview;
	 */
	private void handleMobileCellScroll() {
		mIsMobileScrolling = handleMobileCellScroll(mHoverCellCurrentBounds);
	}

	/**
	 * This method is in charge of determining if the hover cell is above or
	 * below the bounds of the listview. If so, the listview does an appropriate
	 * upward or downward smooth scroll so as to reveal new items.
	 */
	@SuppressLint("NewApi")
	public boolean handleMobileCellScroll(Rect r) {
		if (Build.VERSION.SDK_INT < 8) return false;

		int offset = computeVerticalScrollOffset();
		int height = getHeight();
		int extent = computeVerticalScrollExtent();
		int range = computeVerticalScrollRange();
		int hoverViewTop = r.top;
		int hoverHeight = r.height();
		if (hoverViewTop <= 0 && offset > 0) {
			smoothScrollBy(-mSmoothScrollAmountAtEdge, 0);
			// scrollBy(0, -mSmoothScrollAmountAtEdge);
			return true;
		}

		if (hoverViewTop + hoverHeight >= height && (offset + extent) < range) {
			smoothScrollBy(mSmoothScrollAmountAtEdge, 0);
			// scrollBy(0, mSmoothScrollAmountAtEdge);
			return true;
		}

		return false;
	}

	public void setContentItems(ArrayList<ContentItem> list) {
		contentItems = list;
	}

	public void setItemView(ItemView v) {
		itemView = v;
	}

	public boolean isDragging() {
		return mCellIsMobile || mHoverCell != null;
	}

	/**
	 * This scroll listener is added to the listview in order to handle cell
	 * swapping when the cell is either at the top or bottom edge of the
	 * listview. If the hover cell is at either edge of the listview, the
	 * listview will begin scrolling. As scrolling takes place, the listview
	 * continuously checks if new cells became visible and determines whether
	 * they are potential candidates for a cell swap.
	 */
	private AbsListView.OnScrollListener mScrollListener = new AbsListView.OnScrollListener() {

		private int mPreviousFirstVisibleItem = -1;
		private int mPreviousVisibleItemCount = -1;
		private int mCurrentFirstVisibleItem;
		private int mCurrentVisibleItemCount;
		private int mCurrentScrollState;

		public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
			mCurrentFirstVisibleItem = firstVisibleItem;
			mCurrentVisibleItemCount = visibleItemCount;

			mPreviousFirstVisibleItem = (mPreviousFirstVisibleItem == -1) ? mCurrentFirstVisibleItem : mPreviousFirstVisibleItem;
			mPreviousVisibleItemCount = (mPreviousVisibleItemCount == -1) ? mCurrentVisibleItemCount : mPreviousVisibleItemCount;

			checkAndHandleFirstVisibleCellChange();
			checkAndHandleLastVisibleCellChange();

			mPreviousFirstVisibleItem = mCurrentFirstVisibleItem;
			mPreviousVisibleItemCount = mCurrentVisibleItemCount;
		}

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			mCurrentScrollState = scrollState;
			mScrollState = scrollState;
			isScrollCompleted();
		}

		/**
		 * This method is in charge of invoking 1 of 2 actions. Firstly, if the
		 * listview is in a state of scrolling invoked by the hover cell being
		 * outside the bounds of the listview, then this scrolling event is
		 * continued. Secondly, if the hover cell has already been released,
		 * this invokes the animation for the hover cell to return to its
		 * correct position after the listview has entered an idle scroll state.
		 */
		private void isScrollCompleted() {
			if (mCurrentVisibleItemCount > 0 && mCurrentScrollState == SCROLL_STATE_IDLE) {
				if (mCellIsMobile && mIsMobileScrolling) {
					handleMobileCellScroll();
				} else if (mIsWaitingForScrollFinish) {
					touchEventsEnded();
				}
			}
		}

		/**
		 * Determines if the listview scrolled up enough to reveal a new cell at
		 * the top of the list. If so, then the appropriate parameters are
		 * updated.
		 */
		public void checkAndHandleFirstVisibleCellChange() {
			if (mCurrentFirstVisibleItem != mPreviousFirstVisibleItem) {
				if (mCellIsMobile && mMobileItemId != INVALID_ID) {
					updateNeighborViewsForID(mMobileItemId);
					handleCellSwitch();
				}
			}
		}

		/**
		 * Determines if the listview scrolled down enough to reveal a new cell
		 * at the bottom of the list. If so, then the appropriate parameters are
		 * updated.
		 */
		public void checkAndHandleLastVisibleCellChange() {
			int currentLastVisibleItem = mCurrentFirstVisibleItem + mCurrentVisibleItemCount;
			int previousLastVisibleItem = mPreviousFirstVisibleItem + mPreviousVisibleItemCount;
			if (currentLastVisibleItem != previousLastVisibleItem) {
				if (mCellIsMobile && mMobileItemId != INVALID_ID) {
					updateNeighborViewsForID(mMobileItemId);
					handleCellSwitch();
				}
			}
		}
	};
}